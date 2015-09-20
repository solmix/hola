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

import org.solmix.exchange.data.ObjectInput;
import org.solmix.exchange.data.ObjectReader;
import org.solmix.exchange.interceptor.Fault;
import org.solmix.exchange.model.ArgumentInfo;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月13日
 */

public class SerializationReader implements ObjectReader<ObjectInput> {

    
    @Override
    public void setProperty(String prop, Object value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Object read(ObjectInput input) {
        try {
            return input.readObject();
        } catch (Exception e) {
            throw new Fault(e);
        }
    }

    @Override
    public Object read(ObjectInput input, ArgumentInfo ai) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.exchange.data.ObjectReader#read(java.lang.Object, java.lang.Class)
     */
    @Override
    public <E> E read(ObjectInput input, Class<E> ai) {
        // TODO Auto-generated method stub
        return null;
    }

}
