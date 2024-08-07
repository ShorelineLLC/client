use std::string::ToString;
use jni::JNIEnv;
use jni::objects::JClass;
use lazy_static::lazy_static;
use obfstr::obfstr;

// Obfuscated and development might be different if I'm debugging unremapped classes

pub static mut IS_OBFUSCATED_ENVIRONMENT: bool = false;
pub static mut IS_DEVELOPMENT_ENVIRONMENT: bool = false;
pub static mut IS_ENCRYPTED_ENVIRONMENT: bool = false;

/* ----------------------------- Mappings -----------------------------*/

lazy_static!
{
    // Classes
    static ref LOADER_CLASS_MAPPING: Mapping = Mapping::new(
        obfstr!("net/shoreline/loader/Loader").to_string(),
        obfstr!("net/shoreline/loader/give up").to_string()
    );

    static ref CLIENT_CLASS_MAPPING: Mapping = Mapping::new(
        obfstr!("net/shoreline/client/ShorelineMod").to_string(),
        obfstr!("net/shoreline/client/bs").to_string()
    );

    static ref MIXINSERVICE_CLASS_MAPPING: Mapping = Mapping::new(
        obfstr!("net/shoreline/loader/mixin/MixinServiceExt").to_string(),
        obfstr!("net/shoreline/loader/a").to_string()
    );

    static ref RESOURCEPACK_CLASS_MAPPING: Mapping = Mapping::new(
        obfstr!("net/shoreline/loader/resource/ResourcePackExt").to_string(),
        obfstr!("net/shoreline/loader/b").to_string()
    );

    static ref USERSESSION_CLASS_MAPPING: Mapping = Mapping::new(
        obfstr!("net/shoreline/loader/session/UserSession").to_string(),
        obfstr!("net/shoreline/loader/c").to_string()
    );

    static ref EVENTBUS_CLASS_MAPPING: Mapping = Mapping::new(
        obfstr!("net/shoreline/eventbus/EventBus").to_string(),
        obfstr!("net/shoreline/client/e").to_string()
    );

    static ref EVENT_LISTENER_CLASS_MAPPING: Mapping = Mapping::new(
        obfstr!("net/shoreline/eventbus/annotation/EventListener").to_string(),
        obfstr!("net/shoreline/client/el").to_string()
    );

    static ref EVENT_INVOKER_CLASS_MAPPING: Mapping = Mapping::new(
        obfstr!("net/shoreline/eventbus/EventBus$Invoker").to_string(),
        obfstr!("net/shoreline/client/i").to_string()
    );

    static ref INVOKER_NODE_CLASS_MAPPING: Mapping = Mapping::new(
        obfstr!("net/shoreline/eventbus/EventBus$InvokerNode").to_string(),
        obfstr!("net/shoreline/client/n").to_string()
    );

    pub static ref IRC_MANAGER_CLASS_MAPPING: Mapping = Mapping::new(
        obfstr!("net/shoreline/client/impl/irc/IRCManager").to_string(),
        obfstr!("net/shoreline/client/ir").to_string()
    );

    // Methods

    // Loader
    pub static ref INFO_FUNCTION: Mapping = Mapping::new(
        obfstr!("info").to_string(),
        obfstr!("a").to_string()
    );

    pub static ref ERROR_FUNCTION: Mapping = Mapping::new(
        obfstr!("error").to_string(),
        obfstr!("b").to_string()
    );

    pub static ref PERFORM_VERSION_CHECK_FUNCTION: Mapping = Mapping::new(
        obfstr!("performVersionCheck").to_string(),
        obfstr!("d").to_string()
    );

    pub static ref SHOW_ERROR_WINDOW_FUNCTION: Mapping = Mapping::new(
        obfstr!("showErrorWindow").to_string(),
        obfstr!("b").to_string()
    );

    pub static ref GET_RESOURCE_FUNCTION: Mapping = Mapping::new(
        obfstr!("getResourceInternal").to_string(),
        obfstr!("a").to_string()
    );

    // UserSession
    pub static ref GET_USER_INFO_FUNCTION: Mapping = Mapping::new(
        obfstr!("getUserInfo").to_string(),
        obfstr!("a").to_string()
    );

    // ShorelineMixinService
    pub static ref GET_INTERNAL_CLASS_BYTES_FUNCTION: Mapping = Mapping::new(
        obfstr!("getInternalClassBytes").to_string(),
        obfstr!("a").to_string()
    );

    pub static ref GET_INTERNAL_INPUT_STREAM_FUNCTION: Mapping = Mapping::new(
        obfstr!("getInternalInputStream").to_string(),
        obfstr!("b").to_string()
    );

    // ResourcePackExt
    pub static ref GET_RESOURCE_INTERNAL_FUNCTION: Mapping = Mapping::new(
        obfstr!("getResourceInternal").to_string(),
        obfstr!("a").to_string()
    );

    // Event Bus

    pub static ref EVENT_BUS_SUBSCRIBE_METHOD: Mapping = Mapping::new(
        obfstr!("subscribe").to_string(),
        obfstr!("a").to_string()
    );

    pub static ref EVENT_BUS_UNSUBSCRIBE_METHOD: Mapping = Mapping::new(
        obfstr!("unsubscribe").to_string(),
        obfstr!("b").to_string()
    );

    pub static ref EVENT_BUS_INSTANCE_FIELD: DescMapping = DescMapping::new(
        obfstr!("INSTANCE").to_string(),
        obfstr!("a").to_string(),
        obfstr!("Lnet/shoreline/eventbus/EventBus;").to_string(),
        obfstr!("Lnet/shoreline/client/e;").to_string()
    );

    pub static ref EVENT_BUS_INVOKER_MAP: DescMapping = DescMapping::new(
        obfstr!("event2InvokerMap").to_string(),
        obfstr!("b").to_string(),
        obfstr!("Ljava/util/Map;").to_string(),
        obfstr!("Ljava/util/Map;").to_string()
    );

    pub static ref EVENT_LISTENER_PRIORITY_METHOD: Mapping = Mapping::new(
        obfstr!("priority").to_string(),
        obfstr!("a").to_string()
    );

    pub static ref INVOKER_INVOKE_METHOD: Mapping = Mapping::new(
        obfstr!("invoke").to_string(),
        obfstr!("a").to_string()
    );

    pub static ref INVOKER_NODE_NEXT_FIELD: DescMapping = DescMapping::new(
        obfstr!("next").to_string(),
        obfstr!("a").to_string(),
        obfstr!("Lnet/shoreline/eventbus/EventBus$InvokerNode;").to_string(),
        obfstr!("Lnet/shoreline/client/n;").to_string(),
    );

    pub static ref INVOKER_NODE_SUBSCRIBER_FIELD: Mapping = Mapping::new(
        obfstr!("subscriber").to_string(),
        obfstr!("c").to_string()
    );

    pub static ref INVOKER_NODE_PRIORITY_FIELD: DescMapping = DescMapping::new(
        obfstr!("priority").to_string(),
        obfstr!("d").to_string(),
        obfstr!("Ljava/lang/Integer;").to_string(),
        obfstr!("Ljava/lang/Integer;").to_string(),
    );

    // Client

    // ShorelineMod
    pub static ref CLIENT_INITIALIZATION_FUNCTION: Mapping = Mapping::new(
        obfstr!("onInitializeClient").to_string(),
        obfstr!("a").to_string()
    );

    // IRC
    pub static ref IRC_DISPATCH_PACKET_METHOD: Mapping = Mapping::new(
        obfstr!("dispatchPacket").to_string(),
        obfstr!("a").to_string()
    );

    pub static ref IRC_ATTEMPT_RECONNECTION_METHOD: Mapping = Mapping::new(
        obfstr!("attemptReconnection").to_string(),
        obfstr!("b").to_string()
    );

    pub static ref IRC_READ_INCOMING_METHOD: Mapping = Mapping::new(
        obfstr!("readIncoming").to_string(),
        obfstr!("a").to_string()
    );
}

/* --------------------------------------------------------------------*/

pub struct Mapping
{
    name: String,
    obfuscated_name: String
}

impl Mapping
{
    pub fn new(name: String,
               obfuscated_name: String) -> Mapping
    {
        Mapping {
            name,
            obfuscated_name
        }
    }

    pub unsafe fn get_name(&self) -> &str
    {
        if IS_OBFUSCATED_ENVIRONMENT
        {
            return &self.obfuscated_name
        }

        &self.name
    }
}

pub struct DescMapping
{
    name: String,
    obfuscated_name: String,
    desc: String,
    obfuscated_desc: String
}

impl DescMapping
{
    pub fn new(name: String,
               obfuscated_name: String,
               desc: String,
               obfuscated_desc: String) -> DescMapping
    {
        DescMapping {
            name,
            obfuscated_name,
            desc,
            obfuscated_desc
        }
    }

    pub unsafe fn get_name(&self) -> &str
    {
        if IS_OBFUSCATED_ENVIRONMENT
        {
            return &self.obfuscated_name
        }

        &self.name
    }

    pub unsafe fn get_desc(&self) -> &str
    {
        if IS_OBFUSCATED_ENVIRONMENT
        {
            return &self.obfuscated_desc
        }

        &self.desc
    }
}

pub unsafe fn check_obfuscated_environment(env: &mut JNIEnv)
{
    IS_OBFUSCATED_ENVIRONMENT = match env.find_class(obfstr!("net/shoreline/loader/Loader"))
    {
        Ok(_) => false,
        Err(_) => {
            env.exception_clear().unwrap();

            true
        }
    };

    IS_ENCRYPTED_ENVIRONMENT = IS_OBFUSCATED_ENVIRONMENT && match env.find_class(obfstr!("net/shoreline/loader/a"))
    {
        Ok(_) => false,
        Err(_) => {
            env.exception_clear().unwrap();

            true
        }
    };

    let fabric_loader_instance = env.call_static_method(
        obfstr!("net/fabricmc/loader/api/FabricLoader"),
        obfstr!("getInstance"),
        obfstr!("()Lnet/fabricmc/loader/api/FabricLoader;"),
        &[]
    ).unwrap().l().unwrap();

    IS_DEVELOPMENT_ENVIRONMENT = env.call_method(
        fabric_loader_instance,
        obfstr!("isDevelopmentEnvironment"),
        obfstr!("()Z"),
        &[]
    ).unwrap().z().unwrap();
}

// Loader

pub unsafe fn get_loader_class<'a>(env: &mut JNIEnv<'a>) -> JClass<'a>
{
    env.find_class(LOADER_CLASS_MAPPING.get_name()).unwrap()
}

pub unsafe fn get_user_session_class<'a>(env: &mut JNIEnv<'a>) -> JClass<'a>
{
    env.find_class(USERSESSION_CLASS_MAPPING.get_name()).unwrap()
}

pub unsafe fn get_mixinservice_class<'a>(env: &mut JNIEnv<'a>) -> JClass<'a>
{
    env.find_class(MIXINSERVICE_CLASS_MAPPING.get_name()).unwrap()
}

pub unsafe fn get_resourcepack_class<'a>(env: &mut JNIEnv<'a>) -> JClass<'a>
{
    env.find_class(RESOURCEPACK_CLASS_MAPPING.get_name()).unwrap()
}

// Event Bus

pub unsafe fn get_event_bus_class<'a>(env: &mut JNIEnv<'a>) -> JClass<'a>
{
    env.find_class(EVENTBUS_CLASS_MAPPING.get_name()).unwrap()
}

pub unsafe fn get_event_listener_class<'a>(env: &mut JNIEnv<'a>) -> JClass<'a>
{
    env.find_class(EVENT_LISTENER_CLASS_MAPPING.get_name()).unwrap()
}

pub unsafe fn get_event_invoker_class<'a>(env: &mut JNIEnv<'a>) -> JClass<'a>
{
    env.find_class(EVENT_INVOKER_CLASS_MAPPING.get_name()).unwrap()
}

pub unsafe fn get_invoker_node_class<'a>(env: &mut JNIEnv<'a>) -> JClass<'a>
{
    env.find_class(INVOKER_NODE_CLASS_MAPPING.get_name()).unwrap()
}

// Client

pub unsafe fn get_main_client_class<'a>(env: &mut JNIEnv<'a>) -> JClass<'a>
{
    env.find_class(CLIENT_CLASS_MAPPING.get_name()).unwrap()
}

pub unsafe fn get_irc_manager_class<'a>(env: &mut JNIEnv<'a>) -> JClass<'a>
{
    env.find_class(IRC_MANAGER_CLASS_MAPPING.get_name()).unwrap()
}