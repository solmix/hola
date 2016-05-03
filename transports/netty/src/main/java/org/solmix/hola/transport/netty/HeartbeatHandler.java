
package org.solmix.hola.transport.netty;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.exchange.Message;
import org.solmix.exchange.support.DefaultMessage;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.EventExecutor;

public class HeartbeatHandler extends ChannelHandlerAdapter
{
    private static final Logger LOG  = LoggerFactory.getLogger(HeartbeatHandler.class);
    
    public static final Object HEARTBEAT_EVENT =null;
    
    private NettyConfiguration config;

    private volatile int state;

    volatile ScheduledFuture<?> heartbeatTimeout;

    private int heartbeat;
    volatile long lastReadTime;
    volatile long lastWriteTime;
    private int heartbeatTimeoutms;

    public HeartbeatHandler(NettyConfiguration config)
    {
        this.config = config;
        if (config.enableHeartbeat()) {
            heartbeat = config.getHeartbeat();
            Integer ht = config.getHeartbeatTimeout();
            if (ht == null) {
                ht = 3 * heartbeat;
            } else if (ht < heartbeat * 2) {
                throw new IllegalArgumentException("heartbeatTimeout < heartbeatInterval * 2");
            }
            heartbeatTimeoutms = ht;
        }
        long current = System.currentTimeMillis();
        this.lastReadTime=current;
        this.lastWriteTime=current;
    }
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive() && ctx.channel().isRegistered()) {
            initialize(ctx);
        } else {
        }
    }
    
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        destroy();
    }
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (ctx.channel().isActive()) {
            initialize(ctx);
        }
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        initialize(ctx);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        destroy();
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        lastReadTime = System.currentTimeMillis();
        ctx.fireChannelRead(msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ChannelPromise unvoid = promise.unvoid();
        unvoid.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                lastWriteTime = System.currentTimeMillis();
            }
        });
        ctx.write(msg, unvoid);
    }
    
    private void destroy() {
        state = 2;

        if (heartbeatTimeout != null) {
            heartbeatTimeout.cancel(false);
            heartbeatTimeout = null;
        }
    }

    private void initialize(ChannelHandlerContext ctx) {
        if(!config.enableHeartbeat()){
            return;
        }
        switch (state) {
            case 1:
            case 2:
                return;
        }
        state = 1;
        EventExecutor loop = ctx.executor();
        loop.schedule(new HeartbeatTask(ctx), heartbeat, TimeUnit.MILLISECONDS);
    }

    private final class HeartbeatTask implements Runnable
    {

        private final ChannelHandlerContext ctx;

        public HeartbeatTask(ChannelHandlerContext ctx)
        {
            this.ctx = ctx;
        }

        @Override
        public void run() {
            try {
                ctx.executor().schedule(new HeartbeatTask(ctx), heartbeat, TimeUnit.MILLISECONDS);
                if (!ctx.channel().isOpen()) {
                    return;
                }
                long current = System.currentTimeMillis();
                long lastReadTime = HeartbeatHandler.this.lastReadTime;
                long lastWriteTime = HeartbeatHandler.this.lastWriteTime;
                if ((current - lastReadTime) > heartbeat || (current - lastWriteTime) > heartbeat) {
                    Message msg = new DefaultMessage();
                    msg.put(Message.EVENT_MESSAGE, Boolean.TRUE);
                    msg.setRequest(true);
                    msg.setInbound(false);
                    msg.setContent(Object.class, HEARTBEAT_EVENT);
                    ctx.writeAndFlush(msg);
                    if(LOG.isDebugEnabled()){
                        LOG.debug( "Send heartbeat to remote channel " + ctx.channel().toString()
                        + ", cause: The channel has no data-transmission exceeds a heartbeat period: " + heartbeat + "ms" );
                    }

                }
                if ((current - lastReadTime) > heartbeatTimeoutms) {
                    LOG.warn("Close channel " + ctx.channel() + ", because heartbeat read idle time out: " + heartbeatTimeoutms + " ms");
                    ctx.close();
                }

            } catch (Throwable t) {
                LOG.warn("Unhandled exception when heartbeat, cause: " + t.getMessage(), t);
            }
        }
    }
   

}
