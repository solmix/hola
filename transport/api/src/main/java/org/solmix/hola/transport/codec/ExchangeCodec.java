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

package org.solmix.hola.transport.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.io.Bytes;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.serialize.Serialization;
import org.solmix.hola.transport.channel.Channel;
import org.solmix.hola.transport.exchange.DefaultFuture;
import org.solmix.hola.transport.exchange.Request;
import org.solmix.hola.transport.exchange.Response;
import org.solmix.runtime.Extension;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年8月11日
 */
@Extension(name = ExchangeCodec.NAME)
public class ExchangeCodec extends SerializeCodec implements Codec
{

    private static final Logger LOG = LoggerFactory.getLogger(ExchangeCodec.class.getName());

    public static final String NAME = "exchange";

    private static final int HEADER_LENGTH = 16;

    protected static final short HEADER = 0x1314;

    protected static final byte HEADER_H = Bytes.short1(HEADER);

    protected static final byte HEADER_L = Bytes.short0(HEADER);

    protected static final byte FLAG_REQUEST = (byte) 0x80;

    protected static final byte FLAG_TWOWAY = (byte) 0x40;

    protected static final byte FLAG_EVENT = (byte) 0x20;

    protected static final int SERIALIZATION_MASK = 0x1f;

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.codec.Codec#encode(org.solmix.hola.transport.channel.Channel,
     *      io.netty.buffer.ByteBuf, java.lang.Object)
     */
    @Override
    public void encode(Channel channel, ByteBuf buffer, Object msg)
        throws IOException {
        if (msg instanceof Request) {
            encodeRequest(channel, buffer, (Request) msg);
        } else if (msg instanceof Response) {
            encodeResponse(channel, buffer, (Response) msg);
        } else {
            super.encode(channel, buffer, msg);
        }
    }

    /**
     * @param channel
     * @param buffer
     * @param msg
     * @throws IOException
     */
    protected void encodeResponse(Channel channel, ByteBuf buffer, Response res)
        throws IOException {
        try {
            Serialization serialization = getSerialization(channel.getInfo());
            // header.
            byte[] header = new byte[HEADER_LENGTH];
            // set magic number.
            Bytes.short2bytes(HEADER, header);
            // set request and serialization flag.
            header[2] = serialization.getSerializeId();
            if (res.isHeartbeat())
                header[2] |= FLAG_EVENT;
            // set response status.
            byte status = res.getStatus();
            header[3] = status;
            // set request id.
            Bytes.long2bytes(res.getId(), header, 4);

            int savedWriteIndex = buffer.writerIndex();
            buffer.writerIndex(savedWriteIndex + HEADER_LENGTH);
            ByteBufOutputStream bos = new ByteBufOutputStream(buffer);
            ObjectOutput out = serialization.serialize(channel.getInfo(), bos);
            // encode response data or error message.
            if (status == Response.OK) {
                if (res.isHeartbeat()) {
                    encodeHeartbeatData(channel, out, res.getResult());
                } else {
                    encodeResponseData(channel, out, res.getResult());
                }
            } else{
                if(res.getErrorMessage()!=null)
                    out.writeUTF(res.getErrorMessage());
            }
               
            out.flush();
            bos.flush();
            bos.close();

            int len = bos.writtenBytes();
            checkLength(channel, len);
            Bytes.int2bytes(len, header, 12);
            // write
            buffer.writerIndex(savedWriteIndex);
            buffer.writeBytes(header); // write header.
            buffer.writerIndex(savedWriteIndex + HEADER_LENGTH + len);
        } catch (Throwable t) {
            // 发送失败信息给Consumer，否则Consumer只能等超时了
            if (!res.isEvent() && res.getStatus() != Response.BAD_RESPONSE) {
                try {
                    // FIXME 在Codec中打印出错日志？在IoHanndler的caught中统一处理？
                    LOG.warn(
                        "Fail to encode response: " + res
                            + ", send bad_response info instead, cause: "
                            + t.getMessage(), t);

                    Response r = new Response(res.getId(), res.getVersion());
                    r.setStatus(Response.BAD_RESPONSE);
                    r.setErrorMessage("Failed to send response: " + res
                        + ", cause: " + StringUtils.toString(t));
                    channel.send(r);

                    return;
                } catch (Exception e) {
                    LOG.warn("Failed to send bad_response info back: " + res
                        + ", cause: " + e.getMessage(), e);
                }
            }

            // 重新抛出收到的异常
            if (t instanceof IOException) {
                throw (IOException) t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else if (t instanceof Error) {
                throw (Error) t;
            } else {
                throw new RuntimeException(t.getMessage(), t);
            }
        }

    }

    protected void encodeResponseData(Channel channel, ObjectOutput out,
        Object data) throws IOException {
        encodeResponseData(out, data);
    }

    protected void encodeResponseData(ObjectOutput out, Object data)
        throws IOException {
        out.writeObject(data);
    }

    protected void encodeHeartbeatData(Channel channel, ObjectOutput out,
        Object data) throws IOException {
        encodeHeartbeatData(out, data);
    }

    protected void encodeHeartbeatData(ObjectOutput out, Object data)
        throws IOException {
        encodeEventData(out, data);
    }

    /**
     * @param channel
     * @param buffer
     * @param msg
     * @throws IOException
     */
    protected void encodeRequest(Channel channel, ByteBuf buffer, Request req)
        throws IOException {
        Serialization serialization = getSerialization(channel.getInfo());
        // 设置header
        byte[] header = new byte[HEADER_LENGTH];
        // 前导符
        Bytes.short2bytes(HEADER, header);
        // 序列化方法.
        header[2] = (byte) (FLAG_REQUEST | serialization.getSerializeId());

        if (req.isTwoWay())
            header[2] |= FLAG_TWOWAY;
        if (req.isEvent())
            header[2] |= FLAG_EVENT;

        // 设置4个字节的request id.
        Bytes.long2bytes(req.getId(), header, 4);
        int savedWriteIndex = buffer.writerIndex();
        // 跳过头部开始写入数据
        buffer.writerIndex(savedWriteIndex + HEADER_LENGTH);

        ByteBufOutputStream bos = new ByteBufOutputStream(buffer);
        ObjectOutput out = serialization.serialize(channel.getInfo(), bos);
        if (req.isEvent()) {
            encodeEventData(channel, out, req.getData());
        } else {
            encodeRequestData(channel, out, req.getData());
        }
        out.flush();
        bos.flush();
        bos.close();
        int len = bos.writtenBytes();
        checkLength(channel, len);
        // 4个字节数据长度
        Bytes.int2bytes(len, header, 12);

        // 写入头信息
        buffer.writerIndex(savedWriteIndex);
        buffer.writeBytes(header); // write header.
        buffer.writerIndex(savedWriteIndex + HEADER_LENGTH + len);
    }

    /**
     * @param channel
     * @param out
     * @param data
     * @throws IOException
     */
    protected void encodeRequestData(Channel channel, ObjectOutput out,
        Object data) throws IOException {
        encodeData(out, data);
    }

    protected void encodeRequestData(ObjectOutput out, Object data)
        throws IOException {
        out.writeObject(data);
    }

    /**
     * @param channel
     * @param out
     * @param data
     * @throws IOException
     */
    private void encodeEventData(Channel channel, ObjectOutput out, Object data)
        throws IOException {
        encodeEventData(out, data);

    }

    /**
     * @param out
     * @param data
     * @throws IOException
     */
    private void encodeEventData(ObjectOutput out, Object data)
        throws IOException {
        out.writeObject(data);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.transport.codec.Codec#decode(org.solmix.hola.transport.channel.Channel,
     *      io.netty.buffer.ByteBuf)
     */
    @Override
    public Object decode(Channel channel, ByteBuf buffer) throws IOException {
        int readable = buffer.readableBytes();
        byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
        buffer.readBytes(header);
        return decode(channel, buffer, readable, header);
    }

    protected Object decode(Channel channel, ByteBuf buffer, int readable,
        byte[] header) throws IOException {
        if (readable > 0 && header[0] != HEADER_H || readable > 1
            && header[1] != HEADER_L) {
            return super.decode(channel, buffer.resetReaderIndex());
        }
        if (readable < HEADER_LENGTH)
            return DecodeResult.NEED_MORE_INPUT;
        int len = Bytes.bytes2int(header, 12);
        checkLength(channel, len);
        int tt = len + HEADER_LENGTH;
        if (readable < tt) {
            return DecodeResult.NEED_MORE_INPUT;
        }
        ByteBufInputStream input = new ByteBufInputStream(buffer);
        return decodeBody(channel, input, header);
    }

    protected Object decodeBody(Channel channel, ByteBufInputStream input,
        byte[] header) throws IOException {
        byte flag = header[2], proto = (byte) (flag & SERIALIZATION_MASK);
        Serialization ser = serializationManager.getSerialization(
            channel.getInfo(), proto);

        ObjectInput in = ser.deserialize(channel.getInfo(), input);
        long id = Bytes.bytes2long(header, 4);
        if ((flag & FLAG_REQUEST) == 0) {
            Response res = new Response(id);
            if ((flag & FLAG_EVENT) != 0) {
                res.setEvent(Response.HEARTBEAT_EVENT);
            }
            byte status = header[3];
            res.setStatus(status);
            if (status == Response.OK) {
                try {
                    Object data;
                    if (res.isHeartbeat()) {
                        data = decodeHeartbeatData(channel, in);
                    } else if (res.isEvent()) {
                        data = decodeEventData(channel, in);
                    } else {
                        data = decodeResponseData(channel, in,
                            getRequestData(id));
                    }
                    res.setResult(data);
                } catch (Throwable t) {
                    res.setStatus(Response.CLIENT_ERROR);
                    res.setErrorMessage(StringUtils.toString(t));
                }
            } else {
                res.setErrorMessage(in.readUTF());
            }
            return res;
        } else {
            Request req = new Request(id);
            req.setVersion("0.1.1");
            req.setTwoWay((flag & FLAG_TWOWAY) != 0);
            if ((flag & FLAG_EVENT) != 0) {
                req.setEvent(Request.HEARTBEAT_EVENT);
            }
            try {
                Object data;
                if (req.isHeartbeat()) {
                    data = decodeHeartbeatData(channel, in);
                } else if (req.isEvent()) {
                    data = decodeEventData(channel, in);
                } else {
                    data = decodeRequestData(channel, in);
                }
                req.setData(data);
            } catch (Throwable t) {
                // bad request
                req.setBroken(true);
                req.setData(t);
            }
            return req;
        }
    }

    protected Object decodeResponseData(Channel channel, ObjectInput in,
        Object requestData) throws IOException {
        return decodeResponseData(channel, in);
    }

    protected Object decodeRequestData(Channel channel, ObjectInput in)
        throws IOException {
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString(e));
        }
    }

    protected Object getRequestData(long id) {
        DefaultFuture future = DefaultFuture.getFuture(id);
        if (future == null)
            return null;
        Request req = future.getRequest();
        if (req == null)
            return null;
        return req.getData();
    }

    protected Object decodeResponseData(Channel channel, ObjectInput in)
        throws IOException {
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString(e));
        }
    }

    protected Object decodeEventData(Channel channel, ObjectInput in)
        throws IOException {
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString(e));
        }
    }

    protected Object decodeHeartbeatData(Channel channel, ObjectInput in)
        throws IOException {
        try {
            return in.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(StringUtils.toString(e));
        }
    }

    /**
     * @param channel
     * @param len
     * @throws IOException
     */
    protected void checkLength(Channel channel, int len) throws IOException {
        int limit = HolaConstants.DEFAULT_PALYLOAD;
        if (channel != null && channel.getInfo() != null)
            limit = channel.getInfo().getPayload(HolaConstants.DEFAULT_PALYLOAD);
        if (limit > 0 && len > limit) {
            IOException e = new IOException("Data length too large: " + len
                + ", limit: " + limit + ", channel: " + channel);
            LOG.error("", e);
            throw e;
        }
    }

}
