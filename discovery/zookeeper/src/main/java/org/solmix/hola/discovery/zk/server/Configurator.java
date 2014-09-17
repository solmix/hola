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
package org.solmix.hola.discovery.zk.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.solmix.commons.util.Assert;
import org.solmix.hola.core.identity.ID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月17日
 */

public class Configurator
{
    public static final Configurator INSTANCE = new Configurator();
    private File zooConfFile;
    private File zookeeperData;
    private final List<Configuration> runningConfigs = new ArrayList<Configuration>();
    
    public Configuration createConfig(ID targetId) {
        Assert.isNotNull(targetId);
        Configuration conf = new Configuration(targetId);
        runningConfigs.add(conf);
        return conf;
  }
    public Configuration createConfig(String propsAsString) {
        Assert.isNotNull(propsAsString);
        Configuration conf = new Configuration(propsAsString);
        runningConfigs.add(conf);
        return conf;
  }

  public void cleanAll() {
        for (File file : this.zookeeperData.listFiles()) {
              try {
                    if (file.isDirectory()) {
                          for (File f : file.listFiles())
                                f.delete();
                    }
                    file.delete();
              } catch (Throwable t) {
                    continue;
              }
        }
  }

  public String getConfFile() {
        return this.zooConfFile.toString();
  }

  public static boolean isValid(String flavorInput) {
        Assert.isNotNull(flavorInput);
        boolean valid = flavorInput.contains("=");
        String f = flavorInput.split("=")[0];
        valid &= f.equals(Configuration.ZOODISCOVERY_FLAVOR_CENTRALIZED)
                    || f.equals(Configuration.ZOODISCOVERY_FLAVOR_REPLICATED)
                    || f.equals(Configuration.ZOODISCOVERY_FLAVOR_STANDALONE);
        return valid;
  }

  public static void validateFlavor(String f) {
        if (!isValid(f))
              throw new IllegalArgumentException(f);
  }
}
