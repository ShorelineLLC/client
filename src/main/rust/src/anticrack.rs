use std::process::exit;
use jni::JNIEnv;
use jni::objects::{JClass, JObject, JObjectArray, JString};
use jni::sys::{jint, jlong};
use obfstr::obfstr;

pub unsafe extern "system" fn generate_native_callsite<'a>(mut env: JNIEnv<'a>,
                                                           caller_class: JClass<'a>,
                                                           lookup: JObject<'a>,
                                                           _method_name: JString<'a>,
                                                           _method_type: JObject<'a>,
                                                           key_1: jlong,
                                                           key_2: jlong,
                                                           key_3: jlong,
                                                           access_type: jint) -> JObject<'a>
{
    let key = key_1 ^ key_2 ^ key_3;

    let double_object_array = env.get_static_field(
        &caller_class,
        obfstr!("give up"),
        obfstr!("[[Ljava/lang/Object;"),
    ).unwrap().l().unwrap();
    let double_object_array = JObjectArray::from(double_object_array);

    let target_object_array = env.get_object_array_element(
        double_object_array,
        key as i32
    ).unwrap();
    let target_object_array = JObjectArray::from(target_object_array);

    let target_class_name = env.get_object_array_element(
        &target_object_array,
        0
    ).unwrap();
    let target_class_name = JString::from(target_class_name);
    let target_class_name: String = env.get_string(&target_class_name).unwrap().into();

    let target_class = env.find_class(
        &target_class_name.replace(obfstr!("."), obfstr!("/"))
    ).unwrap();

    let target_member = env.get_object_array_element(
        &target_object_array,
        1
    ).unwrap();
    let target_member = JString::from(target_member);

    let mut method_handle;
    if access_type < 3 // method access
    {
        let method_descriptor = env.get_object_array_element(
            &target_object_array, 2
        ).unwrap();
        let method_descriptor = JString::from(method_descriptor);

        let class_loader = env.call_method(
            &caller_class,
            obfstr!("getClassLoader"),
            obfstr!("()Ljava/lang/ClassLoader;"),
            &[]
        ).unwrap().l().unwrap();

        let method_type = env.call_static_method(
            obfstr!("java/lang/invoke/MethodType"),
            obfstr!("fromMethodDescriptorString"),
            obfstr!("(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;"),
            &[(&method_descriptor).into(), (&class_loader).into()]
        ).unwrap().l().unwrap();
        
        method_handle = match access_type
        {
            0 => {
                env.call_method(
                    lookup,
                    obfstr!("findStatic"),
                    obfstr!("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"),
                    &[(&target_class).into(), (&target_member).into(), (&method_type).into()]
                ).unwrap().l().unwrap()
            }
            1 => {
                env.call_method(
                    lookup,
                    obfstr!("findVirtual"),
                    obfstr!("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"),
                    &[(&target_class).into(), (&target_member).into(), (&method_type).into()]
                ).unwrap().l().unwrap()
            }
            2 => {
                env.call_method(
                    lookup,
                    obfstr!("findConstructor"),
                    obfstr!("(Ljava/lang/Class;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"),
                    &[(&target_class).into(), (&method_type).into()]
                ).unwrap().l().unwrap()
            }
            _ => { // unreachable
                env.throw_new(
                    obfstr!("java/lang/IllegalStateException"),
                    obfstr!("Should never reach here")
                ).unwrap();

                return JObject::null();
            }
        }
    } else // field access
    {
        let owner_class = env.get_object_array_element(
            &target_object_array, 2
        ).unwrap();
        let owner_class = JClass::from(owner_class);

        method_handle = match access_type
        {
            //java. lang. invoke. MethodHandle findGetter(     @NotNull  Class<?> refc,
            //     @NotNull  String name,
            //     @NotNull  Class<?> type )
            3 => {
                env.call_method(
                    lookup,
                    obfstr!("findGetter"),
                    obfstr!("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;"),
                    &[(&target_class).into(), (&target_member).into(), (&owner_class).into()]
                ).unwrap().l().unwrap()
            }
            4 => {
                env.call_method(
                    lookup,
                    obfstr!("findStaticGetter"),
                    obfstr!("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;"),
                    &[(&target_class).into(), (&target_member).into(), (&owner_class).into()]
                ).unwrap().l().unwrap()
            }
            5 => {
                env.call_method(
                    lookup,
                    obfstr!("findSetter"),
                    obfstr!("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;"),
                    &[(&target_class).into(), (&target_member).into(), (&owner_class).into()]
                ).unwrap().l().unwrap()
            }
            6 => {
                env.call_method(
                    lookup,
                    obfstr!("findStaticSetter"),
                    obfstr!("(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;"),
                    &[(&target_class).into(), (&target_member).into(), (&owner_class).into()]
                ).unwrap().l().unwrap()
            }
            _ => { // should be unreachable
                env.throw_new(
                    obfstr!("java/lang/IllegalStateException"),
                    obfstr!("Invalid bootstrap method arguments")
                ).unwrap();

                return JObject::null();
            }
        }
    }

    let method_handle_type = env.call_method(
        &method_handle,
        obfstr!("type"),
        obfstr!("()Ljava/lang/invoke/MethodType;"),
        &[]
    ).unwrap().l().unwrap();

    let transformed_method_handle = env.call_static_method(
        caller_class,
        obfstr!("a"),
        obfstr!("(Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;"),
        &[(&method_handle).into(), (&method_handle_type).into()]
    ).unwrap().l().unwrap();

    return env.new_object(
        obfstr!("java/lang/invoke/ConstantCallSite"),
        obfstr!("(Ljava/lang/invoke/MethodHandle;)V"),
        &[(&transformed_method_handle).into()]
    ).unwrap();
}