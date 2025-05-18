use std::net::TcpStream;
use crypto::digest::Digest;
use crypto::sha2::Sha256;
use hyper_rustls::ConfigBuilderExt;
use lazy_static::lazy_static;
use nom::AsBytes;
use obfstr::obfstr;
use reqwest::Client;
use rustls_connector::{rustls, RustlsConnector};
use serde_json::Value;
use tokio::runtime::Runtime;
use url::Url;

lazy_static!
{
    static ref CLIENT: Client = Client::builder()
        .cookie_store(true)
        .build()
        .unwrap();

    static ref SHORELINE_CERT_SHA256: String = obfstr! {
        "d32a4502efa10564457b9493bf1613eeb35304116ca088c6f567f758f030fe2d"
    }.to_string();

    static ref WE1_CERT_SHA256: String = obfstr! {
        "1dfc1605fbad358d8bc844f76d15203fac9ca5c1a79fd4857ffaf2864fbebf96"
    }.to_string();

    static ref GTS_ROOT_R4_CERT_SHA256: String = obfstr! {
        "76b27b80a58027dc3cf1da68dac17010ed93997d0b603e2fadbe85012493b5a7"
    }.to_string();
}

pub fn get_client<'a>() -> &'a Client
{
    return &CLIENT;
}

pub fn verify_server_integrity(domain: &str) -> Result<(), String>
{
    confirm_server_integrity(domain)
}

fn confirm_server_integrity(domain: &str) -> Result<(), String>
{
    let url_parsed = match Url::parse(domain)
    {
        Ok(url) => url,
        Err(_) => return Err(obfstr! {
            "Shoreline couldn't verify the authenticity of the server connection. Code: 0x1.\n\n
            If this issue persists, contact a developer."
        }.to_string())
    };

    let config = rustls::ClientConfig::builder()
        .with_native_roots();

    let config = match config
    {
        Ok(config ) => config,
        Err(_) => return Err(obfstr! {
            "Shoreline couldn't verify the authenticity of the server connection. Code: 0x2.\n\n
            If this issue persists, contact a developer."
        }.to_string())
    }.with_no_client_auth();

    let connector = RustlsConnector::from(config);

    // connect
    let result = match url_parsed.socket_addrs(|| Some(443))
    {
        Ok(res) => res,
        Err(_) => return Err(obfstr! {
            "Shoreline couldn't verify the authenticity of the server connection. Code: 0x3.\n\n
            If this issue persists, contact a developer."
        }.to_string())
    };

    let stream = match TcpStream::connect(result[0])
    {
        Ok(res) => res,
        Err(_) => return Err(obfstr! {
            "Shoreline couldn't verify the authenticity of the server connection. Code: 0x4.\n\n
            Please make sure that your network is configured for IPv4. If you are having trouble \
            or the issue is persisting, contact a developer."
        }.to_string())
    };

    let host_str = match url_parsed.host_str()
    {
        Some(res) => res,
        None => return Err(obfstr! {
            "Shoreline couldn't verify the authenticity of the server connection. Code: 0x5.\n\n
            If this issue persists, contact a developer."
        }.to_string())
    };

    let stream = match connector.connect(host_str, stream)
    {
        Ok(res) => res,
        Err(_) => return Err(obfstr! {
            "Shoreline couldn't verify the authenticity of the server connection. Code: 0x6.\n\n
            If this issue persists, contact a developer."
        }.to_string())
    };

    // get certs
    let certs = match stream.conn.peer_certificates()
    {
        Some(certs) => certs,
        None => return Err(obfstr! {
            "Shoreline couldn't verify the authenticity of the server connection. Code: 0x7.\n\n
            If this issue persists, contact a developer."
        }.to_string())
    };

    let mut retrieved = Vec::new();

    let mut sha256 = Sha256::new();
    for c in certs
    {
        sha256.input(c.as_ref().as_bytes());
        retrieved.push(sha256.result_str());
        sha256.reset()
    }

    if !compare_certs(retrieved)
    {
        return Err(obfstr! {
            "Shoreline couldn't verify the authenticity of the server connection.\n\n\
            If you are using a proxy, VPN, or network debugger, disable it until Shoreline \
            has finished loading. If the issue persists, contact a developer."
        }.to_string())
    }

    Ok(())
}

fn compare_certs(retrieved: Vec<String>) -> bool
{
    let mut valid_certs = Vec::new();

    valid_certs.push(SHORELINE_CERT_SHA256.clone());
    valid_certs.push(WE1_CERT_SHA256.clone());
    valid_certs.push(GTS_ROOT_R4_CERT_SHA256.clone());

    if valid_certs.len() != retrieved.len()
    {
        return false;
    }

    for n in 0..valid_certs.len()
    {
        if !retrieved.contains(valid_certs.get(n).unwrap())
        {
            return false;
        }
    }

    return true;
}