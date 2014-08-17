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
package org.solmix.hola.transport.dispatch;

import org.junit.Assert;
import org.junit.Test;
import org.solmix.runtime.Container;
import org.solmix.runtime.Containers;
import org.solmix.runtime.extension.ExtensionLoader;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年8月17日
 */

public class DispatcherTest extends Assert
{

    @Test
    public void testGet(){
        Container c=  Containers.get();
        ExtensionLoader<Dispatcher> loader= c.getExtensionLoader(Dispatcher.class);
        assertNotNull(loader);
        assertEquals(4, loader.getLoadedExtensions().size());
        assertTrue(loader.getExtension(AllDispatcher.NAME)instanceof AllDispatcher);
        assertTrue(loader.getExtension(DirectDispatcher.NAME)instanceof DirectDispatcher);
        assertTrue(loader.getExtension(ExecutionDispatcher.NAME)instanceof ExecutionDispatcher);
        assertTrue(loader.getExtension(ReadonlyDispatcher.NAME)instanceof ReadonlyDispatcher);
        AllDispatcher all=(AllDispatcher)  loader.getExtension(AllDispatcher.NAME);
        assertNotNull(all.getContainer());

    }
}
