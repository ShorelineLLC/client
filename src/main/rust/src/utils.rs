use std::ffi::CStr;
use fltk::app::App;
use fltk::dialog::alert_default;
use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString, JValue};
use jni::signature::JavaType;
use jni::signature::Primitive::Void;
use jni::strings::JNIString;
use jni::sys::JNI_TRUE;
use obfstr::obfstr;
use serde_json::json;
use sha2::{Digest, Sha256};

pub unsafe fn crash<'a>(env: &JNIEnv<'a>,
                        class_to_rape: JClass)
{
    let the_unsafe = env.get_static_field(
        env.find_class(obfstr!("sun/misc/Unsafe")).unwrap(),
        obfstr!("theUnsafe"),
        obfstr!("Lsun/misc/Unsafe;")
    ).unwrap().l().unwrap();

    let jvm_obj_ptr = env.call_method(
        class_to_rape,
        obfstr!("hashCode"),
        obfstr!("()I"),
        &[]
    ).unwrap().i().unwrap();

    env.call_method(
        the_unsafe,
        obfstr!("freeMemory"),
        obfstr!("(J)V"),
        &[jvm_obj_ptr.into()]
    ).unwrap().v().unwrap();

    // Shouldn't ever reach beyond this point

    env.throw_new(
        obfstr!("java/lang/Throwable"),
        obfstr!("")
    ).unwrap();

    std::process::exit(0);
}

pub fn error_message(msg: &str)
{
    let app = App::default();

    alert_default(msg);

    app.run().unwrap();
}

pub unsafe fn define_class<'a>(env: &JNIEnv<'a>,
                               name: &str,
                               jvm_bytes: JObject)
{
    let bytes = env.get_byte_array_elements(
        jvm_bytes.into_inner(),
    ).unwrap().0;

    let bytes_len = env.get_array_length(
        jvm_bytes.into_inner()
    ).unwrap() as isize;

    let mut vec = Vec::with_capacity(bytes_len as usize);

    for i in 0..bytes_len
    {
        vec.push(*bytes.offset(i) as u8);
    }

    let current_thread = env.call_static_method(
        obfstr!("java/lang/Thread"),
        obfstr!("currentThread"),
        obfstr!("()Ljava/lang/Thread;"),
        &[]
    ).unwrap().l().unwrap();

    let context_classloader = env.call_method(
        current_thread,
        obfstr!("getContextClassLoader"),
        obfstr!("()Ljava/lang/ClassLoader;"),
        &[]
    ).unwrap().l().unwrap();

    env.define_class(
        name.replace(obfstr!(".class"), obfstr!("")),
        context_classloader,
        vec.as_ref()
    ).unwrap();
}

pub unsafe fn add_to_resource_path<'a>(env: &JNIEnv<'a>,
                                       resource_name: &str,
                                       jvm_bytes: JObject)
{
    let current_thread = env.call_static_method(
        obfstr!("java/lang/Thread"),
        obfstr!("currentThread"),
        obfstr!("()Ljava/lang/Thread;"),
        &[]
    ).unwrap().l().unwrap();

    let context_classloader = env.call_method(
        current_thread,
        obfstr!("getContextClassLoader"),
        obfstr!("()Ljava/lang/ClassLoader;"),
        &[]
    ).unwrap().l().unwrap();

    println!("trying to get urlclassloader");
    let url_classloader = env.get_field(
        context_classloader,
        obfstr!("urlLoader"),
        obfstr!("net/fabricmc/loader/impl/launch/knot/KnotClassLoader$DynamicURLClassLoader")
    ).unwrap().l().unwrap();
    println!("got urlclassloader");


}

pub unsafe fn is_mixin_class<'a>(env: &JNIEnv<'a>,
                                 jvm_bytes: JObject) -> bool
{
    let class_scanner = env.new_object(
        obfstr!("net/shoreline/loader/asm/ClassScanner"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    let class_reader = env.new_object(
        obfstr!("org/objectweb/asm/ClassReader"),
        obfstr!("([B)V"),
        &[jvm_bytes.into()]
    ).unwrap();

    env.call_method(
        class_reader,
        obfstr!("accept"),
        obfstr!("(Lorg/objectweb/asm/ClassVisitor;I)V"),
        &[class_scanner.into(), JValue::Int(0)]
    ).unwrap().v().unwrap();

    let descs = env.get_field(
        class_scanner,
        obfstr!("descs"),
        obfstr!("Ljava/util/List;")
    ).unwrap().l().unwrap();

    let iterator = env.call_method(
        descs,
        obfstr!("iterator"),
        obfstr!("()Ljava/util/Iterator;"),
        &[]
    ).unwrap().l().unwrap();

    while env.call_method(
        iterator,
        obfstr!("hasNext"),
        obfstr!("()Z"),
        &[]
    ).unwrap().z().unwrap()
    {
        let desc = env.call_method(
            iterator,
            obfstr!("next"),
            obfstr!("()Ljava/lang/Object;"),
            &[]
        ).unwrap().l().unwrap();

        let desc_ptr = env.get_string_utf_chars(JString::from(desc)).unwrap();
        let desc_cstr = CStr::from_ptr(desc_ptr).to_str().unwrap();

        if desc_cstr.eq(obfstr!("Lorg/spongepowered/asm/mixin/Mixin;"))
        {
            return true;
        }
    }

    return false;
}

pub unsafe fn is_mixin_accessor<'a>(env: &JNIEnv<'a>,
                                    jvm_bytes: JObject) -> bool
{
    let class_scanner = env.new_object(
        obfstr!("net/shoreline/loader/asm/ClassScanner"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    let class_reader = env.new_object(
        obfstr!("org/objectweb/asm/ClassReader"),
        obfstr!("([B)V"),
        &[jvm_bytes.into()]
    ).unwrap();

    env.call_method(
        class_reader,
        obfstr!("accept"),
        obfstr!("(Lorg/objectweb/asm/ClassVisitor;I)V"),
        &[class_scanner.into(), JValue::Int(0)]
    ).unwrap().v().unwrap();

    let access = env.get_field(
        class_scanner,
        obfstr!("access"),
        obfstr!("I")
    ).unwrap().i().unwrap();

    let acc_interface = 0x0200;

    return (access & acc_interface) != 0;
}

pub unsafe fn is_imixin_class<'a>(env: &JNIEnv<'a>,
                                  jvm_bytes: JObject) -> bool
{
    let class_scanner = env.new_object(
        obfstr!("net/shoreline/loader/asm/ClassScanner"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    let class_reader = env.new_object(
        obfstr!("org/objectweb/asm/ClassReader"),
        obfstr!("([B)V"),
        &[jvm_bytes.into()]
    ).unwrap();

    env.call_method(
        class_reader,
        obfstr!("accept"),
        obfstr!("(Lorg/objectweb/asm/ClassVisitor;I)V"),
        &[class_scanner.into(), JValue::Int(0)]
    ).unwrap().v().unwrap();

    let descs = env.get_field(
        class_scanner,
        obfstr!("descs"),
        obfstr!("Ljava/util/List;")
    ).unwrap().l().unwrap();

    let iterator = env.call_method(
        descs,
        obfstr!("iterator"),
        obfstr!("()Ljava/util/Iterator;"),
        &[]
    ).unwrap().l().unwrap();

    while env.call_method(
        iterator,
        obfstr!("hasNext"),
        obfstr!("()Z"),
        &[]
    ).unwrap().z().unwrap()
    {
        let desc = env.call_method(
            iterator,
            obfstr!("next"),
            obfstr!("()Ljava/lang/Object;"),
            &[]
        ).unwrap().l().unwrap();

        let desc_ptr = env.get_string_utf_chars(JString::from(desc)).unwrap();
        let desc_cstr = CStr::from_ptr(desc_ptr).to_str().unwrap();

        if desc_cstr.eq(obfstr!("Lnet/shoreline/client/impl/imixin/IMixin;"))
        {
            return true;
        }
    }

    return false;
}

pub fn get_immediate_dependents<'a>(env: &JNIEnv<'a>,
                                    jvm_bytes: JObject) -> Vec<String>
{
    let class_scanner = env.new_object(
        obfstr!("net/shoreline/loader/asm/ClassScanner"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    let class_reader = env.new_object(
        obfstr!("org/objectweb/asm/ClassReader"),
        obfstr!("([B)V"),
        &[jvm_bytes.into()]
    ).unwrap();

    env.call_method(
        class_reader,
        obfstr!("accept"),
        obfstr!("(Lorg/objectweb/asm/ClassVisitor;I)V"),
        &[class_scanner.into(), JValue::Int(0)]
    ).unwrap().v().unwrap();

    let super_name = env.get_field(
        class_scanner,
        obfstr!("superName"),
        obfstr!("Ljava/lang/String;")
    ).unwrap().l().unwrap();

    let interfaces = env.get_field(
        class_scanner,
        obfstr!("interfaces"),
        obfstr!("[Ljava/lang/String;")
    ).unwrap().l().unwrap();


    let mut dependents: Vec<String> = Vec::new();

    if !super_name.is_null()
    {
        let super_name = env.get_string(super_name.into()).unwrap().to_str().unwrap().to_string();
        let super_name_with_class = format!("{}{}", super_name, obfstr!(".class"));
        dependents.push(super_name_with_class);
    }

    let interfaces_len = env.get_array_length(*interfaces).unwrap();
    for i in 0..interfaces_len
    {
        let interface_obj = env.get_object_array_element(*interfaces, i).unwrap();
        let interface_name = env.get_string(interface_obj.into()).unwrap().to_str().unwrap().to_string();
        let interface_name_with_class = format!("{}{}", interface_name, obfstr!(".class"));
        dependents.push(interface_name_with_class);
    }

    return dependents;
}

pub fn encrypt(str: &str) -> String
{
    let combined_key = format!(
        "{}{}",
        str,
        obfstr!("VJ146naKEtYcwlmxmVwjS9tFEIeFnD6H")); // Secret key to hash our strings.
                                                      // DON'T LOSE THIS OR ALL HWIDS BECOME CORRUPTED!!!

    let mut hasher = Sha256::new();
    Digest::update(&mut hasher, combined_key);

    hex::encode(hasher.finalize())
}

pub fn alert_webhook(env: &JNIEnv,
                     msg: &str,
                     hwid: &str,
                     username: &str,
                     mods: &str)
{
    let mut content = json!({
        "content": "@everyone",
        "username": "Shoreline",
        "avatar_url": "https://api.shorelineclient.net/assets/shoreline.png",
        "tts": false,
        "embeds": []
    });

    let purple = (106 << 16) | (42 << 8) | 255;

    let embed = json!({
        "title": "Loader Alert",
        "color": purple,
        "footer": {
            "text": "\u{00A9} Shoreline",
            "icon_url": "https://api.shorelineclient.net/assets/shoreline.png"
        },
        "fields": [
            {
                "name": "Reason",
                "value": msg,
                "inline": true
            },
            {
                "name": "Username",
                "value": username,
                "inline": true
            },
            {
                "name": "HWID",
                "value": hwid,
                "inline": true
            },
            {
                "name": "Mods",
                "value": mods,
                "inline": true
            }
        ]
    });

    content["embeds"].as_array_mut().unwrap().push(embed);

    let url_string = JNIString::from(
        obfstr!("https://discord.com/api/webhooks/1242060862689247322/C4DKSYjrhVOQkW2R8Q7Bg9Kdtu7M78_Lq1ud1R4A3gN6oUTTUEs8_m5arX9YGnkUOMFd")
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

    let content_type = env.new_string(obfstr!("Content-Type")).unwrap();
    let content_type_value = env.new_string(obfstr!("application/json")).unwrap();

    env.call_method(
        url_connection,
        obfstr!("addRequestProperty"),
        obfstr!("(Ljava/lang/String;Ljava/lang/String;)V"),
        &[content_type.into(), content_type_value.into()]
    ).unwrap().v().unwrap();

    env.call_method(
        url_connection,
        obfstr!("setDoOutput"),
        obfstr!("(Z)V"),
        &[JNI_TRUE.into()]
    ).unwrap().v().unwrap();

    env.call_method(
        url_connection,
        obfstr!("setDoInput"),
        obfstr!("(Z)V"),
        &[JNI_TRUE.into()]
    ).unwrap().v().unwrap();

    let set_request_method = env.get_method_id(
        obfstr!("java/net/HttpURLConnection"),
        obfstr!("setRequestMethod"),
        obfstr!("(Ljava/lang/String;)V")
    ).unwrap();

    env.call_method_unchecked(
        url_connection,
        set_request_method,
        JavaType::Primitive(Void),
        &[env.new_string(obfstr!("POST")).unwrap().into()]
    ).unwrap().v().unwrap();

    let output_stream = env.call_method(
        url_connection,
        obfstr!("getOutputStream"),
        obfstr!("()Ljava/io/OutputStream;"),
        &[]
    ).unwrap().l().unwrap();

    let content_string = content.to_string();

    let message_bytes = content_string.as_bytes();

    let java_byte_array = env.byte_array_from_slice(message_bytes).unwrap();

    env.call_method(
        output_stream,
        obfstr!("write"),
        obfstr!("([B)V"),
        &[JValue::from(java_byte_array)]
    ).unwrap().v().unwrap();

    env.call_method(
        output_stream,
        obfstr!("flush"),
        obfstr!("()V"),
        &[]
    ).unwrap().v().unwrap();

    env.call_method(
        output_stream,
        obfstr!("close"),
        obfstr!("()V"),
        &[]
    ).unwrap().v().unwrap();

    let input_stream: Option<JValue> = env.call_method(
        url_connection,
        obfstr!("getInputStream"),
        obfstr!("()Ljava/io/InputStream;"),
        &[]
    ).ok();

    if input_stream.is_some() {
        env.call_method(
            input_stream.unwrap().l().unwrap(),
            obfstr!("close"),
            obfstr!("()V"),
            &[]
        ).unwrap().v().unwrap();
    }

    let disconnect = env.get_method_id(
        obfstr!("java/net/HttpURLConnection"),
        obfstr!("disconnect"),
        obfstr!("()V")
    ).unwrap();

    env.call_method_unchecked(
        url_connection,
        disconnect,
        JavaType::Primitive(Void),
        &[]
    ).unwrap().v().unwrap();
}