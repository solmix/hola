/**
 * Copyright 2013 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */

package org.solmix.hola.common;

import java.util.regex.Pattern;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年7月8日
 */

public class HOLA
{
    public static final Pattern SPLIT_SEMICOLON_PATTERN = Pattern.compile("\\s*[;]+\\s*");

    public static final Pattern DISCOVERY_SPLIT_PATTERN = Pattern.compile("\\s*[|;]+\\s*");

    public static final Pattern SPLIT_COMMA_PATTERN = Pattern.compile("\\s*[,]+\\s*");
    /**
     * 构成Address的元素
     */
    public static final String ADDRESS_KEY = "address";
    public static final String GROUP_KEY = "group";

   
    public static final String PROTOCOL_KEY = "protocol";
    public static final String HOST_KEY = "host";
    public static final String PORT_KEY = "port";
    public static final String PATH_KEY = "path";
    public static final String USER_KEY = "user";
    public static final String PASSWORD_KEY = "password";
    
    /**接口名称*/
    public static final String INTERFACE_KEY = "interface";
    
    public static final String SERIALIZATION_KEY = "serialization";
    public static final String PALYLOAD_KEY = "palyload";

    public static final String DEFAULT_SERIALIZATION = "java";
    
    public static final String TIMEOUT_KEY = "timeout";

    public static final String CONNECT_TIMEOUT_KEY = "connectTimeout";

    public static final String RECEIVE_TIMEOUT_KEY = "receiveTimeout";
    
    public static final String ANY_VALUE = "*";
    /**
     * 默认连接超时时间
     */
    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;

    /**
     * 默认读超时时间
     */
    public static final int DEFAULT_RECEIVE_TIMEOUT = 3000;

    /**
     * 默认写超时时间
     */
    public static final int DEFAULT_WRITE_TIMEOUT = 3000;
    
    
    /**
     * 通信时放入头消息中，接收消息端可以根据不同的版本做兼容处理
     */
    public static final String HOLA_VERSION_KEY = "holav";
    
    /**
     * 当前版本号
     */
    public static final String DEFAULT_HOLA_VERSION = "0.6.1";
    
    /**
     * 服务版本号
     */
    public static final String VERSION_KEY = "version";
   
    public static final String VERSION = "0.0.1";
    
    /**
     *不设置Transportr时，使用该值
     */
    public static final String DEFAULT_TRANSPORTER = "netty";

    /**
     * 不设置Protocol时，使用该值
     */
    public static final String DEFAULT_PROTOCOL = "hola";
    
    /**
     * 是否为心跳message
     */
    public static final String  HEARTBEAT_MESSAGE      = HOLA.class.getName()+".HEARTBEAT_MESSAGE";
    
    /**
     * 等待请求超时时间
     */
    public static final int DEFAULT_TIMEOUT = 60*1000;
    
    /**service property 中默认value的key前缀*/
    public static final String DEFAULT_KEY_PREFIX = "default.";
    /***==========================================================
     * 
     =============================================================*/

    /**服务类别：默认为生产者*/
    public static final String DEFAULT_CATEGORY = "provider";    
    
    /**服务类别表示：provider：生产者，consumer：消费者*/
    public static final String CATEGORY_KEY = "category";
    
    public static final String  PROVIDER_CATEGORY                           = "provider";

    public static final String  CONSUMER_CATEGORY                           = "consumer";
    
    public static final String DISCOVERY = "discovery";
    
    public static final String REGISTER = "register";
    public static final String UNREGISTER = "unregister";

    /** 动态注册 */
    public static final String DYNAMIC_KEY = "discovery.dynamic";

    public static final String DISCOVERY_URL = "discovery.url";
    /**公告服务是否同步存储文件，默认异步*/
    public static final String DISCOVERY_SYNC_SAVE_FILE = "discovery.save.file";
    /**存储公告的文件*/
    public static final String DISCOVERY_CACHE_FILE = "discovery.file";
    
    /**公告服务自动重连时间*/
    public static final String DISCOVERY_RECONNECT_PERIOD = "discovery.reconnect.period";
    public static final int DEFAULT_DISCOVERY_RECONNECT_PERIOD = 3000;
    
    /**默认根节点*/
    public static final String DEFAULT_ROOT = "hola";
    
    /**备用公告地址*/
    public static final String BACKUP_KEY = "discovery.backup";
    
    /**公告服务会话超时时间*/
    public static final  String DISCOVERY_SESSION_TIMEOUT="discovery.session.timeout";
    public static final int DEFAULT_SESSION_TIMEOUT = 60000;
    
    /**公告失败重试周期*/
    public static final String RETRY_PERIOD_KEY = "discovery.retry.period";
    public static final int DEFAULT_RETRY_PERIOD = 5000;
    
    /**服务权重*/
    public static final String WEIGHT_KEY="weight";
    public static final int DEFAULT_WEIGHT = 5;
    /**优先级*/
    public static final String PRIORITY_KEY="priority";
    public static final int DEFAULT_PRIORITY = 5;
    
  
    /**生存时间*/
    public static final String   TTL_KEY="ttl";
    
    /**是否开启启动检测*/
    public static final String CHECK_KEY = "check";
    


    public final static String PATH_SEPARATOR = "/";

    public static final String REMOTE_SERVICE_ID = "hola.rs.id";

    public static final String REMOTE_OBJECTCLASS = "hola.rs.objectclass";

    public static final String REMOTE_RANKING = "hola.rs.ranking";

    public static final String PROTOCOL_DISCOVERY = "discovery";

    public static final String CODEC_KEY = "codec";

    public static final String IS_SERVER = "server";

    public static final int DEFAULT_HEARTBEAT = 60 * 1000;

    

   

 

    public static final int DEFAULT_CHANNEL_ACCEPTS = 0;

    public static final int DEFAULT_IDLE_TIMEOUT = 600 * 1000;

    public static final long DEFAULT_SHUTDOWN_TIMEOUT = 15 * 60 * 1000;

    public static final int DEFAULT_RECONNECT_WARNING_PERIOD = 1800;

    /**
     * 发送数据的时候如果没有链接上是否需要重新连接
     */
    public static final String KEY_SEND_RECONNECT = "send.reconnect";

    public static final String KEY_RECONNECT = "reconnect";

    public static final int DEFAULT_RECONNECT_PERIOD = 2000;

    public static final int DEFAULT_IO_THREADS = Runtime.getRuntime().availableProcessors() + 1;

    public static final String KEY_IO_THREADS = "io.threads";

    public static final String KEY_BUFFER = "bufferSize";

    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    public static final int MAX_BUFFER_SIZE = 16 * 1024;

    public static final int MIN_BUFFER_SIZE = 1 * 1024;

    /**
     * 已接收数据报文不够，还需接收更多的数据，true/false
     */
    public static final String NEED_MORE_DATA = "need.more.data";

    public static final String DATALENGTH_KEY = "data.length";

    public static final int DEFAULT_PALYLOAD = 8 * 1024 * 1024;

    public static final int DEFAULT_CORE_THREADS = 0;

    public static final int DEFAULT_ALIVE = 60000;

    public static final int DEFAULT_QUEUES = 0;

    public static final String DEFAULT_DISPATHER = "all";

    public static final String DEFAULT_EXCHANGER = "protocol";

    public static final String DEFAULT_THREADPOOL = "pooled";

    public static final String KEY_CHANNEL_ATTRIBUTE_READONLY = "channel.readonly";

    public static final String HOLA_DEFAULT_CONFIG_FILE = "hola.default.config.file";

    public static final String DEFAULT_HOLA_CONFIG_FILE = "hola.properties";



  

    public static final String DEFAULT_RPC_SERIALIZATION = "hola";

    public static final String TRANSPORTER_KEY = "transporter";

    public static final String DEFAULT_RPC_TRANSPORTER = "netty";

   

    public static final String BUFFER_KEY = "bufferSize";

    public static final String ANYHOST_KEY = "anyhost";

    public static final String ANYHOST_VALUE = "0.0.0.0";

    public static final String LOCALHOST_KEY = "localhost";

    public static final String LOCALHOST_VALUE = "127.0.0.1";
 
    public static final String THREADS_KEY = "threads";

    public static final int DEFAULT_THREADS = 200;

    public static final String WAIT_KEY = "wait";

    public static final boolean DEFAULT_WAIT = false;



    public static final String SERVICE_ID_KEY = "remote.service.id";


    public static final String PIPELINES = "pipelines";
    /*

    public static final String  REGISTER                           = "register";

    public static final String  UNREGISTER                         = "unregister";

    public static final String  SUBSCRIBE                          = "subscribe";

    public static final String  UNSUBSCRIBE                        = "unsubscribe";
    
    public static final String  CATEGORY_KEY                       = "category";

    public static final String  PROVIDERS_CATEGORY                 = "providers";

    public static final String  CONSUMERS_CATEGORY                 = "consumers";

    public static final String  ROUTERS_CATEGORY                   = "routers";

    public static final String  CONFIGURATORS_CATEGORY             = "configurators";

    public static final String  DEFAULT_CATEGORY                   = PROVIDERS_CATEGORY;

    public static final String  ENABLED_KEY                        = "enabled";

    public static final String  DISABLED_KEY                       = "disabled";

    public static final String  VALIDATION_KEY                     = "validation";

    public static final String  CACHE_KEY                          = "cache";

    public static final String  DYNAMIC_KEY                        = "dynamic";



    public static final String  SENT_KEY                           = "sent";

    public static final boolean DEFAULT_SENT                       = false;

    public static final String  REGISTRY_PROTOCOL                  = "registry";

    public static final String  $INVOKE                            = "$invoke";

    public static final String  $ECHO                              = "$echo";

    public static final int     DEFAULT_IO_THREADS                 = Runtime.getRuntime()
                                                                           .availableProcessors() + 1;

    public static final String  DEFAULT_PROXY                      = "javassist";

    public static final int     DEFAULT_PAYLOAD                    = 8 * 1024 * 1024;                      // 8M

    public static final String  DEFAULT_CLUSTER                    = "failover";

    public static final String  DEFAULT_LOADBALANCE                = "random";

    public static final String  DEFAULT_EXCHANGER                  = "header";

    public static final String  DEFAULT_TRANSPORTER                = "netty";

    public static final String  DEFAULT_REMOTING_SERVER            = "netty";

    public static final String  DEFAULT_REMOTING_CLIENT            = "netty";

    public static final String  DEFAULT_REMOTING_SERIALIZATION     = "hessian2";

    public static final String  DEFAULT_HTTP_SERVER                = "servlet";

    public static final String  DEFAULT_HTTP_CLIENT                = "jdk";

    public static final String  DEFAULT_HTTP_SERIALIZATION         = "json";

    public static final String  DEFAULT_CHARSET                    = "UTF-8";

    public static final int     DEFAULT_WEIGHT                     = 100;

    public static final int     DEFAULT_FORKS                      = 2;

    public static final String  DEFAULT_THREAD_NAME                = "Dubbo";

    public static final int     DEFAULT_CORE_THREADS               = 0;

    public static final int     DEFAULT_THREADS                    = 200;

    public static final int     DEFAULT_QUEUES                     = 0;

    public static final int     DEFAULT_ALIVE                      = 60 * 1000;

    public static final int     DEFAULT_CONNECTIONS                = 0;

    public static final int     DEFAULT_ACCEPTS                    = 0;

    public static final int     DEFAULT_IDLE_TIMEOUT               = 600 * 1000;

    public static final int     DEFAULT_HEARTBEAT                  = 60 * 1000;

    public static final int     DEFAULT_TIMEOUT                    = 1000;

    public static final int     DEFAULT_CONNECT_TIMEOUT            = 3000;

    public static final int     DEFAULT_RETRIES                    = 2;

    // default buffer size is 8k.
    public static final int     DEFAULT_BUFFER_SIZE                = 8 * 1024;

    public static final int     MAX_BUFFER_SIZE                    = 16 * 1024;

    public static final int     MIN_BUFFER_SIZE                    = 1 * 1024;

    public static final String  REMOVE_VALUE_PREFIX                = "-";

    public static final String  HIDE_KEY_PREFIX                    = ".";

    public static final String  DEFAULT_KEY_PREFIX                 = "default.";

    public static final String  DEFAULT_KEY                        = "default";

    public static final String  LOADBALANCE_KEY                    = "loadbalance";

    // key for router type, for e.g., "script"/"file",  corresponding to ScriptRouterFactory.NAME, FileRouterFactory.NAME 
    public static final String  ROUTER_KEY                         = "router";

    public static final String  CLUSTER_KEY                        = "cluster";

    public static final String  REGISTRY_KEY                       = "registry";

    public static final String  MONITOR_KEY                        = "monitor";

    public static final String  SIDE_KEY                           = "side";

    public static final String  PROVIDER_SIDE                      = "provider";

    public static final String  CONSUMER_SIDE                      = "consumer";

    public static final String  BACKUP_KEY                         = "backup";

    public static final String  DIRECTORY_KEY                      = "directory";

    public static final String  DEPRECATED_KEY                     = "deprecated";

    public static final String  ANYHOST_KEY                        = "anyhost";

    public static final String  ANYHOST_VALUE                      = "0.0.0.0";

    public static final String  LOCALHOST_KEY                      = "localhost";

    public static final String  LOCALHOST_VALUE                    = "127.0.0.1";

    public static final String  APPLICATION_KEY                    = "application";

    public static final String  LOCAL_KEY                          = "local";

    public static final String  STUB_KEY                           = "stub";

    public static final String  MOCK_KEY                           = "mock";

    public static final String  PROTOCOL_KEY                       = "protocol";

    public static final String  PROXY_KEY                          = "proxy";

    public static final String  WEIGHT_KEY                         = "weight";

    public static final String  FORKS_KEY                          = "forks";

    public static final String  DEFAULT_THREADPOOL                 = "limited";

    public static final String  DEFAULT_CLIENT_THREADPOOL          = "cached";

    public static final String  THREADPOOL_KEY                     = "threadpool";

    public static final String  THREAD_NAME_KEY                    = "threadname";

    public static final String  IO_THREADS_KEY                     = "iothreads";

    public static final String  CORE_THREADS_KEY                   = "corethreads";

    public static final String  THREADS_KEY                        = "threads";

    public static final String  QUEUES_KEY                         = "queues";

    public static final String  ALIVE_KEY                          = "alive";

    public static final String  EXECUTES_KEY                       = "executes";

    public static final String  BUFFER_KEY                         = "buffer";

    public static final String  PAYLOAD_KEY                        = "payload";

    public static final String  REFERENCE_FILTER_KEY               = "reference.filter";

    public static final String  INVOKER_LISTENER_KEY               = "invoker.listener";

    public static final String  SERVICE_FILTER_KEY                 = "service.filter";

    public static final String  EXPORTER_LISTENER_KEY              = "exporter.listener";

    public static final String  ACCESS_LOG_KEY                     = "accesslog";

    public static final String  ACTIVES_KEY                        = "actives";

    public static final String  CONNECTIONS_KEY                    = "connections";

    public static final String  ACCEPTS_KEY                        = "accepts";

    public static final String  IDLE_TIMEOUT_KEY                   = "idle.timeout";

    public static final String  HEARTBEAT_KEY                      = "heartbeat";

    public static final String  HEARTBEAT_TIMEOUT_KEY              = "heartbeat.timeout";

    public static final String  CONNECT_TIMEOUT_KEY                = "connect.timeout";

    public static final String  TIMEOUT_KEY                        = "timeout";

    public static final String  RETRIES_KEY                        = "retries";

    public static final String  PROMPT_KEY                         = "prompt";

    public static final String  CODEC_KEY                          = "codec";

    public static final String  SERIALIZATION_KEY                  = "serialization";

    public static final String  EXCHANGER_KEY                      = "exchanger";

    public static final String  TRANSPORTER_KEY                    = "transporter";

    public static final String  SERVER_KEY                         = "server";

    public static final String  CLIENT_KEY                         = "client";

    public static final String  ID_KEY                             = "id";

    public static final String  ASYNC_KEY                          = "async";

    public static final String  RETURN_KEY                         = "return";

    public static final String  TOKEN_KEY                          = "token";

    public static final String  METHOD_KEY                         = "method";

    public static final String  METHODS_KEY                        = "methods";

    public static final String  CHARSET_KEY                        = "charset";

    public static final String  RECONNECT_KEY                      = "reconnect";

    public static final String  SEND_RECONNECT_KEY                 = "send.reconnect";

    public static final int     DEFAULT_RECONNECT_PERIOD           = 2000;

    public static final String  SHUTDOWN_TIMEOUT_KEY               = "shutdown.timeout";

    public static final int     DEFAULT_SHUTDOWN_TIMEOUT           = 1000 * 60 * 15;

    public static final String  PID_KEY                            = "pid";

    public static final String  TIMESTAMP_KEY                      = "timestamp";
    
    public static final String  WARMUP_KEY                         = "warmup";

    public static final int     DEFAULT_WARMUP                     = 10 * 60 * 1000;

    public static final String  CHECK_KEY                          = "check";

    public static final String  REGISTER_KEY                       = "register";

    public static final String  SUBSCRIBE_KEY                      = "subscribe";

    

    public static final String  GENERIC_KEY                        = "generic";

    public static final String  FILE_KEY                           = "file";

    public static final String  WAIT_KEY                           = "wait";

    public static final String  CLASSIFIER_KEY                     = "classifier";

   


    public static final String  HESSIAN_VERSION_KEY                = "hessian.version";

    public static final String  DISPATCHER_KEY                     = "dispatcher";

    public static final String  CHANNEL_HANDLER_KEY                = "channel.handler";

    public static final String  DEFAULT_CHANNEL_HANDLER            = "default";

    public static final String  ANY_VALUE                          = "*";

    public static final String  COMMA_SEPARATOR                    = ",";


    public final static String  PATH_SEPARATOR                     = "/";

    public static final String  REGISTRY_SEPARATOR                 = "|";

    public static final Pattern REGISTRY_SPLIT_PATTERN             = Pattern
                                                                           .compile("\\s*[|;]+\\s*");

    public static final String  SEMICOLON_SEPARATOR                = ";";

    public static final Pattern SEMICOLON_SPLIT_PATTERN            = Pattern
                                                                           .compile("\\s*[;]+\\s*");

    public static final String  CONNECT_QUEUE_CAPACITY             = "connect.queue.capacity";

    public static final String  CONNECT_QUEUE_WARNING_SIZE         = "connect.queue.warning.size";

    public static final int     DEFAULT_CONNECT_QUEUE_WARNING_SIZE = 1000;

    public static final String  CHANNEL_ATTRIBUTE_READONLY_KEY     = "channel.readonly";

    public static final String  CHANNEL_READONLYEVENT_SENT_KEY     = "channel.readonly.sent";

    public static final String  CHANNEL_SEND_READONLYEVENT_KEY     = "channel.readonly.send";

    public static final String  COUNT_PROTOCOL                     = "count";

    public static final String  TRACE_PROTOCOL                     = "trace";

    public static final String  EMPTY_PROTOCOL                     = "empty";

    public static final String  ADMIN_PROTOCOL                     = "admin";

    public static final String  PROVIDER_PROTOCOL                  = "provider";

    public static final String  CONSUMER_PROTOCOL                  = "consumer";

    public static final String  ROUTE_PROTOCOL                     = "route";

    public static final String  SCRIPT_PROTOCOL                    = "script";

    public static final String  CONDITION_PROTOCOL                 = "condition";

    public static final String  MOCK_PROTOCOL                      = "mock";

    public static final String  RETURN_PREFIX                      = "return ";

    public static final String  THROW_PREFIX                       = "throw";

    public static final String  FAIL_PREFIX                        = "fail:";

    public static final String  FORCE_PREFIX                       = "force:";

    public static final String  FORCE_KEY                          = "force";

    public static final String  MERGER_KEY                         = "merger";

    *//**
     * 集群时是否排除非available的invoker
     *//*
    public static final String  CLUSTER_AVAILABLE_CHECK_KEY        = "cluster.availablecheck";

    *//**
     *//*
    public static final boolean DEFAULT_CLUSTER_AVAILABLE_CHECK    = true;

    *//**
     * 集群时是否启用sticky策略
     *//*
    public static final String  CLUSTER_STICKY_KEY                 = "sticky";

    *//**
     * sticky默认值.
     *//*
    public static final boolean DEFAULT_CLUSTER_STICKY             = false;

    *//**
     * 创建client时，是否先要建立连接。
     *//*
    public static final String  LAZY_CONNECT_KEY                   = "lazy";

    *//**
     * lazy连接的初始状态是连接状态还是非连接状态？
     *//*
    public static final String  LAZY_CONNECT_INITIAL_STATE_KEY     = "connect.lazy.initial.state";

    *//**
     * lazy连接的初始状态默认是连接状态.
     *//*
    public static final boolean DEFAULT_LAZY_CONNECT_INITIAL_STATE = true;

    *//**
     * 注册中心是否同步存储文件，默认异步
     *//*
    public static final String  REGISTRY_FILESAVE_SYNC_KEY         = "save.file";

    *//**
     * 注册中心失败事件重试事件
     *//*
    public static final String  REGISTRY_RETRY_PERIOD_KEY          = "retry.period";

    *//**
     * 重试周期
     *//*
    public static final int DEFAULT_REGISTRY_RETRY_PERIOD          =  5 * 1000;
    
    *//**
     * 注册中心自动重连时间
     *//*
    public static final String  REGISTRY_RECONNECT_PERIOD_KEY      = "reconnect.period";

    public static final int     DEFAULT_REGISTRY_RECONNECT_PERIOD  = 3 * 1000;
    
    public static final String  SESSION_TIMEOUT_KEY                = "session";

    public static final int     DEFAULT_SESSION_TIMEOUT            = 60 * 1000;

    *//**
     * 注册中心导出URL参数的KEY
     *//*
    public static final String  EXPORT_KEY                         = "export";

    *//**
     * 注册中心引用URL参数的KEY
     *//*
    public static final String  REFER_KEY                          = "refer";

    *//**
     * callback inst id
     *//*
    public static final String  CALLBACK_SERVICE_KEY               = "callback.service.instid";

    *//**
     * 每个客户端同一个接口 callback服务实例的限制
     *//*
    public static final String  CALLBACK_INSTANCES_LIMIT_KEY       = "callbacks";

    *//**
     * 每个客户端同一个接口 callback服务实例的限制
     *//*
    public static final int     DEFAULT_CALLBACK_INSTANCES         = 1;

    public static final String  CALLBACK_SERVICE_PROXY_KEY         = "callback.service.proxy";

    public static final String  IS_CALLBACK_SERVICE                = "is_callback_service";

    *//**
     * channel中callback的invokers
     *//*
    public static final String  CHANNEL_CALLBACK_KEY               = "channel.callback.invokers.key";



    public static final String  IS_SERVER_KEY                      = "isserver";

    *//**
     * 默认值毫秒，避免重新计算.
     *//*
    public static final int     DEFAULT_SERVER_SHUTDOWN_TIMEOUT    = 10000;

    public static final String  ON_CONNECT_KEY                     = "onconnect";

    public static final String  ON_DISCONNECT_KEY                  = "ondisconnect";

    public static final String  ON_INVOKE_METHOD_KEY               = "oninvoke.method";

    public static final String  ON_RETURN_METHOD_KEY               = "onreturn.method";

    public static final String  ON_THROW_METHOD_KEY                = "onthrow.method";

    public static final String  ON_INVOKE_INSTANCE_KEY             = "oninvoke.instance";

    public static final String  ON_RETURN_INSTANCE_KEY             = "onreturn.instance";

    public static final String  ON_THROW_INSTANCE_KEY              = "onthrow.instance";

    public static final String  OVERRIDE_PROTOCOL                  = "override";

    public static final String  PRIORITY_KEY                       = "priority";
    
    public static final String  RULE_KEY                           = "rule";

    public static final String  TYPE_KEY                           = "type";

    public static final String  RUNTIME_KEY                        = "runtime";

    // when ROUTER_KEY's value is set to ROUTER_TYPE_CLEAR, RegistryDirectory will clean all current routers
    public static final String  ROUTER_TYPE_CLEAR                  = "clean";

    public static final String  DEFAULT_SCRIPT_TYPE_KEY            = "javascript";


    public static final boolean DEFAULT_STUB_EVENT                 = false;


    //invocation attachment属性中如果有此值，则选择mock invoker
    public static final String  INVOCATION_NEED_MOCK               = "invocation.need.mock";

    public static final String LOCAL_PROTOCOL                      = "injvm";

    public static final String AUTO_ATTACH_INVOCATIONID_KEY          = "invocationid.autoattach";
    
    public static final String SCOPE_KEY                                   = "scope";
    
    public static final String SCOPE_LOCAL                                       = "local";
    
    public static final String SCOPE_REMOTE                                      = "remote";
    
    public static final String SCOPE_NONE                                  = "none";
    
    public static final String RELIABLE_PROTOCOL                           = "napoli";
    
    public static final String TPS_LIMIT_RATE_KEY                  = "tps";

    public static final String TPS_LIMIT_INTERVAL_KEY              = "tps.interval";

    public static final long DEFAULT_TPS_LIMIT_INTERVAL            = 60 * 1000;

    public static final String DECODE_IN_IO_THREAD_KEY             = "decode.in.io";

    public static final boolean DEFAULT_DECODE_IN_IO_THREAD        = true;
    
    public static final String INPUT_KEY                           = "input";
    
    public static final String OUTPUT_KEY                          = "output";

    public static final String EXECUTOR_SERVICE_COMPONENT_KEY      = ExecutorService.class.getName();

    public static final String GENERIC_SERIALIZATION_NATIVE_JAVA   = "nativejava";

    public static final String GENERIC_SERIALIZATION_DEFAULT       = "true";*/


  


    

  

 
    

   

 

 

    
}