use std::collections::HashMap;
use std::process::exit;
use jni::JNIEnv;
use jni::objects::{JByteArray, JClass, JObject};
use obfstr::obfstr;
use crate::classloader::ClassLoader;
use crate::{classloader, notifs};

pub static mut LOADER_BYTECODE: Option<HashMap<String, Vec<u8>>> = None;

pub unsafe fn decrypt_all_classes<'a>(env: &mut JNIEnv,
                                      caller_class: JClass<'a>)
{
    LOADER_BYTECODE = Some(HashMap::new());

    let class_loader = env.call_method(
        &caller_class,
        obfstr!("getClassLoader"),
        obfstr!("()Ljava/lang/ClassLoader;"),
        &[]
    ).unwrap().l().unwrap();

    decrypt_and_define_class(
        env,
        obfstr!("net/shoreline/loader/a"),
        obfstr!("assets/shoreline/海岸线裂缝预防"),
        &class_loader,
        false
    );

    decrypt_and_define_class(
        env,
        obfstr!("net/shoreline/loader/b"),
        obfstr!("assets/shoreline/海岸线数字版权管理"),
        &class_loader,
        true
    );

    decrypt_and_define_class(
        env,
        obfstr!("net/shoreline/loader/c"),
        obfstr!("assets/shoreline/海岸线反白痴技术"),
        &class_loader,
        false
    );
}

unsafe fn decrypt_and_define_class(env: &mut JNIEnv,
                                   class_name: &str,
                                   class_path: &str,
                                   java_class_loader: &JObject,
                                   cache: bool)
{
    let java_name = env.new_string(class_path).unwrap();

    let input_stream = env.call_method(
        java_class_loader,
        obfstr!("getResourceAsStream"),
        obfstr!("(Ljava/lang/String;)Ljava/io/InputStream;"),
        &[(&java_name).into()]
    ).unwrap().l().unwrap();

    if env.is_same_object(&input_stream, JObject::null()).unwrap()
    {
        let msg = obfstr! {
            "Couldn't read Shoreline's assets from the mod jar. \
            Please report this to a developer."
        }.to_string();

        notifs::error(env, &msg);
        notifs::display_error_msg(&msg);

        exit(-1);
    }

    let available = env.call_method(
        &input_stream,
        obfstr!("available"),
        obfstr!("()I"),
        &[]
    ).unwrap().i().unwrap();

    let mut buffer = vec![0; available as usize];

    let baos = env.new_object(
        obfstr!("java/io/ByteArrayOutputStream"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    let java_buffer = env.new_byte_array(buffer.len() as i32).unwrap();

    loop
    {
        let len = env.call_method(
            &input_stream,
            obfstr!("read"),
            obfstr!("([B)I"),
            &[(&java_buffer).into()]
        ).unwrap().i().unwrap();

        if len == -1
        {
            break;
        }

        env.call_method(
            &baos,
            obfstr!("write"),
            obfstr!("([BII)V"),
            &[(&java_buffer).into(), 0.into(), len.into()]
        ).unwrap().v().unwrap();
    }

    let bytes = env.call_method(
        baos,
        obfstr!("toByteArray"),
        obfstr!("()[B"),
        &[]
    ).unwrap().l().unwrap();
    let bytes = JByteArray::from(bytes);

    let length = env.get_array_length(&bytes).unwrap() as usize;
    let mut buffer = vec![0i8; length];
    env.get_byte_array_region(&bytes, 0, &mut buffer).unwrap();

    let rust_bytes = unsafe { &*(buffer.as_slice() as *const [i8] as *const [u8]) };
    let rust_bytes = Vec::from(rust_bytes);

    classloader::define_class_via_knot(
        env,
        class_name,
        rust_bytes.clone()
    );

    if cache
    {
        LOADER_BYTECODE.as_mut().unwrap().insert(class_name.to_string(), rust_bytes);
    }
}