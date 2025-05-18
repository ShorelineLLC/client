use std::fs::File;
use std::io::Read;
use hardware_id::get_id;
use jni::JNIEnv;
use obfstr::obfstr;
use reqwest::header::{CONTENT_TYPE, HeaderMap};
use serde::Deserialize;
use serde_json::json;
use tokio::runtime::Runtime;
use crate::{crypto, network, notifs};

#[derive(Deserialize)]
struct Auth
{
    #[serde(rename = "Username")]
    username: String,
    #[serde(rename = "Password")]
    password: String,
}

#[derive(Deserialize)]
pub struct User
{
    #[serde(rename = "Hardware-ID")]
    pub(crate) hwid: String,
    #[serde(rename = "Username")]
    pub(crate) username: String,
    #[serde(rename = "UID")]
    pub(crate) uid: String,
    #[serde(rename = "User-Type")]
    pub(crate) usertype: String,
    #[serde(rename = "IRC-Token")]
    pub(crate) irc_token: String
}

pub unsafe fn login() -> Result<User, String>
{
    return match read_auth_file()
    {
        Ok(content) => {
            let decrypted_content = match crypto::decrypt(content)
            {
                Ok(content) => content,
                Err(_) => {
                    return Err(obfstr! {
                        "Your Shoreline authorization is corrupted. \
                        Please re-login to the Installer."
                    }.to_string())
                }
            };

            let decrypted_credentials = match std::str::from_utf8(decrypted_content.as_slice())
            {
                Ok(str) => str,
                Err(_) => {
                    return Err(obfstr! {
                        "Your Shoreline authorization is corrupted. \
                        Please re-login to the Installer."
                    }.to_string())
                }
            };

            let auth = match serde_json::from_str::<Auth>(decrypted_credentials)
            {
                Ok(auth) => auth,
                Err(_) => {
                    return Err(obfstr! {
                        "Your Shoreline authorization is corrupted. \
                        Please re-login to the Installer."
                    }.to_string())
                }
            };

            let hwid = match get_id()
            {
                Ok(hwid ) => crypto::encrypt_irreversible(hwid.as_str()),
                Err(_) => {
                    return Err(obfstr! {
                        "Failed to retrieve your computer information. \
                        Please report this to a developer."
                    }.to_string())
                }
            };

            login_internal(auth, hwid)
        }

        Err(e) => {
            Err(e)
        }
    }
}

fn login_internal(auth: Auth,
                  hwid: String) -> Result<User, String>
{
    let server_address = obfstr!("https://api.shorelineclient.dev/login").to_string();

    match network::verify_server_integrity(&server_address)
    {
        Ok(()) => {
            let request = json!({
                obfstr!("Username"): auth.username,
                obfstr!("Password"): auth.password,
                obfstr!("Hardware-ID"): hwid
            });

            let runtime = Runtime::new().unwrap();

            runtime.block_on(async {
                let response = network::get_client()
                    .post(&server_address)
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
                                    "Internal server error. Code 0x00. \
                                    Please report this to a developer."
                            }.to_string())
                        }

                        let response_text = response.text().await;

                        let content = match response_text
                        {
                            Ok(content) => content,
                            Err(_) => {
                                return Err(obfstr! {
                                    "Internal server error. Code 0x01. \
                                    Please report this to a developer."
                                }.to_string())
                            }
                        };

                        let user = match serde_json::from_str::<User>(content.as_str())
                        {
                            Ok(user) => user,
                            Err(_) => {
                                return Err(obfstr! {
                                    "Internal server error. Code 0x02. \
                                    Please report this to a developer."
                                }.to_string())
                            }
                        };

                        Ok(user)
                    }

                    401 => {
                        Err(obfstr! {
                            "Your saved login credentials are invalid. If you recently changed \
                            your password, you will need to re-login to the Installer."
                        }.to_string())
                    }

                    403 => {
                        Err(obfstr! {
                            "You are not authorized to use Shoreline on this computer. \
                            If you believe that this is an issue, please contact support."
                        }.to_string())
                    }

                    406 => {
                        Err(obfstr! {
                            "Your account has been permanently banned. \
                            If you believe this is a mistake, please contact support."
                        }.to_string())
                    }

                    _ => {
                        let formatted = format! {
                            "{}{}{}",
                            obfstr!("Internal server error code 0x0"),
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

fn read_auth_file() -> Result<Vec<u8>, String>
{
    let mut home_dir = match home::home_dir()
    {
        Some(dir) => dir,
        None => return Err(obfstr! {
            "Shoreline was unable to read your home directory. \
            Please report this to a developer!"
        }.to_string()),
    };

    home_dir.push(obfstr!("Shoreline"));
    home_dir.push(obfstr!("shoreline.auth"));

    if !home_dir.exists()
    {
        return Err(obfstr! {
            "You are not logged in to Shoreline. \
            If you own the client, please log in from the Installer. \
            If not, purchase your own copy at shorelineclient.dev!"
        }.to_string());
    }

    if !home_dir.is_file()
    {
        return Err(obfstr! {
            "Your Shoreline login credentials are corrupted. \
            Please re-login from the Installer."
        }.to_string());
    }

    let mut file = match File::open(&home_dir)
    {
        Ok(f) => f,
        Err(e) => {
            let base_msg = obfstr! {
                "Failed to open your Shoreline login credentials. \
                If this issue persists, please re-login from the Installer.\n\nError:"
            }.to_string();

            let error_msg = e.to_string();

            return Err(format!("{}{}", base_msg, error_msg));
        }
    };

    let mut content = Vec::new();
    if let Err(e) = file.read_to_end(&mut content)
    {
        let base_msg = obfstr! {
                "Failed to read your Shoreline login credentials. \
                If this issue persists, please re-login from the Installer.\n\nError:"
            }.to_string();

        let error_msg = e.to_string();

        return Err(format!("{}{}", base_msg, error_msg));
    }

    Ok(content)
}

pub trait HeaderMapExt
{
    fn is_json(&self) -> bool;
}

impl HeaderMapExt for HeaderMap
{
    fn is_json(&self) -> bool
    {
        if let Some(content_type) = self.get(CONTENT_TYPE)
        {
            if let Ok(value) = content_type.to_str()
            {
                return value.eq_ignore_ascii_case(obfstr!("application/json"));
            }
        }
        false
    }
}