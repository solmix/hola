package org.solmix.hola.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.IOException;
import java.nio.channels.Channels;
import java.util.List;

import org.apache.commons.collections.BufferUtils;
import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.Parameters;
import org.solmix.hola.transport.codec.Codec;

final class NettyCodecAdapter {

    private final ChannelHandler encoder = new InternalEncoder();
    
    private final ChannelHandler decoder = new InternalDecoder();

    private final Codec         codec;
    
    private final Parameters            param;
    
    private final int            bufferSize;
    
    private final org.solmix.hola.transport.channel.ChannelHandler handler;

    public NettyCodecAdapter(Codec codec, Parameters param, org.solmix.hola.transport.channel.ChannelHandler handler) {
        this.codec = codec;
        this.param = param;
        this.handler = handler;
        int b = param.getInt(HolaConstants.KEY_BUFFER, HolaConstants.DEFAULT_BUFFER_SIZE,true);
        this.bufferSize = b >= HolaConstants.MIN_BUFFER_SIZE && b <= HolaConstants.MAX_BUFFER_SIZE ? b : HolaConstants.DEFAULT_BUFFER_SIZE;
    }

    public ChannelHandler getEncoder() {
        return encoder;
    }

    public ChannelHandler getDecoder() {
        return decoder;
    }

    @Sharable
    private class InternalEncoder extends MessageToMessageEncoder<Object> {

        
        /**
         * {@inheritDoc}
         * 
         * @see io.netty.handler.codec.MessageToMessageEncoder#encode(io.netty.channel.ChannelHandlerContext, java.lang.Object, java.util.List)
         */
        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg,
            List<Object> out) throws Exception {
            ByteBuf buffer =BufferUtils.dynamicBuffer(1024);
            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), param, handler);
            try {
               codec .encode(channel, buffer, msg);
            } finally {
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
            
        }

        
    }

    private class InternalDecoder extends SimpleChannelInboundHandler<io.netty.buffer.ByteBuf> {

        private ByteBuf buffer = BufferUtils.EMPTY_BUFFER;
        @Override
        protected void messageReceived(ChannelHandlerContext ctx,
            io.netty.buffer.ByteBuf msg) throws Exception {
            int readable = msg.readableBytes();
            if(readable<=0)
                return;
            if(buffer.readable()){
                
            }
            
        }
        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
            Object o = event.getMessage();
            if (! (o instanceof io.netty.buffer.ByteBuf)) {
                ctx.fireChannelRead(msg);
                return;
            }

            ChannelBuffer input = (ChannelBuffer) o;
            int readable = input.readableBytes();
            if (readable <= 0) {
                return;
            }

            com.alibaba.dubbo.remoting.buffer.ChannelBuffer message;
            if (buffer.readable()) {
                if (buffer instanceof DynamicChannelBuffer) {
                    buffer.writeBytes(input.toByteBuffer());
                    message = buffer;
                } else {
                    int size = buffer.readableBytes() + input.readableBytes();
                    message = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.dynamicBuffer(
                        size > bufferSize ? size : bufferSize);
                    message.writeBytes(buffer, buffer.readableBytes());
                    message.writeBytes(input.toByteBuffer());
                }
            } else {
                message = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.wrappedBuffer(
                    input.toByteBuffer());
            }

            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), url, handler);
            Object msg;
            int saveReaderIndex;

            try {
                // decode object.
                do {
                    saveReaderIndex = message.readerIndex();
                    try {
                        msg = codec.decode(channel, message);
                    } catch (IOException e) {
                        buffer = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                        throw e;
                    }
                    if (msg == Codec2.DecodeResult.NEED_MORE_INPUT) {
                        message.readerIndex(saveReaderIndex);
                        break;
                    } else {
                        if (saveReaderIndex == message.readerIndex()) {
                            buffer = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                            throw new IOException("Decode without read data.");
                        }
                        if (msg != null) {
                            Channels.fireMessageReceived(ctx, msg, event.getRemoteAddress());
                        }
                    }
                } while (message.readable());
            } finally {
                if (message.readable()) {
                    message.discardReadBytes();
                    buffer = message;
                } else {
                    buffer = com.alibaba.dubbo.remoting.buffer.ChannelBuffers.EMPTY_BUFFER;
                }
                NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            ctx.sendUpstream(e);
        }

        /**
         * {@inheritDoc}
         * 
         * @see io.netty.channel.SimpleChannelInboundHandler#messageReceived(io.netty.channel.ChannelHandlerContext, java.lang.Object)
         */
       
    }
}