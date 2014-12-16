/*
 * Copyright 2014 The Solmix Project
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

package org.solmix.hola.common.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.solmix.commons.annotation.ThreadSafe;
import org.solmix.commons.util.NetUtils;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年10月2日
 */
@ThreadSafe
public abstract class AbstractURIInfo<T> extends ExtensionInfo<T> {

    private static final long serialVersionUID = -7050624934282645457L;

    /**
     * 主机
     */
    public static final String HOST = "host";

    /**
     * 端口
     */
    public static final String PORT = "port";

    /**
     * 服务路径(contextPath)
     */
    public static final String PATH = "path";

    public static final String PROTOCOL = "protocol";

    /**
     * 用户名
     */
    public static final String USERNAME = "username";

    /**
     * 密码
     */
    public static final String PASSWORD = "password";

    public static final String BACKUP = "backup";

    protected final String protocol;

    protected final String username;

    protected final String password;

    protected final String host;

    protected final int port;

    protected final String path;

    private volatile transient String ip;

    private volatile transient URI uri;

    public AbstractURIInfo() {
        super(null);
        this.protocol = null;
        this.username = null;
        this.password = null;
        this.host = null;
        this.port = 0;
        this.path = null;
    }

    public AbstractURIInfo(String protocol, String username, String password,
        String host, int port, String path, Map<String, Object> properties) {
        super(properties);
        if ((username == null || username.length() == 0) && password != null
            && password.length() > 0) {
            throw new IllegalArgumentException(
                "Invalid url, password without username!");
        }
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        this.path = path;
    }

    protected static Map<String, Object> parseQuery(String query) {
        String[] parts = query.split("\\&");
        Map<String, Object> properties = new HashMap<String, Object>();
        for (String part : parts) {
            part = part.trim();
            if (part.length() > 0) {
                int j = part.indexOf('=');
                if (j >= 0) {
                    properties.put(part.substring(0, j), part.substring(j + 1));
                } else {
                    properties.put(part, part);
                }
            }
        }
        return properties;
    }

    public URI toURI() {
        if (uri == null) {
            try {
                uri = new URI(protocol, getAuthority(), host, port, path,
                    getQuery(), null);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return uri;
    }

    /**
     * @return
     */
    protected String getQuery() {
        StringBuilder buf = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : new TreeMap<String, Object>(
            getProperties()).entrySet()) {
            if (entry.getKey() != null && entry.getKey().length() > 0) {
                if (first) {
                    first = false;
                } else {
                    buf.append("&");
                }
                buf.append(entry.getKey());
                buf.append("=");
                buf.append(entry.getValue() == null ? ""
                    : entry.getValue().toString().trim());

            }
        }
        return null;
    }

    /**
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the host
     */
    public String getHost() {
        return host;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    public String getAuthority() {
        if ((username == null || username.length() == 0)
            && (password == null || password.length() == 0)) {
            return null;
        }
        return (username == null ? "" : username) + ":"
            + (password == null ? "" : password);
    }

    public String getIp() {
        if (ip == null) {
            ip = NetUtils.getIpByHost(host);
        }
        return ip;
    }

    public int getPort(int defaultPort) {
        return port <= 0 ? defaultPort : port;
    }

    public String getAddress() {
        return port <= 0 ? host : host + ":" + port;
    }
}
