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
package org.solmix.hola.rpc.dataformat.java;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Resource;

import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;
import org.solmix.runtime.exchange.dataformat.ObjectInput;
import org.solmix.runtime.exchange.dataformat.ObjectOutput;
import org.solmix.runtime.exchange.dataformat.Serialization;
import org.solmix.runtime.exchange.model.SerializationInfo;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月19日
 */
@Extension(name="java")
public class JavaSerialization implements Serialization {

    private Container container;
    @Override
    public byte getContentTypeId() {
        return 5;
    }
    
    @Resource 
    public void setContainer(Container container) {
        this.container = container;
    }


    @Override
    public String getContentType() {
        return "x-application/java";
    }

    
    @Override
    public ObjectOutput createObjectOutput(SerializationInfo info,
        OutputStream output) throws IOException {
        return new JavaObjectOutput(output);
    }

   
    @Override
    public ObjectInput createObjectInput(SerializationInfo info,
        InputStream input) throws IOException {
       ClassLoader loader = getClassLoader();
        return new JavaObjectInput(input, loader);
    }

    private ClassLoader getClassLoader() {
        return container.getExtension(ClassLoader.class);
    }

}
