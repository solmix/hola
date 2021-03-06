package org.solmix.hola.http.server.transport;

import org.solmix.hola.http.server.HttpAddress;
import org.solmix.hola.http.server.context.VirtualServer;

import io.netty.handler.codec.http.FullHttpRequest;

/**
 * The {@code ServerRequest} class encapsulates a single request.
 */
public class ServerRequest {

    private final VirtualServer virtualServer;

    private final HttpAddress httpAddress;

    private final FullHttpRequest httpRequest;

    private final Integer sequenceId;

    private final Integer streamId;

    private final Integer requestId;

    public ServerRequest(VirtualServer virtualServer, HttpAddress httpAddress,
                         FullHttpRequest httpRequest, Integer sequenceId, Integer streamId, Integer requestId) {
        this.virtualServer = virtualServer;
        this.httpAddress = httpAddress;
        this.httpRequest = httpRequest;
        this.sequenceId = sequenceId;
        this.streamId = streamId;
        this.requestId = requestId;
    }

    public VirtualServer getVirtualServer() {
        return virtualServer;
    }

    public HttpAddress getHttpAddress() {
        return httpAddress;
    }

    public FullHttpRequest getRequest() {
        return httpRequest;
    }

    public Integer getSequenceId() {
        return sequenceId;
    }

    public Integer streamId() {
        return streamId;
    }

    public Integer requestId() {
        return requestId;
    }
}
