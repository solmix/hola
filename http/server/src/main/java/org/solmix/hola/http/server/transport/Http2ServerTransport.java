package org.solmix.hola.http.server.transport;

import java.io.IOException;

import org.solmix.hola.http.server.HttpAddress;
import org.solmix.hola.http.server.Server;
import org.solmix.hola.http.server.context.VirtualServer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.HttpConversionUtil;

public class Http2ServerTransport extends BaseServerTransport {

    public Http2ServerTransport(Server server) {
        super(server);
    }

    @Override
    public void requestReceived(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws IOException {
        requestReceived(ctx, fullHttpRequest, null);
    }

    @Override
    public void requestReceived(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest, Integer sequenceId) throws IOException {
        int requestId = requestCounter.incrementAndGet();
        VirtualServer virtualServer = server.getVirtualServer(fullHttpRequest.headers().get(HttpHeaderNames.HOST));
        if (virtualServer == null) {
            virtualServer = server.getDefaultVirtualServer();
        }
        HttpAddress httpAddress = server.getServerConfig().getAddress();
        Integer streamId = fullHttpRequest.headers().getInt(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
        ServerRequest serverRequest = new ServerRequest(virtualServer, httpAddress, fullHttpRequest,
                sequenceId, streamId, requestId);
        ServerResponse serverResponse = new Http2ServerResponse(serverRequest, ctx);
        if (acceptRequest(serverRequest, serverResponse)) {
            handle(serverRequest, serverResponse);
        }
    }

    @Override
    public void settingsReceived(ChannelHandlerContext ctx, Http2Settings http2Settings) {
    }
}
