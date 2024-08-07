use std::process::exit;
use jni::JNIEnv;
use jni::objects::{JClass, JObject};
use obfstr::obfstr;
use serde_json::json;
use crate::login::User;
use crate::notifs;

pub static mut USER: Option<User> = None;

pub unsafe extern "system" fn get_user_session<'a>(mut env: JNIEnv<'a>,
                                                   _clazz: JClass,
                                                   _unused: JObject) -> JObject<'a>
{
    let user = get_user(&mut env);

    let json = json!({
        obfstr!("Hardware-ID"): user.hwid,
        obfstr!("Username"): user.username,
        obfstr!("UID"): user.uid,
        obfstr!("User-Type"): user.usertype
    }).to_string();

    JObject::from(env.new_string(&json).unwrap())
}

pub unsafe fn get_user<'a>(env: &mut JNIEnv) -> &'a User
{
    return match USER.as_ref()
    {
        Some(user) => user,
        None => {
            let msg = obfstr! {
                "Failed to obtain your session information. \
                Please report this to a developer."
            }.to_string();

            notifs::error(env, &msg);
            notifs::display_error_msg(&msg);

            exit(-1);
        }
    };
}