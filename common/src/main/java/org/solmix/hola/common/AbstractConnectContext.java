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
package org.solmix.hola.common;

import java.util.ArrayList;
import java.util.List;

import org.solmix.hola.common.event.ConnectEvent;



/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年5月4日
 */

public abstract class AbstractConnectContext implements ConnectContext
{
    private final List<ConnectListener> containerListeners = new ArrayList<ConnectListener>(5);

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.ConnectContext#addListener(org.solmix.hola.common.ConnectListener)
     */
    @Override
    public void addListener(ConnectListener listener) {
        synchronized (containerListeners) {
            containerListeners.add(listener);
      }

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.common.ConnectContext#removeListener(org.solmix.hola.common.ConnectListener)
     */
    @Override
    public void removeListener(ConnectListener listener) {
        synchronized (containerListeners) {
            containerListeners.remove(listener);
      }

    }
    
    @Override
    public void destroy(){
        
        //XXX fire distroy event
        synchronized (containerListeners) {
            containerListeners.clear();
      }
    }
    
    protected void fireConnectEvent(ConnectEvent event){
        
    }

}
