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
package org.solmix.hola.common.serialize.hola;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月24日
 */

public class DescriptorBuilder
{
    private static final List<String> mDescList = new ArrayList<String>();

    private static final Map<String, Integer> mDescMap = new ConcurrentHashMap<String, Integer>();

    public static ClassDescriptorMapper DEFAULT_CLASS_DESCRIPTOR_MAPPER = new ClassDescriptorMapper(){
        @Override
        public String getDescriptor(int index)
        {
              if( index < 0 || index >= mDescList.size() )
                    return null;
              return mDescList.get(index);
        }

        @Override
        public int getDescriptorIndex(String desc)
        {
              Integer ret = mDescMap.get(desc);
              return ret == null ? -1 : ret.intValue();
        }
  };

}
