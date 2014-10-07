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
package org.solmix.hola.rt.config;

import java.util.List;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月23日
 */

public class MethodConfig extends AbstractMethodConfig {

	 /**
     * @param container
     */
     MethodConfig()
    {
        super(null);
    }
    private static final long serialVersionUID = 537718142933383316L;
	 
	 /**
	 *方法名称 
	 */
	private String            name;
	
	 // 是否重试
	    private Boolean           retry;
	 // 是否需要返回
	    private Boolean           isReturn;
	    
	    //异步调用回调实例
	    private Object            oninvoke;

	    //异步调用回调方法
	    private String            oninvokeMethod;
	    
	    //异步调用回调实例
	    private Object            onreturn;

	    //异步调用回调方法
	    private String            onreturnMethod;
	    
	    //异步调用异常回调实例
	    private Object            onthrow;
	    
	    //异步调用异常回调方法
	    private String            onthrowMethod;
	    
	    private List<ArgumentConfig> arguments;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		 checkMethodName("name", name);
		this.name = name;
		if (id == null || id.length() == 0) {
	            id = name;
	        }
	}
	 public void setArguments(List<? extends ArgumentConfig> arguments) {
	        this.arguments = (List<ArgumentConfig>) arguments;
	    }
	    public List<ArgumentConfig> getArguments() {
	        return arguments;
	    }
	 
}
