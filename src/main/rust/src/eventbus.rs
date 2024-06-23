use jni::JNIEnv;
use jni::objects::{GlobalRef, JClass, JObject};
use jni::sys::{JNI_FALSE};
use obfstr::obfstr;
use crate::{IS_OBFUSCATED_ENVIRONMENT};

static mut INVOKER_CACHE: Option<GlobalRef> = None;

pub unsafe fn init_internal(env: &JNIEnv)
{
    let concurrent_hash_map = env.new_object(
        obfstr!("java/util/concurrent/ConcurrentHashMap"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    INVOKER_CACHE = Some(
        env.new_global_ref(concurrent_hash_map).unwrap()
    );

    let event_bus_instance;
    if IS_OBFUSCATED_ENVIRONMENT
    {
        event_bus_instance = env.get_static_field(
            env.find_class(obfstr!("net/shoreline/loader/e")).unwrap(),
            obfstr!("a"),
            obfstr!("Lnet/shoreline/loader/e;")
        ).unwrap().l().unwrap();
    } else
    {
        event_bus_instance = env.get_static_field(
            env.find_class(obfstr!("net/shoreline/eventbus/EventBus")).unwrap(),
            obfstr!("INSTANCE"),
            obfstr!("Lnet/shoreline/eventbus/EventBus;")
        ).unwrap().l().unwrap();
    }

    let concurrent_hash_map = env.new_object(
        obfstr!("java/util/concurrent/ConcurrentHashMap"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    if IS_OBFUSCATED_ENVIRONMENT
    {
        env.set_field(
            event_bus_instance,
            obfstr!("a"),
            obfstr!("Ljava/lang/Object;"),
            concurrent_hash_map.into()
        ).unwrap();
    } else
    {
        env.set_field(
            event_bus_instance,
            obfstr!("event2InvokerMap"),
            obfstr!("Ljava/lang/Object;"),
            concurrent_hash_map.into()
        ).unwrap();
    };

    if is_in_dev_environment(&env)
    {
        env.call_static_method(
            env.find_class(obfstr!("net/shoreline/eventbus/dev/DevEventBusLoader")).unwrap(),
            obfstr!("load"),
            obfstr!("()V"),
            &[]
        ).unwrap().v().unwrap();
    }
}

static mut LOOKUP: Option<GlobalRef> = None;

#[export_name = "Java_net_shoreline_loader_e_a"]
pub unsafe extern "system" fn a(env: JNIEnv,
                                caller_instance: JObject,
                                subscriber: JObject)
{
    subscribe(env, caller_instance, subscriber);
}

#[export_name = "Java_net_shoreline_loader_e_b"]
pub unsafe extern "system" fn b(env: JNIEnv,
                                caller_instance: JObject,
                                subscriber: JObject)
{
    unsubscribe(env, caller_instance, subscriber);
}

#[export_name = "Java_net_shoreline_eventbus_EventBus_subscribe"]
pub unsafe extern "system" fn subscribe(env: JNIEnv,
                                        caller_instance: JObject,
                                        subscriber: JObject)
{
    if LOOKUP.is_none()
    {
        let lookup = env.call_static_method(
            env.find_class(obfstr!("java/lang/invoke/MethodHandles")).unwrap(),
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
            let subscriber_class = env.get_object_class(subscriber).unwrap();

            let methods = env.call_method(
                subscriber_class,
                obfstr!("getDeclaredMethods0"),
                obfstr!("(Z)[Ljava/lang/reflect/Method;"),
                &[JNI_FALSE.into()],
            ).unwrap().l().unwrap();

            let length = env.get_array_length(*methods).unwrap();

            for i in 0..length
            {
                let method_obj = env.get_object_array_element(*methods, i).unwrap();

                env.call_method(
                    method_obj,
                    obfstr!("trySetAccessible"),
                    obfstr!("()Z"),
                    &[],
                ).unwrap().z().unwrap();

                let event_listener;
                if IS_OBFUSCATED_ENVIRONMENT
                {
                    event_listener = env.find_class(
                        obfstr!("net/shoreline/client/el"),
                    ).unwrap();
                } else
                {
                    event_listener = env.find_class(
                        obfstr!("net/shoreline/eventbus/annotation/EventListener"),
                    ).unwrap();
                };

                if env.call_method(
                    method_obj,
                    obfstr!("isAnnotationPresent"),
                    obfstr!("(Ljava/lang/Class;)Z"),
                    &[event_listener.into()],
                ).unwrap().z().unwrap()
                {
                    let declared_annotation = env.call_method(
                        method_obj,
                        obfstr!("getDeclaredAnnotation"),
                        obfstr!("(Ljava/lang/Class;)Ljava/lang/annotation/Annotation;"),
                        &[event_listener.into()],
                    ).unwrap().l().unwrap();

                    let priority_int;
                    if IS_OBFUSCATED_ENVIRONMENT
                    {
                        priority_int = env.call_method(
                            declared_annotation,
                            obfstr!("a"),
                            obfstr!("()I"),
                            &[],
                        ).unwrap().i().unwrap();
                    } else
                    {
                        priority_int = env.call_method(
                            declared_annotation,
                            obfstr!("priority"),
                            obfstr!("()I"),
                            &[],
                        ).unwrap().i().unwrap();
                    };

                    let parameters = env.call_method(
                        method_obj,
                        obfstr!("getParameterTypes"),
                        obfstr!("()[Ljava/lang/Class;"),
                        &[],
                    ).unwrap().l().unwrap();

                    let event_type = env.get_object_array_element(*parameters, 0).unwrap();

                    let invoker_cache = INVOKER_CACHE.as_mut().unwrap().as_obj();

                    let invoker_obj = if env.call_method(
                        invoker_cache,
                        obfstr!("contains"),
                        obfstr!("(Ljava/lang/Object;)Z"),
                        &[method_obj.into()]
                    ).unwrap().z().unwrap()
                    {
                        env.call_method(
                            invoker_cache,
                            obfstr!("get"),
                            obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                            &[method_obj.into()]
                        ).unwrap().l().unwrap()
                    } else
                    {
                        let method_type;
                        if IS_OBFUSCATED_ENVIRONMENT
                        {
                            method_type = env.call_static_method(
                                env.find_class(obfstr!("java/lang/invoke/MethodType")).unwrap(),
                                obfstr!("methodType"),
                                obfstr!("(Ljava/lang/Class;)Ljava/lang/invoke/MethodType;"),
                                &[env.find_class(obfstr!("net/shoreline/loader/i")).unwrap().into()]
                            ).unwrap().l().unwrap();
                        } else
                        {
                            method_type = env.call_static_method(
                                env.find_class(obfstr!("java/lang/invoke/MethodType")).unwrap(),
                                obfstr!("methodType"),
                                obfstr!("(Ljava/lang/Class;)Ljava/lang/invoke/MethodType;"),
                                &[env.find_class(obfstr!("net/shoreline/eventbus/EventBus$Invoker")).unwrap().into()]
                            ).unwrap().l().unwrap();
                        };

                        let subscriber_class_array = env.new_object_array(
                            1,
                            obfstr!("java/lang/Class"),
                            subscriber_class
                        ).unwrap();

                        let appended_parameter_types = env.call_method(
                            method_type,
                            obfstr!("appendParameterTypes"),
                            obfstr!("([Ljava/lang/Class;)Ljava/lang/invoke/MethodType;"),
                            &[subscriber_class_array.into()]
                        ).unwrap().l().unwrap();

                        let void_class = env.get_static_field(
                            env.find_class(obfstr!("java/lang/Void")).unwrap(),
                            obfstr!("TYPE"),
                            obfstr!("Ljava/lang/Class;")
                        ).unwrap().l().unwrap();

                        let void_obj_method_type = env.call_static_method(
                            env.find_class(obfstr!("java/lang/invoke/MethodType")).unwrap(),
                            obfstr!("methodType"),
                            obfstr!("(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/invoke/MethodType;"),
                            &[
                                void_class.into(),
                                env.find_class(obfstr!("java/lang/Object")).unwrap().into()
                            ]
                        ).unwrap().l().unwrap();

                        let unreflect = env.call_method(
                            lookup,
                            obfstr!("unreflect"),
                            obfstr!("(Ljava/lang/reflect/Method;)Ljava/lang/invoke/MethodHandle;"),
                            &[method_obj.into()]
                        ).unwrap().l().unwrap();

                        let dynamic_method_type = env.call_static_method(
                            env.find_class(obfstr!("java/lang/invoke/MethodType")).unwrap(),
                            obfstr!("methodType"),
                            obfstr!("(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/invoke/MethodType;"),
                            &[
                                void_class.into(),
                                event_type.into()
                            ]
                        ).unwrap().l().unwrap();

                        let invoke_method_name;
                        if IS_OBFUSCATED_ENVIRONMENT
                        {
                            invoke_method_name = env.new_string(obfstr!("a")).unwrap();
                        } else
                        {
                            invoke_method_name = env.new_string(obfstr!("invoke")).unwrap();
                        };

                        let call_site = env.call_static_method(
                            env.find_class(obfstr!("java/lang/invoke/LambdaMetafactory")).unwrap(),
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
                                invoke_method_name.into(),
                                appended_parameter_types.into(),
                                void_obj_method_type.into(),
                                unreflect.into(),
                                dynamic_method_type.into()
                            ]
                        ).unwrap().l().unwrap();

                        let method_handle = env.call_method(
                            call_site,
                            obfstr!("getTarget"),
                            obfstr!("()Ljava/lang/invoke/MethodHandle;"),
                            &[]
                        ).unwrap().l().unwrap();

                        let subscriber_arg_array = env.new_object_array(
                            1,
                            obfstr!("java/lang/Object"),
                            subscriber
                        ).unwrap();

                        let invoker_impl = env.call_method(
                            method_handle,
                            obfstr!("invokeWithArguments"),
                            obfstr!("([Ljava/lang/Object;)Ljava/lang/Object;"),
                            &[subscriber_arg_array.into()]
                        ).unwrap().l().unwrap();

                        env.call_method(
                            invoker_cache,
                            obfstr!("put"),
                            obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                            &[method_obj.into(), invoker_impl.into()]
                        ).unwrap().l().unwrap();

                        invoker_impl
                    };

                    let priority = env.call_static_method(
                        env.find_class(obfstr!("java/lang/Integer")).unwrap(),
                        obfstr!("valueOf"),
                        obfstr!("(I)Ljava/lang/Integer;"),
                        &[priority_int.into()]
                    ).unwrap().l().unwrap();

                    let invoker;
                    if IS_OBFUSCATED_ENVIRONMENT
                    {
                        invoker = env.new_object(
                            obfstr!("net/shoreline/loader/n"),
                            obfstr!("(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
                            &[invoker_obj.into(), subscriber.into(), priority.into()]
                        ).unwrap();
                    } else
                    {
                        invoker = env.new_object(
                            obfstr!("net/shoreline/eventbus/EventBus$InvokerNode"),
                            obfstr!("(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
                            &[invoker_obj.into(), subscriber.into(), priority.into()]
                        ).unwrap();
                    };

                    let event_map;
                    if IS_OBFUSCATED_ENVIRONMENT
                    {
                        event_map = env.get_field(
                            caller_instance,
                            obfstr!("a"),
                            obfstr!("Ljava/lang/Object;")
                        ).unwrap().l().unwrap();
                    } else
                    {
                        event_map = env.get_field(
                            caller_instance,
                            obfstr!("event2InvokerMap"),
                            obfstr!("Ljava/lang/Object;")
                        ).unwrap().l().unwrap();
                    };

                    let mut prev = env.call_method(
                        event_map,
                        obfstr!("get"),
                        obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                        &[event_type.into()]
                    ).unwrap().l().unwrap();

                    let mut current;
                    if IS_OBFUSCATED_ENVIRONMENT
                    {
                        current = env.get_field(
                            prev,
                            obfstr!("a"),
                            obfstr!("Ljava/lang/Object;")
                        ).unwrap().l().unwrap();
                    } else
                    {
                        current = env.get_field(
                            prev,
                            obfstr!("next"),
                            obfstr!("Ljava/lang/Object;")
                        ).unwrap().l().unwrap();
                    };

                    while !env.is_same_object(current, JObject::null()).unwrap()
                    {
                        let current_priority;
                        if IS_OBFUSCATED_ENVIRONMENT
                        {
                            current_priority = env.get_field(
                                current,
                                obfstr!("d"),
                                obfstr!("Ljava/lang/Object;")
                            ).unwrap().l().unwrap();
                        } else
                        {
                            current_priority = env.get_field(
                                current,
                                obfstr!("priority"),
                                obfstr!("Ljava/lang/Object;")
                            ).unwrap().l().unwrap();
                        };

                        let current_priority_int = env.call_method(
                            current_priority,
                            obfstr!("intValue"),
                            obfstr!("()I"),
                            &[]
                        ).unwrap().i().unwrap();

                        if priority_int > current_priority_int
                        {
                            break;
                        }

                        prev = current;

                        let next;
                        if IS_OBFUSCATED_ENVIRONMENT
                        {
                            next = env.get_field(
                                current,
                                obfstr!("a"),
                                obfstr!("Ljava/lang/Object;")
                            ).unwrap().l().unwrap();
                        } else
                        {
                            next = env.get_field(
                                current,
                                obfstr!("next"),
                                obfstr!("Ljava/lang/Object;")
                            ).unwrap().l().unwrap();
                        };

                        current = next;
                    }

                    if IS_OBFUSCATED_ENVIRONMENT
                    {
                        env.set_field(
                            prev,
                            obfstr!("a"),
                            obfstr!("Ljava/lang/Object;"),
                            invoker.into()
                        ).unwrap();

                        env.set_field(
                            invoker,
                            obfstr!("a"),
                            obfstr!("Ljava/lang/Object;"),
                            current.into()
                        ).unwrap();
                    } else
                    {
                        env.set_field(
                            prev,
                            obfstr!("next"),
                            obfstr!("Ljava/lang/Object;"),
                            invoker.into()
                        ).unwrap();

                        env.set_field(
                            invoker,
                            obfstr!("next"),
                            obfstr!("Ljava/lang/Object;"),
                            current.into()
                        ).unwrap();
                    };
                }
            }
        }
        None => panic!("unable to complete native method subscribe")
    }
}

#[export_name = "Java_net_shoreline_eventbus_EventBus_unsubscribe"]
pub unsafe extern "system" fn unsubscribe(env: JNIEnv,
                                          caller_instance: JObject,
                                          subscriber: JObject)
{
    let event_map;
    if IS_OBFUSCATED_ENVIRONMENT
    {
        event_map = env.get_field(
            caller_instance,
            obfstr!("a"),
            obfstr!("Ljava/lang/Object;")
        ).unwrap().l().unwrap();
    } else
    {
        event_map = env.get_field(
            caller_instance,
            obfstr!("event2InvokerMap"),
            obfstr!("Ljava/lang/Object;")
        ).unwrap().l().unwrap();
    };

    let entry_set = env.call_method(
        event_map,
        obfstr!("entrySet"),
        obfstr!("()Ljava/util/Set;"),
        &[]
    ).unwrap().l().unwrap();

    let iterator = env.call_method(
        entry_set,
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
        let entry = env.call_method(
            iterator,
            obfstr!("next"),
            obfstr!("()Ljava/lang/Object;"),
            &[]
        ).unwrap().l().unwrap();

        let mut prev = env.call_method(
            entry,
            "getValue",
            "()Ljava/lang/Object;",
            &[]
        ).unwrap().l().unwrap();

        let mut tmp;
        if IS_OBFUSCATED_ENVIRONMENT
        {
            tmp = env.get_field(
                prev,
                obfstr!("a"),
                obfstr!("Ljava/lang/Object;")
            ).unwrap().l().unwrap();
        } else
        {
            tmp = env.get_field(
                prev,
                obfstr!("next"),
                obfstr!("Ljava/lang/Object;")
            ).unwrap().l().unwrap();
        };

        while !env.is_same_object(tmp, JObject::null()).unwrap()
        {
            let tmp_instance;
            if IS_OBFUSCATED_ENVIRONMENT
            {
                tmp_instance = env.get_field(
                    tmp,
                    obfstr!("c"),
                    obfstr!("Ljava/lang/Object;")
                ).unwrap().l().unwrap();
            } else
            {
                tmp_instance = env.get_field(
                    tmp,
                    obfstr!("subscriber"),
                    obfstr!("Ljava/lang/Object;")
                ).unwrap().l().unwrap();
            };

            if env.is_same_object(tmp_instance, subscriber).unwrap()
            {
                if IS_OBFUSCATED_ENVIRONMENT
                {
                    let tmp_next = env.get_field(
                        tmp,
                        obfstr!("a"),
                        obfstr!("Ljava/lang/Object;")
                    ).unwrap().l().unwrap();

                    env.set_field(
                        prev,
                        obfstr!("a"),
                        obfstr!("Ljava/lang/Object;"),
                        tmp_next.into()
                    ).unwrap();
                } else
                {
                    let tmp_next = env.get_field(
                        tmp,
                        obfstr!("next"),
                        obfstr!("Ljava/lang/Object;")
                    ).unwrap().l().unwrap();

                    env.set_field(
                        prev,
                        obfstr!("next"),
                        obfstr!("Ljava/lang/Object;"),
                        tmp_next.into()
                    ).unwrap();
                };
            } else
            {
                prev = tmp;
            }

            let tmp_next;
            if IS_OBFUSCATED_ENVIRONMENT
            {
                tmp_next = env.get_field(
                    tmp,
                    obfstr!("a"),
                    obfstr!("Ljava/lang/Object;")
                ).unwrap().l().unwrap();
            } else
            {
                tmp_next = env.get_field(
                    tmp,
                    obfstr!("next"),
                    obfstr!("Ljava/lang/Object;")
                ).unwrap().l().unwrap();
            };

            tmp = tmp_next;
        }
    }
}

pub unsafe fn cache_event_class(env: &JNIEnv,
                                event_clazz: JClass)
{
    let event_bus_instance;
    if IS_OBFUSCATED_ENVIRONMENT
    {
        event_bus_instance = env.get_static_field(
            env.find_class(obfstr!("net/shoreline/loader/e")).unwrap(),
            obfstr!("a"),
            obfstr!("Lnet/shoreline/loader/e;")
        ).unwrap().l().unwrap();
    } else
    {
        event_bus_instance = env.get_static_field(
            env.find_class(obfstr!("net/shoreline/eventbus/EventBus")).unwrap(),
            obfstr!("INSTANCE"),
            obfstr!("Lnet/shoreline/eventbus/EventBus;")
        ).unwrap().l().unwrap();
    }

    let event_map;
    if IS_OBFUSCATED_ENVIRONMENT
    {
        event_map = env.get_field(
            event_bus_instance,
            obfstr!("a"),
            obfstr!("Ljava/lang/Object;")
        ).unwrap().l().unwrap();
    } else
    {
        event_map = env.get_field(
            event_bus_instance,
            obfstr!("event2InvokerMap"),
            obfstr!("Ljava/lang/Object;")
        ).unwrap().l().unwrap();
    };

    let new_null_head_invoker;
    if IS_OBFUSCATED_ENVIRONMENT
    {
        new_null_head_invoker = env.new_object(
            obfstr!("net/shoreline/loader/n"),
            obfstr!("(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
            &[JObject::null().into(), JObject::null().into(), JObject::null().into()]
        ).unwrap();
    } else
    {
        new_null_head_invoker = env.new_object(
            obfstr!("net/shoreline/eventbus/EventBus$InvokerNode"),
            obfstr!("(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"),
            &[JObject::null().into(), JObject::null().into(), JObject::null().into()]
        ).unwrap();
    };

    env.call_method(
        event_map,
        obfstr!("put"),
        obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
        &[event_clazz.into(), new_null_head_invoker.into()]
    ).unwrap().l().unwrap();
}

fn is_in_dev_environment(env: &JNIEnv) -> bool
{
    let fabric_loader_instance = env.call_static_method(
        env.find_class(obfstr!("net/fabricmc/loader/api/FabricLoader")).unwrap(),
        obfstr!("getInstance"),
        obfstr!("()Lnet/fabricmc/loader/api/FabricLoader;"),
        &[]
    ).unwrap().l().unwrap();

    return env.call_method(
        fabric_loader_instance,
        obfstr!("isDevelopmentEnvironment"),
        obfstr!("()Z"),
        &[]
    ).unwrap().z().unwrap();
}