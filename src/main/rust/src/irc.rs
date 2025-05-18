use std::process::exit;
use std::sync::{Arc, RwLock};
use std::time::Duration;
use futures_channel::mpsc;
use futures_channel::mpsc::{unbounded, UnboundedReceiver, UnboundedSender};
use futures_util::{future, pin_mut, StreamExt};
use jni::JNIEnv;
use jni::objects::{JObject, JString};
use lazy_static::lazy_static;
use obfstr::obfstr;
use tokio::runtime::Runtime;
use tokio_tungstenite::connect_async;
use tokio_tungstenite::tungstenite::Message;
use crate::{network, notifs};

static mut INSTANCE: Option<IRC> = None;

pub struct IRC
{
    runtime: Runtime,
    outgoing_tx: UnboundedSender<Message>,
    incoming_rx: UnboundedReceiver<Message>
}

impl IRC
{
    pub unsafe fn create_instance<'a>(token: String,
                                      env: &mut JNIEnv) -> &'a mut Self
    {
        let server_address = obfstr!("wss://irc.shorelineclient.dev/").to_string();
        match network::verify_server_integrity(&server_address)
        {
            Ok(()) => {
                let runtime = Runtime::new().unwrap();

                let (outgoing_tx, outgoing_rx) = unbounded();
                let (incoming_tx, incoming_rx) = unbounded();

                runtime.block_on(async move {
                    tokio::spawn(async move {
                        let mut url = url::Url::parse(&server_address).unwrap();
                        url.query_pairs_mut().append_pair(obfstr!("token"), &token);

                        let (ws_stream, _) = match connect_async(url.as_str()).await
                        {
                            Ok(wss) => wss,
                            Err(_) => {
                                let msg = obfstr! {
                                    "Failed to connect to the Shoreline online users network. \
                                    If this issue persists, please contact a developer."
                                }.to_string();

                                notifs::display_error_msg(&msg);

                                exit(-1);
                            }
                        };

                        let (write, read) = ws_stream.split();
                        let outgoing_future = outgoing_rx.map(Ok).forward(write);

                        let incoming_future = {
                            read.for_each(|message| async {
                                let msg = message.unwrap_or_else(|_| {
                                    Message::text("".to_string())
                                });

                                let text = msg.into_text().unwrap_or_else(|_| {
                                    "".to_string()
                                });

                                if !text.is_empty()
                                {
                                    incoming_tx.unbounded_send(Message::text(text)).unwrap();
                                }
                            })
                        };

                        pin_mut!(outgoing_future, incoming_future);
                        future::select(outgoing_future, incoming_future).await;

                        let disconnection_packet = obfstr! {
                            "{\"Packet\": \"SPacketDisconnect\", \
                            \"Reason\": \"Connection closed.\", \
                            \"Fully-Killed\": false}"
                        }.to_string();

                        incoming_tx.unbounded_send(Message::text(disconnection_packet)).unwrap();
                    });
                });

                let irc = Self {
                    runtime,
                    outgoing_tx,
                    incoming_rx
                };

                INSTANCE = Some(irc);

                return INSTANCE.as_mut().unwrap();
            }
            Err(msg) => {
                notifs::error(env, &msg);
                notifs::display_error_msg(&msg);

                exit(-1);
            }
        }
    }

    pub unsafe fn get_instance<'a>(env: &mut JNIEnv) -> &'a mut Self
    {
        if INSTANCE.is_none()
        {
            let msg = obfstr! {
                "Shoreline has encountered a severe error.\n\n\
                The IRC instance was never instantiated. Please report this \
                to a developer, this should never happen."
            }.to_string();

            notifs::error(env, &msg);
            notifs::display_error_msg(&msg);

            exit(-1);
        }

        INSTANCE.as_mut().unwrap()
    }

    pub unsafe fn reconnect<'a>(token: String,
                                env: &mut JNIEnv)
    {
        let server_address = obfstr!("wss://irc.shorelineclient.dev/").to_string();
        match network::verify_server_integrity(&server_address)
        {
            Ok(()) => {
                let runtime = Runtime::new().unwrap();

                let (outgoing_tx, outgoing_rx) = unbounded();
                let (incoming_tx, incoming_rx) = unbounded();

                runtime.block_on(async move {
                    tokio::spawn(async move {
                        let mut url = url::Url::parse(&server_address).unwrap();
                        url.query_pairs_mut().append_pair(obfstr!("token"), &token);

                        let (ws_stream, _) = match connect_async(url.as_str()).await
                        {
                            Ok(wss) => wss,
                            Err(_) => {
                                return;
                            }
                        };

                        let (write, read) = ws_stream.split();
                        let outgoing_future = outgoing_rx.map(Ok).forward(write);

                        let incoming_future = {
                            read.for_each(|message| async {
                                let msg = message.unwrap_or_else(|_| {
                                    Message::text("".to_string())
                                });

                                let text = msg.into_text().unwrap_or_else(|_| {
                                    "".to_string()
                                });

                                if !text.is_empty()
                                {
                                    incoming_tx.unbounded_send(Message::text(text)).unwrap();
                                }
                            })
                        };

                        pin_mut!(outgoing_future, incoming_future);
                        future::select(outgoing_future, incoming_future).await;

                        let disconnection_packet = obfstr! {
                            "{\"Packet\": \"SPacketDisconnect\", \
                            \"Reason\": \"Connection closed.\", \
                            \"Fully-Killed\": false}"
                        }.to_string();

                        incoming_tx.unbounded_send(Message::text(disconnection_packet)).unwrap();
                    });
                });

                let irc = Self {
                    runtime,
                    outgoing_tx,
                    incoming_rx
                };

                INSTANCE = Some(irc);
            }
            Err(_) => {
                // Don't alert the user that the authenticity couldn't be confirmed,
                // if they are offline then this check will always fail.
            }
        }
    }

    pub fn send_message(&self, message: String)
    {
        match self.outgoing_tx.unbounded_send(Message::text(message))
        {
            Ok(()) => {},
            Err(_) => {}
        }
    }

    pub fn get_new_messages(&mut self) -> Vec<String>
    {
        let mut messages = Vec::new();

        while let Ok(Some(message)) = self.incoming_rx.try_next()
        {
            if let Ok(text) = message.into_text()
            {
                messages.push(text);
            }
        }

        messages
    }
}

pub unsafe extern "system" fn dispatch_packet(mut env: JNIEnv,
                                              _instance: JObject,
                                              packet_json: JString)
{
    let irc = IRC::get_instance(&mut env);

    let rust_str: String = env.get_string(&packet_json).unwrap().into();
    irc.send_message(rust_str);
}

pub unsafe extern "system" fn read_incoming_packets<'a>(mut env: JNIEnv<'a>,
                                                        _instance: JObject) -> JObject<'a>
{
    let irc = IRC::get_instance(&mut env);

    let new_list = env.new_object(
        obfstr!("java/util/ArrayList"),
        obfstr!("()V"),
        &[]
    ).unwrap();

    let new_messages = irc.get_new_messages();

    for new_message in new_messages
    {
        let java_string = env.new_string(new_message).unwrap();

        env.call_method(
            &new_list,
            obfstr!("add"),
            obfstr!("(Ljava/lang/Object;)Z"),
            &[(&java_string).into()]
        ).unwrap().z().unwrap();
    }

    return new_list;
}

pub unsafe extern "system" fn attempt_reconnection(mut env: JNIEnv,
                                                   _instance: JObject,
                                                   backup_token: JString)
{
    let token: String = env.get_string(&backup_token).unwrap().into();
    IRC::reconnect(token, &mut env);
}