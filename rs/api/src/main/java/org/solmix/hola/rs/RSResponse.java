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
package org.solmix.hola.rs;

import java.util.Map;


/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1  2014年8月25日
 */

public interface RSResponse
{
    /**
     * Get invoke result.
     * 
     * @return result. if no result return null.
     */
    Object getValue();

    /**
     * Get exception.
     * 
     * @return exception. if no exception return null.
     */
    Throwable getException();

  /**
   * Has exception.
   * 
   * @return has exception.
   */
  boolean hasException();
  

  Map<String, String> getProperties();
 
  String getProperty(String key);
  
  String getProperty(String key, String defaultValue);
}
