mod utils;
mod eventbus;
mod antidump;

extern crate jni;

use std::collections::{HashMap, VecDeque};
use jni::{JNIEnv, JavaVM};
use obfstr::obfstr;
use jni::sys::{JNI_VERSION_1_8};
use std::os::raw::{c_void, c_int};
use jni::objects::{JObject, JString, JClass, GlobalRef};
use std::ffi::{CStr};
use std::io::Cursor;
use aes::Aes128;
use block_padding::Pkcs7;
use cbc::cipher::{BlockDecryptMut, KeyIvInit};
use cbc::Decryptor;
use hardware_id::get_id;
use lazy_static::lazy_static;
use reqwest::Client;
use tokio::runtime::Runtime;
use zip::ZipArchive;
use crate::utils::{define_class, encrypt, get_immediate_dependents, is_imixin_class, is_mixin_accessor, is_mixin_class, error_message, crash, alert_webhook, alert_webhook_async, define_class_internal};

lazy_static! {
    static ref CLIENT: Client = Client::builder().cookie_store(true).build().unwrap();
}

static mut USER_INFO: Option<GlobalRef> = None;

static mut MIXIN_LIST: Option<GlobalRef> = None;
static mut MIXIN_REFMAP: Option<GlobalRef> = None;

static mut LATE_LOADING_CLASSES: Option<GlobalRef> = None;
static mut LOADER_CLASS_BYTECODE: Option<GlobalRef> = None;

static mut RESOURCES_MAP: Option<GlobalRef> = None;

static mut IS_OBFUSCATED_ENVIRONMENT: bool = false;

#[no_mangle]
pub unsafe extern "system" fn JNI_OnLoad(vm: JavaVM,
                                         _reserved: &mut c_void) -> c_int
{
    let env = vm.get_env().unwrap();

    match env.find_class(obfstr!("net/shoreline/loader/Loader"))
    {
        Ok(_) => {}
        Err(_) => {
            if env.exception_check().unwrap()
            {
                env.exception_clear().unwrap();
            }
            IS_OBFUSCATED_ENVIRONMENT = true;
        }
    }

    if IS_OBFUSCATED_ENVIRONMENT
    {
        // decrypt loader classes
        let caller_class = env.find_class(
            obfstr!("net/shoreline/loader/give up")
        ).unwrap();
        decrypt_all_classes(&env, caller_class);
    }

    let crash_clazz = env.find_class(
        obfstr!("java/lang/System")
    ).unwrap();

    let raw_hwid = match get_id()
    {
        Ok(res) => {
            res
        }
        Err(_err) => {
            error_message(
                obfstr!("Shoreline failed to retrieve your computer information.\n\nPlease contact a developer!")
            );

            crash(&env, crash_clazz);

            return JNI_VERSION_1_8;
        }
    };

    let hwid = encrypt(&raw_hwid);

    let runtime = Runtime::new().unwrap();

    let client = &CLIENT;

    runtime.block_on(async {
        let response = client
            .get(obfstr!("https://api.shorelineclient.net/auth"))
            .header(obfstr!("User-Agent"), obfstr!("shoreline-client"))
            .header(obfstr!("Hardware-ID"), hwid)
            .send()
            .await;

        match response
        {
            Ok(res) => {
                let http_response_code = res.status().as_u16();

                match http_response_code
                {
                    200 => {
                        let body = res.text().await.unwrap();
                        USER_INFO = Some(env.new_global_ref(env.new_string(body).unwrap()).unwrap());

                        // User is authed, load our event bus
                        eventbus::init_internal(&env);
                    }
                    401 => {
                        error_message(
                            obfstr!("Invalid user credentials.\n\nPurchase your own version of Shoreline at shorelineclient.net!")
                        );

                        crash(&env, crash_clazz);
                    }
                    _ => {
                        let msg = format!(
                            "{}{}{}",
                            obfstr!("Internal server error "),
                            http_response_code,
                            obfstr!(".\n\nPlease contact Shoreline support!")
                        );

                        error_message(&msg);

                        crash(&env, crash_clazz);
                    }
                }
            }
            Err(_) => {
                error_message(
                    obfstr!("Failed to connect to Shoreline servers.\n\nPlease contact Shoreline support!")
                );

                crash(&env, crash_clazz);
            }
        }
    });

    return JNI_VERSION_1_8
}

type Aes128CbcDec = Decryptor<Aes128>;

const IV_KEY: [i8; 16] = [-2, 4, -45, -51, 15, -1, -121, -91, -27, -42, 55, -48, 69, 106, 8, -41];
const ENCRYPTION_KEY: [i8; 16] = [16, 110, 1, -8, 44, 103, 18, 0, 84, 37, -111, -112, -2, 39, 120, 54];

#[export_name = "Java_net_shoreline_loader_Loader_a"]
pub unsafe extern "system" fn decrypt_loader_classes<'a>(_env: JNIEnv<'a>,
                                                         _caller_class: JClass<'a>)
{

}

pub unsafe fn decrypt_all_classes<'a>(env: &JNIEnv,
                                      caller_class: JClass<'a>)
{
    let mut class_queue = VecDeque::new();

    let class_bytecode = env.new_object(
        obfstr!("java/util/HashMap"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    LOADER_CLASS_BYTECODE = env.new_global_ref(class_bytecode).ok();

    let class_loader = env.call_method(
        caller_class,
        obfstr!("getClassLoader"),
        obfstr!("()Ljava/lang/ClassLoader;"),
        &[]
    ).unwrap().l().unwrap();

    let alphabet = ["a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
        "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"];

    let mut count = 0;
    let mut input_stream;

    loop
    {
        let resource_name = obfstr!("assets/shoreline/").to_owned() + &alphabet[count] + obfstr!(".shoreline");
        count += 1;

        let formatted_name;
        // this MUST be changed when adding or removing classes from the loader
        // must also be synced with how bonfuscator renames Natives
        if resource_name.eq(obfstr!("assets/shoreline/p.shoreline"))
        {
            formatted_name = obfstr!("net/shoreline/loader/Natives.class").to_owned();
        } else
        {
            let raw_name = resource_name.replace(obfstr!("assets/shoreline/"), obfstr!(""));
            formatted_name = obfstr!("net/shoreline/loader/").to_owned() + raw_name.replace(obfstr!(".shoreline"), obfstr!(".class")).as_str();
        }

        let java_name = env.new_string(resource_name).unwrap();

        input_stream = env.call_method(
            class_loader,
            obfstr!("getResourceAsStream"),
            obfstr!("(Ljava/lang/String;)Ljava/io/InputStream;"),
            &[java_name.into()]
        ).unwrap().l().unwrap();

        if input_stream.is_null()
        {
            break;
        }

        let available = env.call_method(
            input_stream,
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
                input_stream,
                obfstr!("read"),
                obfstr!("([B)I"),
                &[java_buffer.into()]
            ).unwrap().i().unwrap();

            if len == -1
            {
                break;
            }

            env.call_method(
                baos,
                obfstr!("write"),
                obfstr!("([BII)V"),
                &[java_buffer.into(), 0.into(), len.into()]
            ).unwrap().v().unwrap();
        }

        let bytes = env.call_method(
            baos,
            obfstr!("toByteArray"),
            obfstr!("()[B"),
            &[]
        ).unwrap().l().unwrap();

        let iv_length = 16; // Length of IV_KEY
        let bytecode_len = env.get_array_length(*bytes).unwrap();

        let iv_key = env.call_static_method(
            obfstr!("java/util/Arrays"),
            obfstr!("copyOfRange"),
            obfstr!("([BII)[B"),
            &[(*bytes).into(), 0.into(), iv_length.into()],
        ).unwrap().l().unwrap();

        let iv_spec = env.new_object(
            obfstr!("javax/crypto/spec/IvParameterSpec"),
            obfstr!("([B)V"),
            &[iv_key.into()]
        ).unwrap();

        let encryption_key = env.byte_array_from_slice(&[16, 110, 1, 248, 44, 103, 18, 0, 84, 37, 145, 144, 254, 39, 120, 54]).unwrap();

        let secret_key_spec = env.new_object(
            obfstr!("javax/crypto/spec/SecretKeySpec"),
            obfstr!("([BLjava/lang/String;)V"),
            &[encryption_key.into(), env.new_string(obfstr!("AES")).unwrap().into()],
        ).unwrap();

        let encrypted_bytecode = env.call_static_method(
            obfstr!("java/util/Arrays"),
            obfstr!("copyOfRange"),
            obfstr!("([BII)[B"),
            &[(*bytes).into(), iv_length.into(), bytecode_len.into()],
        ).unwrap().l().unwrap();

        let cipher = env.call_static_method(
            obfstr!("javax/crypto/Cipher"),
            obfstr!("getInstance"),
            obfstr!("(Ljava/lang/String;)Ljavax/crypto/Cipher;"),
            &[env.new_string(obfstr!("AES/CBC/PKCS5Padding")).unwrap().into()],
        ).unwrap().l().unwrap();

        env.call_method(
            cipher,
            obfstr!("init"),
            obfstr!("(ILjava/security/Key;Ljava/security/spec/AlgorithmParameterSpec;)V"),
            &[2.into(), secret_key_spec.into(), iv_spec.into()],
        ).unwrap();

        // Decrypt the bytecode
        let decrypted = env.call_method(
            cipher,
            "doFinal",
            "([B)[B",
            &[encrypted_bytecode.into()],
        ).unwrap().l().unwrap();

        // Define ClassScanner (f) first, so we can use get_immediate_dependants to
        // figure out class circularity issues
        if formatted_name.eq(obfstr!("net/shoreline/loader/f.class"))
        {
            define_class(&env, obfstr!("net/shoreline/loader/f.class"), decrypted);
        } else
        {
            class_queue.push_back((formatted_name, decrypted));
        }
    }

    let mut defined_classes = Vec::new();
    let mut dependency_map = HashMap::new();
    let mut bytes_map = HashMap::new();

    for (name, bytes) in class_queue.iter()
    {
        let mut dependencies = get_immediate_dependents(&env, *bytes);

        dependencies.retain(|s| s.starts_with(obfstr!("net/shoreline/loader/")));

        dependency_map.insert(name.clone(), dependencies);
        bytes_map.insert(name.clone(), bytes);
    }

    while !dependency_map.is_empty()
    {
        let mut defined_this_iteration = Vec::new();

        for (class_name, dependencies) in dependency_map.iter()
        {
            if dependencies.is_empty()
            {
                let bytes = *bytes_map.remove(class_name).unwrap();
                let clazz = define_class(&env, class_name, bytes);
                defined_classes.push(clazz);
                defined_this_iteration.push(class_name.clone());

                // Cache loader code for mixins
                if class_name.eq("net/shoreline/loader/e.class") // EventBus.class
                    || class_name.eq("net/shoreline/loader/b.class") // ClassLoader.class (for loading resources)
                    || class_name.eq("net/shoreline/loader/a.class") // ShorelineResourcePack.class (for loading resources)
                {
                    env.call_method(
                        class_bytecode,
                        obfstr!("put"),
                        obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                        &[env.new_string(class_name).unwrap().into(), bytes.into()]
                    ).unwrap();
                }
            }
        }

        for defined_class in defined_this_iteration.iter()
        {
            dependency_map.remove(defined_class);
        }

        for dependencies in dependency_map.values_mut()
        {
            dependencies.retain(|dep| !defined_this_iteration.contains(dep));
        }
    }
}


/**
 * Create and return a new class instance without calling <init>
 */
#[export_name = "Java_net_shoreline_loader_Natives_a"]
pub extern "system" fn create_raw_instance<'a>(env: JNIEnv<'a>,
                                               _caller_class: JClass<'a>,
                                               class_ctor_instance: JObject<'a>) -> JObject<'a>
{
    let declaring_class = env.call_method(
        class_ctor_instance,
        obfstr!("getDeclaringClass"),
        obfstr!("()Ljava/lang/Class;"),
        &[]
    ).unwrap().l().unwrap();

    let the_unsafe = env.get_static_field(
        env.find_class(obfstr!("sun/misc/Unsafe")).unwrap(),
        obfstr!("theUnsafe"),
        obfstr!("Lsun/misc/Unsafe;")
    ).unwrap().l().unwrap();

    return env.call_method(
        the_unsafe,
        obfstr!("allocateInstance"),
        obfstr!("(Ljava/lang/Class;)Ljava/lang/Object;"),
        &[declaring_class.into()]
    ).unwrap().l().unwrap();
}

/**
 * Return the list of mixins
 */
#[export_name = "Java_net_shoreline_loader_Natives_b"]
pub unsafe extern "system" fn get_mixins<'a>(env: JNIEnv<'a>,
                                             caller_class: JClass<'a>,
                                             _unused_obscure: JObject<'a>) -> JObject<'a>
{
    return match MIXIN_LIST.as_ref().take()
    {
        Some(config_ref) => config_ref.as_obj(),
        None => {
            error_message(
                obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: 1")
            );

            crash(&env, caller_class);

            JObject::null()
        }
    }
}

/**
 * Return the mixin refmap
 */
#[export_name = "Java_net_shoreline_loader_Natives_c"]
pub unsafe extern "system" fn get_refmap<'a>(env: JNIEnv<'a>,
                                             caller_class: JClass<'a>,
                                             _unused_obscure: JObject<'a>) -> JObject<'a>
{
    return match MIXIN_REFMAP.as_ref().take()
    {
        Some(refmap_ptr) => refmap_ptr.as_obj().clone(),
        None => {
            error_message(
                obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: 2")
            );

            crash(&env, caller_class);

            JObject::null()
        }
    }
}

pub fn log(env: &JNIEnv, msg: &str)
{
    let logger = env.get_static_field(
        env.find_class("net/shoreline/loader/give up").unwrap(),
        "a",
        "Lorg/apache/logging/log4j/Logger;"
    ).unwrap().l().unwrap();

    let msg_str = env.new_string(msg).unwrap();

    env.call_method(
        logger,
        "info",
        "(Ljava/lang/String;)V",
        &[msg_str.into()]
    ).unwrap().v().unwrap();
}

/**
 * Download and store all client classes & resources
 */
#[export_name = "Java_net_shoreline_loader_Natives_d"]
pub unsafe extern "system" fn download_client<'a>(env: JNIEnv<'a>,
                                                  caller_class: JClass<'a>,
                                                  information_array: JObject<'a>) -> JObject<'a>
{
    // Get the loader hash and verify with the server
    let protection_domain = env.call_method(
        env.find_class(obfstr!("net/shoreline/loader/give up")).unwrap(),
        obfstr!("getProtectionDomain"),
        obfstr!("()Ljava/security/ProtectionDomain;"),
        &[]
    ).unwrap().l().unwrap();

    let code_source = env.call_method(
        protection_domain,
        obfstr!("getCodeSource"),
        obfstr!("()Ljava/security/CodeSource;"),
        &[]
    ).unwrap().l().unwrap();

    let location = env.call_method(
        code_source,
        obfstr!("getLocation"),
        obfstr!("()Ljava/net/URL;"),
        &[]
    ).unwrap().l().unwrap();

    let location_uri = env.call_method(
        location,
        obfstr!("toURI"),
        obfstr!("()Ljava/net/URI;"),
        &[]
    ).unwrap().l().unwrap();

    let file_jar = env.new_object(
        obfstr!("java/io/File"),
        obfstr!("(Ljava/net/URI;)V"),
        &[location_uri.into()]
    ).unwrap();

    let file_path = env.call_method(
        file_jar,
        obfstr!("toPath"),
        obfstr!("()Ljava/nio/file/Path;"),
        &[]
    ).unwrap().l().unwrap();

    let jvm_bytes = env.call_static_method(
        env.find_class(obfstr!("java/nio/file/Files")).unwrap(),
        obfstr!("readAllBytes"),
        obfstr!("(Ljava/nio/file/Path;)[B"),
        &[file_path.into()]
    ).unwrap().l().unwrap();

    let bytes_to_string = env.call_static_method(
        env.find_class(obfstr!("java/util/Arrays")).unwrap(),
        obfstr!("toString"),
        obfstr!("([B)Ljava/lang/String;"),
        &[jvm_bytes.into()]
    ).unwrap().l().unwrap();

    let bytes_to_string_ptr = env.get_string_utf_chars(JString::from(bytes_to_string)).unwrap();
    let bytes_to_string_internal = CStr::from_ptr(bytes_to_string_ptr).to_str().unwrap();

    let encrypted_bytes = encrypt(bytes_to_string_internal);

    let rt = Runtime::new().unwrap();

    let client = &CLIENT;

    let temporary_token: Option<String> = rt.block_on(async {

        let response = client
            .get(obfstr!("https://api.shorelineclient.net/integrity"))
            .header(obfstr!("User-Agent"), obfstr!("shoreline-client"))
            .header(obfstr!("Loader-Hash"), encrypted_bytes)
            .send()
            .await;

        return match response
        {
            Ok(res) => {
                let http_response_code = res.status().as_u16();

                match http_response_code
                {
                    200 => {
                        Some(res.text().await.unwrap())
                    }
                    406 => {
                        // The jar has been tampered with
                        error_message(
                            obfstr!("We know what you did")
                        );

                        let message = env.new_string(obfstr!("Loader has been tampered with")).unwrap();

                        env.set_object_array_element(*information_array, 0, message).unwrap();

                        alert_webhook_async(
                            &env,
                            information_array,
                            client
                        ).await;

                        crash(&env, caller_class);

                        None
                    }
                    _ => {
                        let msg = format!(
                            "{}{}{}",
                            obfstr!("Internal server error "),
                            http_response_code,
                            obfstr!(".\n\nPlease contact Shoreline support!")
                        );

                        error_message(&msg);

                        crash(&env, caller_class);

                        None
                    }
                }
            }
            Err(_) => {
                error_message(
                    obfstr!("Failed to connect to Shoreline servers.\n\nPlease contact Shoreline support!")
                );

                crash(&env, caller_class);

                None
            }
        }
    });

    if temporary_token.is_none()
    {
        return JObject::null();
    }

    let temporary_token = temporary_token.unwrap();

    // A queue of all classes that need to be defined
    let mut class_queue = VecDeque::new();

    // A queue of all classes that need their bytecode cached on the java side
    // Will be added to the class_map below
    let mut cache_queue = VecDeque::new();

    // All mc extending classes that cannot be defined yet
    let mc_class_dependents = env.new_object(
        obfstr!("java/util/HashMap"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    LATE_LOADING_CLASSES = env.new_global_ref(mc_class_dependents).ok();

    // A map of all classes that need their bytecode parsed by the Java side
    let class_map = env.new_object(
        obfstr!("java/util/HashMap"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    let resources_map = env.new_object(
        obfstr!("java/util/HashMap"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    RESOURCES_MAP = env.new_global_ref(resources_map).ok();

    let mixin_list = env.new_object(
        obfstr!("java/util/HashSet"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    MIXIN_LIST = env.new_global_ref(mixin_list).ok();

    rt.block_on(async {
        let response = client
            .get(obfstr!("https://api.shorelineclient.net/download"))
            .header(obfstr!("User-Agent"), obfstr!("shoreline-client"))
            .header(obfstr!("Download-Token"), temporary_token)
            .send()
            .await;

        match response
        {
            Ok(res) => {
                let http_response_code = res.status().as_u16();

                match http_response_code {
                    200 => {
                        let bytes = res.bytes().await;
                        let bytes = match bytes {
                            Ok(content) => {
                                content
                            }
                            Err(_) => {
                                error_message(
                                    obfstr!("Failed to load Shoreline.\n\nPlease contact Shoreline support!\n\nError code: 32")
                                );

                                crash(&env, caller_class);

                                return;
                            }
                        };

                        let archive = ZipArchive::new(Cursor::new(bytes));
                        let mut archive = match archive {
                            Ok(zip) => {
                                zip
                            }
                            Err(_) => {
                                error_message(
                                    obfstr!("Failed to load Shoreline.\n\nPlease contact Shoreline support!\n\nError code: 33")
                                );

                                crash(&env, caller_class);

                                return;
                            }
                        };

                        for i in 0..archive.len()
                        {
                            let file = archive.by_index(i);
                            let mut file = match file {
                                Ok(zip) => {
                                    zip
                                }
                                Err(_) => {
                                    error_message(
                                        obfstr!("Failed to load Shoreline.\n\nPlease contact Shoreline support!\n\nError code: 34")
                                    );

                                    crash(&env, caller_class);

                                    return;
                                }
                            };

                            let name = file.name().to_string();

                            let mut buffer = Vec::new();
                            let copy = std::io::copy(&mut file, &mut buffer);
                            match copy {
                                Ok(_) => {

                                }
                                Err(_) => {
                                    error_message(
                                        obfstr!("Failed to load Shoreline.\n\nPlease contact Shoreline support!\n\nError code: 35")
                                    );

                                    crash(&env, caller_class);

                                    return;
                                }
                            }

                            let jvm_name = env.new_string(name.clone()).unwrap();
                            let jvm_bytes = JObject::from(env.byte_array_from_slice(&buffer).unwrap());

                            if name.ends_with(obfstr!(".class"))
                            {
                                if is_mixin_class(&env, jvm_bytes)
                                {
                                    env.call_method(
                                        mixin_list,
                                        obfstr!("add"),
                                        obfstr!("(Ljava/lang/Object;)Z"),
                                        &[jvm_name.into()]
                                    ).unwrap().z().unwrap();
                                }

                                let dependents = get_immediate_dependents(&env, jvm_bytes);

                                if dependents.iter().any(|s| s.starts_with(obfstr!("net/minecraft/")))
                                {
                                    cache_queue.push_back((jvm_name, jvm_bytes));

                                    env.call_method(
                                        mc_class_dependents,
                                        obfstr!("put"),
                                        obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                                        &[jvm_name.into(), jvm_bytes.into()]
                                    ).unwrap();
                                } else if is_mixin_class(&env, jvm_bytes)
                                {
                                    cache_queue.push_back((jvm_name, jvm_bytes));

                                    if is_mixin_accessor(&env, jvm_bytes)
                                    {
                                        class_queue.push_back((jvm_name, jvm_bytes));
                                    }
                                } else if is_imixin_class(&env, jvm_bytes)
                                {
                                    cache_queue.push_back((jvm_name, jvm_bytes));
                                    class_queue.push_back((jvm_name, jvm_bytes));
                                } else
                                {
                                    // Cache it if it is mentioned in a Mixin
                                    if name.contains(obfstr!("net/shoreline/client/nT")) // Globals
                                        || name.contains(obfstr!("net/shoreline/client/it")) // InteractType
                                        || name.contains(obfstr!("net/shoreline/client/ct")) // CapeManager$CapeTexture
                                        || name.contains(obfstr!("net/shoreline/client/lc")) // RenderLayersClient
                                    {
                                        cache_queue.push_back((jvm_name, jvm_bytes));
                                    }

                                    // Define it!
                                    class_queue.push_back((jvm_name, jvm_bytes));
                                }
                            } else if name.starts_with(obfstr!("assets/"))
                            {
                                env.call_method(
                                    resources_map,
                                    obfstr!("put"),
                                    obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
                                    &[jvm_name.into(), jvm_bytes.into()]
                                ).unwrap();
                            } else if name.eq(obfstr!("shoreline-refmap.json"))
                            {
                                MIXIN_REFMAP = env.new_global_ref(jvm_bytes).ok();
                            }
                        }
                    }
                    401 => {
                        // Either the token is expired, or it's incorrect
                        // Probably doesn't mean anyone is trying to crack
                        error_message(
                            obfstr!("Error: INVALID\n\nIf this issue persists, please contact Shoreline support.")
                        );

                        crash(&env, caller_class);
                    }
                    _ => {
                        let msg = format!(
                            "{}{}{}",
                            obfstr!("Internal server error "),
                            http_response_code,
                            obfstr!(".\n\nPlease contact Shoreline support!")
                        );

                        error_message(&msg);

                        crash(&env, caller_class);
                    }
                }
            }
            Err(_) => {
                error_message(
                    obfstr!("Failed to connect to Shoreline servers.\n\nPlease contact Shoreline support!")
                );

                crash(&env, caller_class);
            }
        }
    });

    let mut dependency_map = HashMap::new();
    let mut bytes_map = HashMap::new();

    for (name, bytes) in class_queue.iter()
    {
        let rs_name = env.get_string((*name).into()).unwrap().to_str().unwrap().to_string();
        let mut dependencies = get_immediate_dependents(&env, *bytes);

        dependencies.retain(|s| s.starts_with(obfstr!("net/shoreline/client/")));

        dependency_map.insert(rs_name.clone(), dependencies);
        bytes_map.insert(rs_name.clone(), bytes);
    }

    let mut event_extending_classes: Vec<String> = Vec::new();

    // Event.class
    event_extending_classes.push(obfstr!("net/shoreline/client/fR").to_string());

    while !dependency_map.is_empty()
    {
        let mut defined_this_iteration = Vec::new();

        for (class_name, dependencies) in dependency_map.iter()
        {
            if dependencies.is_empty()
            {
                let bytes = *bytes_map.remove(class_name).unwrap();

                if class_name.starts_with(obfstr!("net/shoreline/client/fR")) // Event.class
                    || class_name.starts_with(obfstr!("net/shoreline/client/es")) // StageEvent$EventStage.class
                {
                    cache_queue.push_back((env.new_string(class_name).unwrap(), bytes))
                }

                let dependents = get_immediate_dependents(&env, bytes);

                let mut is_event_class: bool = false;
                for event_extending_class in event_extending_classes.iter()
                {
                    if dependents.iter().any(|s| s.starts_with(event_extending_class))
                    {
                        cache_queue.push_back((env.new_string(class_name).unwrap(), bytes));
                        is_event_class = true;
                    }
                }

                let clazz = define_class(&env, class_name, bytes);

                defined_this_iteration.push(class_name.clone());

                if is_event_class
                {
                    event_extending_classes.push(class_name.clone().replace(obfstr!(".class"), obfstr!("")));
                    eventbus::cache_event_class(&env, clazz);
                }

                // Can't do enums since the JVM uses reflection to find their value
                if !dependents.iter().any(|s| s.starts_with(obfstr!("java/lang/Enum")))
                    && !class_name.starts_with("net/shoreline/client/el")
                    // Can't do EventListener because we call methods from it natively which requires reflection
                {
                    // Add it to the reflection filter map
                    env.call_static_method(
                        caller_class,
                        obfstr!("i"),
                        obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                        &[clazz.into()]
                    ).unwrap().l().unwrap();
                }
            }
        }

        for defined_class in defined_this_iteration.iter()
        {
            dependency_map.remove(defined_class);
        }

        for dependencies in dependency_map.values_mut()
        {
            dependencies.retain(|dep| !defined_this_iteration.contains(dep));
        }
    }

    for (name, bytes) in cache_queue.iter()
    {
        env.call_method(
            class_map,
            obfstr!("put"),
            obfstr!("(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            &[(*name).into(), (*bytes).into()]
        ).unwrap().l().unwrap();
    }

    return class_map;
}

/**
 * Define all late-loading MC extending classes
 */
#[export_name = "Java_net_shoreline_loader_Natives_e"]
pub unsafe extern "system" fn define_late_loading_classes<'a>(env: JNIEnv<'a>,
                                                              caller_class: JClass<'a>,
                                                              _unused_obscure: JObject<'a>) -> JObject<'a>
{
    return match LATE_LOADING_CLASSES.as_ref().take()
    {
        Some(class_cache) =>
            {
                while !env.call_method(
                    class_cache,
                    obfstr!("isEmpty"),
                    obfstr!("()Z"),
                    &[]
                ).unwrap().z().unwrap() {
                    let hashmap_entryset = env.call_method(
                        class_cache,
                        obfstr!("entrySet"),
                        obfstr!("()Ljava/util/Set;"),
                        &[]
                    ).unwrap().l().unwrap();

                    let hashmap_iter = env.call_method(
                        hashmap_entryset,
                        obfstr!("iterator"),
                        obfstr!("()Ljava/util/Iterator;"),
                        &[]
                    ).unwrap().l().unwrap();

                    let next = env.call_method(
                        hashmap_iter,
                        obfstr!("next"),
                        obfstr!("()Ljava/lang/Object;"),
                        &[]
                    ).unwrap().l().unwrap();

                    let key = JString::from(env.call_method(
                        next,
                        obfstr!("getKey"),
                        obfstr!("()Ljava/lang/Object;"),
                        &[]
                    ).unwrap().l().unwrap());

                    let value = env.call_method(
                        class_cache,
                        obfstr!("remove"),
                        obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                        &[key.into()]
                    ).unwrap().l().unwrap();

                    let name_ptr = env.get_string_utf_chars(key).unwrap();
                    let name = CStr::from_ptr(name_ptr).to_str().unwrap();

                    let dependents = get_immediate_dependents(&env, value);

                    let clazz = define_class(&env, name, value);

                    // Can't do enums since the JVM uses reflection to find their value
                    if !dependents.iter().any(|s| s.starts_with(obfstr!("java/lang/Enum")))
                    {
                        // Add it to the reflection filter map
                        env.call_static_method(
                            caller_class,
                            obfstr!("i"),
                            obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                            &[clazz.into()]
                        ).unwrap().l().unwrap();
                    }

                    env.release_string_utf_chars(key, name_ptr).unwrap();
                }

                JObject::null()
            },
        None => {
            error_message(obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: 4"));

            crash(&env, caller_class);

            JObject::null()
        }
    }
}

/**
 * Return the user information retrieved earlier
 */
#[export_name = "Java_net_shoreline_loader_Natives_f"]
pub unsafe extern "system" fn get_user_information<'a>(env: JNIEnv<'a>,
                                                       caller_class: JClass<'a>,
                                                       _unused_obscure: JObject<'a>) -> JObject<'a>
{
    return match USER_INFO.as_ref().take()
    {
        Some(user_info_ref) => user_info_ref.as_obj(),
        None => {
            error_message(obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: 5"));

            crash(&env, caller_class);

            JObject::null()
        }
    }
}

/**
 * Confirm with the server that the loader versions match
 */
#[export_name = "Java_net_shoreline_loader_Natives_g"]
pub unsafe extern "system" fn version_check<'a>(env: JNIEnv<'a>,
                                                caller_class: JClass<'a>,
                                                loader_version: JObject<'a>) -> JObject<'a>
{
    let loader_version_utf_chars = env.get_string_utf_chars(JString::from(loader_version)).unwrap();
    let loader_version_cstr = CStr::from_ptr(loader_version_utf_chars).to_str().unwrap();

    let client = &CLIENT;

    let rt = Runtime::new().unwrap();

    rt.block_on(async {

        let response = client
            .get(obfstr!("https://api.shorelineclient.net/version"))
            .header(obfstr!("User-Agent"), obfstr!("shoreline-client"))
            .header(obfstr!("Current-Version"), loader_version_cstr)
            .send()
            .await;

        match response
        {
            Ok(res) => {
                let http_response_code = res.status().as_u16();

                match http_response_code
                {
                    200 => {
                        return;
                    }
                    409 => {
                        error_message(
                            obfstr!("Your Shoreline loader is out of date!\n\nPlease install the latest version via the installer.")
                        );

                        crash(&env, caller_class);
                    }
                    _ => {
                        let msg = format!(
                            "{}{}{}",
                            obfstr!("Internal server error "),
                            http_response_code,
                            obfstr!(".\n\nPlease contact Shoreline support!")
                        );

                        error_message(&msg);

                        crash(&env, caller_class);
                    }
                }
            }
            Err(_) => {
                error_message(
                    obfstr!("Failed to connect to Shoreline servers.\n\nPlease contact Shoreline support!")
                );

                crash(&env, caller_class);
            }
        }
    });

    return JObject::null();
}

/**
 * Alerts the webhook and crashes
 */

#[export_name = "Java_net_shoreline_loader_Natives_h"]
pub unsafe extern "system" fn alert_webhook_and_crash<'a>(env: JNIEnv<'a>,
                                                          caller_class: JClass<'a>,
                                                          param_array: JObject<'a>) -> JObject<'a>
{
    alert_webhook(
        &env,
        param_array
    );

    crash(&env, caller_class);

    return JObject::null();
}

/**
 * Adds the class to the reflection filter map
 */

#[export_name = "Java_net_shoreline_loader_Natives_i"]
pub unsafe extern "system" fn disable_reflection<'a>(env: JNIEnv<'a>,
                                                     _caller_class: JClass<'a>,
                                                     class: JObject<'a>) -> JObject<'a>
{
    let wildcard = env.new_string("*").unwrap();

    let set = env.call_static_method(
        env.find_class(obfstr!("java/util/Set")).unwrap(),
        obfstr!("of"),
        obfstr!("(Ljava/lang/Object;)Ljava/util/Set;"),
        &[wildcard.into()]
    ).unwrap().l().unwrap();

    env.call_static_method(
        env.find_class(obfstr!("jdk/internal/reflect/Reflection")).unwrap(),
        obfstr!("registerFieldsToFilter"),
        obfstr!("(Ljava/lang/Class;Ljava/util/Set;)V"),
        &[class.into(), set.into()]
    ).unwrap().v().unwrap();

    env.call_static_method(
        env.find_class(obfstr!("jdk/internal/reflect/Reflection")).unwrap(),
        obfstr!("registerMethodsToFilter"),
        obfstr!("(Ljava/lang/Class;Ljava/util/Set;)V"),
        &[class.into(), set.into()]
    ).unwrap().v().unwrap();

    return JObject::null();
}

/**
 * Run antidump checks
 */

#[export_name = "Java_net_shoreline_loader_Natives_j"]
pub unsafe extern "system" fn run_anti_dump<'a>(env: JNIEnv<'a>,
                                                _caller_class: JClass<'a>,
                                                _information_array: JObject<'a>) -> JObject<'a>
{

    // let antidump_check_result = run_antidump_checks();
    //
    // if !antidump_check_result.eq(obfstr!("Safe"))
    // {
    //     error_message(
    //         obfstr!("We know what you did")
    //     );
    //
    //     let message = env.new_string(antidump_check_result).unwrap();
    //
    //     env.set_object_array_element(*information_array, 0, message).unwrap();
    //
    //     env.call_static_method(
    //         caller_class,
    //         obfstr!("h"),
    //         obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
    //         &[information_array.into()]
    //     ).unwrap().l().unwrap();
    //
    //     crash(&env, caller_class);
    // }

    return JObject::null();
}

/**
 * Gets a resource from native memory
 */
#[export_name = "Java_net_shoreline_loader_Natives_k"]
pub unsafe extern "system" fn get_resource<'a>(env: JNIEnv<'a>,
                                               caller_class: JClass<'a>,
                                               resource_name: JObject<'a>) -> JObject<'a>
{
    return match RESOURCES_MAP.as_mut()
    {
        Some(resources_map) => {
            return env.call_method(
                resources_map.as_obj(),
                obfstr!("get"),
                obfstr!("(Ljava/lang/Object;)Ljava/lang/Object;"),
                &[resource_name.into()]
            ).unwrap().l().unwrap()
        },
        None => {
            error_message(obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: 10"));

            crash(&env, caller_class);

            JObject::null()
        }
    }
}

/**
 * Check for Fabric API
 */
#[export_name = "Java_net_shoreline_loader_Natives_l"]
pub unsafe extern "system" fn check_fabric_api_presence<'a>(env: JNIEnv<'a>,
                                                            caller_class: JClass<'a>,
                                                            _unused_obscure: JObject<'a>) -> JObject<'a>
{
    let fabric_api_class = env.find_class(
        obfstr!("net/fabricmc/fabric/api/resource/ModResourcePack")
    );

    if env.exception_check().unwrap()
    {
        env.exception_clear().unwrap();

        error_message(
            obfstr!("Shoreline depends on Fabric API.\n\nPlease install it from the Fabric website!")
        );

        crash(&env, caller_class);
    }

    fabric_api_class.unwrap();

    return JObject::null();
}

#[export_name = "Java_net_shoreline_loader_Natives_m"]
pub unsafe extern "system" fn return_loader_bytecode_4_mixins<'a>(env: JNIEnv<'a>,
                                                                  caller_class: JClass<'a>,
                                                                  _unused_obscure: JObject<'a>) -> JObject<'a>
{
   return match LOADER_CLASS_BYTECODE.as_ref().take()
    {
        Some(bytecode) => bytecode.as_obj(),
        None => {
            error_message(
                obfstr!("An internal error has occurred.\n\nPlease report this to a Shoreline developer!\n\nError code: 17")
            );

            crash(&env, caller_class);

            JObject::null()
        }
    }
}