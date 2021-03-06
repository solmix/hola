package org.solmix.hola.http.client.handler;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.solmix.hola.http.client.ClientConfig;
import org.solmix.hola.http.client.HttpAddress;
import org.solmix.hola.http.client.transport.Transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http2.DefaultHttp2SettingsFrame;
import io.netty.handler.codec.http2.Http2ConnectionPrefaceAndSettingsFrameWrittenEvent;
import io.netty.handler.codec.http2.Http2FrameLogger;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2MultiplexCodec;
import io.netty.handler.codec.http2.Http2MultiplexCodecBuilder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.SslHandler;

public class Http2ChannelInitializer extends ChannelInitializer<Channel> {

    private static final Logger logger = Logger.getLogger(Http2ChannelInitializer.class.getName());

    private final ClientConfig clientConfig;

    private final HttpAddress httpAddress;

    private final SslHandler sslHandler;

    public Http2ChannelInitializer(ClientConfig clientConfig,
                            HttpAddress httpAddress,
                            SslHandler sslHandler) {
        this.clientConfig = clientConfig;
        this.httpAddress = httpAddress;
        this.sslHandler = sslHandler;
    }

    @Override
    public void initChannel(Channel channel) {
        if (clientConfig.isDebug()) {
            channel.pipeline().addLast(new TrafficLoggingHandler(LogLevel.DEBUG));
        }
        if (httpAddress.isSecure()) {
            configureEncrypted(channel);
        } else {
            configureCleartext(channel);
        }
        if (clientConfig.isDebug()) {
            logger.log(Level.FINE, "HTTP/2 client channel initialized: " + channel.pipeline().names());
        }
    }

    private void configureEncrypted(Channel channel) {
        channel.pipeline().addLast(sslHandler);
        configureCleartext(channel);
    }

    public void configureCleartext(Channel ch) {
        ChannelInitializer<Channel> initializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                throw new IllegalStateException();
            }
        };
        Http2MultiplexCodecBuilder clientMultiplexCodecBuilder = Http2MultiplexCodecBuilder.forClient(initializer)
                .initialSettings(clientConfig.getHttp2Settings());
        if (clientConfig.isDebug()) {
            clientMultiplexCodecBuilder.frameLogger(new PushPromiseHandler(LogLevel.DEBUG, "client"));
        }
        Http2MultiplexCodec http2MultiplexCodec = clientMultiplexCodecBuilder.build();
        ChannelPipeline p = ch.pipeline();
        p.addLast("client-codec", http2MultiplexCodec);
        p.addLast("client-messages", new ClientMessages());
    }

    class ClientMessages extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof DefaultHttp2SettingsFrame) {
                DefaultHttp2SettingsFrame settingsFrame = (DefaultHttp2SettingsFrame) msg;
                Transport transport = ctx.channel().attr(Transport.TRANSPORT_ATTRIBUTE_KEY).get();
                if (transport != null) {
                    transport.settingsReceived(settingsFrame.settings());
                }
            } else {
                logger.log(Level.FINE, "received msg " + msg.getClass().getName());
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof Http2ConnectionPrefaceAndSettingsFrameWrittenEvent) {
                Http2ConnectionPrefaceAndSettingsFrameWrittenEvent event =
                        (Http2ConnectionPrefaceAndSettingsFrameWrittenEvent)evt;
                Transport transport = ctx.channel().attr(Transport.TRANSPORT_ATTRIBUTE_KEY).get();
                if (transport != null) {
                    transport.settingsReceived(null);
                }
            }
            ctx.fireUserEventTriggered(evt);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            Transport transport = ctx.channel().attr(Transport.TRANSPORT_ATTRIBUTE_KEY).get();
            if (transport != null) {
                transport.fail(cause);
            }
        }
    }

    class PushPromiseHandler extends Http2FrameLogger {

        public PushPromiseHandler(LogLevel level, String name) {
            super(level, name);
        }

        @Override
        public void logPushPromise(Direction direction, ChannelHandlerContext ctx, int streamId, int promisedStreamId,
                                   Http2Headers headers, int padding) {
            super.logPushPromise(direction, ctx, streamId, promisedStreamId, headers, padding);
            Transport transport = ctx.channel().attr(Transport.TRANSPORT_ATTRIBUTE_KEY).get();
            if (transport != null) {
                transport.pushPromiseReceived(ctx.channel(), streamId, promisedStreamId, headers);
            }
        }
    }
}
