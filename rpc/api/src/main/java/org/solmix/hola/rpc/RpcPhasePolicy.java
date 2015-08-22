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

import org.solmix.commons.collections.SortedArraySet;
import org.solmix.exchange.interceptor.phase.Phase;
import org.solmix.exchange.interceptor.phase.PhasePolicy;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年12月4日
 */

public class RpcPhasePolicy implements PhasePolicy {

    private SortedSet<Phase> inPhases;

    private SortedSet<Phase> outPhases;

    public RpcPhasePolicy() {
        createInPhases();
        createOutPhases();
    }

    @Override
    public SortedSet<Phase> getInPhases() {
        return inPhases;
    }

    @Override
    public SortedSet<Phase> getOutPhases() {
        return outPhases;
    }

    private void createInPhases() {
        int i = 0;
        inPhases = new SortedArraySet<Phase>();
        inPhases.add(new Phase(Phase.RECEIVE, ++i * 1000));
        inPhases.add(new Phase(Phase.PRE_STREAM, ++i * 1000));
        inPhases.add(new Phase(Phase.USER_STREAM, ++i * 1000));
        inPhases.add(new Phase(Phase.POST_STREAM, ++i * 1000));
        inPhases.add(new Phase(Phase.READ, ++i * 1000));
        inPhases.add(new Phase(Phase.PRE_PROTOCOL, ++i * 1000));
        inPhases.add(new Phase(Phase.PRE_PROTOCOL_FRONTEND, ++i * 1000));
        inPhases.add(new Phase(Phase.USER_PROTOCOL, ++i * 1000));
        inPhases.add(new Phase(Phase.PROTOCOL, ++i * 1000));
        inPhases.add(new Phase(Phase.POST_PROTOCOL, ++i * 1000));
        inPhases.add(new Phase(Phase.PRE_DECODE, ++i * 1000));
        inPhases.add(new Phase(Phase.DECODE, ++i * 1000));
        inPhases.add(new Phase(Phase.POST_DECODE, ++i * 1000));
        inPhases.add(new Phase(Phase.PRE_LOGICAL, ++i * 1000));
        inPhases.add(new Phase(Phase.USER_LOGICAL, ++i * 1000));
        inPhases.add(new Phase(Phase.POST_LOGICAL, ++i * 1000));
        inPhases.add(new Phase(Phase.PRE_INVOKE, ++i * 1000));
        inPhases.add(new Phase(Phase.INVOKE, ++i * 1000));
        inPhases.add(new Phase(Phase.POST_INVOKE, ++i * 1000));

    }

    private void createOutPhases() {
        int i = 0;
        outPhases = new SortedArraySet<Phase>();
        outPhases.add(new Phase(Phase.SETUP, ++i * 1000));
        outPhases.add(new Phase(Phase.PRE_LOGICAL, ++i * 1000));
        outPhases.add(new Phase(Phase.USER_LOGICAL, ++i * 1000));
        outPhases.add(new Phase(Phase.POST_LOGICAL, ++i * 1000));

        outPhases.add(new Phase(Phase.PREPARE_SEND, ++i * 1000));
        outPhases.add(new Phase(Phase.PRE_STREAM, ++i * 1000));
        outPhases.add(new Phase(Phase.PRE_PROTOCOL, ++i * 1000));
        outPhases.add(new Phase(Phase.PRE_PROTOCOL_FRONTEND, ++i * 1000));
        outPhases.add(new Phase(Phase.WRITE, ++i * 1000));

        outPhases.add(new Phase(Phase.PRE_ENCODE, ++i * 1000));
        outPhases.add(new Phase(Phase.ENCODE, ++i * 1000));
        outPhases.add(new Phase(Phase.POST_ENCODE, ++i * 1000));

        outPhases.add(new Phase(Phase.USER_PROTOCOL, ++i * 1000));
        outPhases.add(new Phase(Phase.PROTOCOL, ++i * 1000));
        outPhases.add(new Phase(Phase.POST_PROTOCOL, ++i * 1000));

        outPhases.add(new Phase(Phase.USER_STREAM, ++i * 1000));
        outPhases.add(new Phase(Phase.POST_STREAM, ++i * 1000));

        outPhases.add(new Phase(Phase.SEND, ++i * 1000));
        outPhases.add(new Phase(Phase.SEND_ENDING, ++i * 1000));
        outPhases.add(new Phase(Phase.POST_STREAM_ENDING, ++i * 1000));
        outPhases.add(new Phase(Phase.USER_STREAM_ENDING, ++i * 1000));

        outPhases.add(new Phase(Phase.POST_PROTOCOL_ENDING, ++i * 1000));
        outPhases.add(new Phase(Phase.USER_PROTOCOL_ENDING, ++i * 1000));

        outPhases.add(new Phase(Phase.ENCODE_ENDING, ++i * 1000));
        outPhases.add(new Phase(Phase.WRITE_ENDING, ++i * 1000));

        outPhases.add(new Phase(Phase.PRE_PROTOCOL_ENDING, ++i * 1000));

        outPhases.add(new Phase(Phase.PRE_STREAM_ENDING, ++i * 1000));

        outPhases.add(new Phase(Phase.PREPARE_SEND_ENDING, ++i * 1000));
        outPhases.add(new Phase(Phase.POST_LOGICAL_ENDING, ++i * 1000));
        outPhases.add(new Phase(Phase.USER_LOGICAL_ENDING, ++i * 1000));
        outPhases.add(new Phase(Phase.PRE_LOGICAL_ENDING, ++i * 1000));
        outPhases.add(new Phase(Phase.SETUP_ENDING, ++i * 1000));

    }
}
