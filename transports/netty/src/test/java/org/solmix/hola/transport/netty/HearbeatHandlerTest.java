/**
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

package org.solmix.hola.transport.netty;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Dictionary;


import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.solmix.commons.util.NetUtils;
import org.solmix.exchange.ExchangeRuntimeException;
import org.solmix.exchange.Message;
import org.solmix.exchange.Processor;
import org.solmix.exchange.Transporter;
import org.solmix.exchange.model.EndpointInfo;
import org.solmix.exchange.support.DefaultMessage;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.serial.ObjectInput;
import org.solmix.hola.common.serial.ObjectOutput;
import org.solmix.hola.common.serial.SerialConfiguration;
import org.solmix.hola.common.serial.SerializationManager;
import org.solmix.hola.transport.RemoteAddress;
import org.solmix.hola.transport.RemoteProtocol;
import org.solmix.hola.transport.codec.RemoteCodec;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2016年4月29日
 */

public class HearbeatHandlerTest extends AbstractExContainerSupport
{


    private static final int PORT1 = NetUtils.getAvailablePort();

 

    volatile int sendHeartbeatCount = 0;

    @Test
    public void test() throws IOException {
        NettyTransportFactory factory = new NettyTransportFactory();
        String address = "hola://localhost:" + PORT1 + "/heartbeat";
        String client = address + "?heartbeat=100&heartbeat.timeout=500";
        EndpointInfo ei = getepi(address, factory);
        EndpointInfo cei = getepi(client, factory);

        Transporter trans = factory.getTransporter(ei, container);
        Assert.assertNotNull(trans);
        RemoteProtocol pro = EasyMock.createMock(RemoteProtocol.class);

        RemoteCodec codec = new RemoteCodec() {

            @Override
            protected void encodeEventBody(ByteBuf buffer, ObjectOutput objectOut, Message outMsg) throws IOException {
                Object data = outMsg.getContent(Object.class);
                if (data == HeartbeatHandler.HEARTBEAT_EVENT) {
                    sendHeartbeatCount++;
                }
                super.encodeEventBody(buffer, objectOut, outMsg);
            }

            @Override
            protected Object decodeEventBody(ObjectInput input) throws IOException {
                if (sendHeartbeatCount > 5) {
                    return 1;
                } else {
                    return super.decodeEventBody(input);
                }

            }
        };
        codec.setSerializationManager(container.getExtension(SerializationManager.class));
        codec.setSerialConfiguration(new SerialConfiguration("hola"));
        EasyMock.expect(pro.getCodec()).andReturn(codec).anyTimes();
        EasyMock.expect(pro.createMessage()).andReturn(new DefaultMessage()).anyTimes();
        EasyMock.replay(pro);
        trans.setProtocol(pro);
        trans.setProcessor(new Processor() {

            @Override
            public void process(Message message) throws ExchangeRuntimeException {

            }
        });

        NettyPipeline pl = (NettyPipeline) factory.getPipeline(cei, container);
        Assert.assertNotNull(pl);
        DefaultMessage msg = new DefaultMessage();
        msg.put(Message.ENDPOINT_ADDRESS, client);
        pl.setProtocol(pro);
        pl.prepare(msg);
        // 等待发送完成
        sleep(3000);
        // 接受一定数量的心跳
        Assert.assertTrue(sendHeartbeatCount >= 5);
        // 超过一定时间不能正常返回心跳,超时断连接
        Assert.assertFalse(pl.channel.isActive());
        // Assert.assertEquals(5, sendHeartbeatCount);
    }

    private EndpointInfo getepi(String address, NettyTransportFactory factory) {
        EndpointInfo ei = new EndpointInfo();
        ei.setAddress(address);
        Dictionary<String, Object> dic = PropertiesUtils.toProperties(address);
        RemoteAddress ra = new RemoteAddress(dic);
        ei.addExtension(ra);

        PropertiesUtils.makeConfigAsEndpointInfoExtension(factory, ei, dic);
        return ei;
    }

    private void sleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
