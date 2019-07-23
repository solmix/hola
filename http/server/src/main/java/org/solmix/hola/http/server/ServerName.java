package org.solmix.hola.http.server;

import java.util.Optional;

import io.netty.bootstrap.Bootstrap;

/**
 * Server name.
 */
public final class ServerName {

    /**
     * The default value for {@code Server} header.
     */
    private static final String SERVER_NAME = String.format("HolaHttpServer/%s (Java/%s/%s) (Netty/%s)",
            httpServerVersion(), javaVendor(), javaVersion(), nettyVersion());

    private ServerName() {
    }

    public static String getServerName() {
        return SERVER_NAME;
    }

    private static String httpServerVersion() {
        return Optional.ofNullable(Server.class.getPackage().getImplementationVersion())
                .orElse("unknown");
    }

    private static String javaVendor() {
        return Optional.ofNullable(System.getProperty("java.vendor"))
                .orElse("unknown");
    }

    private static String javaVersion() {
        return Optional.ofNullable(System.getProperty("java.version"))
                .orElse("unknown");
    }

    private static String nettyVersion() {
        return Optional.ofNullable(Bootstrap.class.getPackage().getImplementationVersion())
                .orElse("unknown");
    }
}
