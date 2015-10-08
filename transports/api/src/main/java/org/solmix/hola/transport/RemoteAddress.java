/*
 * Copyright 2015 The Solmix Project
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

package org.solmix.hola.transport;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Assert;
import org.solmix.commons.annotation.Immutable;
import org.solmix.commons.util.NetUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.transport.identity.ServerKeyID;

/**
 * 区别于EndpointInfo.getAddress();
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年10月4日
 */
@Immutable
public class RemoteAddress
{

    private final String protocol;

    private final String username;

    private final String password;

    private final int port;

    private final String host;

    private final String path;

    private final ServerKeyID serverKey;

    private Map<String, String> attributes;

    private volatile transient String string;

    private volatile transient String parameter;
    public RemoteAddress(Dictionary<String, ?> properties){
        Assert.assertNotNull(properties);
        String protocol =(String) properties.get(HOLA.PROTOCOL_KEY);
        String user =(String) properties.get(HOLA.USER_KEY);
        String host=(String) properties.get(HOLA.HOST_KEY);
        String password=(String)properties.get(HOLA.PASSWORD_KEY);
        String port=String.valueOf(properties.get(HOLA.PORT_KEY));
        int iport;
        if(port==null){
            iport=0;
        }else{
            iport=Integer.valueOf(port);
            if(iport<0){
                iport=0;
            }
        }
        String path=(String) properties.get(HOLA.PATH_KEY);
        if(StringUtils.isEmpty(host)){
            host=HOLA.LOCALHOST_VALUE;
        }
        Map<String,String> params= new HashMap<String, String>();
        String[] excluds = { HOLA.ADDRESS_KEY, HOLA.PROTOCOL_KEY, HOLA.HOST_KEY,
            HOLA.PORT_KEY, HOLA.PATH_KEY, HOLA.USER_KEY, HOLA.PASSWORD_KEY };
        List<String> excludes = (excluds == null || excluds.length == 0 ? null : Arrays.asList(excluds));
        Enumeration<String> enums= properties.keys();
        while(enums.hasMoreElements()){
            String key = enums.nextElement();
            if(excludes.contains(key)){
                continue;
            }
            params.put(key, properties.get(key).toString());
        }
        this.protocol = protocol;
        this.username = user;
        this.password = password;
        this.host = host;
        this.port = iport;
        this.path = path;
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        this.attributes = Collections.unmodifiableMap(params);
        serverKey = new ServerKeyID(protocol, host, iport);
    }
    
    public RemoteAddress(String protocol, String host, int port, String path, Map<String, String> attributes)
    {
        this(protocol, null, null, host, port, path, attributes);
    }

    public RemoteAddress(String protocol, String host, int port, String path)
    {
        this(protocol, null, null, host, port, path, null);
    }

    public RemoteAddress(String protocol, String host, int port)
    {
        this(protocol, null, null, host, port, null, null);
    }
    public RemoteAddress(String protocol, String username, String password, String host, int port, String path, Map<String, String> attributes)
    {
        this.protocol = protocol;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = (port < 0 ? 0 : port);
        this.path = path;
        while (path != null && path.startsWith("/")) {
            path = path.substring(1);
        }
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        } else {
            attributes = new HashMap<String, String>(attributes);
        }
        this.attributes = Collections.unmodifiableMap(attributes);
        serverKey = new ServerKeyID(protocol, host, port);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getProtocol() {
        return protocol;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getAddress() {
        return toString();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public ServerKeyID getServerKey() {
        return serverKey;
    }

    @Override
    public String toString() {
        if (string != null) {
            return string;
        }
        return string = buildString(false, true);
    }

    public String toParameterString() {
        if (parameter != null) {
            return parameter;
        }
        return parameter = toParameterString(new String[0]);
    }

    public String toParameterString(String... parameters) {
        StringBuilder buf = new StringBuilder();
        buildParameters(buf, false, parameters);
        return buf.toString();
    }

    private void buildParameters(StringBuilder buf, boolean concat, String[] parameters) {
        if (getAttributes() != null && getAttributes().size() > 0) {
            List<String> includes = (parameters == null || parameters.length == 0 ? null : Arrays.asList(parameters));
            boolean first = true;
            for (Map.Entry<String, String> entry : new TreeMap<String, String>(getAttributes()).entrySet()) {
                if (entry.getKey() != null && entry.getKey().length() > 0 && (includes == null || includes.contains(entry.getKey()))) {
                    if (first) {
                        if (concat) {
                            buf.append("?");
                        }
                        first = false;
                    } else {
                        buf.append("&");
                    }
                    buf.append(entry.getKey());
                    buf.append("=");
                    buf.append(entry.getValue() == null ? "" : entry.getValue().trim());
                }
            }
        }
    }

    private String buildString(boolean appendUser, boolean appendParameter, String... parameters) {
        return buildString(appendUser, appendParameter, false, false, parameters);
    }

    private String buildString(boolean appendUser, boolean appendParameter, boolean useIP, boolean useService, String... parameters) {
        StringBuilder buf = new StringBuilder();
        if (protocol != null && protocol.length() > 0) {
            buf.append(protocol);
            buf.append("://");
        }
        if (appendUser && username != null && username.length() > 0) {
            buf.append(username);
            if (password != null && password.length() > 0) {
                buf.append(":");
                buf.append(password);
            }
            buf.append("@");
        }
        String host;
        if (useIP) {
            host = NetUtils.getIpByHost(this.host);
        } else {
            host = getHost();
        }
        if (host != null && host.length() > 0) {
            buf.append(host);
            if (port > 0) {
                buf.append(":");
                buf.append(port);
            }
        }
        String path = getPath();
        if (path != null && path.length() > 0) {
            buf.append("/");
            buf.append(path);
        }
        if (appendParameter) {
            buildParameters(buf, true, parameters);
        }
        return buf.toString();
    }

    public java.net.URL toJavaURL() {
        try {
            return new java.net.URL(toString());
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static RemoteAddress fromAddress(String address) {
        if (address == null || (address = address.trim()).length() == 0) {
            throw new IllegalArgumentException("address == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;
        int i = address.indexOf("?");
        if (i >= 0) {
            String[] parts = address.substring(i + 1).split("\\&");
            parameters = new Hashtable<String, String>();
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            address = address.substring(0, i);
        }
        i = address.indexOf("://");
        if (i >= 0) {
            if (i == 0)
                throw new IllegalStateException("address missing protocol: \"" + address + "\"");
            protocol = address.substring(0, i);
            address = address.substring(i + 3);
        } else {
            // case: file:/path/to/file.txt
            i = address.indexOf(":/");
            if (i >= 0) {
                if (i == 0)
                    throw new IllegalStateException("address missing protocol: \"" + address + "\"");
                protocol = address.substring(0, i);
                address = address.substring(i + 1);
            }
        }

        i = address.indexOf("/");
        if (i >= 0) {
            path = address.substring(i + 1);
            address = address.substring(0, i);
        }
        i = address.indexOf("@");
        if (i >= 0) {
            username = address.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            address = address.substring(i + 1);
        }
        i = address.indexOf(":");
        if (i >= 0 && i < address.length() - 1) {
            port = Integer.parseInt(address.substring(i + 1));
            address = address.substring(0, i);
        }
        if (address.length() > 0)
            host = address;
        return new RemoteAddress(protocol, username, password, host, port, path, parameters);
    }
}
