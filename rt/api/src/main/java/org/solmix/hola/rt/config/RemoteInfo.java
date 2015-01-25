/*
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

package org.solmix.hola.rt.config;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.util.NetUtils;
import org.solmix.hola.common.identity.ID;
import org.solmix.hola.common.identity.IDFactory;
import org.solmix.hola.common.identity.support.URIID;

/**
 * 简化EndpointInfo中参数调用.
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年8月13日
 */
@ThreadSafe
public class RemoteInfo extends AbstractURIInfo<RemoteInfo>
{

    private static final long serialVersionUID = 7701475600214777271L;

//    private static final Logger LOG = LoggerFactory.getLogger(RemoteInfo.class);
    /**
     * 版本信息
     */
    public static final String VERSION = "version";

    /**
     * 群组名称
     */
    public static final String GROUP = "group";

    /**
     * 心跳周期
     */
    public static final String HEARTBEAT = "heartbeat";

    /**
     * 心跳超时时间
     */
    public static final String HEARTBEAT_TIMEOUT = "heartbeatTimeout";

    /**
     * 序列化实现名称:java/json/avro
     */
    public static final String SERIAL = "serial";

    /**
     * 交换层实现
     */
    public static final String EXCHANGER = "exchanger";

    /**
     * 传输层实现
     */
    public static final String TRANSPORT = "transporter";


    /**
     * 超时时间(ms)
     */
    public static final String TIMEOUT = "timeout";

    /**
     * 关闭等待时间
     */
    public static final String SHUTDOWN_TIMEOUT = "shutdownTimeout";

    /**
     * 连接超时时间(ms)
     */
    public static final String CONNECT_TIMEOUT = "connectTimeout";

    /**
     * 通道空闲超时(ms)
     */
    public static final String IDLE_TIMEOUT = "idleTimeout";

    
    /**
     * 编码/解码实现
     */
    public static final String CODEC = "codec";

    /**
     * 分发实现
     */
    public static final String DISPATHER = "dispather";

    /**
     * 连接数
     */
    public static final String CONNECTIONS = "connections";

    /**
     * 最大允许建立信道数
     */
    public static final String ACCEPTS = "accepts";

    /**
     * 信道buffer大小
     */
    public static final String BUFFER = "buffer";

    /**
     * 信道字符集
     */
    public static final String CHARSET = "charset";

    /**
     * IO线程数
     */
    public static final String IO_THREADS = "iothreads";

    /**
     * 每次通信负载
     */
    public static final String PAYLOAD = "payload";

    /**
     * 是否为服务端
     */
    public static final String SERVER = "server";

    /**
     * 是否启用等待超时策略
     */
    public static final String AWAIT = "await";

    /**
     * 是否启用只读模式
     */
    public static final String READ_ONLY = "readonly";

    /**
     * 是否启用重连
     */
    public static final String RECONNECT = "reconnect";

    /**
     * 重连周期
     */
    public static final String RECONNECT_PERIOD = "reconnectPeriod";

    /**
     * 发送事项是否启用重连
     */
    public static final String SEND_RECONNECT = "sendReconnect";

    /**
     * 重连警告周期,多次重连失败后发出警告信息
     */
    public static final String RECONNECT_WARNING_PERIOD = "reconnectWarningPeriod";

    /**
     * 是否检查启动错误,如果为true,当出现错误时抛错.
     */
    public static final String CHECK = "check";
    /**
     * 线程池实现
     */
    public static final String THREAD_POOL = "threadPool";

    /**
     * 线程名称
     */
    public static final String THREAD_NAME = "threadName";
    /**
     * 核心线程
     */
    public static  final  String EXECUTOR_CORE_THREADS="corethreads";
    /**
     * 线程
     */
    public static  final  String EXECUTOR_THREADS="threads";
    /**
     * 队列
     */
    public static  final  String EXECUTOR_QUEUES="queues";
    /**
     * 活动时间
     */
    public static  final  String EXECUTOR_ALIVE="alive";
  
    @Resource
    private IDFactory idFactory;

    /**
     * @param properties
     */
    public RemoteInfo(String protocol,  String host, int port)
    {
       this(protocol,null,null,host,port,null,null);
    }
    public RemoteInfo(String protocol,  String host, int port,String path,Map<String, Object> properties)
    {
       this(protocol,null,null,host,port,path,properties);
    }
    public RemoteInfo(String protocol, String username, String password, String host, int port, String path, Map<String, Object> properties) {
        super(protocol,username,password,host,port,path,properties);
        
    }

    public static RemoteInfo valueOf(URI uri) {
        if (uri == null)
            return null;
        Builder b = newBuilder();
        b.setProtocol(uri.getScheme());
        String userInfo = uri.getRawUserInfo();
        String username = null;
        String password = null;
        if (userInfo != null && userInfo.length() > 0) {
            int j = userInfo.indexOf(":");
            if (j >= 0) {
                password = userInfo.substring(j + 1);
                username = userInfo.substring(0, j);
            } else {
                username = userInfo;
            }
        }
        b.setUserName(username);
        b.setPassword(password);
        b.setHost(uri.getHost());
        b.setPort(uri.getPort());
        b.setPath(uri.getRawPath());
        Map<String, Object> param = parseQuery(uri.getRawQuery());
        if (param != null && param.size() > 0)
            b.setProperties(param);

        return b.build();
    }
   
    
    public static RemoteInfo valueOf(String uri){
        try {
            URI u = new URI(uri);
            return valueOf(u);
        } catch (URISyntaxException e) {
            throw  new IllegalArgumentException(e);
        }
    }
    

    public ID getRemoteID(){
        
        return idFactory.createID(URIID.class.getName(), new Object[]{toURI()});
    }
  

    /**
     * @return the reconnectPeriod
     */
    public Integer getReconnectPeriod() {
        return getInt(RECONNECT_PERIOD);
    }

    public Integer getReconnectPeriod(Integer defaultValue) {
        return getInt(RECONNECT_PERIOD, defaultValue);
    }

    /**
     * @return the check
     */
    public Boolean getCheck() {
        return getBoolean(CHECK);
    }

    public Boolean getCheck(boolean df) {
        return getBoolean(CHECK, df);
    }

  
    /**
     * @return the transporter
     */
    public String getTransport() {
        return getString(TRANSPORT);
    }

    public String getTransport(String df) {
        return getString(TRANSPORT, df);
    }

    /**
     * @return the threadName
     */
    public String getThreadName() {
        return getString(THREAD_NAME);
    }

    public String getThreadName(String defaultValue) {
        return getString(THREAD_NAME, defaultValue);
    }

    public Integer getCoreThreads(int dfValue) {
        return getInt(EXECUTOR_CORE_THREADS, dfValue);
    }
    public Integer getThreads() {
        return getInt(EXECUTOR_THREADS);
    }
    public Integer getThreads(int dfValue) {
        return getInt(EXECUTOR_THREADS, dfValue);
    }
    public Integer getQueues(int dfValue) {
        return getInt(EXECUTOR_QUEUES, dfValue);
    }
    public Integer getAlive(int dfValue) {
        return getInt(EXECUTOR_ALIVE, dfValue);
    }
    /**
     * @return the heartbeat
     */
    public Integer getHeartbeat() {
        return getInt(HEARTBEAT);
    }

    public Integer getHeartbeat(int defaultValue) {
        return getInt(HEARTBEAT, defaultValue);
    }

    /**
     * @return the serialName
     */
    public String getSerial() {
        return getString(SERIAL);
    }

    public String getSerial(String name) {
        return getString(SERIAL, name);
    }

    /**
     * @return the exchanger
     */
    public String getExchanger() {
        return getString(EXCHANGER);
    }

    public String getExchanger(String df) {
        return getString(EXCHANGER, df);
    }

    /**
     * @return the timeout
     */
    public Integer getTimeout() {
        return getInt(TIMEOUT);
    }

    public Integer getTimeout(Integer defaultValue) {
        return getInt(TIMEOUT, defaultValue);
    }

    /**
     * @return the threadPool
     */
    public String getThreadPool() {
        return getString(THREAD_POOL);
    }

    public String getThreadPool(String df) {
        return getString(THREAD_POOL, df);
    }

    /**
     * @return the codec
     */
    public String getCodec() {
        return getString(CODEC);
    }

    public String getCodec(String defaultName) {
        return getString(CODEC, defaultName);
    }

    /**
     * @return the dispather
     */
    public String getDispather() {
        return getString(DISPATHER);
    }

    public String getDispather(String df) {
        return getString(DISPATHER, df);
    }

    /**
     * @return the connections
     */
    public Integer getConnections() {
        return getInt(CONNECTIONS);
    }

    public Integer getConnections(int df) {
        return getInt(CONNECTIONS, df);
    }

    /**
     * @return the ioThreads
     */
    public Integer getIoThreads() {
        return getInt(IO_THREADS);
    }

    public Integer getIoThreads(int defaultValue) {
        return getInt(IO_THREADS, defaultValue);
    }

    /**
     * @return the server
     */
    public Boolean getServer() {
        return getBoolean(SERVER);
    }

    public Boolean getServer(boolean df) {
        return getBoolean(SERVER, df);
    }

    /**
     * @return the heartbeatTimeout
     */
    public Integer getHeartbeatTimeout() {
        return getInt(HEARTBEAT_TIMEOUT);
    }

    public Integer getHeartbeatTimeout(int defaultValue) {
        return getInt(HEARTBEAT_TIMEOUT, defaultValue);
    }

    /**
     * @return the shutdownTimeout
     */
    public Long getShutdownTimeout() {
        return getLong(SHUTDOWN_TIMEOUT);
    }

    public long getShutdownTimeout(Long defaultValue) {
        return getLong(SHUTDOWN_TIMEOUT, defaultValue);
    }

    /**
     * @return the connectTimeout
     */
    public Integer getConnectTimeout() {
        return getInt(CONNECT_TIMEOUT);
    }

    public Integer getConnectTimeout(Integer deafultValue) {
        return getInt(CONNECT_TIMEOUT, deafultValue);
    }

    /**
     * @return the idleTimeout
     */
    public Integer getIdleTimeout() {
        return getInt(IDLE_TIMEOUT);
    }

    public Integer getIdleTimeout(Integer defaultValue) {
        return getInt(IDLE_TIMEOUT, defaultValue);
    }

    /**
     * @return the accepts
     */
    public Integer getAccepts() {
        return getInt(ACCEPTS);
    }

    public Integer getAccepts(Integer defaultValue) {
        return getInt(ACCEPTS, defaultValue);
    }

    /**
     * @return the buffer
     */
    public Integer getBuffer() {
        return getInt(BUFFER);
    }

    public Integer getBuffer(int defaultValue) {
        return getInt(BUFFER, defaultValue);
    }

    /**
     * @return the charset
     */
    public String getCharset() {
        return getString(CHARSET);
    }

    /**
     * @return the payload
     */
    public Integer getPayload() {
        return getInt(PAYLOAD);
    }

    public Integer getPayload(int deafultValue) {
        return getInt(PAYLOAD, deafultValue);
    }

    /**
     * @return the await
     */
    public Boolean getAwait() {
        return getBoolean(AWAIT);
    }

    public Boolean getAwait(Boolean defaultValue) {
        return getBoolean(AWAIT, defaultValue);
    }

    /**
     * @return the readOnly
     */
    public Boolean getReadOnly() {
        return getBoolean(READ_ONLY);
    }

    /**
     * @return the reconnect
     */
    public Boolean getReconnect() {
        return getBoolean(RECONNECT);
    }

    public Boolean getReconnect(boolean defaultValue) {
        return getBoolean(RECONNECT, defaultValue);
    }

    /**
     * @return the sendReconnect
     */
    public Boolean getSendReconnect() {
        return getBoolean(SEND_RECONNECT);
    }

    /**
     * @return the reconnectWarningPeriod
     */
    public Integer getReconnectWarningPeriod() {
        return getInt(RECONNECT_WARNING_PERIOD);
    }

    /**
     * @param defaultValue
     * @return
     */
    public Integer getReconnectWarningPeriod(Integer defaultValue) {
        return getInt(RECONNECT_WARNING_PERIOD, defaultValue);
    }

    /**
     * @return
     */
    public String getVersion() {
        return getString(VERSION);
    }

    public String getVersion(String df) {
        return getString(VERSION, df);
    }

    /**
     * @return
     */
    public String getGroup() {
        return getString(GROUP);
    }

    @Override
    public String getAddress() {
        return getPort() <= 0 ? getHost() : getHost() + ":" + getPort();
    }

    public InetSocketAddress toInetSocketAddress() {
        return new InetSocketAddress(getHost(), getPort());
    }

    @Override
    public String getIp() {
        return NetUtils.getIpByHost(getHost());
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(AbstractURIInfo<?> info) {
        return new Builder(info);
    }

    public static class Builder
    {

        private  Map<String, Object> properties = new HashMap<String, Object>();
        protected  String protocol;
        protected  String username;
        protected  String password;
        protected  String host;
        protected  int port;
        protected  String path;
        private Builder()
        {
        }

        /**
         * @param info
         */
        public Builder(AbstractURIInfo<?> info)
        {
            properties.putAll(info.getProperties());
            protocol=info.getProtocol();
            username=info.getUsername();
            password=info.getPassword();
            host=info.getHost();
            port=info.getPort();
            path=info.getPath();
        }
        
        public Builder setPropertyIfAbsent(String key, Object value){
            if (key == null || key.length() == 0 || value == null)
                return this;
            if (hasProperty(key))
                return this;
            properties.put(key, value);
            return this;
        }

        public boolean hasProperty(String key){
            Object value = properties.get(key);
            return value != null;
        }
        /**
         * @param heartbeat the heartbeat to set
         */
        public Builder setHeartbeat(Integer heartbeat) {
            properties.put(HEARTBEAT, heartbeat);
            return this;
        }

        public Builder setCoreThreads(Integer coreThreads) {
            properties.put(EXECUTOR_CORE_THREADS, coreThreads);
            return this;
        }
        public Builder setThreads(Integer threads) {
            properties.put(EXECUTOR_THREADS, threads);
            return this;
        }
        public Builder setQueues(int queues) {
            properties.put(EXECUTOR_QUEUES, queues);
            return this;
        }
        public Builder setAlive(int alive) {
            properties.put(EXECUTOR_ALIVE, alive);
            return this;
        }
        /**
         * @param transporter the transporter to set
         */
        public Builder setTransport(String transport) {
            properties.put(TRANSPORT, transport);
            return this;
        }

        /**
         * @param serialName the serialName to set
         */
        public Builder setSerialName(String serialName) {
            properties.put(SERIAL, serialName);
            return this;
        }

        /**
         * @param exchanger the exchanger to set
         */
        public Builder setExchanger(String exchangeName) {
            properties.put(EXCHANGER, exchangeName);
            return this;
        }

        /**
         * @param threadName the threadName to set
         */
        public Builder setThreadName(String threadName) {
            properties.put(THREAD_NAME, threadName);
            return this;
        }

        /**
         * @param timeout the timeout to set
         */
        public Builder setTimeout(Integer timeout) {
            properties.put(TIMEOUT, timeout);
            return this;
        }

        /**
         * @param shutdownTimeout the shutdownTimeout to set
         */
        public Builder setShutdownTimeout(Long shutdownTimeout) {
            properties.put(SHUTDOWN_TIMEOUT, shutdownTimeout);
            return this;
        }

        /**
         * @param connectTimeout the connectTimeout to set
         */
        public Builder setConnectTimeout(Integer connectTimeout) {
            properties.put(CONNECT_TIMEOUT, connectTimeout);
            return this;
        }

        /**
         * @param idleTimeout the idleTimeout to set
         */
        public Builder setIdleTimeout(Integer idleTimeout) {
            properties.put(IDLE_TIMEOUT, idleTimeout);
            return this;
        }

        /**
         * @param threadPool the threadPool to set
         */
        public Builder setThreadPool(String threadPool) {
            properties.put(THREAD_POOL, threadPool);
            return this;
        }

        /**
         * @param codec the codec to set
         */
        public Builder setCodec(String codec) {
            properties.put(CODEC, codec);
            return this;
        }

        /**
         * @param dispather the dispather to set
         */
        public Builder setDispather(String dispather) {
            properties.put(DISPATHER, dispather);
            return this;
        }

        /**
         * @param connections the connections to set
         */
        public Builder setConnections(Integer connections) {
            properties.put(CONNECTIONS, connections);
            return this;
        }

        /**
         * @param accepts the accepts to set
         */
        public Builder setAccepts(Integer accepts) {
            properties.put(ACCEPTS, accepts);
            return this;
        }
        public Builder setPort(int  port) {
            this.port=port;
             return this;
         }
        public Builder setPath(String  path) {
           this.path=path;
            return this;
        }
        public Builder setUserName(String  username) {
            this.username=username;
             return this;
         }
        public Builder setPassword(String  password) {
            this.password=password;
             return this;
         }
        
        public Builder setProtocol(String  protocol) {
            this.protocol=protocol;
             return this;
         }
        /**
         * @param buffer the buffer to set
         */
        public Builder setBuffer(Integer buffer) {
            properties.put(BUFFER, buffer);
            return this;
        }

        /**
         * @param charset the charset to set
         */
        public Builder setCharset(String charset) {
            properties.put(CHARSET, charset);
            return this;
        }

        /**
         * @param ioThreads the ioThreads to set
         */
        public Builder setIoThreads(Integer ioThreads) {
            properties.put(IO_THREADS, ioThreads);
            return this;
        }

        /**
         * @param payload the payload to set
         */
        public Builder setPayload(Integer payload) {
            properties.put(PAYLOAD, payload);
            return this;
        }

        /**
         * @param server the server to set
         */
        public Builder setServer(Boolean server) {
            properties.put(SERVER, server);
            return this;
        }

        /**
         * @param await the await to set
         */
        public Builder setAwait(Boolean await) {
            properties.put(AWAIT, await);
            return this;
        }

        /**
         * @param readOnly the readOnly to set
         */
        public Builder setReadOnly(Boolean readOnly) {
            properties.put(READ_ONLY, readOnly);
            return this;
        }
        
        public Builder setProperties(Map<String,Object> properties) {
            this.properties=new HashMap<String, Object>(properties);
            return this;
        }

        /**
         * @param reconnect the reconnect to set
         */
        public Builder setReconnect(Boolean reconnect) {
            properties.put(RECONNECT, reconnect);
            return this;
        }

        /**
         * @param reconnectPeriod the reconnectPeriod to set
         */
        public Builder setReconnectPeriod(Integer reconnectPeriod) {
            properties.put(RECONNECT_PERIOD, reconnectPeriod);
            return this;
        }

        /**
         * @param sendReconnect the sendReconnect to set
         */
        public Builder setSendReconnect(Boolean sendReconnect) {
            properties.put(SEND_RECONNECT, sendReconnect);
            return this;
        }

        /**
         * @param host the host to set
         */
        public Builder setHost(String host) {
           this.host=host;
            return this;
        }

      
        /**
         * 多少次连接失败后输出一次警告信息.
         */
        public Builder setReconnectWarningPeriod(Integer reconnectWarningPeriod) {
            properties.put(RECONNECT_WARNING_PERIOD, reconnectWarningPeriod);
            return this;
        }

        /**
         * 启动错误检查
         */
        public Builder setCheck(Boolean check) {
            properties.put(CHECK, check);
            return this;
        }

        public RemoteInfo build() {

            return new RemoteInfo(protocol,username,password,host,port,path,properties);
        }

        /**
         * @param i
         * @return
         */
        public Builder setHeartbeatTimeout(Integer i) {
            properties.put(HEARTBEAT_TIMEOUT, i);
            return this;
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rt.config.ExtensionInfo#getSelf()
     */
    @Override
    protected RemoteInfo getSelf() {
        return this;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rt.config.ExtensionInfo#makeSelf(java.util.Map)
     */
    @Override
    protected RemoteInfo makeSelf(Map<String, Object> map) {
        return new RemoteInfo(protocol,username,password,host,port,path,map);
    }

}
