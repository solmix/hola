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
     * 地址
     */
    public static final String ADDRESS_KEY = "address";
    public static final String GROUP_KEY = "group";
    /**协议*/
    public static final String PROTOCOL_KEY = "protocol";
    /**主机/IP*/
    public static final String HOST_KEY = "host";
    /**端口号*/
    public static final String PORT_KEY = "port";
    /**路径*/
    public static final String PATH_KEY = "path";
    /**用户名称*/
    public static final String USER_KEY = "user";
    /**密码*/
    public static final String PASSWORD_KEY = "password";
    
    /**接口名称*/
    public static final String INTERFACE_KEY = "interface";
    
    public static final String SERIALIZATION_KEY = "serialization";
    public static final String PALYLOAD_KEY = "palyload";

    public static final String DEFAULT_SERIALIZATION = "java";
    
    public static final String TIMEOUT_KEY = "timeout";
    /**连接超时时间*/
    public static final String CONNECT_TIMEOUT_KEY = "connectTimeout";
    /**接收超时时间*/
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
    
    public static final String PROVIDER_CATEGORY = "provider";

    public static final String CONSUMER_CATEGORY = "consumer";

    public static final String ROUTER_CATEGORY = "router";

    public static final String CONFIGURATOR_CATEGORY = "configurator";
    
    public static final String DISCOVERY_KEY = "discovery";
    
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
    public static final int DEFAULT_WEIGHT = 100;
    /**优先级*/
    public static final String PRIORITY_KEY="priority";
    public static final int DEFAULT_PRIORITY = 5;
    
  
    /**生存时间*/
    public static final String   TTL_KEY="ttl";
    
    /**是否开启启动检测*/
    public static final String CHECK_KEY = "check";
    
    /**负载均衡*/
    public static final Object LOADBALANCE_KEY = "loadbalance";

    public static final String DEFAULT_LOADBALANCE = "random";

    public final static String PATH_SEPARATOR = "/";

    public static final String REMOTE_SERVICE_ID = "hola.rs.id";

    public static final String REMOTE_OBJECTCLASS = "hola.rs.objectclass";

    public static final String REMOTE_RANKING = "hola.rs.ranking";

    public static final String PROTOCOL_DISCOVERY = "discovery";

    public static final String CODEC_KEY = "codec";

    public static final String IS_SERVER = "server";

    public static final int DEFAULT_HEARTBEAT = 60 * 1000;
    
    /**
     * 集群时，启动是否检查可用
     */
    public static final String CLUSTER_AVAILABLE_CHECK = "cluster.available.check";

    /**
     * 集群时粘滞RemoteService
     */
    public static final Object CLUSTER_STICKY_KEY = "sticky";
    
    /**
     * 集群时调用失败重试其他provider
     */
    public static final String CLUSTER_RETRY_KEY = "retry";
    public static final int DEFAULT_CLUSTER_RETRY= 2;
    

    public static final String CLUSTER_FORK_KEY = "fork";

    public static final int DEFAULT_CLUSTER_FORK = 2;
 

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

   

    /**
     * 数据缓冲大小
     */
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
    
    /**
     *指定合并实现类型 
     */
    public static final String MERGER_KEY = "merger";

    /**
     * 时间戳
     */
    public static final String TIMESTAMP_KEY = "timestamp";

    public static final String WARMUP_KEY = "warmup";

    public static final int DEFAULT_WARMUP = 600000;

    /**
     * 集群路由实现类型
     */
    public static final Object ROUTER_KEY = "router";

    /**
     * 集群路由规则
     */
    public static final Object ROUTER_RULE_KEY = "router.rule";

    public static final String NO_AVAILABLE = "N/A";

    public static final String DEFAULT_KEY = "default";

    /**
     * 进程号
     */
    public static final String PID_KEY = "pid";

    public static final String SCOPE_KEY = "scope";

    public static final String MONITOR_KEY = "monitor";

    public static final String DELEGATE_FACTORY = "delegate";

    /**
     * 不在公告中显示的参数的前缀
     */
    public static final String DIC_HIDDEN_PREFIX = ".";

    public static final String CLUSTER_KEY = "cluster";




    



 

 

    
}
