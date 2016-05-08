/**
 * Copyright 2015 The Solmix Project
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

package org.solmix.hola.transport.netty;

import org.solmix.runtime.security.KeystoreInfo;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2016年5月4日
 */

public class KeystoreInfoWrapper extends KeystoreInfo
{

    private final NettyConfiguration config;

    public KeystoreInfoWrapper(NettyConfiguration config)
    {
        this.config = config;
    }

    @Override
    public String getKeyCN() {
        return config.getKeyCN();
    }

    @Override
    public void setKeyCN(String keyCN) {
        config.setKeyCN(keyCN);
    }

    @Override
    public boolean isDefault() {
        //没设置,可以按照配置信息自动生成
        return config.isKeyAuto()!=null?config.isKeyAuto():true;
    }

    @Override
    public void setIsDefault(boolean isDefault) {
        config.setKeyAuto(isDefault);
    }

    @Override
    public String getAlias() {
        return config.getKeyAlias();
    }

    @Override
    public void setAlias(String alias) {
        config.setKeyAlias(alias);
    }

    @Override
    public String getFilePath() {
        return config.getKeyFilePath();
    }

    @Override
    public void setFilePath(String filePath) {
        config.setKeyFilePath(filePath);
    }

    @Override
    public String getFilePassword() {
        return config.getKeyFilePassword();
    }

    @Override
    public void setFilePassword(String filePassword) {
        config.setKeyFilePassword(filePassword);
    }

}
