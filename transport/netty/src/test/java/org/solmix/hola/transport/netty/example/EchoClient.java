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
package org.solmix.hola.transport.netty.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends one message when a connection is open and echoes back any received
 * data to the server.  Simply put, the echo client initiates the ping-pong
 * traffic between the echo client and server by sending the first message to
 * the server.
 */
public class EchoClient {

    private final String host;
    private final int port;
    private final int firstMessageSize;

    public EchoClient(String host, int port, final int firstMessageSize) throws Exception {
        this.host = host;
        this.port = port;
        this.firstMessageSize = firstMessageSize;
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(
                             //new LoggingHandler(LogLevel.INFO),
                             new EchoClientHandler(firstMessageSize));
                 }
             });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();
            // Wait until the connection is closed.
//            f.channel().closeFuture().sync();
        } finally {
            // Shut down the event loop to terminate all threads.
//            group.shutdownGracefully();
        }
    }
    public Channel channel;

    public void send (Object msg) throws InterruptedException{
        while(channel==null||!channel.isActive()){
            Thread.sleep(100);
        }
        channel.write(msg);
        channel.flush();
    }
    public void close(){
        channel.close();
    }
    
  class  EchoClientHandler extends ChannelHandlerAdapter {

      private  final Logger logger = Logger.getLogger(
              EchoClientHandler.class.getName());

      private final ByteBuf firstMessage;
      /**
       * Creates a client-side handler.
       */
      public EchoClientHandler(int firstMessageSize) {
          if (firstMessageSize <= 0) {
              throw new IllegalArgumentException("firstMessageSize: " + firstMessageSize);
          }
          firstMessage = Unpooled.buffer(firstMessageSize);
          for (int i = 0; i < firstMessage.capacity(); i ++) {
              firstMessage.writeByte((byte) i);
          }
      }

      @Override
      public void channelActive(ChannelHandlerContext ctx) {
//          ctx.writeAndFlush(firstMessage);
          channel=ctx.channel();
      }

      @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//          ctx.write(msg);
          System.out.println(msg);
          channel.write(msg);
      }

      @Override
      public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
         ctx.flush();
      }

      @Override
      public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
          // Close the connection when an exception is raised.
          logger.log(Level.WARNING, "Unexpected exception from downstream.", cause);
          ctx.close();
      }
  }
    public static void main(String[] args) throws Exception {
       

        EchoClient e=  new EchoClient("localhost", 8080, 256);
       
        e.send("hello");
//        Thread.sleep(1000*5);
//        e.close();
//        e.channel.writeAndFlush("hell");
//        e.channel.flush();
//        Thread.sleep(1000*20);
//        e.channel.closeFuture().sync();
    }
}
