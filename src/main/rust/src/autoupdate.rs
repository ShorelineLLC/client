use std::ffi::CStr;
use std::fs::File;
use std::path::Path;
use jni::objects::JObject;
use jni::JNIEnv;
use obfstr::obfstr;
use crate::network;

pub async unsafe fn download_loader(path: &str) -> Result<(), String>

{
    let server_address = obfstr!("https://api.shorelineclient.dev/update").to_string();

    return match network::verify_server_integrity(&server_address)
    {
        Ok(()) => {
            let client = network::get_client();

            let response = client
                .get(&server_address)
                .header(obfstr!("User-Agent"), obfstr!("shoreline-client"))
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

            match http_response_code
            {
                200 => {
                    let bytes = response.bytes().await;

                    let bytes = match bytes
                    {
                        Ok(content) => content,
                        Err(_) => {
                            return Err(obfstr! {
                                "Couldn't read the content of the updated loader. \
                                Please report this to a developer."
                            }.to_string())
                        }
                    };

                    let output_path = Path::new(path);
                    let file = File::create(output_path);

                    let mut file = match file
                    {
                        Ok(file) => file,
                        Err(e) => {
                            let base_msg = obfstr! {
                                "Failed to create the file for the updated loader. \
                                If this issue persists, please contact a developer.\n\nError:"
                            }.to_string();

                            let error_msg = e.to_string();

                            return Err(format!("{}{}", base_msg, error_msg));
                        }
                    };

                    return match std::io::copy(&mut bytes.as_ref(), &mut file)
                    {
                        Ok(_) => Ok(()),
                        Err(e) => {
                            let base_msg = obfstr! {
                                "Failed to copy into the file for the updated loader. \
                                If this issue persists, please contact a developer.\n\nError:"
                            }.to_string();

                            let error_msg = e.to_string();

                            return Err(format!("{}{}", base_msg, error_msg));
                        }
                    }
                }

                _ => {
                    let formatted = format! {
                        "{}{}{}",
                        obfstr!("Internal server error code 0x1"),
                        http_response_code,
                        obfstr!(". Please report this to a developer.")
                    };

                    Err(formatted)
                }
            }
        }

        Err(e) => Err(e)
    }
}