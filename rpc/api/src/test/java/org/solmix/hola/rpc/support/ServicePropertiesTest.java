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
package org.solmix.hola.rpc.support;

import java.util.Hashtable;

import org.junit.Assert;
import org.junit.Test;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2015年1月22日
 */

public class ServicePropertiesTest extends Assert {

    @Test(expected=UnsupportedOperationException.class)
    public void test(){
        Hashtable<String, Object> dic = new Hashtable<String, Object>();
        dic.put("key", "value");
        dic.put("replace", "revalue");
        dic.put("remove", "remove");
        ServiceProperties sp = new ServiceProperties(dic);
        sp.set("replace", "replace",true);
        assertEquals(sp.get("replace"), "replace");
        sp.setReadOnly();
        sp.set("replace", "replace",true);
    }
}
