package org.solmix.hola.transport.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.IOException;
import java.util.List;

import org.solmix.hola.core.HolaConstants;
import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.transport.codec.Codec;

final class NettyCodecAdapter {

    private final ChannelHandler encoder = new NettyEncoder();
    
    private final ChannelHandler decoder = new NettyDecoder();

    private final Codec         codec;
    
    private final RemoteInfo            endpointInfo;
    
    private final int            bufferSize;
    
    private final org.solmix.hola.transport.channel.ChannelHandler handler;

    public NettyCodecAdapter(Codec codec, RemoteInfo endpointInfo, org.solmix.hola.transport.channel.ChannelHandler handler) {
        this.codec = codec;
        this.endpointInfo = endpointInfo;
        this.handler = handler;
        int b = endpointInfo.getBuffer( HolaConstants.DEFAULT_BUFFER_SIZE);
        this.bufferSize = b >= HolaConstants.MIN_BUFFER_SIZE && b <= HolaConstants.MAX_BUFFER_SIZE ? b : HolaConstants.DEFAULT_BUFFER_SIZE;
    }

    public ChannelHandler getEncoder() {
        return encoder;
    }

    public ChannelHandler getDecoder() {
        return decoder;
    }

    @Sharable
    private class NettyEncoder extends MessageToMessageEncoder<Object> {

        /**
         * {@inheritDoc}
         * 
         * @see io.netty.handler.codec.MessageToMessageEncoder#encode(io.netty.channel.ChannelHandlerContext, java.lang.Object, java.util.List)
         */
        @Override
        protected void encode(ChannelHandlerContext ctx, Object msg,
            List<Object> out) throws Exception {
            ByteBufAllocator alloc= ctx.alloc();
            ByteBuf buffer =alloc.buffer(bufferSize);
            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), endpointInfo, handler);
            try {
               codec .encode(channel, buffer, msg);
               out.add(buffer);
               System.out.println(out.size()+"@@@>发送"+ByteBufUtil.hexDump(buffer));
            } finally {
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        }
    }

    private class NettyDecoder extends SimpleChannelInboundHandler<io.netty.buffer.ByteBuf> {

        private ByteBuf buffer ;
        @Override
        protected void messageReceived(ChannelHandlerContext ctx,
            io.netty.buffer.ByteBuf bf) throws Exception {
            int readable = bf.readableBytes();
            if(readable<=0)
                return;
            if(buffer==null){
               buffer= ctx.alloc().buffer();
            }
            if(buffer.isReadable()){
                buffer.writeBytes(bf);
            }else{
                buffer=bf;
            }
            NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), endpointInfo, handler);
            Object msg;
            int saveReaderIndex;
            try {
                // decode object.
                do {
                    saveReaderIndex = buffer.readerIndex();
                    try {
                        System.out.println("@@@>接受"+ByteBufUtil.hexDump(buffer));
                        msg = codec.decode(channel, buffer);
                    } catch (IOException e) {
                        buffer =buffer.clear();
                        throw e;
                    }
                    if (msg == Codec.DecodeResult.NEED_MORE_INPUT) {
                        buffer.readerIndex(saveReaderIndex);
                        break;
                    } else {
                        if (saveReaderIndex == buffer.readerIndex()) {
                            buffer.clear();
                            throw new IOException("Decode without read data.");
                        }
                        if (msg != null) {
                            ctx.fireChannelRead(msg);
                        }
                    }
                } while (buffer.isReadable());
            } finally {
                if (buffer.isReadable()) {
                    buffer.discardReadBytes();
                } else {
                    buffer.clear();
                }
                NettyChannel.removeChannelIfDisconnected(ctx.channel());
            }
        }
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
           cause.printStackTrace();
        }
       
    }
}