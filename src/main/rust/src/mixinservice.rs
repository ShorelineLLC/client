use std::io::Read;
use std::process::exit;
use jni::JNIEnv;
use jni::objects::{JByteArray, JObject, JString, JValue};
use jni::signature::{JavaType, ReturnType};
use jni::sys::{jboolean, jbyteArray, JNI_TRUE};
use obfstr::obfstr;

use crate::classloader::ClassLoader;
use crate::{notifs, obfuscation};

pub fn write_mixin_package(env: &mut JNIEnv)
{
    let all_configs = env.get_static_field(
        obfstr!("org/spongepowered/asm/mixin/transformer/Config"),
        obfstr!("allConfigs"),
        obfstr!("Ljava/util/Map;")
    ).unwrap().l().unwrap();

    let shoreline_cfg_name = env.new_string(
        obfstr!("mixins.shoreline.plugin.json")
    ).unwrap();

    let shoreline_cfg = env.call_method(
        all_configs,
        obfstr!("get"),
        obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
        &[(&shoreline_cfg_name).into()]
    ).unwrap().l().unwrap();

    let our_config = env.call_method(
        shoreline_cfg,
        obfstr!("getConfig"),
        obfstr!("()Lorg/spongepowered/asm/mixin/extensibility/IMixinConfig;"),
        &[]
    ).unwrap().l().unwrap();

    let parent_class = env.find_class(
        obfstr!("org/spongepowered/asm/mixin/transformer/MixinConfig")
    ).unwrap();

    let our_mixin_package = env.new_string(
        obfstr!("net.shoreline.client")
    ).unwrap();

    env.set_field_unchecked(
        our_config,
        (&parent_class, obfstr!("mixinPackage"), obfstr!("Ljava/lang/String;")),
        (&our_mixin_package).into()
    ).unwrap();
}

pub unsafe fn inject_mixin_service(env: &mut JNIEnv)
{
    let mixin_service_class = obfuscation::get_mixinservice_class(env);

    let new_instance = env.new_object(
        mixin_service_class,
        obfstr!("()V"),
        &[]
    ).unwrap();

    let current_instance = env.call_static_method(
        obfstr!("org/spongepowered/asm/service/MixinService"),
        obfstr!("getInstance"),
        obfstr!("()Lorg/spongepowered/asm/service/MixinService;"),
        &[]
    ).unwrap().l().unwrap();

    env.set_field(
        current_instance,
        obfstr!("service"),
        obfstr!("Lorg/spongepowered/asm/service/IMixinService;"),
        (&new_instance).into()
    ).unwrap();

    let all_configs = env.get_static_field(
        obfstr!("org/spongepowered/asm/mixin/transformer/Config"),
        obfstr!("allConfigs"),
        obfstr!("Ljava/util/Map;")
    ).unwrap().l().unwrap();

    let our_config_str = env.new_string(obfstr!("mixins.shoreline.plugin.json")).unwrap();
    let our_config = env.call_method(
        all_configs,
        obfstr!("get"),
        obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
        &[(&our_config_str).into()]
    ).unwrap().l().unwrap();

    let our_config = env.call_method(
        our_config,
        obfstr!("getConfig"),
        obfstr!("()Lorg/spongepowered/asm/mixin/extensibility/IMixinConfig;"),
        &[]
    ).unwrap().l().unwrap();

    let parent_class = env.find_class(
        obfstr!("org/spongepowered/asm/mixin/transformer/MixinConfig")
    ).unwrap();

    env.set_field_unchecked(
        our_config,
        (&parent_class, obfstr!("service"), obfstr!("Lorg/spongepowered/asm/service/IMixinService;")),
        (&new_instance).into()
    ).unwrap();
}

pub unsafe extern "system" fn get_class_bytes<'a>(mut env: JNIEnv<'a>,
                                                  _instance: JObject,
                                                  name: JString) -> JObject<'a>
{
    return match get_class_bytes_internal(&mut env, &name)
    {
        Ok(bytes) => bytes,
        Err(msg) => {
            notifs::error(&mut env, &msg);
            notifs::display_error_msg(&msg);

            exit(-1)
        }
    }
}

unsafe fn get_class_bytes_internal<'a>(env: &mut JNIEnv<'a>,
                                       name: &JString) -> Result<JObject<'a>, String>
{
    let classloader = ClassLoader::get_instance(env);

    let rust_name: String = env.get_string(name).unwrap().into();

    let class = classloader.payload.mixin_bytecode
        .keys()
        .find(|class_name| {
            rust_name.replace(obfstr!("/"), obfstr!(".")).eq(*class_name)
        })
        .cloned();

    return match class
    {
        Some(class) => {
            let encrypted_bytes = classloader.payload.mixin_bytecode.remove(&class).unwrap();

            let decrypted_class_bytes = match crate::crypto::decrypt(encrypted_bytes)
            {
                Ok(content) => content,
                Err(_) => {
                    return Err(obfstr! {
                        "Failed to load a mixin due to improper decryption. \
                        Please report this to a developer."
                    }.to_string())
                }
            };

            let java_bytes = env.byte_array_from_slice(decrypted_class_bytes.as_slice()).unwrap();

            Ok(JObject::from(java_bytes))
        }

        None => Ok(JObject::null())
    }
}

pub unsafe extern "system" fn get_resource_as_stream<'a>(mut env: JNIEnv<'a>,
                                                         _instance: JObject,
                                                         name: JString) -> JObject<'a>
{
    return match get_resource_as_stream_internal(&mut env, &name)
    {
        Ok(stream) => stream,
        Err(msg) => {
            notifs::error(&mut env, &msg);
            notifs::display_error_msg(&msg);

            exit(-1)
        }
    }
}

unsafe fn get_resource_as_stream_internal<'a>(env: &mut JNIEnv<'a>,
                                              name: &JString) -> Result<JObject<'a>, String>
{
    let rust_name: String = env.get_string(name).unwrap().into();

    if rust_name.eq(obfstr!("shoreline-refmap"))
    {
        let classloader = ClassLoader::get_instance(env);

        let refmap = match classloader.payload.refmap.take()
        {
            Some(refmap) => refmap,
            None => {
                return Err(obfstr! {
                    "Failed to load mixins because the refmap was unable to be found. \
                    This should never happen, please report this to a developer."
                }.to_string());
            }
        };

        let decrypted_refmap = match crate::crypto::decrypt(refmap)
        {
            Ok(content) => content,
            Err(_) => {
                return Err(obfstr! {
                    "Failed to load the mixin refmap due to improper decryption. \
                    Please report this to a developer."
                }.to_string());
            }
        };

        let java_bytes = env.byte_array_from_slice(decrypted_refmap.as_slice()).unwrap();

        let byte_array_input_stream = env.new_object(
            obfstr!("java/io/ByteArrayInputStream"),
            obfstr!("([B)V"),
            &[(&java_bytes).into()]
        ).unwrap();

        return Ok(byte_array_input_stream);
    }

    Ok(JObject::null())
}