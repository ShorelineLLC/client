mod utils;
mod eventbus;
mod antivm;

extern crate jni;

use std::collections::{HashMap, VecDeque};
use jni::{JNIEnv, JavaVM};
use obfstr::obfstr;
use jni::sys::{JNI_GetCreatedJavaVMs, JNI_VERSION_1_8};
use std::os::raw::{c_void, c_int};
use jni::strings::{JNIString};
use jni::objects::{JObject, JValue, JString, JClass, GlobalRef};
use std::ffi::{CStr, CString};
use std::fmt::format;
use std::process::exit;
use std::ptr::{null, null_mut};
use hardware_id::get_id;
use jni::signature::JavaType;
use jni::signature::Primitive::Int;
use winapi::um::debugapi::IsDebuggerPresent;
use crate::antivm::inside_vm;
use crate::eventbus::INVOKE;
use crate::utils::{define_class, encrypt, get_immediate_dependents, is_imixin_class, is_mixin_accessor, is_mixin_class, error_message, crash, alert_webhook};

static mut USER_INFO: Option<GlobalRef> = None;

static mut LATE_LOADING_CLASSES: Option<GlobalRef> = None;

static mut MIXIN_CONFIG: Option<GlobalRef> = None;
static mut MIXIN_REFMAP: Option<GlobalRef> = None;

static mut RESOURCES_MAP: Option<GlobalRef> = None;

#[no_mangle]
pub unsafe extern "system" fn JNI_OnLoad(vm: JavaVM,
                                         _reserved: &mut c_void) -> c_int
{
    let env = vm.get_env().unwrap();

    let crash_clazz = env.find_class(
        obfstr!("java/lang/System")
    ).unwrap();

    let raw_hwid = match get_id()
    {
        Ok(res) => {
            res
        }
        Err(error) => {
            error_message(
                obfstr!("Shoreline failed to retrieve your computer information.\n\nPlease contact a developer!")
            );

            crash(&env, crash_clazz);

            return JNI_VERSION_1_8;
        }
    };

    let hwid = encrypt(&raw_hwid);

    let url_string = JNIString::from(
        obfstr!("https://api.shorelineclient.net/auth")
    );

    let url = env.new_object(
        obfstr!("java/net/URL"),
        obfstr!("(Ljava/lang/String;)V"),
        &[env.new_string(url_string).unwrap().into()]
    ).unwrap();

    let url_connection = env.call_method(
        url,
        obfstr!("openConnection"),
        obfstr!("()Ljava/net/URLConnection;"),
        &[]
    ).unwrap().l().unwrap();

    let user_agent = env.new_string(obfstr!("User-Agent")).unwrap();
    let user_agent_value = env.new_string(obfstr!("shoreline-client")).unwrap();

    env.call_method(
        url_connection,
        obfstr!("addRequestProperty"),
        obfstr!("(Ljava/lang/String;Ljava/lang/String;)V"),
        &[user_agent.into(), user_agent_value.into()]
    ).unwrap().v().unwrap();

    let hwid_req = env.new_string(obfstr!("HWID")).unwrap();
    let hwid_req_value = env.new_string(hwid).unwrap();

    env.call_method(
        url_connection,
        obfstr!("addRequestProperty"),
        obfstr!("(Ljava/lang/String;Ljava/lang/String;)V"),
        &[hwid_req.into(), hwid_req_value.into()]
    ).unwrap().v().unwrap();

    let get_response_code = env.get_method_id(
        obfstr!("java/net/HttpURLConnection"),
        obfstr!("getResponseCode"),
        obfstr!("()I")
    ).unwrap();

    let http_response = env.call_method_unchecked(
        url_connection,
        get_response_code,
        JavaType::Primitive(Int),
        &[]
    );

    let http_response_code = match http_response
    {
        Ok(code) => code.i().unwrap(),
        Err(_) => {
            -1
        }
    };

    match http_response_code
    {
        -1 => {
            error_message(
                obfstr!("Failed to connect to Shoreline servers.\n\nPlease contact Shoreline support!")
            );

            crash(&env, crash_clazz);
        }
        200 => {
            let input_stream = env.call_method(
                url_connection,
                obfstr!("getInputStream"),
                obfstr!("()Ljava/io/InputStream;"),
                &[]
            ).unwrap().l().unwrap();

            let input_stream_reader = env.new_object(
                obfstr!("java/io/InputStreamReader"),
                obfstr!("(Ljava/io/InputStream;)V"),
                &[input_stream.into()]
            ).unwrap();

            let buffered_reader = env.new_object(
                obfstr!("java/io/BufferedReader"),
                obfstr!("(Ljava/io/Reader;)V"),
                &[input_stream_reader.into()]
            ).unwrap();

            let read_line = env.call_method(
                buffered_reader,
                obfstr!("readLine"),
                obfstr!("()Ljava/lang/String;"),
                &[]
            ).unwrap().l().unwrap();

            USER_INFO = Some(
                env.new_global_ref(read_line).unwrap()
            );
        }
        401 => {
            error_message(
                obfstr!("Invalid user credentials.\n\nPurchase your own version of Shoreline at shorelineclient.net!")
            );

            crash(&env, crash_clazz);
        }

        _ => {
            let msg = format!(
                "{}{}{}",
                obfstr!("Internal server error "),
                http_response_code,
                obfstr!(".\n\nPlease contact Shoreline support!")
            );

            error_message(&msg);

            crash(&env, crash_clazz);
        }
    }

    return JNI_VERSION_1_8
}

/**
 * Create and return a new class instance without calling <init>
 *
 * Also, wow. Turns out native methods with underscores in them need to have a '1' appended
 * after the underscore, or it's not recognized as the correct function signature. What the fuck.
 */
#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_10"]
pub extern "system" fn stop_decompiling_0<'a>(env: JNIEnv<'a>,
                                              _caller_class: JClass<'a>,
                                              class_ctor_instance: JObject<'a>) -> JObject<'a>
{
    let declaring_class = env.call_method(
        class_ctor_instance,
        obfstr!("getDeclaringClass"),
        obfstr!("()Ljava/lang/Class;"),
        &[]
    ).unwrap().l().unwrap();

    let the_unsafe = env.get_static_field(
        env.find_class(obfstr!("sun/misc/Unsafe")).unwrap(),
        obfstr!("theUnsafe"),
        obfstr!("Lsun/misc/Unsafe;")
    ).unwrap().l().unwrap();

    return env.call_method(
        the_unsafe,
        obfstr!("allocateInstance"),
        obfstr!("(Ljava/lang/Class;)Ljava/lang/Object;"),
        &[declaring_class.into()]
    ).unwrap().l().unwrap();
}

/**
 * Return the mixin config
 */
#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_11"]
pub unsafe extern "system" fn stop_decompiling_1<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     _unused_obscure: JObject<'a>) -> JObject<'a>
{
    return match MIXIN_CONFIG.as_ref().take()
    {
        Some(config_ref) => config_ref.as_obj(),
        None => {
            error_message(obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: 1"));

            crash(&env, caller_class);

            JObject::null()
        }
    }
}

/**
 * Return the mixin refmap
 */
#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_12"]
pub unsafe extern "system" fn stop_decompiling_2<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     _unused_obscure: JObject<'a>) -> JObject<'a>
{
    return match MIXIN_REFMAP.as_ref().take()
    {
        Some(refmap_ptr) => refmap_ptr.as_obj().clone(),
        None => {
            error_message(obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: 2"));

            crash(&env, caller_class);

            JObject::null()
        }
    }
}

pub fn log(env: &JNIEnv, msg: &str)
{
    let logger = env.get_static_field(
        env.find_class("net/shoreline/loader/Loader").unwrap(),
        "LOGGER",
        "Lorg/apache/logging/log4j/Logger;"
    ).unwrap().l().unwrap();

    let msg_str = env.new_string(msg).unwrap();

    env.call_method(
        logger,
        "info",
        "(Ljava/lang/String;)V",
        &[msg_str.into()]
    ).unwrap().v().unwrap();
}

/**
 * Download and store all client classes & resources
 */
#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_13"]
pub unsafe extern "system" fn stop_decompiling_3<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     information_array: JObject<'a>) -> JObject<'a>
{
    // Get the loader hash and verify with the server
    let protection_domain = env.call_method(
        caller_class,
        obfstr!("getProtectionDomain"),
        obfstr!("()Ljava/security/ProtectionDomain;"),
        &[]
    ).unwrap().l().unwrap();

    let code_source = env.call_method(
        protection_domain,
        obfstr!("getCodeSource"),
        obfstr!("()Ljava/security/CodeSource;"),
        &[]
    ).unwrap().l().unwrap();

    let location = env.call_method(
        code_source,
        obfstr!("getLocation"),
        obfstr!("()Ljava/net/URL;"),
        &[]
    ).unwrap().l().unwrap();

    let location_uri = env.call_method(
        location,
        obfstr!("toURI"),
        obfstr!("()Ljava/net/URI;"),
        &[]
    ).unwrap().l().unwrap();

    let file_jar = env.new_object(
        obfstr!("java/io/File"),
        obfstr!("(Ljava/net/URI;)V"),
        &[location_uri.into()]
    ).unwrap();

    let file_path = env.call_method(
        file_jar,
        obfstr!("toPath"),
        obfstr!("()Ljava/nio/file/Path;"),
        &[]
    ).unwrap().l().unwrap();

    let jvm_bytes = env.call_static_method(
        env.find_class(obfstr!("java/nio/file/Files")).unwrap(),
        obfstr!("readAllBytes"),
        obfstr!("(Ljava/nio/file/Path;)[B"),
        &[file_path.into()]
    ).unwrap().l().unwrap();

    let bytes_to_string = env.call_static_method(
        env.find_class(obfstr!("java/util/Arrays")).unwrap(),
        obfstr!("toString"),
        obfstr!("([B)Ljava/lang/String;"),
        &[jvm_bytes.into()]
    ).unwrap().l().unwrap();

    let bytes_to_string_ptr = env.get_string_utf_chars(JString::from(bytes_to_string)).unwrap();
    let bytes_to_string_internal = CStr::from_ptr(bytes_to_string_ptr).to_str().unwrap();

    let encrypted_bytes = encrypt(bytes_to_string_internal);

    let user_agent = env.new_string(obfstr!("User-Agent")).unwrap();
    let user_agent_value = env.new_string(obfstr!("shoreline-client")).unwrap();

    let url_string = JNIString::from(
        obfstr!("https://api.shorelineclient.net/hashcheck")
    );

    let url = env.new_object(
        obfstr!("java/net/URL"),
        obfstr!("(Ljava/lang/String;)V"),
        &[env.new_string(url_string).unwrap().into()]
    ).unwrap();

    let url_connection = env.call_method(
        url,
        obfstr!("openConnection"),
        obfstr!("()Ljava/net/URLConnection;"),
        &[]
    ).unwrap().l().unwrap();

    env.call_method(
        url_connection,
        obfstr!("addRequestProperty"),
        obfstr!("(Ljava/lang/String;Ljava/lang/String;)V"),
        &[user_agent.into(), user_agent_value.into()]
    ).unwrap().v().unwrap();

    let user_type = env.new_string(obfstr!("User-Type")).unwrap();
    let user_type_value = JString::from(env.get_object_array_element(*information_array, 3).unwrap());

    env.call_method(
        url_connection,
        obfstr!("addRequestProperty"),
        obfstr!("(Ljava/lang/String;Ljava/lang/String;)V"),
        &[user_type.into(), user_type_value.into()]
    ).unwrap().v().unwrap();

    let hash_req = env.new_string(obfstr!("hash")).unwrap();
    let hash_req_value = env.new_string(encrypted_bytes).unwrap();

    env.call_method(
        url_connection,
        obfstr!("addRequestProperty"),
        obfstr!("(Ljava/lang/String;Ljava/lang/String;)V"),
        &[hash_req.into(), hash_req_value.into()]
    ).unwrap().v().unwrap();

    let get_response_code = env.get_method_id(
        obfstr!("java/net/HttpURLConnection"),
        obfstr!("getResponseCode"),
        obfstr!("()I")
    ).unwrap();

    let http_response = env.call_method_unchecked(
        url_connection,
        get_response_code,
        JavaType::Primitive(Int),
        &[]
    );

    let http_response_code = match http_response
    {
        Ok(code) => code.i().unwrap(),
        Err(_) => {
            -1
        }
    };

    let temporary_token = match http_response_code
    {
        -1 => {
            error_message(
                obfstr!("Failed to connect to Shoreline servers.\n\nPlease contact Shoreline support!")
            );

            crash(&env, caller_class);

            return JObject::null();
        }
        200 => {
            let input_stream = env.call_method(
                url_connection,
                obfstr!("getInputStream"),
                obfstr!("()Ljava/io/InputStream;"),
                &[]
            ).unwrap().l().unwrap();

            let input_stream_reader = env.new_object(
                obfstr!("java/io/InputStreamReader"),
                obfstr!("(Ljava/io/InputStream;)V"),
                &[input_stream.into()]
            ).unwrap();

            let buffered_reader = env.new_object(
                obfstr!("java/io/BufferedReader"),
                obfstr!("(Ljava/io/Reader;)V"),
                &[input_stream_reader.into()]
            ).unwrap();

            let read_line = env.call_method(
                buffered_reader,
                obfstr!("readLine"),
                obfstr!("()Ljava/lang/String;"),
                &[]
            ).unwrap().l().unwrap();

            let read_line_ptr = env.get_string_utf_chars(JString::from(read_line)).unwrap();
            CStr::from_ptr(read_line_ptr).to_str().unwrap()
        }
        401 => {
            // Happens if the request properties aren't set correctly
            // But we just did them here, so there's no way this would happen
            error_message(
                obfstr!("Something seemingly impossible happened.\n\nPlease report this to a Shoreline developer!")
            );

            crash(&env, caller_class);

            return JObject::null();
        }
        406 => {
            // The jar has been tampered with
            error_message(
                obfstr!("We know what you did")
            );

            let message = env.new_string(obfstr!("Loader has been tampered with")).unwrap();

            env.set_object_array_element(*information_array, 0, message).unwrap();

            env.call_static_method(
                caller_class,
                obfstr!("stop_decompiling_7"),
                obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                &[information_array.into()]
            ).unwrap().l().unwrap();

            crash(&env, caller_class);

            return JObject::null();
        }

        _ => {
            let msg = format!(
                "{}{}{}",
                obfstr!("Internal server error "),
                http_response_code,
                obfstr!(".\n\nPlease contact Shoreline support!")
            );

            error_message(&msg);

            crash(&env, caller_class);

            return JObject::null();
        }
    };

    // A queue of all classes that need to be defined
    let mut class_queue = VecDeque::new();

    // All mc extending classes that cannot be defined yet
    let mc_class_dependents = env.new_object(
        obfstr!("java/util/HashMap"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    LATE_LOADING_CLASSES = env.new_global_ref(mc_class_dependents).ok();

    // A map of all classes that need their bytecode parsed by the Java side
    let class_map = env.new_object(
        obfstr!("java/util/HashMap"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    let resources_map = env.new_object(
        obfstr!("java/util/HashMap"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    RESOURCES_MAP = env.new_global_ref(resources_map).ok();

    let url_string = JNIString::from(
        obfstr!("https://api.shorelineclient.net/assets/client.jar")
    );

    let url = env.new_object(
        obfstr!("java/net/URL"),
        obfstr!("(Ljava/lang/String;)V"),
        &[env.new_string(url_string).unwrap().into()]
    ).unwrap();

    let url_connection = env.call_method(
        url,
        obfstr!("openConnection"),
        obfstr!("()Ljava/net/URLConnection;"),
        &[]
    ).unwrap().l().unwrap();

    env.call_method(
        url_connection,
        obfstr!("addRequestProperty"),
        obfstr!("(Ljava/lang/String;Ljava/lang/String;)V"),
        &[user_agent.into(), user_agent_value.into()]
    ).unwrap().v().unwrap();

    let token_req = env.new_string(obfstr!("token")).unwrap();
    let token_req_value = env.new_string(temporary_token).unwrap();

    env.call_method(
        url_connection,
        obfstr!("addRequestProperty"),
        obfstr!("(Ljava/lang/String;Ljava/lang/String;)V"),
        &[token_req.into(), token_req_value.into()]
    ).unwrap().v().unwrap();

    let get_response_code = env.get_method_id(
        obfstr!("java/net/HttpURLConnection"),
        obfstr!("getResponseCode"),
        obfstr!("()I")
    ).unwrap();

    let http_response = env.call_method_unchecked(
        url_connection,
        get_response_code,
        JavaType::Primitive(Int),
        &[]
    );

    let http_response_code = match http_response
    {
        Ok(code) => code.i().unwrap(),
        Err(_) => {
            -1
        }
    };

    match http_response_code
    {
        -1 => {
            error_message(
                obfstr!("Failed to connect to Shoreline servers.\n\nPlease contact Shoreline support!")
            );

            crash(&env, caller_class);
        }
        200 => {
            let input_stream = env.call_method(
                url_connection,
                obfstr!("getInputStream"),
                obfstr!("()Ljava/io/InputStream;"),
                &[]
            ).unwrap();

            let zip_input_stream = env.new_object(
                obfstr!("java/util/zip/ZipInputStream"),
                obfstr!("(Ljava/io/InputStream;)V"),
                &[input_stream.into()]
            ).unwrap();

            let mut zip_entry: Option<JValue> = env.call_method(
                zip_input_stream,
                obfstr!("getNextEntry"),
                obfstr!("()Ljava/util/zip/ZipEntry;"),
                &[]
            ).ok();

            let null = JObject::null();

            while !env.is_same_object(zip_entry.unwrap().l().unwrap(), null).unwrap()
            {
                let jvm_name = JString::from(env.call_method(
                    zip_entry.unwrap().l().unwrap(),
                    obfstr!("getName"),
                    obfstr!("()Ljava/lang/String;"),
                    &[]
                ).unwrap().l().unwrap());

                let jvm_bytes = env.call_static_method(
                    env.find_class(obfstr!("org/apache/commons/io/IOUtils")).unwrap(),
                    obfstr!("toByteArray"),
                    obfstr!("(Ljava/io/InputStream;)[B"),
                    &[JValue::from(zip_input_stream)]
                ).unwrap().l().unwrap();

                let name_ptr = env.get_string_utf_chars(jvm_name).unwrap();
                let name = CStr::from_ptr(name_ptr).to_str().unwrap();

                match name
                {
                    "mixins.shoreline.json" => {
                        MIXIN_CONFIG = env.new_global_ref(jvm_bytes).ok();
                    }
                    "shoreline-refmap.json" => {
                        MIXIN_REFMAP = env.new_global_ref(jvm_bytes).ok();
                    }
                    _ => {
                        if name.ends_with(obfstr!(".class"))
                        {
                            let dependents = get_immediate_dependents(&env, jvm_bytes);

                            if dependents.iter().any(|s| s.starts_with(obfstr!("net/minecraft/")))
                            {
                                env.call_method(
                                    class_map,
                                    obfstr!("put"),
                                    obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                                    &[jvm_name.into(), jvm_bytes.into()]
                                ).unwrap().l().unwrap();

                                env.call_method(
                                    mc_class_dependents,
                                    obfstr!("put"),
                                    obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                                    &[jvm_name.into(), jvm_bytes.into()]
                                ).unwrap();
                            } else if is_mixin_class(&env, jvm_bytes)
                            {
                                env.call_method(
                                    class_map,
                                    obfstr!("put"),
                                    obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                                    &[jvm_name.into(), jvm_bytes.into()]
                                ).unwrap().l().unwrap();

                                if is_mixin_accessor(&env, jvm_bytes)
                                {
                                    class_queue.push_back((jvm_name, jvm_bytes));
                                }
                            } else if is_imixin_class(&env, jvm_bytes)
                            {
                                env.call_method(
                                    class_map,
                                    obfstr!("put"),
                                    obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                                    &[jvm_name.into(), jvm_bytes.into()]
                                ).unwrap().l().unwrap();

                                class_queue.push_back((jvm_name, jvm_bytes));
                            } else
                            {
                                // Cache it if it is mentioned in a Mixin
                                if name.contains(obfstr!("net/shoreline/client/impl/event/"))
                                    // Other exclusions
                                    || name.contains(obfstr!("net/shoreline/client/util/Globals"))
                                    || name.contains(obfstr!("net/shoreline/client/util/network/InteractType"))
                                    || name.contains(obfstr!("net/shoreline/client/impl/manager/client/cape/CapeManager$CapeTexture"))

                                    || name.contains("net/shoreline/client/api/render/RenderLayersClient")
                                {
                                    env.call_method(
                                        class_map,
                                        obfstr!("put"),
                                        obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                                        &[jvm_name.into(), jvm_bytes.into()]
                                    ).unwrap().l().unwrap();
                                }

                                // Define it!
                                class_queue.push_back((jvm_name, jvm_bytes));
                            }
                        } else if name.starts_with(obfstr!("assets/"))
                        {
                            env.call_method(
                                resources_map,
                                obfstr!("put"),
                                obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                                &[jvm_name.into(), jvm_bytes.into()]
                            ).unwrap();
                        }
                    }
                }

                zip_entry = env.call_method(
                    zip_input_stream,
                    obfstr!("getNextEntry"),
                    obfstr!("()Ljava/util/zip/ZipEntry;"),
                    &[]
                ).ok();

                env.release_string_utf_chars(jvm_name, name_ptr).unwrap();
            }
        }
        401 => {
            // Either the token is expired, or it's incorrect
            // Probably doesn't mean anyone is trying to crack
            error_message(
                obfstr!("Error: INVALID\n\nIf this issue persists, please contact Shoreline support.")
            );

            crash(&env, caller_class);

            return JObject::null();
        }

        _ => {
            let msg = format!(
                "{}{}{}",
                obfstr!("Internal server error "),
                http_response_code,
                obfstr!(".\n\nPlease contact Shoreline support!")
            );

            error_message(&msg);

            crash(&env, caller_class);

            return JObject::null();
        }
    };

    let mut dependency_map = HashMap::new();
    let mut bytes_map = HashMap::new();

    for (name, bytes) in class_queue.iter()
    {
        let rs_name = env.get_string((*name).into()).unwrap().to_str().unwrap().to_string();
        let mut dependencies = get_immediate_dependents(&env, *bytes);

        dependencies.retain(|s| s.starts_with(obfstr!("net/shoreline/client/")));

        dependency_map.insert(rs_name.clone(), dependencies);
        bytes_map.insert(rs_name.clone(), bytes);
    }

    while !dependency_map.is_empty()
    {
        let mut defined_this_iteration = Vec::new();

        for (class_name, dependencies) in dependency_map.iter()
        {
            if dependencies.is_empty()
            {
                let bytes = *bytes_map.remove(class_name).unwrap();

                let dependents = get_immediate_dependents(&env, bytes);

                let clazz = define_class(&env, class_name, bytes);
                defined_this_iteration.push(class_name.clone());

                // Can't do enums since the JVM uses reflection to find their value
                if !dependents.iter().any(|s| s.starts_with(obfstr!("java/lang/Enum")))
                {
                    // Add it to the reflection filter map
                    env.call_static_method(
                        caller_class,
                        obfstr!("stop_decompiling_8"),
                        obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                        &[clazz.into()]
                    ).unwrap().l().unwrap();
                }
            }
        }

        for defined_class in defined_this_iteration.iter()
        {
            dependency_map.remove(defined_class);
        }

        for dependencies in dependency_map.values_mut()
        {
            dependencies.retain(|dep| !defined_this_iteration.contains(dep));
        }
    }

    return class_map;
}

/**
 * Define all late-loading MC extending classes
 */
#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_14"]
pub unsafe extern "system" fn stop_decompiling_4<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     _unused_obscure: JObject<'a>) -> JObject<'a>
{
    return match LATE_LOADING_CLASSES.as_ref().take()
    {
        Some(class_cache) =>
            {
                while !env.call_method(
                    class_cache,
                    obfstr!("isEmpty"),
                    obfstr!("()Z"),
                    &[]
                ).unwrap().z().unwrap() {
                    let hashmap_entryset = env.call_method(
                        class_cache,
                        obfstr!("entrySet"),
                        obfstr!("()Ljava/util/Set;"),
                        &[]
                    ).unwrap().l().unwrap();

                    let hashmap_iter = env.call_method(
                        hashmap_entryset,
                        obfstr!("iterator"),
                        obfstr!("()Ljava/util/Iterator;"),
                        &[]
                    ).unwrap().l().unwrap();

                    let next = env.call_method(
                        hashmap_iter,
                        obfstr!("next"),
                        obfstr!("()Ljava/lang/Object;"),
                        &[]
                    ).unwrap().l().unwrap();

                    let key = JString::from(env.call_method(
                        next,
                        obfstr!("getKey"),
                        obfstr!("()Ljava/lang/Object;"),
                        &[]
                    ).unwrap().l().unwrap());

                    let value = env.call_method(
                        class_cache,
                        obfstr!("remove"),
                        obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                        &[key.into()]
                    ).unwrap().l().unwrap();

                    let name_ptr = env.get_string_utf_chars(key).unwrap();
                    let name = CStr::from_ptr(name_ptr).to_str().unwrap();

                    let dependents = get_immediate_dependents(&env, value);

                    let clazz = define_class(&env, name, value);

                    // Can't do enums since the JVM uses reflection to find their value
                    if !dependents.iter().any(|s| s.starts_with(obfstr!("java/lang/Enum")))
                    {
                        // Add it to the reflection filter map
                        env.call_static_method(
                            caller_class,
                            obfstr!("stop_decompiling_8"),
                            obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                            &[clazz.into()]
                        ).unwrap().l().unwrap();
                    }

                    env.release_string_utf_chars(key, name_ptr).unwrap();
                }

                JObject::null()
            },
        None => {
            error_message(obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: 4"));

            crash(&env, caller_class);

            JObject::null()
        }
    }
}

/**
 * Return the user information retrieved earlier
 */
#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_15"]
pub unsafe extern "system" fn stop_decompiling_5<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     _unused_obscure: JObject<'a>) -> JObject<'a>
{
    return match USER_INFO.as_ref().take()
    {
        Some(user_info_ref) => user_info_ref.as_obj(),
        None => {
            error_message(obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: 5"));

            crash(&env, caller_class);

            JObject::null()
        }
    }
}

/**
 * Confirm with the server that the loader versions match
 */
#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_16"]
pub unsafe extern "system" fn stop_decompiling_6<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     loader_version: JObject<'a>) -> JObject<'a>
{
    let url_string = JNIString::from(
        obfstr!("https://api.shorelineclient.net/versioncheck")
    );

    let url = env.new_object(
        obfstr!("java/net/URL"),
        obfstr!("(Ljava/lang/String;)V"),
        &[env.new_string(url_string).unwrap().into()]
    ).unwrap();

    let url_connection = env.call_method(
        url,
        obfstr!("openConnection"),
        obfstr!("()Ljava/net/URLConnection;"),
        &[]
    ).unwrap().l().unwrap();

    let user_agent = env.new_string(obfstr!("User-Agent")).unwrap();
    let user_agent_value = env.new_string(obfstr!("shoreline-client")).unwrap();

    env.call_method(
        url_connection,
        obfstr!("addRequestProperty"),
        obfstr!("(Ljava/lang/String;Ljava/lang/String;)V"),
        &[user_agent.into(), user_agent_value.into()]
    ).unwrap().v().unwrap();

    let get_response_code = env.get_method_id(
        obfstr!("java/net/HttpURLConnection"),
        obfstr!("getResponseCode"),
        obfstr!("()I")
    ).unwrap();

    let http_response = env.call_method_unchecked(
        url_connection,
        get_response_code,
        JavaType::Primitive(Int),
        &[]
    );

    let http_response_code = match http_response
    {
        Ok(code) => code.i().unwrap(),
        Err(_) => {
            -1
        }
    };

    match http_response_code
    {
        -1 => {
            error_message(
                obfstr!("Failed to connect to Shoreline servers.\n\nPlease contact Shoreline support!")
            );

            crash(&env, caller_class);
        }
        200 => {
            let input_stream = env.call_method(
                url_connection,
                obfstr!("getInputStream"),
                obfstr!("()Ljava/io/InputStream;"),
                &[]
            ).unwrap().l().unwrap();

            let input_stream_reader = env.new_object(
                obfstr!("java/io/InputStreamReader"),
                obfstr!("(Ljava/io/InputStream;)V"),
                &[input_stream.into()]
            ).unwrap();

            let buffered_reader = env.new_object(
                obfstr!("java/io/BufferedReader"),
                obfstr!("(Ljava/io/Reader;)V"),
                &[input_stream_reader.into()]
            ).unwrap();

            let read_line = JString::from(env.call_method(
                buffered_reader,
                obfstr!("readLine"),
                obfstr!("()Ljava/lang/String;"),
                &[]
            ).unwrap().l().unwrap());

            let server_ver_ptr = env.get_string_utf_chars(read_line).unwrap();
            let server_ver = CStr::from_ptr(server_ver_ptr).to_str().unwrap();

            let loader_ver_ptr = env.get_string_utf_chars(JString::from(loader_version)).unwrap();
            let loader_ver = CStr::from_ptr(loader_ver_ptr).to_str().unwrap();

            if !server_ver.eq(loader_ver)
            {
                error_message(
                    obfstr!("Your Shoreline loader is out of date!\n\nPlease install the latest version via the installer.")
                );

                crash(&env, caller_class);
            }
        }
        _ => {
            let msg = format!(
                "{}{}{}",
                obfstr!("Internal server error "),
                http_response_code,
                obfstr!(".\n\nPlease contact Shoreline support!")
            );

            error_message(&msg);

            crash(&env, caller_class);
        }
    }

    return JObject::null();
}

/**
 * Alerts the webhook and crashes
 */

#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_17"]
pub unsafe extern "system" fn stop_decompiling_7<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     param_array: JObject<'a>) -> JObject<'a>
{
    let msg = env.get_object_array_element(*param_array, 0).unwrap();
    let hwid = env.get_object_array_element(*param_array, 1).unwrap();
    let username = env.get_object_array_element(*param_array, 2).unwrap();
    let mods = env.get_object_array_element(*param_array, 4).unwrap(); // skip index 3, its usertype

    let msg_ptr = env.get_string_utf_chars(JString::from(msg)).unwrap();
    let hwid_ptr = env.get_string_utf_chars(JString::from(hwid)).unwrap();
    let username_ptr = env.get_string_utf_chars(JString::from(username)).unwrap();
    let mods_ptr = env.get_string_utf_chars(JString::from(mods)).unwrap();

    let msg_cstr = CStr::from_ptr(msg_ptr).to_str().unwrap();
    let hwid_cstr = CStr::from_ptr(hwid_ptr).to_str().unwrap();
    let username_cstr = CStr::from_ptr(username_ptr).to_str().unwrap();
    let mods_cstr = CStr::from_ptr(mods_ptr).to_str().unwrap();

    alert_webhook(
        &env,
        msg_cstr,
        hwid_cstr,
        username_cstr,
        mods_cstr
    );

    crash(&env, caller_class);

    return JObject::null();
}

/**
 * Adds the class to the reflection filter map
 */

#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_18"]
pub unsafe extern "system" fn stop_decompiling_8<'a>(env: JNIEnv<'a>,
                                                     _caller_class: JClass<'a>,
                                                     class: JObject<'a>) -> JObject<'a>
{
    let wildcard = env.new_string("*").unwrap();

    let set = env.call_static_method(
        env.find_class(obfstr!("java/util/Set")).unwrap(),
        obfstr!("of"),
        obfstr!("(Ljava/lang/Object;)Ljava/util/Set;"),
        &[wildcard.into()]
    ).unwrap().l().unwrap();

    let res = env.call_static_method(
        env.find_class(obfstr!("jdk/internal/reflect/Reflection")).unwrap(),
        obfstr!("registerFieldsToFilter"),
        obfstr!("(Ljava/lang/Class;Ljava/util/Set;)V"),
        &[class.into(), set.into()]
    );

    if env.exception_check().unwrap() {
        env.exception_describe().unwrap();
    }

    res.unwrap().v().unwrap();

    env.call_static_method(
        env.find_class(obfstr!("jdk/internal/reflect/Reflection")).unwrap(),
        obfstr!("registerMethodsToFilter"),
        obfstr!("(Ljava/lang/Class;Ljava/util/Set;)V"),
        &[class.into(), set.into()]
    ).unwrap().v().unwrap();

    return JObject::null();
}

#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_19"]
pub unsafe extern "system" fn stop_decompiling_9<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     information_array: JObject<'a>) -> JObject<'a>
{
    if IsDebuggerPresent() != 0
    {
        error_message(
            obfstr!("We know what you did")
        );

        let message = env.new_string(obfstr!("Native debugger present")).unwrap();

        env.set_object_array_element(*information_array, 0, message).unwrap();

        env.call_static_method(
            caller_class,
            obfstr!("stop_decompiling_7"),
            obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
            &[information_array.into()]
        ).unwrap().l().unwrap();

        crash(&env, caller_class);
    }

    if inside_vm()
    {
        error_message(
            obfstr!("We know what you did")
        );

        let message = env.new_string(obfstr!("Launched inside VM")).unwrap();

        env.set_object_array_element(*information_array, 0, message).unwrap();

        env.call_static_method(
            caller_class,
            obfstr!("stop_decompiling_7"),
            obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
            &[information_array.into()]
        ).unwrap().l().unwrap();

        crash(&env, caller_class);
    }

    return JObject::null();
}

#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_110"]
pub unsafe extern "system" fn stop_decompiling_10<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     resource_name: JObject<'a>) -> JObject<'a>
{
    return match RESOURCES_MAP.as_mut()
    {
        Some(resources_map) => {
            return env.call_method(
                resources_map.as_obj(),
                obfstr!("get"),
                obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                &[resource_name.into()]
            ).unwrap().l().unwrap()
        },
        None => {
            error_message(obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: 10"));

            crash(&env, caller_class);

            JObject::null()
        }
    }
}