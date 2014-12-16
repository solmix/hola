/*
 * Copyright 2013 The Solmix Project
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

package org.solmix.hola.common.serialize;

import java.util.HashMap;
import java.util.Map;

import org.solmix.hola.common.HolaConstants;
import org.solmix.hola.common.config.RemoteInfo;
import org.solmix.hola.common.serialize.hola.HolaSerialization;
import org.solmix.hola.common.serialize.java.JavaSerialization;
import org.solmix.runtime.Container;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年8月11日
 */

public class SerializationManagerImpl implements SerializationManager
{

    private static Map<Byte, Serialization> ID_SERIALIZATION_MAP = new HashMap<Byte, Serialization>();

    private final Container container;
    static {
        HolaSerialization hola = new HolaSerialization();
        ID_SERIALIZATION_MAP.put(hola.getSerializeId(), hola);
        JavaSerialization java = new JavaSerialization();
        ID_SERIALIZATION_MAP.put(java.getSerializeId(), java);
    }
    //inject container
   public SerializationManagerImpl(Container container){
        this.container=container;
    }
    @Override
    public  Serialization getSerialization(byte id) {
        return ID_SERIALIZATION_MAP.get(id);
    }

    @Override
    public  Serialization getSerialization(RemoteInfo info) {
        return container.getExtensionLoader(Serialization.class).getExtension(
            info.getSerial(HolaConstants.DEFAULT_SERIALIZATION));
    }

    @Override
    public  Serialization getSerialization(RemoteInfo info, byte id) {
        Serialization s = getSerialization(id);
        if (s == null) {
            s = getSerialization(info);
        }
        return s;
    }

}