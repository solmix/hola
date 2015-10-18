/*
 * Copyright 2015 The Solmix Project
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

package org.solmix.hola.transport.support;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageUtils;
import org.solmix.exchange.Pipeline;
import org.solmix.hola.common.HOLA;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年10月16日
 */
public class RemoteResponses
{

    private static final Logger LOG = LoggerFactory.getLogger(RemoteResponses.class);
    private static final Map<Long, Pipeline> PIPELINES = new ConcurrentHashMap<Long, Pipeline>();

    private static final Map<Long, Message> MESSAGES = new ConcurrentHashMap<Long, Message>();
    public static final  String START_TIME="RemoteResponses.scan.start";
    private RemoteResponses()
    {
    }

    public static void bind(Pipeline pipeline, Message message) {
        long id = message.getId();
        message.put(START_TIME, System.currentTimeMillis());
        MESSAGES.put(id, message);
        PIPELINES.put(id, pipeline);
    }

    /**
     * @param id
     */
    public static Message getOutMessage(long id) {
        return MESSAGES.get(id);

    }

    /**
     * @param response
     */
    public static void unbind(Message response) {
       MESSAGES.remove(response.getId());
       PIPELINES.remove(response.getId());
    }
    
    private static  class RemoteResponseScanner implements  Runnable {

       

        @Override
        public void run() {
            do{
                try{
                    for(Message msg:MESSAGES.values()){
                        int timeout=MessageUtils.getInt(msg, HOLA.TIMEOUT_KEY);
                        if(System.currentTimeMillis()-MessageUtils.getLong(msg, START_TIME)>timeout*2){
                            LOG.warn("message timeout with 2*timeout({}ms),message:{}",timeout*2,msg);
                            unbind(msg);
                        }
                    }
                    
                }catch(Throwable e){
                    LOG.error("Exception when scan the timeout response.", e);
                }
            }while(true);
        }
    }
    static {
        Thread th = new Thread(new RemoteResponseScanner(), "ResponseTimeoutScanner");
        th.setDaemon(true);
//        th.start();
    }

}
