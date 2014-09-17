/*
 * Copyright 2014 The Solmix Project
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
package org.solmix.hola.discovery.zk;

import java.util.List;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月15日
 */

public interface ZKClient extends java.io.Closeable
{
    void create(String path, boolean ephemeral);

    void create(String path, byte[] data,boolean ephemeral);
    void delete(String path);
    
    byte[] getData(String path);

    List<String> getChildren(String path);

    List<String> addChildListener(String path, ChildListener listener);

    void removeChildListener(String path, ChildListener listener);

    void addStateListener(StateListener listener);
    
    void removeStateListener(StateListener listener);

    boolean isConnected();

}