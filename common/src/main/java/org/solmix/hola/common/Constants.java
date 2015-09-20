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

public class Constants
{

    public static final String DEFAULT_TRANSPORTER = "netty";

    public static final String DEFAULT_PROTOCOL = "hola";

    public static final Pattern SPLIT_SEMICOLON_PATTERN = Pattern.compile("\\s*[;]+\\s*");

    public static final Pattern REGISTRY_SPLIT_PATTERN = Pattern.compile("\\s*[|;]+\\s*");

    public static final Pattern SPLIT_COMMA_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    public final static String PATH_SEPARATOR = "/";

    public static final String REMOTE_SERVICE_ID = "hola.rs.id";

    public static final String REMOTE_OBJECTCLASS = "hola.rs.objectclass";

    public static final String REMOTE_RANKING = "hola.rs.ranking";

    public static final String PROTOCOL_DISCOVERY = "discovery";

    public static final String CODEC_KEY = "codec";

    public static final String IS_SERVER = "server";

    public static final int DEFAULT_HEARTBEAT = 60 * 1000;

    public static final String DEFAULT_KEY_PREFIX = "";

    public static final int DEFAULT_TIMEOUT = 1000;

    public static final String VERSION_KEY = "version";

    public static final String VERSION = "0.0.1";

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

    public static final String KEY_SERIALIZATION = "serialization";

    public static final String DEFAULT_SERIALIZATION = "java";

    public static final String KEY_DATALENGTH = "data.length";

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

    public static final String KEY_DYNAMIC = "hola.dynamic";

    public static final String SERIALIZATION_KEY = "serialization";

    public static final String DEFAULT_RPC_SERIALIZATION = "hola";

    public static final String TRANSPORTER_KEY = "transporter";

    public static final String DEFAULT_RPC_TRANSPORTER = "netty";

    public static final String PROTOCOL_KEY = "protocol";

    public static final String BUFFER_KEY = "buffer";

    public static final String HOST_KEY = "host";

    public static final String ANYHOST_KEY = "anyhost";

    public static final String ANYHOST_VALUE = "0.0.0.0";

    public static final String LOCALHOST_KEY = "localhost";

    public static final String LOCALHOST_VALUE = "127.0.0.1";

    public static final String PORT_KEY = "port";

    public static final String THREADS_KEY = "threads";

    public static final int DEFAULT_THREADS = 200;

    public static final String WAIT_KEY = "wait";

    public static final boolean DEFAULT_WAIT = false;

    public static final String TIMEOUT_KEY = "timeout";

    public static final String CONNECT_TIMEOUT_KEY = "connectTimeout";

    public static final String RECEIVE_TIMEOUT_KEY = "receiveTimeout";

    
    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;

    public static final int DEFAULT_RECEIVE_TIMEOUT = 3000;

    public static final int DEFAULT_WRITE_TIMEOUT = 3000;

    public static final String SERVICE_ID_KEY = "remote.service.id";

    public static final String GROUP_KEY = "group";

    public static final String PATH_KEY = "path";

    public static final String PIPELINES = "pipelines";
}
