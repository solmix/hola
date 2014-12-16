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
package org.solmix.hola.common;

import java.util.SortedSet;

import org.solmix.runtime.interceptor.phase.Phase;
import org.solmix.runtime.interceptor.phase.PhasePolicy;


/**
 * <pre>
 *       +---------------------------------------------------------+   
 *       |                     Client(Endpoint)                    |    
 *       +----------------+-----------------------+----------------+ 
 *                  |                                  /|\
 *             handleMessae()                     handleFault()
 *                 \|/                                  |
 *       +----------------------------------------------+----------+   
 *       |          |        Interceptor     2                     |    
 *       +----------+-----------------------------------+----------+ 
 *                             .       |                 |
 *                  |          .       |               发生异常              
 *                  |          .       |---------------------------+       .                                             
 *                 \|/         .       |    
 *       +----------------------------------------------+----------+   
 *       |          |        Interceptor     N                     |    
 *       +----------+-----------------------------------+----------+ 
 *  
 *  </pre>
 */

public class HolaPhasePolicy implements PhasePolicy
{

   
    @Override
    public SortedSet<Phase> getInPhases() {
        // TODO Auto-generated method stub
        return null;
    }

   
    @Override
    public SortedSet<Phase> getOutPhases() {
        // TODO Auto-generated method stub
        return null;
    }

}
