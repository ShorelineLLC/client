mod obfuscation;
mod login;
mod notifs;
mod crypto;
mod network;
mod irc;
mod session;
mod integrity;
mod core;
mod autoupdate;
mod classloader;
mod mixinservice;
mod asm;
mod resources;
mod eventbus;
mod anticrack;
mod decryptor;

use std::fmt::format;
use std::os::raw::{c_int, c_void};
use std::process::exit;
use jni::{JavaVM, JNIEnv, NativeMethod};
use jni::objects::{GlobalRef, JClass, JObject, JString};
use jni::strings::JNIString;
use jni::sys::{jboolean, JNI_ERR, JNI_FALSE, JNI_TRUE, JNI_VERSION_1_8};
use lazy_static::lazy_static;
use obfstr::obfstr;
use crate::classloader::ClassLoader;
use crate::irc::IRC;
use crate::login::User;

#[no_mangle]
pub unsafe extern "system" fn JNI_OnLoad(vm: JavaVM,
                                         _reserved: &mut c_void) -> c_int
{
    let mut env = vm.get_env().unwrap();

    obfuscation::check_obfuscated_environment(&mut env);

    notifs::info(&mut env, obfstr!("Logging in..."));

    match login::login()
    {
        Ok(user) => {
            notifs::info(
                &mut env,
                format! {
                    "{}{}{}",
                    obfstr!("Welcome, "),
                    user.username,
                    obfstr!("!")
                }.as_str()
            );

            session::USER = Some(user);

            if obfuscation::IS_ENCRYPTED_ENVIRONMENT
            {
                let loader_class = obfuscation::get_loader_class(&mut env);
                decryptor::decrypt_all_classes(&mut env, loader_class);
            }

            if obfuscation::IS_DEVELOPMENT_ENVIRONMENT
            {
                eventbus::init_internal(&mut env);
            }

            register_natives(&mut env);
        }
        Err(e) => {
            notifs::error(&mut env, &e);
            notifs::display_error_msg(&e);

            exit(-1);
        }
    }

    JNI_VERSION_1_8
}

unsafe fn register_natives(env: &mut JNIEnv)
{
    /* -------------------------- Loader -------------------------- */

    let loader_class = obfuscation::get_loader_class(env);

    let loader_methods = [
        NativeMethod
        {
            name: JNIString::from(obfstr!("onPreLaunch")),
            sig: JNIString::from(obfstr!("()V")),
            fn_ptr: on_pre_launch as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfstr!("onInitializeClient")),
            sig: JNIString::from(obfstr!("()V")),
            fn_ptr: on_initialize_client as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfstr!("onLoad")),
            sig: JNIString::from(obfstr!("(Ljava/lang/String;)V")),
            fn_ptr: on_load as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfstr!("getRefMapperConfig")),
            sig: JNIString::from(obfstr!("()Ljava/lang/String;")),
            fn_ptr: get_refmapper_config as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfstr!("shouldApplyMixin")),
            sig: JNIString::from(obfstr!("(Ljava/lang/String;Ljava/lang/String;)Z")),
            fn_ptr: should_apply_mixin as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfstr!("acceptTargets")),
            sig: JNIString::from(obfstr!("(Ljava/util/Set;Ljava/util/Set;)V")),
            fn_ptr: should_apply_mixin as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfstr!("getMixins")),
            sig: JNIString::from(obfstr!("()Ljava/util/List;")),
            fn_ptr: get_mixins as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfstr!("preApply")),
            sig: JNIString::from(obfstr!("(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;Ljava/lang/String;Lorg/spongepowered/asm/mixin/extensibility/IMixinInfo;)V")),
            fn_ptr: pre_apply as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfstr!("postApply")),
            sig: JNIString::from(obfstr!("(Ljava/lang/String;Lorg/objectweb/asm/tree/ClassNode;Ljava/lang/String;Lorg/spongepowered/asm/mixin/extensibility/IMixinInfo;)V")),
            fn_ptr: post_apply as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfuscation::SHOW_ERROR_WINDOW_FUNCTION.get_name()),
            sig: JNIString::from(obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;")),
            fn_ptr: notifs::show_error_window as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfuscation::PERFORM_VERSION_CHECK_FUNCTION.get_name()),
            sig: JNIString::from(obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;")),
            fn_ptr: integrity::perform_version_check as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfuscation::GET_RESOURCE_FUNCTION.get_name()),
            sig: JNIString::from(obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;")),
            fn_ptr: resources::open_resource as *mut c_void, // same fn_ptr as ResourcePackExt#openResourceInternal
        }
    ];

    env.register_native_methods(loader_class, &loader_methods).unwrap();

    /* -------------------------- UserSession -------------------------- */

    let user_session_class = obfuscation::get_user_session_class(env);

    let user_session_methods = [
        NativeMethod
        {
            name: JNIString::from(obfuscation::GET_USER_INFO_FUNCTION.get_name()),
            sig: JNIString::from(obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;")),
            fn_ptr: session::get_user_session as *mut c_void,
        }
    ];

    env.register_native_methods(user_session_class, &user_session_methods).unwrap();

    /* ----------------------------- Mixin Service ----------------------------- */

    let mixin_service_class = obfuscation::get_mixinservice_class(env);

    let mixin_service_methods = [
        NativeMethod
        {
            name: JNIString::from(obfuscation::GET_INTERNAL_CLASS_BYTES_FUNCTION.get_name()),
            sig: JNIString::from(obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;")),
            fn_ptr: mixinservice::get_class_bytes as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfuscation::GET_INTERNAL_INPUT_STREAM_FUNCTION.get_name()),
            sig: JNIString::from(obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;")),
            fn_ptr: mixinservice::get_resource_as_stream as *mut c_void,
        }
    ];

    env.register_native_methods(mixin_service_class, &mixin_service_methods).unwrap();

    /* -------------------------- Resource Pack Extension -------------------------- */

    let resource_pack_class = obfuscation::get_resourcepack_class(env);

    let resource_pack_methods = [
        NativeMethod
        {
            name: JNIString::from(obfuscation::GET_RESOURCE_INTERNAL_FUNCTION.get_name()),
            sig: JNIString::from(obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;")),
            fn_ptr: resources::open_resource as *mut c_void,
        }
    ];

    env.register_native_methods(resource_pack_class, &resource_pack_methods).unwrap();
}

unsafe fn register_client_natives(env: &mut JNIEnv)
{
    /* -------------------------------- IRC Manager -------------------------------- */

    let irc_manager_class = obfuscation::get_irc_manager_class(env);

    let irc_manager_methods = [
        NativeMethod
        {
            name: JNIString::from(obfuscation::IRC_DISPATCH_PACKET_METHOD.get_name()),
            sig: JNIString::from(obfstr!("(Ljava/lang/String;)V")),
            fn_ptr: irc::dispatch_packet as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfuscation::IRC_ATTEMPT_RECONNECTION_METHOD.get_name()),
            sig: JNIString::from(obfstr!("(Ljava/lang/String;)V")),
            fn_ptr: irc::attempt_reconnection as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfuscation::IRC_READ_INCOMING_METHOD.get_name()),
            sig: JNIString::from(obfstr!("()Ljava/util/List;")),
            fn_ptr: irc::read_incoming_packets as *mut c_void,
        }
    ];

    env.register_native_methods(irc_manager_class, &irc_manager_methods).unwrap();

    /* -------------------------------- Event Bus -------------------------------- */

    let event_bus_class = obfuscation::get_event_bus_class(env);

    let event_bus_methods = [
        NativeMethod
        {
            name: JNIString::from(obfuscation::EVENT_BUS_SUBSCRIBE_METHOD.get_name()),
            sig: JNIString::from(obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;")),
            fn_ptr: eventbus::subscribe as *mut c_void,
        },
        NativeMethod
        {
            name: JNIString::from(obfuscation::EVENT_BUS_UNSUBSCRIBE_METHOD.get_name()),
            sig: JNIString::from(obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;")),
            fn_ptr: eventbus::unsubscribe as *mut c_void,
        }
    ];

    env.register_native_methods(event_bus_class, &event_bus_methods).unwrap();
}

/* -------------------------------- Fabric -------------------------------- */

pub unsafe extern "system" fn on_pre_launch(mut env: JNIEnv,
                                            _instance: JObject)
{
    let system_property = env.new_string(obfstr!("java.awt.headless")).unwrap();
    let value = env.new_string(obfstr!("true")).unwrap();

    env.call_static_method(
        obfstr!("java/lang/System"),
        obfstr!("setProperty"),
        obfstr!("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
        &[(&system_property).into(), (&value).into()]
    ).unwrap().l().unwrap();

    env.call_static_method(
        obfstr!("java/awt/GraphicsEnvironment"),
        obfstr!("isHeadless"),
        obfstr!("()Z"),
        &[]
    ).unwrap().z().unwrap();
}

pub unsafe extern "system" fn on_initialize_client(mut env: JNIEnv,
                                                   _instance: JObject)
{
    if !obfuscation::IS_DEVELOPMENT_ENVIRONMENT
    {
        let classloader = ClassLoader::get_instance(&mut env);

        classloader.define_late_loading_classes(&mut env);
    }

    register_client_natives(&mut env);

    let token = session::get_user(&mut env).irc_token.clone();
    IRC::create_instance(token, &mut env);

    let main_client_class = obfuscation::get_main_client_class(&mut env);
    let client_initialization_function = obfuscation::CLIENT_INITIALIZATION_FUNCTION.get_name();

    let the_unsafe = env.get_static_field(
        obfstr!("sun/misc/Unsafe"),
        obfstr!("theUnsafe"),
        obfstr!("Lsun/misc/Unsafe;")
    ).unwrap().l().unwrap();

    let main_client_instance = env.call_method(
        the_unsafe,
        obfstr!("allocateInstance"),
        obfstr!("(Ljava/lang/Class;)Ljava/lang/Object;"),
        &[(&main_client_class).into()]
    ).unwrap().l().unwrap();

    let init_call = env.call_method(
        main_client_instance,
        client_initialization_function,
        obfstr!("()V"),
        &[]
    );

    if env.exception_check().unwrap()
    {
        env.exception_describe().unwrap();

        let msg = obfstr! {
            "Failed to initialize Shoreline. Please copy the stacktrace found in your latest.log \
            and send it to a developer."
        }.to_string();

        notifs::error(&mut env, &msg);
        notifs::display_error_msg(&msg);

        exit(-1);
    }

    init_call.unwrap();
}

/* -------------------------------- Sponge -------------------------------- */

pub unsafe extern "system" fn on_load(mut env: JNIEnv,
                                      _instance: JObject,
                                      _mixin_package: JString)
{
    // we exclude the 'package' property from mixins.shoreline.plugin.json for obscurity,
    // which is '"package": "net.shoreline.client"', and write it manually via reflection
    mixinservice::write_mixin_package(&mut env);

    let token = match core::request_token(&mut env)
    {
        Ok(token) => token,
        Err(e) => {
            notifs::error(&mut env, &e);
            notifs::display_error_msg(&e);

            exit(-1);
        }
    };

    let payload = match core::download_resources(token)
    {
        Ok(payload) => payload,
        Err(e) => {
            notifs::error(&mut env, &e);
            notifs::display_error_msg(&e);

            exit(-1);
        }
    };

    // Create native classloader impl
    let classloader = match ClassLoader::create_instance(payload)
    {
        Ok(classloader) => classloader,
        Err(msg) => {
            notifs::error(&mut env, &msg);
            notifs::display_error_msg(&msg);

            exit(-1);
        }
    };

    // Define our classes
    classloader.define_classes(&mut env);

    // Inject our mixin service
    mixinservice::inject_mixin_service(&mut env);

    // Load the access widener
    classloader.load_access_widener(&mut env);

    notifs::info(&mut env, obfstr!("Finished loading Shoreline!"));
}

pub unsafe extern "system" fn get_refmapper_config<'a>(mut env: JNIEnv<'a>,
                                                       _instance: JObject) -> JString<'a>
{
    return env.new_string(obfstr!("shoreline-refmap")).unwrap();
}

pub unsafe extern "system" fn should_apply_mixin(_env: JNIEnv,
                                                 _instance: JObject,
                                                 _target_class_name: JString,
                                                 _mixin_class_name: JString) -> jboolean
{
    return JNI_TRUE;
}

pub unsafe extern "system" fn accept_targets(_env: JNIEnv,
                                             _instance: JObject,
                                             _my_targets: JObject,
                                             _other_targets: JObject)
{
}

pub unsafe extern "system" fn get_mixins<'a>(mut env: JNIEnv<'a>,
                                             _instance: JObject) -> JObject<'a>
{
    let mixin_list = env.new_object(
        obfstr!("java/util/ArrayList"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    let classloader = ClassLoader::get_instance(&mut env);

    let mixins = match classloader.payload.mixin_list.take()
    {
        Some(mixins) => mixins,
        None => {
            let msg = obfstr! {
                "Failed to load mixins because the mixin list was unable to be found. \
                This should never happen, please report this to a developer."
            }.to_string();

            notifs::error(&mut env, &msg);
            notifs::display_error_msg(&msg);

            exit(-1);
        }
    };

    for mixin in mixins
    {
        let java_str = env.new_string(mixin).unwrap();

        env.call_method(
            &mixin_list,
            obfstr!("add"),
            obfstr!("(Ljava/lang/Object;)Z"),
            &[(&java_str).into()]
        ).unwrap().z().unwrap();
    }

    return mixin_list;
}

pub unsafe extern "system" fn pre_apply(mut env: JNIEnv,
                                        _instance: JObject,
                                        _target_class_name: JString,
                                        _target_class: JObject,
                                        _mixin_class_name: JString,
                                        _mixin_info: JObject)
{
}

// Clear cached mixin bytecode to prevent dumping
pub unsafe extern "system" fn post_apply(mut env: JNIEnv,
                                         _instance: JObject,
                                         _target_class_name: JString,
                                         _target_class: JObject,
                                         _mixin_class_name: JString,
                                         mixin_info: JObject)
{
    env.set_field(
        &mixin_info,
        obfstr!("state"),
        obfstr!("Lorg/spongepowered/asm/mixin/transformer/MixinInfo$State;"),
        (&JObject::null()).into()
    ).unwrap();

    // Not sure if pendingState is even valid at this point but we will clear it anyway

    env.set_field(
        mixin_info,
        obfstr!("pendingState"),
        obfstr!("Lorg/spongepowered/asm/mixin/transformer/MixinInfo$State;"),
        (&JObject::null()).into()
    ).unwrap();
}