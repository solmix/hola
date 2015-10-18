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

package org.solmix.hola.rs.generic.codec;

import static org.solmix.hola.rs.generic.codec.HolaCodec.RESPONSE_NO_NULL;
import static org.solmix.hola.rs.generic.codec.HolaCodec.RESPONSE_NULL;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.solmix.commons.util.ClassDescUtils;
import org.solmix.commons.util.ObjectUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.exchange.Attachment;
import org.solmix.exchange.Exchange;
import org.solmix.exchange.Message;
import org.solmix.exchange.MessageList;
import org.solmix.exchange.interceptor.InterceptorChain;
import org.solmix.exchange.model.ArgumentInfo;
import org.solmix.exchange.model.MessageInfo;
import org.solmix.exchange.model.NamedID;
import org.solmix.exchange.model.NamedIDPolicy;
import org.solmix.exchange.model.OperationInfo;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.serial.ObjectInput;
import org.solmix.hola.serial.SerialConfiguration;
import org.solmix.hola.transport.codec.Decodeable;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年10月15日
 */

public class DecodeableMessage implements Decodeable, Message
{

    private Message message;

    private ObjectInput input;

    private InputStream inputStream;

    private SerialConfiguration serialConfiguration;

    private volatile boolean hasDecoded;

    public DecodeableMessage(Message inMsg, ObjectInput input)
    {
        this(inMsg, input, null);
    }

    public DecodeableMessage(Message inMsg, ObjectInput input, Exchange exchange)
    {
        this.message = inMsg;
        this.input = input;
        this.setExchange(exchange);
    }

    @Override
    public void decode() throws Exception {
        if (!hasDecoded && input != null) {
            try {
                if (isRequest()) {
                    DecodeableMessage.decodeRequest(this, input);
                } else {
                    DecodeableMessage.decodeResponse(this, input, getExchange());
                }

            } finally {
                hasDecoded = true;
            }

        }
    }

    static void decodeResponse(Message msg, ObjectInput input, Exchange ex) throws IOException {
        try {
            msg.setExchange(ex);
            OperationInfo oi = ex.get(OperationInfo.class);
            MessageInfo output = oi.getOutput();
            byte flag = input.readByte();
            switch (flag) {
                case RESPONSE_NULL:
                    break;
                case RESPONSE_NO_NULL:
                    List<ArgumentInfo> returnMsgs = output.getArguments();
                    if (returnMsgs != null) {
                        Object[] args = new Object[returnMsgs.size()];
                        for (int i = 0; i < returnMsgs.size(); i++) {
                            ArgumentInfo ai = returnMsgs.get(i);
                            args[i] = input.readObject(ai.getTypeClass());
                        }
                        msg.setContent(List.class, new MessageList(args));
                    } else {
                        throw new IllegalArgumentException();
                    }
            }
        } catch (ClassNotFoundException cnf) {
            throw new IOException(StringUtils.toString("Read Response data failed.", cnf));
        } catch (Exception e) {
            msg.setContent(Exception.class, e);
        }
    }

    static void decodeRequest(Message msg, ObjectInput input) throws IOException {
        try {
            msg.put(HOLA.HOLA_VERSION_KEY, input.readUTF());
            msg.put(Message.PATH_INFO, input.readUTF());
            msg.put(HOLA.VERSION_KEY, input.readUTF());
            String operationId = input.readUTF();
            NamedID operationName = NamedID.formIdentityString(operationId);
            msg.put(Message.OPERATION, operationName);
            String name = operationName.getName();
            String paramDesc = NamedIDPolicy.getParamsDescFromOperationName(name);
            Object[] args;
            Class<?>[] pts;
            if (paramDesc.length() == 0) {
                args = ObjectUtils.EMPTY_OBJECT_ARRAY;
                pts = ObjectUtils.EMPTY_CLASS_ARRAY;
            } else {
                pts = ClassDescUtils.typeDesc2ClassArray(paramDesc);
                args = new Object[pts.length];
            }
            for (int i = 0; i < pts.length; i++) {
                Class<?> ai = pts[i];
                args[i] = input.readObject(ai);
            }
            MessageList ml = new MessageList(args);
            msg.setContent(List.class, ml);
        } catch (ClassNotFoundException cnf) {
            throw new IOException(StringUtils.toString("Read Request data failed.", cnf));
        } catch (Exception e) {
            msg.setContent(Exception.class, e);
        }

    }

    @Override
    public <T> T get(Class<T> key) {
        return message.get(key);
    }

    @Override
    public <T> void put(Class<T> key, T value) {
        message.put(key, value);
    }

    @Override
    public int size() {
        return message.size();
    }

    @Override
    public boolean isEmpty() {
        return message.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return message.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return message.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return message.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return message.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return message.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        message.putAll(m);
    }

    @Override
    public void clear() {
        message.clear();
    }

    @Override
    public Set<String> keySet() {
        return message.keySet();
    }

    @Override
    public Collection<Object> values() {
        return message.values();
    }

    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet() {
        return message.entrySet();
    }

    @Override
    public boolean isRequest() {
        return message.isRequest();
    }

    @Override
    public void setRequest(boolean isRequest) {
        message.setRequest(isRequest);

    }

    @Override
    public boolean isInbound() {
        return message.isInbound();
    }

    @Override
    public void setInbound(boolean isRequest) {
        message.setInbound(isRequest);
    }

    @Override
    public long getId() {
        return message.getId();
    }

    @Override
    public void setId(long id) {
        message.setId(id);
    }

    @Override
    public InterceptorChain getInterceptorChain() {
        return message.getInterceptorChain();
    }

    @Override
    public void setInterceptorChain(InterceptorChain chain) {
        message.setInterceptorChain(chain);
    }

    @Override
    public Exchange getExchange() {
        return message.getExchange();
    }

    @Override
    public void setExchange(Exchange e) {
        message.setExchange(e);
    }

    @Override
    public <T> T getContent(Class<T> type) {
        return message.getContent(type);
    }

    @Override
    public <T> void setContent(Class<T> type, Object content) {
        message.setContent(type, content);
    }

    @Override
    public Set<Class<?>> getContentTypes() {
        return message.getContentTypes();
    }

    @Override
    public <T> void removeContent(Class<T> type) {
        message.removeContent(type);
    }

    @Override
    public Collection<Attachment> getAttachments() {
        return message.getAttachments();
    }

    @Override
    public void setAttachments(Collection<Attachment> attachments) {
        message.setAttachments(attachments);
    }

}
