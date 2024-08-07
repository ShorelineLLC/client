use jni::JNIEnv;
use jni::objects::{JObjectArray, JString};
use jni::sys::jsize;
use obfstr::obfstr;

pub fn get_immediate_dependants(env: &mut JNIEnv,
                                class_bytes: Vec<u8>) -> Vec<String>
{
    let java_bytes = env.byte_array_from_slice(class_bytes.as_slice()).unwrap();

    let class_node = env.new_object(
        obfstr!("org/objectweb/asm/tree/ClassNode"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    let class_reader = env.new_object(
        obfstr!("org/objectweb/asm/ClassReader"),
        obfstr!("([B)V"),
        &[(&java_bytes).into()]
    ).unwrap();

    env.call_method(
        class_reader,
        obfstr!("accept"),
        obfstr!("(Lorg/objectweb/asm/ClassVisitor;I)V"),
        &[(&class_node).into(), 0.into()]
    ).unwrap();

    let mut dependants = Vec::new();

    let super_name = env.get_field(
        &class_node,
        obfstr!("superName"),
        obfstr!("Ljava/lang/String;")
    ).unwrap().l().unwrap();

    let rust_super_name: String = env.get_string(&JString::from(super_name)).unwrap().into();

    dependants.push(rust_super_name.replace(obfstr!("/"), obfstr!(".")));

    let interfaces = env.get_field(
        &class_node,
        obfstr!("interfaces"),
        obfstr!("Ljava/util/List;")
    ).unwrap().l().unwrap();

    let interfaces_array = env.call_method(
        &interfaces,
        obfstr!("toArray"),
        obfstr!("()[Ljava/lang/Object;"),
        &[]
    ).unwrap().l().unwrap();

    let interfaces_array = JObjectArray::from(interfaces_array);

    let interfaces_len = env.get_array_length(&interfaces_array).unwrap();

    for i in 0..interfaces_len
    {
        let interface_name = env.get_object_array_element(&interfaces_array, i).unwrap();
        let rust_interface_name: String = env.get_string(&JString::from(interface_name)).unwrap().into();
        dependants.push(rust_interface_name.replace(obfstr!("/"), obfstr!(".")));
    }

    return dependants;
}