use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString};
use obfstr::obfstr;
use crate::obfuscation;

pub unsafe fn info(env: &mut JNIEnv,
                   msg: &str)
{
    let java_msg = env.new_string(msg).unwrap();

    let loader_class = obfuscation::get_loader_class(env);
    let info_function = obfuscation::INFO_FUNCTION.get_name();

    env.call_static_method(
        loader_class,
        info_function,
        obfstr!("(Ljava/lang/String;)V"),
        &[(&java_msg).into()]
    ).unwrap().v().unwrap();
}

pub unsafe fn error(env: &mut JNIEnv,
                    msg: &str)
{
    while env.exception_check().unwrap()
    {
        env.exception_describe().unwrap();
        env.exception_clear().unwrap();
    }

    let java_msg = env.new_string(msg).unwrap();

    let loader_class = obfuscation::get_loader_class(env);
    let error_function = obfuscation::ERROR_FUNCTION.get_name();

    env.call_static_method(
        loader_class,
        error_function,
        obfstr!("(Ljava/lang/String;)V"),
        &[(&java_msg).into()]
    ).unwrap().v().unwrap();
}

pub unsafe extern "system" fn show_error_window(mut env: JNIEnv,
                                                _class: JClass,
                                                message: JObject)
{
    if env.exception_check().unwrap()
    {
        env.exception_clear().unwrap();
    }

    let str_message = JString::from(message);

    let java_str = env.get_string(&str_message).unwrap();
    let rs_str = java_str.to_str().unwrap();

    display_error_msg(rs_str);
}

pub unsafe fn display_info_msg(msg: &str)
{
    platform::display_info_msg(msg);
}

pub unsafe fn display_error_msg(msg: &str)
{
    platform::display_error_msg(msg);
}

pub unsafe fn display_confirmation_msg(msg: &str) -> bool
{
    platform::display_confirmation_message(msg)
}

#[cfg(target_os = "windows")]
mod platform
{
    use std::iter::once;
    use std::ptr::null_mut;
    use obfstr::obfstr;
    use winapi::um::winuser::{MB_ICONERROR, MB_ICONINFORMATION, MB_ICONQUESTION, MB_OK, MB_SYSTEMMODAL, MB_YESNO, MessageBoxW};

    pub unsafe fn display_info_msg(msg: &str)
    {
        let text: Vec<u16> = msg.encode_utf16().chain(once(0)).collect();
        let caption: Vec<u16> = obfstr!("Shoreline").encode_utf16().chain(once(0)).collect();
        let window_type = MB_OK | MB_ICONINFORMATION;

        MessageBoxW(
            null_mut(),
            text.as_ptr(),
            caption.as_ptr(),
            window_type
        );
    }

    pub unsafe fn display_error_msg(msg: &str)
    {
        let text: Vec<u16> = msg.encode_utf16().chain(once(0)).collect();
        let caption: Vec<u16> = obfstr!("Shoreline").encode_utf16().chain(once(0)).collect();
        let window_type = MB_OK | MB_ICONERROR;

        MessageBoxW(
            null_mut(),
            text.as_ptr(),
            caption.as_ptr(),
            window_type
        );
    }

    pub unsafe fn display_confirmation_message(msg: &str) -> bool
    {
        let text: Vec<u16> = msg.encode_utf16().chain(once(0)).collect();
        let caption: Vec<u16> = obfstr!("Shoreline").encode_utf16().chain(once(0)).collect();
        let window_type = MB_YESNO | MB_ICONQUESTION;

        let result = MessageBoxW(
            null_mut(),
            text.as_ptr(),
            caption.as_ptr(),
            window_type
        );

        return match result
        {
            winapi::um::winuser::IDYES => true,
            winapi::um::winuser::IDNO => false,
            _ => false
        }
    }
}

#[cfg(target_os = "macos")]
mod platform
{
    pub fn display_error_msg(msg: String)
    {
        std::panic!("not implemented");
    }
}

#[cfg(target_os = "linux")]
mod platform
{
    pub fn display_error_msg(msg: String)
    {
        std::panic!("not implemented");
    }
}