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

package org.solmix.hola.rs.data;

import org.solmix.exchange.Service;
import org.solmix.exchange.data.DataProcessor;
import org.solmix.exchange.data.ObjectInput;
import org.solmix.exchange.data.ObjectOutput;
import org.solmix.exchange.data.ObjectReader;
import org.solmix.exchange.data.ObjectWriter;
import org.solmix.exchange.interceptor.support.InterceptorProviderSupport;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月13日
 */

public class SerializationDataProcessor extends InterceptorProviderSupport
    implements DataProcessor {

    private static final long serialVersionUID = -7151041361258209941L;

    private final SerializationReader reader;

    private final SerializationWriter writer;

    public SerializationDataProcessor() {
        this.reader = new SerializationReader();
        this.writer = new SerializationWriter();
    }

    @Override
    public void initialize(Service service) {
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ObjectReader<T> createReader(Class<T> cls) {
        if(cls == ObjectInput.class){
            return (ObjectReader<T>)reader;
        }else{
            throw new UnsupportedOperationException("The type " + cls.getName() + " is not supported.");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ObjectWriter<T> createWriter(Class<T> cls) {
        if(cls == ObjectOutput.class){
            return (ObjectWriter<T>)writer;
        }else{
            throw new UnsupportedOperationException("The type " + cls.getName() + " is not supported.");
        }
    }

    @Override
    public Class<?>[] getSupportedReaderFormats() {
        return new Class[] {ObjectInput.class };
    }

    @Override
    public Class<?>[] getSupportedWriterFormats() {
        return new Class[] {ObjectOutput.class };
    }

}
