/**
 * Copyright (c) 2014 The Solmix Project
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
package org.solmix.hola.transport.local;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Assert;
import org.junit.Test;
//import org.solmix.exchange.Exchange;
import org.solmix.exchange.ExchangeRuntimeException;
import org.solmix.exchange.Message;
//import org.solmix.exchange.MessageList;
import org.solmix.exchange.Pipeline;
import org.solmix.exchange.Processor;
import org.solmix.exchange.Transporter;
//import org.solmix.exchange.support.DefaultExchange;
//import org.solmix.exchange.support.DefaultMessage;
//import org.solmix.runtime.Container;
//import org.solmix.runtime.ContainerFactory;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年12月22日
 */

public class LocalTest extends Assert {
    
    @Test
    public void testDirectDispatch() throws Exception{
//        test(true,1000);
    }
  /*  
    @Test
    public void testDirectDispatchWithFileCache() throws Exception{
        test(true,100000);
    }
    @Test
    public void testPipeDispatch() throws Exception{
        test(false,1000);
    }*/
    
    /*@Test
    public void testResponseCode() throws IOException{
        LocalTransportFactory ltf = new LocalTransportFactory();
        
        RemoteServiceInfo rei =  RemoteServiceInfo.valueOf("local://localhost/local");
        Container con = ContainerFactory.getDefaultContainer();
        LocalPipeline pl= (LocalPipeline)ltf.getPipeline(rei, con);
        Transporter trans =ltf.getTransporter(rei, con);
        
        trans.setProcessor(new EchoProcessor());
        ResponseProcessor rp = new ResponseProcessor();
        pl.setProcessor(rp);
        
        DefaultMessage dm = new DefaultMessage();
        dm.put(Transporter.class, trans);
        dm.put(LocalPipeline.IN_PIPE, pl);
        Exchange ex =new  DefaultExchange();
        ex.put(Container.class, con);
        dm.setExchange(ex);
        
        Integer code = (Integer)dm.get(Message.RESPONSE_CODE);
        assertNull(code);
        
        Pipeline pipeline=trans.getBackPipeline(dm);
        pipeline.close(dm);
        
        code = (Integer)dm.get(Message.RESPONSE_CODE);
        assertNotNull(code);
        assertEquals(200, code.intValue());
        
    }
    
    public void test(boolean direct,int length) throws Exception{
        LocalTransportFactory ltf = new LocalTransportFactory();
        
//        RemoteServiceInfo rei =  RemoteServiceInfo.valueOf("local://localhost/local");
        Container con = ContainerFactory.getDefaultContainer();
        LocalPipeline pl= (LocalPipeline)ltf.getPipeline(rei, con);
        Transporter trans =ltf.getTransporter(rei, con);
        
        trans.setProcessor(new EchoProcessor());
        
        ResponseProcessor rp = new ResponseProcessor();
        pl.setProcessor(rp);

        StringBuilder builder = new StringBuilder();
        for (int x = 0; x < length; x++) {
            builder.append("hello");
        }
        DefaultMessage dm = new DefaultMessage();
        MessageList ml =new MessageList(builder.toString());
        dm.setContent(List.class, ml);
        if(direct)
            dm.put(LocalPipeline.DIRECT_DISPATCH, Boolean.TRUE);
        dm.put(Transporter.class, trans);
        Exchange ex =new  DefaultExchange();
        ex.put(Container.class, con);
        dm.setExchange(ex);
        
        pl.prepare(dm);
        
        OutputStream out = dm.getContent(OutputStream.class);
        
        out.write(builder.toString().getBytes());
        out.close();
        //传输管道关闭,数据传输完毕.
        pl.close(dm);

        assertEquals(builder.toString(), rp.getResponseStream().toString());
        
    }*/
    
    static class EchoProcessor implements Processor{

      
        @Override
        public void process(Message message) throws ExchangeRuntimeException {
           
            try {
                message.getExchange().setIn(message);
                Pipeline bpl=  message.get(Transporter.class).getBackPipeline(message);
                InputStream in = message.getContent(InputStream.class);
                assertNotNull(in);   
                bpl.prepare(message);
                OutputStream out = message.getContent(OutputStream.class);
                assertNotNull(out);                             
                copy(in, out, 1024);
                out.close();
                in.close();                
                bpl.close(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
       
        
    }
    private static void copy(final InputStream input, final OutputStream output, final int bufferSize)
        throws IOException {
        try {
            final byte[] buffer = new byte[bufferSize];

            int n = input.read(buffer);
            while (-1 != n) {
                output.write(buffer, 0, n);
                n = input.read(buffer);
            }
        } finally {
            input.close();
            output.close();
        }
    }
    class ResponseProcessor implements Processor{

        ByteArrayOutputStream response = new ByteArrayOutputStream();
        boolean written;
        Message inMessage;
        
        public synchronized ByteArrayOutputStream getResponseStream() throws Exception {
            if (!written) {
                wait();
            }
            return response;
        }
        

        @Override
        public synchronized void process(Message message) {
            try {
                message.remove(LocalPipeline.DIRECT_DISPATCH);
                copy(message.getContent(InputStream.class), response, 1024);
                inMessage = message;
            } catch (IOException e) {
                e.printStackTrace();
                fail();
            } finally {
                written = true;
                notifyAll();
            }
        }
    }

}
