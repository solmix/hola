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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.solmix.hola.core.identity.ID;
import org.solmix.hola.shared.SharedMessageSerializer;
import org.solmix.hola.shared.SharedService;
import org.solmix.hola.shared.generic.GenericSSProvider;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年5月21日
 */

public class GenericMessageSerializer implements SharedMessageSerializer
{

    private final GenericSSProvider provider;
    public GenericMessageSerializer(GenericSSProvider provider){
        this.provider=provider;
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedMessageSerializer#deserialize(byte[])
     */
    @Override
    public Object deserialize(byte[] data) throws IOException,
        ClassNotFoundException {
        final ByteArrayInputStream bins = new ByteArrayInputStream(data);
        Object obj = null;
        try {
              
              final ObjectInputStream oins = new ObjectInputStream(bins);
              obj = oins.readObject();
        } catch (final ClassNotFoundException e) {
              // first reset stream
              bins.reset();
              // Now try with shared object classloader
              final IdentifiableObjectInputStream iins = new IdentifiableObjectInputStream(new ClassLoaderMapper() {
                  @Override  
                  public ClassLoader mapNameToClassLoader(String name) {
                         
                          ID[] ids =  provider.getServiceIDs();
                          ID found = null;
                          for (int i = 0; i < ids.length; i++) {
                                ID id = ids[i];
                                if (name.equals(id.getName())) {
                                      found = id;
                                      break;
                                }
                          }
                          if (found == null)
                                return null;
                          SharedService service = provider.getSharedService(found);
                          if (service == null)
                                return null;
                          return service.getClass().getClassLoader();
                    }
              }, bins);
              obj = iins.readObject();
              iins.close();
        }
        return obj;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.shared.SharedMessageSerializer#serialize(org.solmix.hola.core.identity.ID, java.lang.Object)
     */
    @Override
    public byte[] serialize(ID serviceId, Object message)
        throws IOException {
        if (!(message instanceof Serializable))
            throw new NotSerializableException("service =" + serviceId + " message=" + message + " not serializable"); 
      final ByteArrayOutputStream bouts = new ByteArrayOutputStream();
      final IdentifiableObjectOutputStream ioos = new IdentifiableObjectOutputStream(serviceId.getName(), bouts);
      ioos.writeObject(message);
      ioos.close();
      return bouts.toByteArray();
    }

}
