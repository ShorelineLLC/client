use std::process::exit;
use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString};
use obfstr::obfstr;
use crate::{notifs, obfuscation};
use crate::classloader::ClassLoader;

pub unsafe extern "system" fn open_resource<'a>(mut env: JNIEnv<'a>,
                                                _class: JClass,
                                                name: JString) -> JObject<'a>
{
    return match open_resource_internal(&mut env, &name)
    {
        Ok(inputstream) => inputstream,
        Err(msg) => {
            notifs::error(&mut env, &msg);
            notifs::display_error_msg(&msg);

            exit(-1)
        }
    }
}

unsafe fn open_resource_internal<'a>(env: &mut JNIEnv<'a>,
                                     name: &JString) -> Result<JObject<'a>, String>
{
    if obfuscation::IS_DEVELOPMENT_ENVIRONMENT
    {
        return Ok(JObject::null());
    }

    let classloader = ClassLoader::get_instance(env);

    let rust_name: String = env.get_string(name).unwrap().into();

    return match classloader.payload.resource_bytecode.get(&rust_name)
    {
        Some(bytes) => {
            let decrypted_bytes = match crate::crypto::decrypt(bytes.clone())
            {
                Ok(content) => content,
                Err(_) => {
                    return Err(obfstr! {
                        "Failed to load a resource due to improper decryption. \
                        Please report this to a developer."
                    }.to_string())
                }
            };

            let java_bytes = env.byte_array_from_slice(decrypted_bytes.as_slice()).unwrap();

            let byte_array_input_stream = env.new_object(
                obfstr!("java/io/ByteArrayInputStream"),
                obfstr!("([B)V"),
                &[(&java_bytes).into()]
            ).unwrap();

            Ok(byte_array_input_stream)
        }

        None => Ok(JObject::null())
    };
}