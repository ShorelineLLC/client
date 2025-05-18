use std::ffi::CStr;
use std::path::PathBuf;
use std::process::exit;
use fltk::app::run;
use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString};
use obfstr::obfstr;
use serde_json::json;
use tokio::runtime::Runtime;
use crate::{autoupdate, network, notifs, obfuscation};
use crate::login::{HeaderMapExt, User};

pub unsafe extern "system" fn perform_version_check<'a>(mut env: JNIEnv,
                                                        _class: JClass,
                                                        current_version: JObject) -> JObject<'a>
{
    if obfuscation::IS_OBFUSCATED_ENVIRONMENT
    {
        let str_message = JString::from(current_version);
        let java_str = env.get_string(&str_message).unwrap();
        let loader_current_version = java_str.to_str().unwrap();

        match internal_version_check(loader_current_version, &mut env)
        {
            Ok(()) => {},
            Err(e) => {
                let msg = format! {
                    "{}{}",
                    obfstr!("Error during version check/autoupdate:\n\n"),
                    e
                };

                notifs::error(&mut env, &e);
                notifs::display_error_msg(&msg);

                exit(-1)
            }
        }
    }

    return JObject::null();
}

unsafe fn internal_version_check(current_version: &str,
                                 env: &mut JNIEnv) -> Result<(), String>
{
    let server_address = obfstr!("https://api.shorelineclient.dev/versioncheck").to_string();

    match network::verify_server_integrity(&server_address)
    {
        Ok(()) => {
            let request = json!({
                obfstr!("Current-Version"): current_version,
            });

            let runtime = Runtime::new().unwrap();

            runtime.block_on(async {
                let response = network::get_client()
                    .get(&server_address)
                    .header(obfstr!("User-Agent"), obfstr!("shoreline-client"))
                    .header(obfstr!("Content-Type"), obfstr!("application/json"))
                    .json(&request)
                    .send()
                    .await;

                let response = match response
                {
                    Ok(response) => response,
                    Err(_) => {
                        return Err(obfstr! {
                            "Failed to connect to Shoreline servers. Please contact support!"
                        }.to_string());
                    }
                };

                let http_response_code = response.status().as_u16();

                return match http_response_code
                {
                    200 => {
                        Ok(())
                    }

                    409 => {
                        let res = notifs::display_confirmation_msg(obfstr! {
                            "Your loader is outdated. Would you like to update it?"
                        });

                        if res
                        {
                            let jar_file = get_loader_file(env)
                                .map_err(|_| "Failed to resolve loader jar path")?;
                            
                            let absolute_path = env.call_method(
                                jar_file,
                                obfstr!("getAbsolutePath"),
                                obfstr!("()Ljava/lang/String;"),
                                &[]
                            )
                                .map_err(|_| obfstr!("Failed JNI call").to_string())?
                                .l()
                                .map_err(|_| obfstr!("Failed JNI call").to_string())?;
                            
                            let jstring = JString::from(absolute_path);
                            let path_str = env.get_string(&jstring)
                                .map_err(|_| obfstr!("Failed JNI call").to_string())?;
                            let path_str = path_str.to_str()
                                .map_err(|_| obfstr!("Failed JNI call").to_string())?;
                            
                            let mut path_buf = PathBuf::from(path_str);

                            match autoupdate::download_loader(path_buf.to_str().unwrap()).await
                            {
                                Ok(()) => {
                                    notifs::display_info_msg(obfstr! {
                                        "Successfully updated your loader! Please relaunch the game."
                                    });
                                },
                                Err(e) => {
                                    notifs::error(env, &e);
                                    notifs::display_error_msg(&e);
                                }
                            }

                            exit(-1);
                        } else
                        {
                            notifs::display_info_msg(obfstr! {
                                "The program will now exit as Shoreline is outdated. \
                                If you wish to update your loader, please visit the Installer."
                            });

                            exit(-1);
                        }
                    }

                    _ => {
                        let formatted = format! {
                            "{}{}{}",
                            obfstr!("Internal server error code 0x2"),
                            http_response_code,
                            obfstr!(". Please report this to a developer.")
                        };

                        Err(formatted)
                    }
                }
            })
        },
        Err(msg) => return Err(msg)
    }
}

pub unsafe fn get_loader_hash(env: &mut JNIEnv) -> Result<String, String>
{
    let file_jar = get_loader_file(env)
        .map_err(|_| "Failed to resolve the loader jar path")?;

    let file_path = env.call_method(
        file_jar,
        obfstr!("toPath"),
        obfstr!("()Ljava/nio/file/Path;"),
        &[]
    )
        .map_err(|_| obfstr!("Failed JNI call").to_string())?
        .l()
        .map_err(|_| obfstr!("Failed JNI call").to_string())?;

    let jvm_bytes = env.call_static_method(
        obfstr!("java/nio/file/Files"),
        obfstr!("readAllBytes"),
        obfstr!("(Ljava/nio/file/Path;)[B"),
        &[(&file_path).into()]
    )
        .map_err(|_| obfstr!("Failed JNI call").to_string())?
        .l()
        .map_err(|_| obfstr!("Failed JNI call").to_string())?;

    let bytes_to_string = env.call_static_method(
        obfstr!("java/util/Arrays"),
        obfstr!("toString"),
        obfstr!("([B)Ljava/lang/String;"),
        &[(&jvm_bytes).into()]
    )
        .map_err(|_| obfstr!("Failed JNI call").to_string())?
        .l()
        .map_err(|_| obfstr!("Failed JNI call").to_string())?;

    let j_string = JString::from(bytes_to_string);

    let java_str = env.get_string(&j_string)
        .map_err(|_| obfstr!("Failed JNI call").to_string())?;

    let rust_str = java_str.to_str()
        .map_err(|_| obfstr!("Failed JNI call").to_string())?;

    Ok(crate::crypto::encrypt_irreversible(rust_str))
}

// returns a File java object
unsafe fn get_loader_file<'a>(env: &mut JNIEnv<'a>) -> Result<JObject<'a>, String>
{
    let loader_class = obfuscation::get_loader_class(env);

    let protection_domain = env.call_method(
        loader_class,
        obfstr!("getProtectionDomain"),
        obfstr!("()Ljava/security/ProtectionDomain;"),
        &[]
    )
        .map_err(|_| obfstr!("Failed JNI call").to_string())?
        .l()
        .map_err(|_| obfstr!("Failed JNI call").to_string())?;

    let code_source = env.call_method(
        protection_domain,
        obfstr!("getCodeSource"),
        obfstr!("()Ljava/security/CodeSource;"),
        &[]
    )
        .map_err(|_| obfstr!("Failed JNI call").to_string())?
        .l()
        .map_err(|_| obfstr!("Failed JNI call").to_string())?;

    let location = env.call_method(
        code_source,
        obfstr!("getLocation"),
        obfstr!("()Ljava/net/URL;"),
        &[]
    )
        .map_err(|_| obfstr!("Failed JNI call").to_string())?
        .l()
        .map_err(|_| obfstr!("Failed JNI call").to_string())?;

    let location_uri = env.call_method(
        location,
        obfstr!("toURI"),
        obfstr!("()Ljava/net/URI;"),
        &[]
    )
        .map_err(|_| obfstr!("Failed JNI call").to_string())?
        .l()
        .map_err(|_| obfstr!("Failed JNI call").to_string())?;

    let file_jar = env.new_object(
        obfstr!("java/io/File"),
        obfstr!("(Ljava/net/URI;)V"),
        &[(&location_uri).into()]
    )
        .map_err(|_| obfstr!("Failed JNI call").to_string())?;

    Ok(file_jar)
}