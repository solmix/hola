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


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年12月5日
 */

public class HelloServiceImpl implements HelloService {

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rm.generic.HelloService#sayHello()
     */
    @Override
    public String sayHello() {
        return "hello";
    }

    @Override
    public String sayHelloTo(String name) {
        return new StringBuilder().append("Hello ").append(name).append(" !").toString();
    }

}
