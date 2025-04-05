use std::collections::HashMap;
use std::fmt::format;
use std::os::raw::c_void;
use std::process::exit;
use fltk::app::event;
use jni::objects::{GlobalRef, JClass, JObject, JString};
use jni::{JNIEnv, NativeMethod};
use jni::strings::JNIString;
use obfstr::obfstr;
use crate::core::Payload;
use crate::{anticrack, asm, eventbus, notifs, obfuscation};

static mut INSTANCE: Option<ClassLoader> = None;
static mut EVENT_CLASS: Option<GlobalRef> = None;

pub struct ClassLoader
{
    pub(crate) payload: Payload
}

impl ClassLoader
{
    pub unsafe fn create_instance<'a>(payload: Payload) -> Result<&'a mut Self, String>
    {
        if INSTANCE.is_some()
        {
            return Err(obfstr! {
                "Shoreline has encountered a severe error while loading.\n\n\
                Something has tried to create a duplicate ClassLoader instance. \
                Please report this to a developer, this should never happen."
            }.to_string())
        }

        INSTANCE = Some(
            ClassLoader {
                payload
            }
        );

        Ok(INSTANCE.as_mut().unwrap())
    }

    pub unsafe fn get_instance<'a>(env: &mut JNIEnv) -> &'a mut Self
    {
        if INSTANCE.is_none()
        {
            let msg = obfstr! {
                "Shoreline has encountered a severe error while loading.\n\n\
                The class loader was never instantiated. Please report this \
                to a developer, this should never happen."
            }.to_string();

            notifs::error(env, &msg);
            notifs::display_error_msg(&msg);

            exit(-1);
        }

        INSTANCE.as_mut().unwrap()
    }

    pub unsafe fn define_classes(&mut self,
                                 env: &mut JNIEnv)
    {
        let mut dependency_map: HashMap<String, Vec<String>> = HashMap::new();

        for (name, bytes) in self.payload.class_bytecode.clone()
        {
            let decrypted_class_bytes = match crate::crypto::decrypt(bytes)
            {
                Ok(content) => content,
                Err(_) => {
                    let msg = obfstr! {
                        "Failed to load a class due to improper decryption. \
                        Please report this to a developer."
                    }.to_string();

                    notifs::error(env, &msg);
                    notifs::display_error_msg(&msg);

                    exit(-1);
                }
            };

            let mut dependants = asm::get_immediate_dependants(env, decrypted_class_bytes);
            dependants.retain(|superclass| {
                self.payload.class_bytecode.contains_key(superclass)
            });

            dependency_map.insert(name, dependants);
        }

        let mut iterations = 0;
        while !dependency_map.is_empty()
        {
            let zero_dependant_classes: Vec<String> = self.payload.class_bytecode
                .keys()
                .filter(|class| {
                    dependency_map.get(*class).unwrap().is_empty()
                })
                .map(|class| class.clone())
                .collect();

            for class_name in zero_dependant_classes.iter()
            {
                let bytes = self.payload.class_bytecode.remove(class_name).unwrap();

                define_class_via_knot(env, class_name, bytes);
            }

            for defined_class in zero_dependant_classes.iter()
            {
                dependency_map.remove(defined_class);
            }

            for dependencies in dependency_map.values_mut()
            {
                dependencies.retain(|dep| !zero_dependant_classes.contains(dep));
            }

            // By the first iteration of class definitions, our event bus and its needed classes
            // have been defined because they don't extend anything. So we can initialize it here
            // since classes in the second/third/fourth iteration that extend Event can be initialized
            if iterations == 0
            {
                eventbus::init_internal(env);
            }

            iterations = iterations + 1;
        }
    }

    pub unsafe fn define_late_loading_classes(&mut self,
                                              env: &mut JNIEnv)
    {
        let mut dependency_map: HashMap<String, Vec<String>> = HashMap::new();

        for (name, bytes) in self.payload.late_loading_bytecode.clone()
        {
            let decrypted_class_bytes = match crate::crypto::decrypt(bytes)
            {
                Ok(content) => content,
                Err(_) => {
                    let msg = obfstr! {
                        "Failed to load a class due to improper decryption. \
                        Please report this to a developer."
                    }.to_string();

                    notifs::error(env, &msg);
                    notifs::display_error_msg(&msg);

                    exit(-1);
                }
            };

            let mut dependants = asm::get_immediate_dependants(env, decrypted_class_bytes);
            dependants.retain(|superclass| {
                self.payload.late_loading_bytecode.contains_key(superclass)
            });

            dependency_map.insert(name, dependants);
        }

        while !dependency_map.is_empty()
        {
            let zero_dependant_classes: Vec<String> = self.payload.late_loading_bytecode
                .keys()
                .filter(|class| {
                    dependency_map.get(*class).unwrap().is_empty()
                })
                .map(|class| class.clone())
                .collect();

            for class_name in zero_dependant_classes.iter()
            {
                let bytes = self.payload.late_loading_bytecode.remove(class_name).unwrap();

                define_class_via_knot(env, class_name, bytes);
            }

            for defined_class in zero_dependant_classes.iter()
            {
                dependency_map.remove(defined_class);
            }

            for dependencies in dependency_map.values_mut()
            {
                dependencies.retain(|dep| !zero_dependant_classes.contains(dep));
            }
        }
    }

    pub unsafe fn load_access_widener(&mut self,
                                      env: &mut JNIEnv)
    {
        let access_widener_bytes = match self.payload.access_widener.take()
        {
            Some(encrypted_bytes) => {
                let decrypted_bytes = match crate::crypto::decrypt(encrypted_bytes)
                {
                    Ok(content) => content,
                    Err(_) => {
                        let msg = obfstr! {
                            "Failed to load the access transformer due to improper decryption. \
                            Please report this to a developer."
                        }.to_string();

                        notifs::error(env, &msg);
                        notifs::display_error_msg(&msg);

                        exit(-1);
                    }
                };

                decrypted_bytes
            }
            None => {
                let msg = obfstr! {
                    "Failed to load Shoreline because the access transformer was unable to be found. \
                    This should never happen, please report this to a developer."
                }.to_string();

                notifs::error(env, &msg);
                notifs::display_error_msg(&msg);

                exit(-1);
            }
        };

        let access_widener_bytes = env.byte_array_from_slice(access_widener_bytes.as_slice()).unwrap();

        let fabric_loader_impl_instance = env.get_static_field(
            obfstr!("net/fabricmc/loader/impl/FabricLoaderImpl"),
            obfstr!("INSTANCE"),
            obfstr!("Lnet/fabricmc/loader/impl/FabricLoaderImpl;")
        ).unwrap().l().unwrap();

        let access_widener = env.call_method(
            fabric_loader_impl_instance,
            obfstr!("getAccessWidener"),
            obfstr!("()Lnet/fabricmc/loader/impl/lib/accesswidener/AccessWidener;"),
            &[]
        ).unwrap().l().unwrap();

        let access_widener_reader = env.new_object(
            obfstr!("net/fabricmc/loader/impl/lib/accesswidener/AccessWidenerReader"),
            obfstr!("(Lnet/fabricmc/loader/impl/lib/accesswidener/AccessWidenerVisitor;)V"),
            &[(&access_widener).into()]
        ).unwrap();

        let fabric_launcher_base_instance = env.call_static_method(
            obfstr!("net/fabricmc/loader/impl/launch/FabricLauncherBase"),
            obfstr!("getLauncher"),
            obfstr!("()Lnet/fabricmc/loader/impl/launch/FabricLauncher;"),
            &[]
        ).unwrap().l().unwrap();

        let target_name_space = env.call_method(
            fabric_launcher_base_instance,
            obfstr!("getTargetNamespace"),
            obfstr!("()Ljava/lang/String;"),
            &[]
        ).unwrap().l().unwrap();

        env.call_method(
            access_widener_reader,
            obfstr!("read"),
            obfstr!("([BLjava/lang/String;)V"),
            &[(&access_widener_bytes).into(), (&target_name_space).into()]
        ).unwrap().v().unwrap();
    }
}

pub unsafe fn define_class_via_knot(env: &mut JNIEnv,
                                    name: &str,
                                    bytes: Vec<u8>)
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

    let decrypted_class_bytes = match crate::crypto::decrypt(bytes)
    {
        Ok(content) => content,
        Err(_) => {
            let msg = obfstr! {
                    "Failed to load a class due to improper decryption. \
                    Please report this to a developer."
                }.to_string();

            notifs::error(env, &msg);
            notifs::display_error_msg(&msg);

            exit(-1);
        }
    };

    let clazz = env.define_class(
        name.replace(obfstr!("."), obfstr!("/")),
        &context_classloader,
        decrypted_class_bytes.as_slice()
    );

    if env.exception_check().unwrap()
    {
        let msg = obfstr! {
            "Failed to define a Shoreline class. The server class cache is likely outdated. \
            Please report this to a developer."
        }.to_string();

        notifs::error(env, &msg);
        notifs::display_error_msg(&msg);

        env.exception_describe().unwrap();

        //exit(-1);
    }

    let clazz = clazz.unwrap();

    // attempt to autoregister the native bootstrap methods (outdated) and the key bootstrapper, which will error if it doesnt exist
    // so we'll just clear the error >:)
    // https://media.discordapp.net/attachments/1035106423589326889/1118046735957246033/caption.gif?ex=66b303aa&is=66b1b22a&hm=d270260c0f5c60c7322331e6077dbeeb91176b513c011baa04c6f3b97b8dbbac&
    let native_methods = [
        NativeMethod
        {
            name: JNIString::from(obfstr!("ur_not_cracking_this")),
            sig: JNIString::from(obfstr!("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;JJJI)Ljava/lang/invoke/CallSite;")),
            fn_ptr: anticrack::generate_native_callsite as *mut c_void,
        }
    ];

    let register = env.register_native_methods(&clazz, &native_methods);

    if env.exception_check().unwrap()
    {
        env.exception_clear().unwrap()
    } else
    {
        register.unwrap();
    }

    let native_methods = [
        NativeMethod
        {
            name: JNIString::from(obfstr!("bootstrap")),
            sig: JNIString::from(obfstr!("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;JLjava/lang/String;)J")),
            fn_ptr: anticrack::generate_key_bootstrapper as *mut c_void,
        }
    ];

    let register = env.register_native_methods(&clazz, &native_methods);

    if env.exception_check().unwrap()
    {
        env.exception_clear().unwrap()
    } else
    {
        register.unwrap();
    }

    let native_methods = [
        NativeMethod
        {
            name: JNIString::from(obfstr!("bootstrap")),
            sig: JNIString::from(obfstr!("(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/Class;JLjava/lang/String;Ljava/lang/Class;)J")),
            fn_ptr: anticrack::generate_interface_key_bootstrapper as *mut c_void,
        }
    ];

    let register = env.register_native_methods(&clazz, &native_methods);

    if env.exception_check().unwrap()
    {
        env.exception_clear().unwrap()
    } else
    {
        register.unwrap();
    }

    // If the class is an event class, cache it in the event bus
    if name.eq(obfstr!("net.shoreline.client.fR"))
    {
        EVENT_CLASS = Some(
            env.new_global_ref(clazz).unwrap()
        );
    } else
    {
        if EVENT_CLASS.is_some()
        {
            let event_class = EVENT_CLASS.as_ref().unwrap().as_obj();

            if env.call_method(
                event_class,
                obfstr!("isAssignableFrom"),
                obfstr!("(Ljava/lang/Class;)Z"),
                &[(&clazz).into()]
            ).unwrap().z().unwrap()
            {
                eventbus::cache_event_class(env, clazz);
            }
        }
    }
}