/**
 * Copyright (c) 2015 The Solmix Project
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

package org.solmix.hola.common;

import java.util.Dictionary;

import org.solmix.commons.collections.DataTypeMap;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月22日
 */

public final class ParamsUtils {

    private ParamsUtils() {

    }

    public static String getAddress(DataTypeMap props) {

        return getAddress(
            props.getString(Params.PROTOCOL_KEY, Params.DEFAULT_PROTOCOL),
            props.getString(Params.HOST_KEY), props.getInt(Params.PORT_KEY, -1));
    }

    public static String getAddress(Dictionary<String, ?> props) {
        String protocol;
        String host;
        int port;
        Object p = props.get(Params.PROTOCOL_KEY);
        protocol = p == null ? Params.DEFAULT_PROTOCOL : p.toString();
        Object po = props.get(Params.PORT_KEY);
        port = po == null ? -1 : Integer.valueOf(po.toString());
        Object h = props.get(Params.HOST_KEY);
        host = h.toString();
        return getAddress(protocol, host, port);
    }

    public static String getAddress(String protocol, String host, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://").append(host);
        if (port > 0) {
            sb.append(":").append(port);
        }

        return sb.toString();
    }

}
