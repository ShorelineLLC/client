use std::ffi::CStr;
use jni::JNIEnv;
use jni::objects::{JObject, JString, JValue};
use obfstr::obfstr;

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
        let super_name_with_class = format!("{}.class", super_name);
        dependents.push(super_name_with_class);
    }

    let interfaces_len = env.get_array_length(*interfaces).unwrap();
    for i in 0..interfaces_len
    {
        let interface_obj = env.get_object_array_element(*interfaces, i).unwrap();
        let interface_name = env.get_string(interface_obj.into()).unwrap().to_str().unwrap().to_string();
        let interface_name_with_class = format!("{}.class", interface_name);
        dependents.push(interface_name_with_class);
    }

    return dependents;
}