/**
 * Copyright (c) 2014 The Solmix Project
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
package org.solmix.hola.rpc;

import java.util.SortedSet;

import org.solmix.runtime.interceptor.phase.Phase;
import org.solmix.runtime.interceptor.phase.PhasePolicy;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年12月4日
 */

public class RpcPhasePolicy implements PhasePolicy {

    private SortedSet<Phase> inPhases;
    private SortedSet<Phase> outPhases;
    
    public RpcPhasePolicy() {
        createInPhases();
        createOutPhases();
    }
    
    private void createOutPhases() {
        // TODO Auto-generated method stub
        
    }
    
    private void createInPhases() {
        // TODO Auto-generated method stub
        
    }
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
