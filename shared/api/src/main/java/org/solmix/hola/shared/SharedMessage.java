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

package org.solmix.hola.shared;

import java.io.Serializable;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年5月19日
 */

public class SharedMessage implements Serializable
{

    private static final long serialVersionUID = 3174957091235503680L;

    /**
     * @serial clazz the class name for the message
     */
    protected String clazz;

    /**
     * @serial method the method name of the message
     */
    protected String method;

    /**
     * @serial args arguments
     */
    protected Object[] parameters;

    public SharedMessage(String clazz, String method, Object... parameters)
    {
        this.clazz = clazz;
        this.method = method;
        this.parameters = parameters;
    }

    /**
     * @return the serialversionuid
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * @return the clazz
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * @return the method
     */
    public String getMethod() {
        return method;
    }

    /**
     * @return the parameters
     */
    public Object[] getParameters() {
        return parameters;
    }
    public static Builder  newBuilder() {
        return new Builder();
      }

    public static class Builder
    {

        private String clazz;
        private String method;
        private Object[] parameters;

        private Builder()
        {
        }

        public Builder setClazz(String clazz) {
            this.clazz = clazz;
            return this;
        }
        public Builder setMethod(String method) {
            this.method = method;
            return this;
        }
        public Builder setParameters(Object... parameters) {
            this.parameters = parameters;
            return this;
        }
        public SharedMessage build(){
            SharedMessage __return= new SharedMessage(clazz, method, parameters);
            return __return;
        }
    }
}
