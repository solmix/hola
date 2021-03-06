package org.solmix.hola.http.client.handler;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.solmix.hola.http.client.ClientConfig;
import org.solmix.hola.http.client.HttpAddress;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslHandler;

public class HttpChannelInitializer extends ChannelInitializer<Channel> {

    private static final Logger logger = Logger.getLogger(HttpChannelInitializer.class.getName());

    private final ClientConfig clientConfig;

    private final HttpAddress httpAddress;

    private final SslHandler sslHandler;

    private final HttpResponseHandler httpResponseHandler;

    private final Http2ChannelInitializer http2ChannelInitializer;

    public HttpChannelInitializer(ClientConfig clientConfig,
                                  HttpAddress httpAddress,
                                  SslHandler sslHandler,
                                  Http2ChannelInitializer http2ChannelInitializer) {
        this.clientConfig = clientConfig;
        this.httpAddress = httpAddress;
        this.sslHandler = sslHandler;
        this.http2ChannelInitializer = http2ChannelInitializer;
        this.httpResponseHandler = new HttpResponseHandler();
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
            logger.log(Level.FINE, "HTTP 1.1 client channel initialized: " + channel.pipeline().names());
        }
    }

    private void configureEncrypted(Channel channel)  {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(sslHandler);
        if (clientConfig.isEnableNegotiation()) {
            ApplicationProtocolNegotiationHandler negotiationHandler =
                    new ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_1_1) {
                @Override
                protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                    if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
                        http2ChannelInitializer.configureCleartext(ctx.channel());
                        if (clientConfig.isDebug()) {
                            logger.log(Level.FINE, "after negotiation to HTTP/2: " + ctx.pipeline().names());
                        }
                        return;
                    }
                    if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
                        configureCleartext(ctx.channel());
                        if (clientConfig.isDebug()) {
                            logger.log(Level.FINE, "after negotiation to HTTP 1.1: " + ctx.pipeline().names());
                        }
                        return;
                    }
                    ctx.close();
                    throw new IllegalStateException("protocol not accepted: " + protocol);
                }
            };
            channel.pipeline().addLast(negotiationHandler);
        } else {
            configureCleartext(channel);
        }
    }

    private void configureCleartext(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new HttpClientCodec(clientConfig.getMaxInitialLineLength(),
                 clientConfig.getMaxHeadersSize(), clientConfig.getMaxChunkSize()));
        if (clientConfig.isEnableGzip()) {
            pipeline.addLast(new HttpContentDecompressor());
        }
        HttpObjectAggregator httpObjectAggregator = new HttpObjectAggregator(clientConfig.getMaxContentLength(),
                false);
        httpObjectAggregator.setMaxCumulationBufferComponents(clientConfig.getMaxCompositeBufferComponents());
        pipeline.addLast(httpObjectAggregator);
        /*if (clientConfig.isEnableGzip()) {
            pipeline.addLast(new HttpChunkContentCompressor(6));
        }
        pipeline.addLast(new ChunkedWriteHandler());*/
        pipeline.addLast(httpResponseHandler);
    }
}
