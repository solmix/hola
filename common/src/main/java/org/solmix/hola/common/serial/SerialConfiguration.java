/**
 * Copyright (c) 2015 The Solmix Project
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

package org.solmix.hola.common.serial;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2015年1月13日
 */

public class SerialConfiguration
{

    private String serial;

    private Integer palyload;

    private boolean decodeInIo = true;

    public SerialConfiguration()
    {

    }

    public SerialConfiguration(String serialization)
    {
        this.serial = serialization;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serialization) {
        this.serial = serialization;
    }

    public Integer getPalyload() {
        return palyload;
    }

    public void setPalyload(Integer palyload) {
        this.palyload = palyload;
    }

    public boolean isDecodeInIo() {
        return decodeInIo;
    }

    public void setDecodeInIo(boolean decodeInIo) {
        this.decodeInIo = decodeInIo;
    }

}
