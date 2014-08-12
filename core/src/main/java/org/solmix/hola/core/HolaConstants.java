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
package org.solmix.hola.core;

import java.util.regex.Pattern;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年7月8日
 */

public class HolaConstants
{
    public static final Pattern SPLIT_SEMICOLON_PATTERN            = Pattern.compile("\\s*[;]+\\s*");
    
    public static final Pattern SPLIT_COMMA_PATTERN                = Pattern.compile("\\s*[,]+\\s*");
    
    public final static String  PATH_SEPARATOR                     = "/";

    public static final String REMOTE_SERVICE_ID                   = "hola.rs.id";
    
    public static final String REMOTE_OBJECTCLASS                  = "hola.rs.objectclass";
    
    public static final String REMOTE_RANKING                      = "hola.rs.ranking";
    
    public static final String PROTOCOL_DISCOVERY                  = "discovery";
    
    public static final String CODEC_KEY                           = "hola.rs.codec";

    public static final String IS_SERVER                           = "server";
    
    public static final String KEY_HOST                            = "host";
    
    public static final String KEY_PORT                            = "port";
    
    public static final String KEY_PATH                            = "path";
    
    public static final String KEY_KEEPALIVE                       = "keepAlive";

    public static final String KEY_HEARTBEAT                       = "heartbeat";
    
    public static final String KEY_CODEC                           = "codec";
    
    public static final int    DEFAULT_HEARTBEAT                   = 60 * 1000;
    
    public static final String DEFAULT_KEY_PREFIX                  = "";

    public static final String KEY_HEARTBEAT_TIMEOUT               = "heartbeat.timeout";

    public static final String KEY_TIMEOUT = "timeout";

    public static final int DEFAULT_TIMEOUT = 0;

    public static final String KEY_SENT = "send";

    public static final String VERSION_KEY = "version";

    public static final String KEY_CHANNEL_ATTRIBUTE_READONLY = "channel.readonly";

    public static final String KEY_CHANNEL_SEND_READONLYEVENT = "channel.send.readonly";

    public static final String VERSION = "0.0.1";

    public static final String KEY_CHANNEL_READONLYEVENT_SENT = "channel.readonly.event.send";

    public static final String KEY_CHANNEL_ACCEPTS = "channel.accepts";

    public static final String KEY_IDLE_TIMEOUT = "idle.timeout";

    public static final String KEY_THREADS = "threads";

    public static final String KEY_CONNECT_TIMEOUT = "connect.timeout";

    public static final int DEFAULT_CHANNEL_ACCEPTS = 0;

    public static final int DEFAULT_IDLE_TIMEOUT = 600*1000;

    public static final String KEY_THREAD_NAME = "thread.name";

    public static final String DEFAULT_THREADPOOL = "pooled";

    public static final String KEY_THREADPOOL = "thread.pool";

    public static final String KEY_SHUTDOWN_TIMEOUT = "shutdown.timeout";

    public static final long DEFAULT_SHUTDOWN_TIMEOUT = 15*60*1000;

    public static final String KEY_RECONNECT_WARNING_PERIOD = "reconnect.warning.period";

    public static final int DEFAULT_RECONNECT_WARNING_PERIOD = 1800;

    public static final String KEY_CHECK = "check";

    /**
     * 发送数据的时候如果没有链接上是否需要重新连接
     */
    public static final String KEY_SEND_RECONNECT = "send.reconnect";

    public static final String KEY_RECONNECT = "reconnect";

    public static final int DEFAULT_RECONNECT_PERIOD = 2000;

    public static final int DEFAULT_IO_THREADS =  Runtime.getRuntime().availableProcessors() + 1;

    public static final String KEY_IO_THREADS = "io.threads";

    public static final String KEY_BUFFER = "buffer";

    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;

    public static final int     MAX_BUFFER_SIZE                    = 16 * 1024;

    public static final int     MIN_BUFFER_SIZE                    = 1 * 1024;

    public static final String KEY_SERIALIZATION = "serialization";

    public static final String DEFAULT_SERIALIZATION = "hola";

    public static final String KEY_DATALENGTH = "data.length";

    public static final int DEFAULT_DATALENGTH = 8 * 1024 * 1024;   

}
