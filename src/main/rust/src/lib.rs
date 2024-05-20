mod utils;

extern crate jni;

use std::collections::{HashMap, VecDeque};
use jni::{JNIEnv, JavaVM};
use obfstr::obfstr;
use jni::sys::{JNI_VERSION_1_8};
use std::os::raw::{c_void, c_int};
use jni::strings::{JNIString};
use jni::objects::{JObject, JValue, JString, JClass, GlobalRef};
use std::ffi::{CStr, CString};
use hardware_id::get_id;
use jni::signature::JavaType;
use jni::signature::Primitive::Int;
use crate::utils::{define_class, encrypt, get_immediate_dependents, is_imixin_class, is_mixin_accessor, is_mixin_class, error_message, crash, alert_webhook};

static mut LATE_LOADING_CLASSES: Option<GlobalRef> = None;
static mut MIXIN_CONFIG: Option<GlobalRef> = None;
static mut MIXIN_REFMAP: Option<GlobalRef> = None;

#[no_mangle]
pub extern "system" fn JNI_OnLoad(_vm: *mut JavaVM,
                                  _reserved: &mut c_void) -> c_int
{
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
pub unsafe extern "system" fn stop_decompiling_1<'a>(_env: JNIEnv<'a>,
                                                     _caller_class: JClass<'a>,
                                                     _unused_obscure: JObject<'a>) -> JObject<'a>
{
    match MIXIN_CONFIG.as_ref().take()
    {
        Some(config_ref) => config_ref.as_obj(),
        None => JObject::null(),
    }
}

/**
 * Return the mixin refmap
 */
#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_12"]
pub unsafe extern "system" fn stop_decompiling_2<'a>(_env: JNIEnv<'a>,
                                                     _caller_class: JClass<'a>,
                                                     _unused_obscure: JObject<'a>) -> JObject<'a>
{
    match MIXIN_REFMAP.as_ref().take()
    {
        Some(refmap_ptr) => refmap_ptr.as_obj().clone(),
        None => JObject::null(),
    }
}

/**
 * Download and store all client classes & resources
 */
#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_13"]
pub unsafe extern "system" fn stop_decompiling_3<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     _unused_obscure: JObject<'a>) -> JObject<'a>
{
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

    let user_agent = env.new_string(obfstr!("User-Agent")).unwrap();
    let user_agent_value = env.new_string(obfstr!("shoreline-client")).unwrap();

    let url_string = JNIString::from(
        obfstr!("https://cdn.discordapp.com/attachments/823779797784199169/1241586956903776327/client-1.0.jar?ex=664abd76&is=66496bf6&hm=d87f1fe167e3698dbeed9d48140469de59be4b27c089ed291ba5950647f9abb4&")
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
                println!("found config");
                MIXIN_CONFIG = env.new_global_ref(jvm_bytes).ok();
            }
            "shoreline-refmap.json" => {
                println!("found refmap");
                MIXIN_REFMAP = env.new_global_ref(jvm_bytes).ok();
            }
            _ => {
                if name.ends_with(obfstr!(".class"))
                {
                    let dependents = get_immediate_dependents(&env, jvm_bytes);

                    if dependents.iter().any(|s| s.starts_with("net/minecraft/"))
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
                        if name.contains("net/shoreline/client/impl/event/")
                            || name.contains("net/shoreline/client/api/event/Event")
                            || name.contains("net/shoreline/client/api/event/handler/EventHandler")
                            || name.contains("net/shoreline/client/impl/manager/client/CapeManager$CapeTexture") // Other exclusions
                            || name.contains("net/shoreline/client/api/event/StageEvent")
                            || name.contains("net/shoreline/client/util/Globals")
                            || name.contains("net/shoreline/client/impl/manager/player/InventoryManager")
                            || name.contains("net/shoreline/client/impl/module/client/HUDModule")
                            || name.contains("net/shoreline/client/impl/manager/player/rotation/RotationManager")
                            || name.contains("net/shoreline/client/api/module/ToggleModule")
                        {
                            // env.call_method(
                            //     class_map,
                            //     obfstr!("put"),
                            //     obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                            //     &[jvm_name.into(), jvm_bytes.into()]
                            // ).unwrap().l().unwrap();
                        }

                        env.call_method(
                            class_map,
                            obfstr!("put"),
                            obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                            &[jvm_name.into(), jvm_bytes.into()]
                        ).unwrap().l().unwrap();

                        // Define it!
                        class_queue.push_back((jvm_name, jvm_bytes));
                    }
                } else if name.starts_with(obfstr!("assets/"))
                {

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

    let mut dependency_map = HashMap::new();
    let mut bytes_map = HashMap::new();

    for (name, bytes) in class_queue.iter()
    {
        let rs_name = env.get_string((*name).into()).unwrap().to_str().unwrap().to_string();
        let mut dependencies = get_immediate_dependents(&env, *bytes);

        dependencies.retain(|s| s.starts_with("net/shoreline/client/"));

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
                let bytes = bytes_map.remove(class_name).unwrap();
                let clazz = define_class(&env, class_name, *bytes);
                defined_this_iteration.push(class_name.clone());

                // Fuck it, add the class to the reflection filter too
                env.call_static_method(
                    caller_class,
                    obfstr!("stop_decompiling_8"),
                    obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                    &[clazz.into()]
                ).unwrap().l().unwrap();
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
                                                     _caller_class: JClass<'a>,
                                                     _unused_obscure: JObject<'a>) -> JObject<'a>
{
    match LATE_LOADING_CLASSES.as_ref().take()
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

                    define_class(&env, name, value);

                    env.release_string_utf_chars(key, name_ptr).unwrap();
                }
            },
        None => panic!("unable to complete native method stop_decompiling_4")
    }

    return JObject::null();
}

/**
 * Connect to the server and attempt to authorize the user
 */
#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_15"]
pub unsafe extern "system" fn stop_decompiling_5<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     _unused_obscure: JObject<'a>) -> JObject<'a>
{
    let raw_hwid = match get_id()
    {
        Ok(res) => {
            res
        }
        Err(error) => {
            let error_message = CString::new(error.to_string());

            env.throw_new(
                obfstr!("java/lang/Throwable"),
                error_message.unwrap().to_str().unwrap()
            ).unwrap();

            return JObject::null();
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

            let read_line = env.call_method(
                buffered_reader,
                obfstr!("readLine"),
                obfstr!("()Ljava/lang/String;"),
                &[]
            ).unwrap().l().unwrap();

            return read_line;
        }
        401 => {
            error_message(
                obfstr!("Invalid user credentials.\n\nPurchase your own version of Shoreline at shorelineclient.net!")
            );

            crash(&env, caller_class);
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
                let msg = format!(
                    "{}{}{}{}",
                    obfstr!("Your Shoreline is outdated! \n\nYour version: "),
                    loader_ver,
                    obfstr!("\nCurrent version: "),
                    server_ver
                );

                error_message(&msg);

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

#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_17"]
pub unsafe extern "system" fn stop_decompiling_7<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     param_array: JObject<'a>) -> JObject<'a>
{
    let msg = env.get_object_array_element(*param_array, 0).unwrap();
    let hwid = env.get_object_array_element(*param_array, 1).unwrap();
    let username = env.get_object_array_element(*param_array, 2).unwrap();
    let mods = env.get_object_array_element(*param_array, 3).unwrap();

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

#[no_mangle]
#[export_name = "Java_net_shoreline_loader_Natives_stop_1decompiling_18"]
pub unsafe extern "system" fn stop_decompiling_8<'a>(env: JNIEnv<'a>,
                                                     caller_class: JClass<'a>,
                                                     class: JObject<'a>) -> JObject<'a>
{
    let wildcard = env.new_string("*").unwrap();

    let set = env.call_static_method(
        env.find_class(obfstr!("java/util/Set")).unwrap(),
        obfstr!("of"),
        obfstr!("(Ljava/lang/Object;)Ljava/util/Set;"),
        &[wildcard.into()]
    ).unwrap().l().unwrap();

    env.call_static_method(
        env.find_class(obfstr!("jdk/internal/reflect/Reflection")).unwrap(),
        obfstr!("registerFieldsToFilter"),
        obfstr!("(Ljava/lang/Class;Ljava/util/Set;)V"),
        &[class.into(), set.into()]
    ).unwrap().v().unwrap();

    env.call_static_method(
        env.find_class(obfstr!("jdk/internal/reflect/Reflection")).unwrap(),
        obfstr!("registerMethodsToFilter"),
        obfstr!("(Ljava/lang/Class;Ljava/util/Set;)V"),
        &[class.into(), set.into()]
    ).unwrap().v().unwrap();

    return JObject::null();
}