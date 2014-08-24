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
package org.solmix.hola.core.serialize.hola;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;

import org.solmix.hola.core.model.RemoteInfo;
import org.solmix.hola.core.serialize.Serialization;
import org.solmix.runtime.Extension;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月11日
 */
@Extension(name=HolaSerialization.CODEC)
public class HolaSerialization implements Serialization
{

    public static final String CODEC = "hola";

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.serialize.Serialization#getContentType()
     */
    @Override
    public String getContentType() {
        return "x-application/hola";
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.serialize.Serialization#serialize(org.solmix.hola.core.model.SerializeInfo, java.io.OutputStream)
     */
    @Override
    public ObjectOutput serialize(RemoteInfo info, OutputStream output)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.serialize.Serialization#deserialize(org.solmix.hola.core.model.SerializeInfo, java.io.InputStream)
     */
    @Override
    public ObjectInput deserialize(RemoteInfo info, InputStream input)
        throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.core.serialize.Serialization#getSerializeId()
     */
    @Override
    public byte getSerializeId() {
        return 1;
    }

}
