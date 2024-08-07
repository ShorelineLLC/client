use std::process::exit;
use aes::Aes128;
use block_padding::Pkcs7;
use cbc::cipher::{BlockDecryptMut, KeyIvInit};
use cbc::Decryptor;
use jni::JNIEnv;
use obfstr::obfstr;
use sha2::{Digest, Sha256};
use crate::notifs;

static KEY: [u8; 16] = [16, 110, 1, 248, 44, 103, 18, 0, 84, 37, 145, 144, 254, 39, 120, 54];

type Aes128Decryptor = Decryptor<Aes128>;

pub fn encrypt_irreversible(str: &str) -> String
{
    let combined_key = format!(
        "{}{}",
        str,
        obfstr!("VJ146naKEtYcwlmxmVwjS9tFEIeFnD6H")); // Secret key to hash our strings.
                                                      // DON'T LOSE THIS OR ALL HWIDS BECOME CORRUPTED!!!

    let mut hasher = Sha256::new();
    Digest::update(&mut hasher, combined_key);

    hex::encode(hasher.finalize())
}

pub fn decrypt(bytes: Vec<u8>) -> Result<Vec<u8>, String>
{
    if bytes.len() <= 16
    {
        return Err(obfstr!("invalid length").to_string());
    }

    let (iv_key, data) = bytes.split_at(16);
    let cipher = match Aes128Decryptor::new_from_slices(&KEY, iv_key)
    {
        Ok(cipher) => cipher,
        Err(e) => return Err(e.to_string())
    };

    let buf_size = bytes.len();
    let mut buf = vec![0u8; buf_size];

    return match cipher.decrypt_padded_b2b_mut::<Pkcs7>(data, &mut buf)
    {
        Ok(content) => Ok(content.to_vec()),
        Err(e) => Err(e.to_string())
    };
}