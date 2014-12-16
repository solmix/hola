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

package org.solmix.hola.rm.generic;

import org.solmix.runtime.Container;
import org.solmix.runtime.Extension;
import org.solmix.runtime.exchange.Protocol;
import org.solmix.runtime.exchange.model.ProtocolInfo;
import org.solmix.runtime.exchange.support.AbstractProtocolFactory;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年12月10日
 */
@Extension(name = HolaProtocolFactory.NAME)
public class HolaProtocolFactory extends AbstractProtocolFactory {

    public static final String NAME = "hola";

    /**
     * @param container
     */
    public HolaProtocolFactory(Container container) {
        super(container);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.ProtocolFactory#createProtocol(org.solmix.runtime.exchange.model.ProtocolInfo)
     */
    @Override
    public Protocol createProtocol(ProtocolInfo info) {
        HolaProtocol hp = new HolaProtocol(info);

        return hp;
    }

}
