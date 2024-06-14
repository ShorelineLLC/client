use std::ffi::CStr;
use fltk::app::App;
use fltk::dialog::alert_default;
use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString, JValue};
use obfstr::obfstr;
use reqwest::Client;
use serde_json::{json, Value};
use sha2::{Digest, Sha256};
use tokio::runtime::Runtime;

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
                               jvm_bytes: JObject) -> JClass<'a>
{
    let crash_clazz = env.find_class(
        obfstr!("java/lang/System")
    ).unwrap();

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

    let clazz = env.define_class(
        name.replace(obfstr!(".class"), obfstr!("")),
        context_classloader,
        vec.as_ref()
    );

    if env.exception_check().unwrap()
    {
        env.exception_clear().unwrap();

        error_message(
            obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: dc")
        );

        crash(&env, crash_clazz);

        return crash_clazz;
    }

    return clazz.unwrap();
}

pub unsafe fn is_mixin_class<'a>(env: &JNIEnv<'a>,
                                 jvm_bytes: JObject) -> bool
{
    let class_scanner = env.new_object(
        obfstr!("net/shoreline/loader/f"), // ClassScanner
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
        obfstr!("d"), // descs
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
        obfstr!("net/shoreline/loader/f"), // ClassScanner
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
        obfstr!("a"), // access
        obfstr!("I")
    ).unwrap().i().unwrap();

    let acc_interface = 0x0200;

    return (access & acc_interface) != 0;
}

pub unsafe fn is_imixin_class<'a>(env: &JNIEnv<'a>,
                                  jvm_bytes: JObject) -> bool
{
    let class_scanner = env.new_object(
        obfstr!("net/shoreline/loader/f"), // ClassScanner
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
        obfstr!("d"), // descs
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

        if desc_cstr.eq(obfstr!("Lnet/shoreline/client/im;")) // IMixin
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
        obfstr!("net/shoreline/loader/f"), // ClassScanner
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
        obfstr!("b"), // SuperName
        obfstr!("Ljava/lang/String;")
    ).unwrap().l().unwrap();

    let interfaces = env.get_field(
        class_scanner,
        obfstr!("c"), // Interfaces
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

pub unsafe fn alert_webhook<'a>(env: &JNIEnv<'a>,
                                information_array: JObject<'a>)
{
    let client = Client::new();

    let rt = Runtime::new().unwrap();

    rt.block_on(async {
        alert_webhook_async(env, information_array, &client)
            .await;
    });
}

pub async unsafe fn alert_webhook_async<'a>(env: &JNIEnv<'a>,
                                            information_array: JObject<'a>,
                                            client: &Client)
{
    let content = get_json(env, information_array);

    let response = client
        .post(obfstr!("https://discord.com/api/webhooks/1242060862689247322/C4DKSYjrhVOQkW2R8Q7Bg9Kdtu7M78_Lq1ud1R4A3gN6oUTTUEs8_m5arX9YGnkUOMFd"))
        .header(obfstr!("User-Agent"), obfstr!("shoreline-client"))
        .header(obfstr!("Content-Type"), obfstr!("application/json"))
        .json(&content)
        .send()
        .await;

    match response
    {
        Ok(_) => {}
        Err(_) => {}
    }
}

unsafe fn get_json<'a>(env: &JNIEnv<'a>,
                       param_array: JObject<'a>) -> Value
{
    let msg = env.get_object_array_element(*param_array, 0).unwrap();
    let hwid = env.get_object_array_element(*param_array, 1).unwrap();
    let username = env.get_object_array_element(*param_array, 2).unwrap();
    let mods = env.get_object_array_element(*param_array, 4).unwrap(); // skip index 3, its usertype

    let msg_ptr = env.get_string_utf_chars(JString::from(msg)).unwrap();
    let hwid_ptr = env.get_string_utf_chars(JString::from(hwid)).unwrap();
    let username_ptr = env.get_string_utf_chars(JString::from(username)).unwrap();
    let mods_ptr = env.get_string_utf_chars(JString::from(mods)).unwrap();

    let msg = CStr::from_ptr(msg_ptr).to_str().unwrap();
    let hwid = CStr::from_ptr(hwid_ptr).to_str().unwrap();
    let username = CStr::from_ptr(username_ptr).to_str().unwrap();
    let mods = CStr::from_ptr(mods_ptr).to_str().unwrap();

    let mut content = json!({
        obfstr!("content"): obfstr!("@everyone"),
        obfstr!("username"): obfstr!("Shoreline"),
        obfstr!("avatar_url"): obfstr!("https://api.shorelineclient.net/assets/shoreline.png"),
        obfstr!("tts"): false,
        obfstr!("embeds"): []
    });

    let purple = (106 << 16) | (42 << 8) | 255;

    let embed = json!({
        obfstr!("title"): obfstr!("Loader Alert"),
        obfstr!("color"): purple,
        obfstr!("footer"): {
            obfstr!("text"): obfstr!("\u{00A9} Shoreline"),
            obfstr!("icon_url"): obfstr!("https://api.shorelineclient.net/assets/shoreline.png")
        },
        obfstr!("fields"): [
            {
                obfstr!("name"): obfstr!("Reason"),
                obfstr!("value"): msg,
                obfstr!("inline"): true
            },
            {
                obfstr!("name"): obfstr!("Username"),
                obfstr!("value"): username,
                obfstr!("inline"): true
            },
            {
                obfstr!("name"): obfstr!("HWID"),
                obfstr!("value"): hwid,
                obfstr!("inline"): true
            },
            {
                obfstr!("name"): obfstr!("Mods"),
                obfstr!("value"): mods,
                obfstr!("inline"): true
            }
        ]
    });

    content[obfstr!("embeds")].as_array_mut().unwrap().push(embed);

    return content;
}