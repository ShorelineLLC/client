use std::ffi::CStr;
use std::hash::{Hash, Hasher};
use std::ptr::{null, null_mut};
use jni::JNIEnv;
use jni::objects::{JClass, JMethodID, JObject, JString, JValue};
use jni::signature::JavaType;
use jni::sys::{jmethodID, JNI_FALSE};
use obfstr::obfstr;
use priority_queue::PriorityQueue;
use log::log;
use crate::log;

#[no_mangle]
#[export_name = "Java_net_shoreline_eventbus_bus_EventBus_init"]
pub unsafe extern "system" fn init(env: JNIEnv,
                                   caller_instance: JObject)
{
    let head_invoker = env.new_object(
        obfstr!("net/shoreline/eventbus/bus/EventBus$stop_decompiling"),
        obfstr!("(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;I)V"),
        &[JObject::null().into(), JObject::null().into(), JObject::null().into(), JValue::Int(0)]
    ).unwrap();

    env.set_field(
        caller_instance,
        obfstr!("a"),
        obfstr!("Lnet/shoreline/eventbus/bus/EventBus$stop_decompiling;"),
        head_invoker.into()
    ).unwrap();
}

#[no_mangle]
#[export_name = "Java_net_shoreline_eventbus_bus_EventBus_subscribe"]
pub unsafe extern "system" fn subscribe(env: JNIEnv,
                                        caller_instance: JObject,
                                        subscriber: JObject)
{
    let methods = env.call_method(
        env.get_object_class(subscriber).unwrap(),
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

        let event_listener = env.find_class(
            obfstr!("net/shoreline/eventbus/annotation/EventListener"),
        ).unwrap();

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

            let priority = env.call_method(
                declared_annotation,
                obfstr!("priority"),
                obfstr!("()I"),
                &[],
            ).unwrap().i().unwrap();

            let parameters = env.call_method(
                method_obj,
                obfstr!("getParameterTypes"),
                obfstr!("()[Ljava/lang/Class;"),
                &[],
            ).unwrap().l().unwrap();

            let event_type = env.get_object_array_element(*parameters, 0).unwrap();

            let invoker = env.new_object(
                obfstr!("net/shoreline/eventbus/bus/EventBus$stop_decompiling"),
                obfstr!("(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;I)V"),
                &[method_obj.into(), subscriber.into(), event_type.into(), priority.into()]
            ).unwrap();

            let mut prev = env.get_field(
                caller_instance,
                obfstr!("a"),
                obfstr!("Lnet/shoreline/eventbus/bus/EventBus$stop_decompiling;")
            ).unwrap().l().unwrap();

            let mut current = env.get_field(
                prev,
                obfstr!("e"),
                obfstr!("Lnet/shoreline/eventbus/bus/EventBus$stop_decompiling;")
            ).unwrap().l().unwrap();

            while !env.is_same_object(current, JObject::null()).unwrap()
            {
                let current_priority = env.get_field(
                    current,
                    obfstr!("d"),
                    obfstr!("I")
                ).unwrap().i().unwrap();
                
                if priority > current_priority
                {
                    break;
                }

                prev = current;

                let next = env.get_field(
                    current,
                    obfstr!("e"),
                    obfstr!("Lnet/shoreline/eventbus/bus/EventBus$stop_decompiling;")
                ).unwrap().l().unwrap();

                current = next;
            }

            env.set_field(
                prev,
                obfstr!("e"),
                obfstr!("Lnet/shoreline/eventbus/bus/EventBus$stop_decompiling;"),
                invoker.into()
            ).unwrap();

            env.set_field(
                invoker,
                obfstr!("e"),
                obfstr!("Lnet/shoreline/eventbus/bus/EventBus$stop_decompiling;"),
                current.into()
            ).unwrap();
        }
    }
}

#[no_mangle]
#[export_name = "Java_net_shoreline_eventbus_bus_EventBus_unsubscribe"]
pub unsafe extern "system" fn unsubscribe(env: JNIEnv,
                                          caller_instance: JObject,
                                          subscriber: JObject)
{
    let mut prev = env.get_field(
        caller_instance,
        obfstr!("a"),
        obfstr!("Lnet/shoreline/eventbus/bus/EventBus$stop_decompiling;")
    ).unwrap().l().unwrap();

    let mut tmp = env.get_field(
        prev,
        obfstr!("e"),
        obfstr!("Lnet/shoreline/eventbus/bus/EventBus$stop_decompiling;")
    ).unwrap().l().unwrap();

    while !env.is_same_object(tmp, JObject::null()).unwrap()
    {
        let tmp_instance = env.get_field(
            tmp,
            obfstr!("b"),
            obfstr!("Ljava/lang/Object;")
        ).unwrap().l().unwrap();

        if env.is_same_object(tmp_instance, subscriber).unwrap()
        {
            let tmp_next = env.get_field(
                tmp,
                obfstr!("e"),
                obfstr!("Lnet/shoreline/eventbus/bus/EventBus$stop_decompiling;")
            ).unwrap().l().unwrap();

            env.set_field(
                prev,
                obfstr!("e"),
                obfstr!("Lnet/shoreline/eventbus/bus/EventBus$stop_decompiling;"),
                tmp_next.into()
            ).unwrap();
        } else
        {
            prev = tmp;
        }

        let tmp_next = env.get_field(
            tmp,
            obfstr!("e"),
            obfstr!("Lnet/shoreline/eventbus/bus/EventBus$stop_decompiling;")
        ).unwrap().l().unwrap();

        tmp = tmp_next;
    }
}

pub static mut INVOKE: Option<JMethodID> = None;

#[no_mangle]
#[export_name = "Java_net_shoreline_eventbus_bus_EventBus_dispatch_1internal"]
pub unsafe extern "system" fn dispatch_internal(env: JNIEnv<'static>,
                                                _caller_instance: JObject,
                                                method: JObject,
                                                instance: JObject,
                                                event: JObject)
{
    if !INVOKE.is_some()
    {
        let method_class = env.find_class("java/lang/reflect/Method").unwrap();

        let method_id = env.get_method_id(
            method_class,
            obfstr!("invoke"),
            obfstr!("(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;")
        ).unwrap();

        INVOKE = Some(method_id);
    }

    match INVOKE.as_ref()
    {
        Some(invoke) => {
            env.call_method_unchecked(
                method,
                *invoke,
                JavaType::Object(String::from(obfstr!("Ljava/lang/Object;"))),
                &[instance.into(), event.into()]
            ).unwrap();
        }
        None => panic!("unable to complete native method dispatch_internal")
    }
}