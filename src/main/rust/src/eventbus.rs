use jni::objects::{GlobalRef, JClass, JObject, JObjectArray};
use jni::JNIEnv;
use jni::sys::JNI_FALSE;
use obfstr::obfstr;
use crate::{notifs, obfuscation};

static mut INVOKER_CACHE: Option<GlobalRef> = None;

pub unsafe fn init_internal(env: &mut JNIEnv)
{
    let concurrent_hash_map = env.new_object(
        obfstr!("java/util/concurrent/ConcurrentHashMap"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    INVOKER_CACHE = Some(
        env.new_global_ref(concurrent_hash_map).unwrap()
    );

    let event_bus_class = obfuscation::get_event_bus_class(env);

    let event_bus_instance = env.get_static_field(
        event_bus_class,
        obfuscation::EVENT_BUS_INSTANCE_FIELD.get_name(),
        obfuscation::EVENT_BUS_INSTANCE_FIELD.get_desc()
    ).unwrap().l().unwrap();

    let concurrent_hash_map = env.new_object(
        obfstr!("java/util/concurrent/ConcurrentHashMap"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    env.set_field(
        event_bus_instance,
        obfuscation::EVENT_BUS_INVOKER_MAP.get_name(),
        obfuscation::EVENT_BUS_INVOKER_MAP.get_desc(),
        (&concurrent_hash_map).into()
    ).unwrap();

    if obfuscation::IS_DEVELOPMENT_ENVIRONMENT
    {
        env.call_static_method(
            obfstr!("net/shoreline/eventbus/dev/DevEventBusLoader"),
            obfstr!("load"),
            obfstr!("()V"),
            &[]
        ).unwrap().v().unwrap();
    }
}

static mut LOOKUP: Option<GlobalRef> = None;

pub unsafe extern "system" fn subscribe<'a>(mut env: JNIEnv,
                                            caller_instance: JObject,
                                            subscriber: JObject<'a>) -> JObject<'a>
{
    if env.is_same_object(&subscriber, &JObject::null()).unwrap()
    {
        return subscriber;
    }

    if LOOKUP.is_none()
    {
        let lookup = env.call_static_method(
            obfstr!("java/lang/invoke/MethodHandles"),
            obfstr!("lookup"),
            obfstr!("()Ljava/lang/invoke/MethodHandles$Lookup;"),
            &[]
        ).unwrap().l().unwrap();

        LOOKUP = Some(
            env.new_global_ref(lookup).unwrap()
        );
    }

    match LOOKUP.as_ref()
    {
        Some(lookup) => {
            let subscriber_class = env.get_object_class(&subscriber).unwrap();

            let methods = env.call_method(
                &subscriber_class,
                obfstr!("getDeclaredMethods"),
                obfstr!("()[Ljava/lang/reflect/Method;"),
                &[],
            ).unwrap().l().unwrap();

            let methods = JObjectArray::from(methods);

            let length = env.get_array_length(&methods).unwrap();

            for i in 0..length
            {
                let method_obj = env.get_object_array_element(&methods, i).unwrap();

                env.call_method(
                    &method_obj,
                    obfstr!("trySetAccessible"),
                    obfstr!("()Z"),
                    &[],
                ).unwrap().z().unwrap();

                let event_listener_class = obfuscation::get_event_listener_class(&mut env);

                if env.call_method(
                    &method_obj,
                    obfstr!("isAnnotationPresent"),
                    obfstr!("(Ljava/lang/Class;)Z"),
                    &[(&event_listener_class).into()],
                ).unwrap().z().unwrap()
                {
                    let declared_annotation = env.call_method(
                        &method_obj,
                        obfstr!("getDeclaredAnnotation"),
                        obfstr!("(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;"),
                        &[(&event_listener_class).into()],
                    ).unwrap().l().unwrap();

                    let priority_int = env.call_method(
                        &declared_annotation,
                        obfuscation::EVENT_LISTENER_PRIORITY_METHOD.get_name(),
                        obfstr!("()I"),
                        &[],
                    ).unwrap().i().unwrap();

                    let parameters = env.call_method(
                        &method_obj,
                        obfstr!("getParameterTypes"),
                        obfstr!("()[Ljava/lang/Class;"),
                        &[],
                    ).unwrap().l().unwrap();

                    let parameters = JObjectArray::from(parameters);

                    let event_type = env.get_object_array_element(&parameters, 0).unwrap();

                    let invoker_cache = INVOKER_CACHE.as_mut().unwrap().as_obj();

                    let invoker_obj = if env.call_method(
                        invoker_cache,
                        obfstr!("contains"),
                        obfstr!("(Ljava/lang/Object;)Z"),
                        &[(&method_obj).into()]
                    ).unwrap().z().unwrap()
                    {
                        env.call_method(
                            invoker_cache,
                            obfstr!("get"),
                            obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                            &[(&method_obj).into()]
                        ).unwrap().l().unwrap()
                    } else
                    {
                        let event_invoker_class = obfuscation::get_event_invoker_class(&mut env);

                        let method_type = env.call_static_method(
                            obfstr!("java/lang/invoke/MethodType"),
                            obfstr!("methodType"),
                            obfstr!("(Ljava/lang/Class;)Ljava/lang/invoke/MethodType;"),
                            &[(&event_invoker_class).into()]
                        ).unwrap().l().unwrap();

                        let subscriber_class_array = env.new_object_array(
                            1,
                            obfstr!("java/lang/Class"),
                            &subscriber_class
                        ).unwrap();

                        let appended_parameter_types = env.call_method(
                            &method_type,
                            obfstr!("appendParameterTypes"),
                            obfstr!("([Ljava/lang/Class;)Ljava/lang/invoke/MethodType;"),
                            &[(&subscriber_class_array).into()]
                        ).unwrap().l().unwrap();

                        let void_class = env.get_static_field(
                            obfstr!("java/lang/Void"),
                            obfstr!("TYPE"),
                            obfstr!("Ljava/lang/Class;")
                        ).unwrap().l().unwrap();

                        let object_class = env.find_class(obfstr!("java/lang/Object")).unwrap();

                        let void_obj_method_type = env.call_static_method(
                            obfstr!("java/lang/invoke/MethodType"),
                            obfstr!("methodType"),
                            obfstr!("(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/invoke/MethodType;"),
                            &[
                                (&void_class).into(),
                                (&object_class).into()
                            ]
                        ).unwrap().l().unwrap();

                        let unreflect = env.call_method(
                            lookup,
                            obfstr!("unreflect"),
                            obfstr!("(Ljava/lang/reflect/Method;)Ljava/lang/invoke/MethodHandle;"),
                            &[(&method_obj).into()]
                        ).unwrap().l().unwrap();

                        let dynamic_method_type = env.call_static_method(
                            obfstr!("java/lang/invoke/MethodType"),
                            obfstr!("methodType"),
                            obfstr!("(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/invoke/MethodType;"),
                            &[
                                (&void_class).into(),
                                (&event_type).into()
                            ]
                        ).unwrap().l().unwrap();

                        let invoke_method_name= env.new_string(
                            obfuscation::INVOKER_INVOKE_METHOD.get_name()
                        ).unwrap();

                        let call_site = env.call_static_method(
                            obfstr!("java/lang/invoke/LambdaMetafactory"),
                            obfstr!("metafactory"),
                            obfstr!(
                            "(Ljava/lang/invoke/MethodHandles$Lookup;\
                            Ljava/lang/String;\
                            Ljava/lang/invoke/MethodType;\
                            Ljava/lang/invoke/MethodType;\
                            Ljava/lang/invoke/MethodHandle;\
                            Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;"
                        ),
                            &[
                                lookup.into(),
                                (&invoke_method_name).into(),
                                (&appended_parameter_types).into(),
                                (&void_obj_method_type).into(),
                                (&unreflect).into(),
                                (&dynamic_method_type).into()
                            ]
                        ).unwrap().l().unwrap();

                        let method_handle = env.call_method(
                            &call_site,
                            obfstr!("getTarget"),
                            obfstr!("()Ljava/lang/invoke/MethodHandle;"),
                            &[]
                        ).unwrap().l().unwrap();

                        let subscriber_arg_array = env.new_object_array(
                            1,
                            obfstr!("java/lang/Object"),
                            &subscriber
                        ).unwrap();

                        let invoker_impl = env.call_method(
                            &method_handle,
                            obfstr!("invokeWithArguments"),
                            obfstr!("([Ljava/lang/Object;)Ljava/lang/Object;"),
                            &[(&subscriber_arg_array).into()]
                        ).unwrap().l().unwrap();

                        env.call_method(
                            &invoker_cache,
                            obfstr!("put"),
                            obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                            &[(&method_obj).into(), (&invoker_impl).into()]
                        ).unwrap().l().unwrap();

                        invoker_impl
                    };

                    let priority = env.call_static_method(
                        obfstr!("java/lang/Integer"),
                        obfstr!("valueOf"),
                        obfstr!("(I)Ljava/lang/Integer;"),
                        &[priority_int.into()]
                    ).unwrap().l().unwrap();

                    let invoker_node_class = obfuscation::get_invoker_node_class(&mut env);

                    let invoker = env.new_object(
                        &invoker_node_class,
                        obfstr!("(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
                        &[(&invoker_obj).into(), (&subscriber).into(), (&priority).into()]
                    ).unwrap();

                    let event_map = env.get_field(
                        &caller_instance,
                        obfuscation::EVENT_BUS_INVOKER_MAP.get_name(),
                        obfuscation::EVENT_BUS_INVOKER_MAP.get_desc()
                    ).unwrap().l().unwrap();

                    let mut prev = env.call_method(
                        &event_map,
                        obfstr!("get"),
                        obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                        &[(&event_type).into()]
                    ).unwrap().l().unwrap();

                    let mut current = env.get_field(
                        &prev,
                        obfuscation::INVOKER_NODE_NEXT_FIELD.get_name(),
                        obfuscation::INVOKER_NODE_NEXT_FIELD.get_desc()
                    ).unwrap().l().unwrap();

                    while !env.is_same_object(&current, JObject::null()).unwrap()
                    {
                        let current_priority = env.get_field(
                            &current,
                            obfuscation::INVOKER_NODE_PRIORITY_FIELD.get_name(),
                            obfuscation::INVOKER_NODE_PRIORITY_FIELD.get_desc()
                        ).unwrap().l().unwrap();

                        let current_priority_int = env.call_method(
                            &current_priority,
                            obfstr!("intValue"),
                            obfstr!("()I"),
                            &[]
                        ).unwrap().i().unwrap();

                        if priority_int > current_priority_int
                        {
                            break;
                        }

                        let current_next = env.get_field(
                            &current,
                            obfuscation::INVOKER_NODE_NEXT_FIELD.get_name(),
                            obfuscation::INVOKER_NODE_NEXT_FIELD.get_desc()
                        ).unwrap().l().unwrap();

                        prev = current;

                        current = current_next;
                    }

                    env.set_field(
                        &prev,
                        obfuscation::INVOKER_NODE_NEXT_FIELD.get_name(),
                        obfuscation::INVOKER_NODE_NEXT_FIELD.get_desc(),
                        (&invoker).into()
                    ).unwrap();

                    env.set_field(
                        &invoker,
                        obfuscation::INVOKER_NODE_NEXT_FIELD.get_name(),
                        obfuscation::INVOKER_NODE_NEXT_FIELD.get_desc(),
                        (&current).into()
                    ).unwrap();
                }
            }
        }
        None => panic!("unable to complete native method a")
    }

    return JObject::null()
}

pub unsafe extern "system" fn unsubscribe<'a>(mut env: JNIEnv,
                                              caller_instance: JObject,
                                              subscriber: JObject<'a>) -> JObject<'a>
{
    if env.is_same_object(&subscriber, &JObject::null()).unwrap()
    {
        return subscriber;
    }

    let event_map = env.get_field(
        &caller_instance,
        obfuscation::EVENT_BUS_INVOKER_MAP.get_name(),
        obfuscation::EVENT_BUS_INVOKER_MAP.get_desc()
    ).unwrap().l().unwrap();

    let entry_set = env.call_method(
        &event_map,
        obfstr!("entrySet"),
        obfstr!("()Ljava/util/Set;"),
        &[]
    ).unwrap().l().unwrap();

    let iterator = env.call_method(
        &entry_set,
        obfstr!("iterator"),
        obfstr!("()Ljava/util/Iterator;"),
        &[]
    ).unwrap().l().unwrap();

    while env.call_method(
        &iterator,
        obfstr!("hasNext"),
        obfstr!("()Z"),
        &[]
    ).unwrap().z().unwrap()
    {
        let entry = env.call_method(
            &iterator,
            obfstr!("next"),
            obfstr!("()Ljava/lang/Object;"),
            &[]
        ).unwrap().l().unwrap();

        let mut prev = env.call_method(
            &entry,
            "getValue",
            "()Ljava/lang/Object;",
            &[]
        ).unwrap().l().unwrap();

        let mut tmp = env.get_field(
            &prev,
            obfuscation::INVOKER_NODE_NEXT_FIELD.get_name(),
            obfuscation::INVOKER_NODE_NEXT_FIELD.get_desc()
        ).unwrap().l().unwrap();

        while !env.is_same_object(&tmp, JObject::null()).unwrap()
        {
            let tmp_instance = env.get_field(
                &tmp,
                obfuscation::INVOKER_NODE_SUBSCRIBER_FIELD.get_name(),
                obfstr!("Ljava/lang/Object;")
            ).unwrap().l().unwrap();

            let tmp_next = env.get_field(
                &tmp,
                obfuscation::INVOKER_NODE_NEXT_FIELD.get_name(),
                obfuscation::INVOKER_NODE_NEXT_FIELD.get_desc()
            ).unwrap().l().unwrap();

            if env.is_same_object(&tmp_instance, &subscriber).unwrap()
            {
                let tmp_next = env.get_field(
                    &tmp,
                    obfuscation::INVOKER_NODE_NEXT_FIELD.get_name(),
                    obfuscation::INVOKER_NODE_NEXT_FIELD.get_desc()
                ).unwrap().l().unwrap();

                env.set_field(
                    &prev,
                    obfuscation::INVOKER_NODE_NEXT_FIELD.get_name(),
                    obfuscation::INVOKER_NODE_NEXT_FIELD.get_desc(),
                    (&tmp_next).into()
                ).unwrap();
            } else
            {
                prev = tmp;
            }

            tmp = tmp_next;
        }
    }

    return JObject::null();
}

pub unsafe fn cache_event_class(env: &mut JNIEnv,
                                event_clazz: JClass)
{
    let event_bus_class = obfuscation::get_event_bus_class(env);

    let event_bus_instance = env.get_static_field(
        event_bus_class,
        obfuscation::EVENT_BUS_INSTANCE_FIELD.get_name(),
        obfuscation::EVENT_BUS_INSTANCE_FIELD.get_desc()
    ).unwrap().l().unwrap();

    let event_map = env.get_field(
        &event_bus_instance,
        obfuscation::EVENT_BUS_INVOKER_MAP.get_name(),
        obfuscation::EVENT_BUS_INVOKER_MAP.get_desc()
    ).unwrap().l().unwrap();

    let invoker_node_class = obfuscation::get_invoker_node_class(env);

    let new_null_head_invoker = env.new_object(
        &invoker_node_class,
        obfstr!("(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
        &[(&JObject::null()).into(), (&JObject::null()).into(), (&JObject::null()).into()]
    ).unwrap();

    env.call_method(
        event_map,
        obfstr!("put"),
        obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
        &[(&event_clazz).into(), (&new_null_head_invoker).into()]
    ).unwrap().l().unwrap();
}