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
package org.solmix.hola.shared.generic.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月21日
 */

public class IdentifiableObjectInputStream extends ObjectInputStream
{

    private final ClassLoaderMapper loderMapper;
    /**
     * @throws IOException
     * @throws SecurityException
     */
    protected IdentifiableObjectInputStream(ClassLoaderMapper loderMapper,InputStream input) throws IOException,
        SecurityException
    {
        super(input);
        this.loderMapper=loderMapper;
        
    }
    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        String name = readUTF();
        if (name == null || loderMapper == null) {
              return super.resolveClass(desc);
        }
        ClassLoader cl = loderMapper.mapNameToClassLoader(name);
        if (cl == null)
              return super.resolveClass(desc);
        return Class.forName(desc.getName(), true, cl);
  }
}
