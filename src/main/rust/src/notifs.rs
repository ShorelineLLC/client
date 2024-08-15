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
    platform::display_confirmation_msg(msg)
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

    pub unsafe fn display_confirmation_msg(msg: &str) -> bool
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
    use cocoa::base::{id, nil};
    use cocoa::foundation::NSString;
    use obfstr::obfstr;
    use objc::{class, msg_send};
    use crate::notifs::platform::NSAlertStyle::{Critical, Informational};
    use objc::sel;
    use objc::sel_impl;

    pub unsafe fn display_info_msg(msg: &str)
    {
        let alert = NSAlert::alloc(nil).init().autorelease();

        alert.addButton(NSString::alloc(nil).init_str(obfstr!("OK")));
        alert.setMessageText(NSString::alloc(nil).init_str(obfstr!("Shoreline")));
        alert.setInformativeText(NSString::alloc(nil).init_str(msg));
        alert.setAlertStyle(Informational);
        alert.setWindowLevel(10);
        alert.runModal();
    }

    pub unsafe fn display_error_msg(msg: &str)
    {
        let alert = NSAlert::alloc(nil).init().autorelease();

        alert.addButton(NSString::alloc(nil).init_str(obfstr!("OK")));
        alert.setMessageText(NSString::alloc(nil).init_str(obfstr!("Shoreline")));
        alert.setInformativeText(NSString::alloc(nil).init_str(msg));
        alert.setAlertStyle(Critical);
        alert.setWindowLevel(10);
        alert.runModal();
    }

    pub unsafe fn display_confirmation_msg(msg: &str) -> bool
    {
        let alert = NSAlert::alloc(nil).init().autorelease();

        alert.addButton(NSString::alloc(nil).init_str(obfstr!("Yes")));
        alert.addButton(NSString::alloc(nil).init_str(obfstr!("No")));
        alert.setMessageText(NSString::alloc(nil).init_str(obfstr!("Shoreline")));
        alert.setInformativeText(NSString::alloc(nil).init_str(msg));
        alert.setAlertStyle(Informational);
        alert.setWindowLevel(10);

        let response: i32 = msg_send![alert, runModal];
        if response == 1000
        {
            return true;
        }

        return false;
    }

    pub enum NSAlertStyle
    {
        Warning = 0,
        Informational = 1,
        Critical = 2,
    }

    pub trait NSAlert: Sized
    {
        unsafe fn alloc(_: Self) -> id
        {
            msg_send![class!(NSAlert), alloc]
        }

        unsafe fn init(self) -> id;
        unsafe fn autorelease(self) -> id;

        unsafe fn setAlertStyle(self, style: NSAlertStyle);
        unsafe fn setMessageText(self, messageText: id);
        unsafe fn setInformativeText(self, informativeText: id);
        unsafe fn addButton(self, withTitle: id);
        unsafe fn window(self) -> id;
        unsafe fn setWindowLevel(self, level: i32);
        unsafe fn runModal(self) -> id;
    }

    impl NSAlert for id
    {
        unsafe fn init(self) -> id
        {
            msg_send![self, init]
        }

        unsafe fn autorelease(self) -> id
        {
            msg_send![self, autorelease]
        }

        unsafe fn setAlertStyle(self, alertStyle: NSAlertStyle)
        {
            msg_send![self, setAlertStyle: alertStyle]
        }

        unsafe fn setMessageText(self, messageText: id)
        {
            msg_send![self, setMessageText: messageText]
        }

        unsafe fn setInformativeText(self, informativeText: id)
        {
            msg_send![self, setInformativeText: informativeText]
        }

        unsafe fn addButton(self, withTitle: id) {
            msg_send![self, addButtonWithTitle: withTitle]
        }

        unsafe fn window(self) -> id {
            msg_send![self, window]
        }

        unsafe fn runModal(self) -> id {
            msg_send![self, runModal]
        }

        unsafe fn setWindowLevel(self, level: i32) {
            msg_send![self.window(), setLevel: level]
        }
    }
}

#[cfg(target_os = "linux")]
mod platform
{
    pub fn display_info_msg(msg: &str)
    {
        std::panic!("not implemented");
    }

    pub fn display_error_msg(msg: &str)
    {
        std::panic!("not implemented");
    }

    pub unsafe fn display_confirmation_msg(msg: &str) -> bool
    {
        std::panic!("not implemented");
    }
}