use std::collections::HashMap;
use std::io::Cursor;
use std::process::exit;
use jni::JNIEnv;
use jni::sys::jbyteArray;
use obfstr::obfstr;
use serde::Deserialize;
use serde_json::json;
use tokio::runtime::Runtime;
use zip::ZipArchive;
use crate::{decryptor, integrity, network, notifs};
use crate::login::{HeaderMapExt, User};

#[derive(Deserialize)]
struct Token
{
    #[serde(rename = "Token")]
    token: String,
}

pub struct Payload
{
    // Class bytecode we can normally define
    pub(crate) class_bytecode: HashMap<String, Vec<u8>>,
    // Resource bytecode, the bytes of resource files and assets
    pub(crate) resource_bytecode: HashMap<String, Vec<u8>>,
    // Mixin bytecode, the bytecode that Mixin must have access to
    pub(crate) mixin_bytecode: HashMap<String, Vec<u8>>,
    // Bytecode of late-loading, mc extending classes
    pub(crate) late_loading_bytecode: HashMap<String, Vec<u8>>,
    // The list of mixins
    pub(crate) mixin_list: Option<Vec<String>>,
    // The mixin refmap
    pub(crate) refmap: Option<Vec<u8>>,
    // The access widener
    pub(crate) access_widener: Option<Vec<u8>>,
    // The decryption keys
    pub(crate) keys: HashMap<i64, i64>
}

pub unsafe fn request_token(env: &mut JNIEnv) -> Result<String, String>
{
    let loader_hash = match integrity::get_loader_hash(env)
    {
        Ok(hash) => hash,
        Err(_) => {
            return Err(obfstr! {
                "Failed to read the contents of your loader jar. \
                Please report this to a developer!"
            }.to_string())
        }
    };

    return match request_token_internal(loader_hash)
    {
        Ok(token) => Ok(token.token),
        Err(e) => Err(e)
    }
}

fn request_token_internal(loader_hash: String) -> Result<Token, String>
{
    let server_address = obfstr!("https://api.shorelineclient.dev/gentoken").to_string();

    match network::verify_server_integrity(&server_address)
    {
        Ok(()) => {
            let request = json!({
                obfstr!("Loader-Hash"): loader_hash,
            });

            let runtime = Runtime::new().unwrap();

            runtime.block_on(async {
                let response = network::get_client()
                    .get(&server_address)
                    .header(obfstr!("User-Agent"), obfstr!("shoreline-client"))
                    .header(obfstr!("Content-Type"), obfstr!("application/json"))
                    .json(&request)
                    .send()
                    .await;

                let response = match response
                {
                    Ok(response) => response,
                    Err(_) => {
                        return Err(obfstr! {
                            "Failed to connect to Shoreline servers. Please contact support!"
                        }.to_string());
                    }
                };

                let http_response_code = response.status().as_u16();

                return match http_response_code
                {
                    200 => {
                        if !response.headers().is_json()
                        {
                            return Err(obfstr! {
                                "Internal server error. Code 0x30. \
                                 Please report this to a developer."
                            }.to_string())
                        }

                        let response_text = response.text().await;

                        let content = match response_text
                        {
                            Ok(content) => content,
                            Err(_) => {
                                return Err(obfstr! {
                                    "Internal server error. Code 0x31. \
                                    Please report this to a developer."
                                }.to_string())
                            }
                        };

                        let token = match serde_json::from_str::<Token>(content.as_str())
                        {
                            Ok(token) => token,
                            Err(_) => {
                                return Err(obfstr! {
                                    "Internal server error. Code 0x32. \
                                    Please report this to a developer."
                                }.to_string())
                            }
                        };

                        Ok(token)
                    }

                    406 => {
                        Err(obfstr! {
                            "Your account has been permanently banned for violating the Terms of Service. \
                            If you believe this is a mistake, please contact support."
                        }.to_string())
                    }

                    _ => {
                        let formatted = format! {
                            "{}{}{}",
                            obfstr!("Internal server error code 0x3"),
                            http_response_code,
                            obfstr!(". Please report this to a developer.")
                        };

                        Err(formatted)
                    }
                }
            })
        },
        Err(msg) => return Err(msg)
    }
}

pub unsafe fn download_resources(token: String) -> Result<Payload, String>
{
    return match download_resources_internal(token)
    {
        Ok(payload) => Ok(payload),
        Err(e) => Err(e)
    }
}

fn download_resources_internal(token: String) -> Result<Payload, String>
{
    let server_address = obfstr!("https://api.shorelineclient.dev/loadresources").to_string();

    match network::verify_server_integrity(&server_address)
    {
        Ok(()) => {
            let request = json!({
                obfstr!("Token"): token,
            });

            let runtime = Runtime::new().unwrap();

            runtime.block_on(async {
                let response = network::get_client()
                    .get(&server_address)
                    .header(obfstr!("User-Agent"), obfstr!("shoreline-client"))
                    .header(obfstr!("Content-Type"), obfstr!("application/json"))
                    .json(&request)
                    .send()
                    .await;

                let response = match response
                {
                    Ok(response) => response,
                    Err(_) => {
                        return Err(obfstr! {
                            "Failed to connect to Shoreline servers. Please contact support!"
                        }.to_string());
                    }
                };

                let http_response_code = response.status().as_u16();

                return match http_response_code
                {
                    200 => unsafe {
                        let bytes = response.bytes().await;

                        let bytes = match bytes
                        {
                            Ok(content) => content,
                            Err(_) => {
                                return Err(obfstr! {
                                    "Couldn't read Shoreline's downloaded resources. Code 0x40. \
                                    Please report this to a developer."
                                }.to_string())
                            }
                        };

                        let archive = ZipArchive::new(Cursor::new(bytes));
                        let mut archive = match archive
                        {
                            Ok(zip) => zip,
                            Err(_) => {
                                return Err(obfstr! {
                                    "Couldn't read Shoreline's downloaded resources. Code 0x41. \
                                    Please report this to a developer."
                                }.to_string())
                            }
                        };

                        let mut class_bytecode = HashMap::new();
                        let mut resource_bytecode = HashMap::new();
                        let mut mixin_bytecode = HashMap::new();
                        let mut late_loading_bytecode = HashMap::new();
                        let mut mixin_list = Vec::new();
                        let mut refmap = None;
                        let mut access_widener = None;
                        let mut keys = HashMap::new();

                        for i in 0..archive.len()
                        {
                            let mut file = match archive.by_index(i)
                            {
                                Ok(file) => file,
                                Err(_) => {
                                    return Err(obfstr! {
                                        "Couldn't read Shoreline's downloaded resources. Code 0x42. \
                                        Please report this to a developer."
                                    }.to_string())
                                }
                            };

                            let mut buffer = Vec::new();
                            match std::io::copy(&mut file, &mut buffer)
                            {
                                Ok(_) => {}
                                Err(_) => {
                                    return Err(obfstr! {
                                        "Couldn't read Shoreline's downloaded resources. Code 0x43. \
                                        Please report this to a developer."
                                    }.to_string())
                                }
                            }

                            const DEFINE: u32 = 1 << 0;
                            const CACHE: u32 = 1 << 1;
                            const MIXIN: u32 = 1 << 2;
                            const LATE_LOADING: u32 = 1 << 3;
                            const RESOURCE: u32 = 1 << 4;
                            const REFMAP: u32 = 1 << 5;
                            const ACCESS_WIDENER: u32 = 1 << 6;
                            const KEY_FILE: u32 = 1 << 7;

                            let name = file.name().to_string();

                            let split: Vec<&str> = name.split(',').collect();
                            let flags_str = split.get(split.len() - 1).unwrap();

                            let flags = match flags_str.parse::<u32>()
                            {
                                Ok(flags) => flags,
                                Err(_) => {
                                    return Err(obfstr! {
                                        "Corrupted format on Shoreline's downloaded resources. \
                                        Please report this to a developer."
                                    }.to_string())
                                }
                            };

                            if (flags & DEFINE) != 0
                            {
                                let class_name = split.get(0).unwrap().to_string();
                                let class_name = class_name.replace(obfstr!("/"), obfstr!("."));

                                class_bytecode.insert(class_name, buffer.clone());
                            }

                            if (flags & CACHE) != 0
                            {
                                let class_name = split.get(0).unwrap().to_string();
                                let class_name = class_name.replace(obfstr!("/"), obfstr!("."));

                                mixin_bytecode.insert(class_name, buffer.clone());
                            }

                            if (flags & MIXIN) != 0
                            {
                                let class_name = split.get(0).unwrap().to_string();
                                let mixin_name = class_name.replace(obfstr!("net/shoreline/client/"), "");

                                mixin_list.push(mixin_name);
                            }

                            if (flags & LATE_LOADING) != 0
                            {
                                let class_name = split.get(0).unwrap().to_string();
                                let class_name = class_name.replace(obfstr!("/"), obfstr!("."));

                                late_loading_bytecode.insert(class_name, buffer.clone());
                            }

                            if (flags & RESOURCE) != 0
                            {
                                let resource_name = split.get(0).unwrap().to_string();
                                resource_bytecode.insert(resource_name, buffer.clone());
                            }

                            if (flags & REFMAP) != 0
                            {
                                refmap = Some(buffer.clone());
                            } else if (flags & ACCESS_WIDENER) != 0
                            {
                                access_widener = Some(buffer.clone());
                            }

                            if (flags & KEY_FILE) != 0
                            {
                                let decrypted_key_bytes = match crate::crypto::decrypt(buffer)
                                {
                                    Ok(content) => content,
                                    Err(_) => {
                                        let msg = obfstr! {
                                            "Failed to load the client due to improper decryption. \
                                            Please report this to a developer."
                                        }.to_string();

                                        notifs::display_error_msg(&msg);

                                        exit(-1);
                                    }
                                };

                                let content = String::from_utf8(decrypted_key_bytes).unwrap();

                                let key_pairs: Vec<i64> = content
                                    .split_whitespace()
                                    .map(|s| s.parse::<i64>())
                                    .collect::<Result<Vec<_>, _>>()
                                    .unwrap();

                                for i in (0..key_pairs.len()).step_by(2)
                                {
                                    let key = key_pairs[i];
                                    let value = key_pairs[i + 1];
                                    keys.insert(key, value);
                                }
                            }
                        }

                        match decryptor::LOADER_BYTECODE.take()
                        {
                            Some(hashmap) => {
                                for (class_name, bytes) in hashmap
                                {
                                    let class_name = class_name.replace(obfstr!("/"), obfstr!("."));

                                    mixin_bytecode.insert(class_name, bytes);
                                }
                            }
                            None => {
                            }
                        }

                        Ok(Payload {
                            class_bytecode,
                            mixin_bytecode,
                            resource_bytecode,
                            late_loading_bytecode,
                            mixin_list: Some(mixin_list),
                            refmap,
                            access_widener,
                            keys
                        })
                    }

                    _ => {
                        let formatted = format! {
                            "{}{}{}",
                            obfstr!("Internal server error code 0x4"),
                            http_response_code,
                            obfstr!(". If this issue persists, please report it to a developer.")
                        };

                        Err(formatted)
                    }
                }
            })
        },
        Err(msg) => return Err(msg)
    }
}