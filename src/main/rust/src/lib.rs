mod utils;

extern crate jni;

use std::collections::{HashMap, VecDeque};
use jni::{JNIEnv, JavaVM};
use obfstr::obfstr;
use jni::sys::{JNI_VERSION_1_8};
use std::os::raw::{c_void, c_int};
use jni::strings::{JNIString};
use jni::objects::{JObject, JValue, JString, JClass, GlobalRef};
use std::ffi::CStr;
use crate::utils::{define_class, get_immediate_dependents, is_imixin_class, is_mixin_accessor, is_mixin_class};

static mut LATE_LOADING_CLASSES: Option<GlobalRef> = None;
static mut MIXIN_CONFIG: Option<GlobalRef> = None;
static mut MIXIN_REFMAP: Option<GlobalRef> = None;

#[no_mangle]
pub extern "system" fn JNI_OnLoad(_vm: *mut JavaVM,
                                  _reserved: &mut c_void) -> c_int
{
    println!("Successfully loaded native library");

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
                                                     _caller_class: JClass<'a>,
                                                     class_loader: JObject<'a>) -> JObject<'a>
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
        obfstr!("https://cdn.discordapp.com/attachments/823779797784199169/1240857446969380905/client-1.0.jar?ex=6648160d&is=6646c48d&hm=622a2125d4adfbdac9088305b634493d0a54b142be8f7c9719330a0176a4fa56&")
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
                        // Define it!
                        if name.contains("event")
                            || name.contains("net/shoreline/client/impl/manager/client/CapeManager$CapeTexture") // special exclusions
                            || name.contains("net/shoreline/client/Shoreline")
                            || name.contains("net/shoreline/client/util/Globals")
                            || name.contains("net/shoreline/client/init/Managers")
                            || name.contains("net/shoreline/client/impl/manager/player/InventoryManager")
                            || name.contains("net/shoreline/client/impl/manager/player/rotation/RotationManager")
                            || name.contains("net/shoreline/client/init/Modules")
                            || name.contains("net/shoreline/client/impl/module/client/HUDModule")
                            || name.contains("net/shoreline/client/api/module/ToggleModule")
                            || name.contains("net/shoreline/client/api/render/RenderManager")
                            || name.contains("net/shoreline/client/util/network/InteractType")
                        {
                            // NIGGAGAGGAGAGA

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

                        class_queue.push_back((jvm_name, jvm_bytes));
                    }
                } else
                {
                    // It's an asset


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
                define_class(&env, class_name, *bytes);
                defined_this_iteration.push(class_name.clone());
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