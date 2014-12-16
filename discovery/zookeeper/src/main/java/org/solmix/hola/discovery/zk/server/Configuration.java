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
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.commons.util.Assert;
import org.solmix.hola.common.identity.ID;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月17日
 */

public class Configuration
{
  public static final  String ZOODISCOVERY_FLAVOR_STANDALONE = "zoodiscovery.flavor.standalone"; 
  public static final  String ZOODISCOVERY_FLAVOR_CENTRALIZED = "zoodiscovery.flavor.centralized";
  public static final  String ZOODISCOVERY_FLAVOR_REPLICATED = "zoodiscovery.flavor.replicated"; 

  public static final  String ZOODISCOVERY_CONSOLELOG = "consoleLog"; 

  public static final  String ZOOKEEPER_TICKTIME = "tickTime";

   
  public static final  String ZOOKEEPER_AUTOSTART = "autoStart";

    /** The directory where zookeeper can work. OPTIONAL **/
  public static final  String ZOOKEEPER_TEMPDIR = "tempDir"; 

    /**
     * The single name of the directory in {@link #ZOOKEEPER_TEMPDIR} where the
     * snapshot is stored. OPIONAL
     **/
  public static final String ZOOKEEPER_DATADIR = "dataDir"; 

    /**
     * The single name of the directory in {@link #ZOOKEEPER_TEMPDIR} where the
     * LOG is stored. It may be the same as {@link #ZOOKEEPER_DATADIR} but
     * better if separate. OPTIONAL
     **/
  public static final  String ZOOKEEPER_DATALOGDIR = "dataLogDir"; 

    /**
     * The number of ticks that the initial synchronization phase can take.
     * OPTIONAL
     **/
  public static final  String ZOOKEEPER_INITLIMIT = "initLimit"; 

    /**
     * The number of ticks that can pass between sending a request and getting
     * an acknowledgment. OPTIONAL
     **/
  public static final  String ZOOKEEPER_SYNCLIMIT = "syncLimit"; 

    /** The port at which the clients will connect. OPTIONAL **/
  public static final  String ZOOKEEPER_CLIENTPORT = "clientPort"; 

    /** Server to server port. OPTIONAL **/
  public static final  String ZOOKEEPER_SERVER_PORT = "serverPort"; 

    /** Leader election port. OPTIONAL **/
  public static final  String ZOOKEEPER_ELECTION_PORT = "electionPort"; 
  public static final String DEFAULT_FLAVOR = ZOODISCOVERY_FLAVOR_STANDALONE
      + "=" + getLocation();
  public static final String TEMPDIR_DEFAULT = System.getProperties()
      .getProperty("java.io.tmpdir"); 
  public static final String DATADIR_DEFAULT = "zookeeperData";
  public static final String DATALOGDIR_DEFAULT = DATADIR_DEFAULT;
  public static final String SERVER_PORT_DEFAULT = "2888";
  public static final String ELECTION_PORT_DEFAULT = "3888";
  public static final String CLIENT_PORT_DEFAULT = "2181";
  public static final String TICKTIME_DEFAULT = "2000";
  public static final String INITLIMIT_DEFAULT = "50";
  public static final String SYNCLIMIT_DEFAULT = "2";            
  protected static Map<String, Object> defaultConfigProperties = new HashMap<String, Object>();

  private static final Logger LOG= LoggerFactory.getLogger(Configuration.class);
  public static final String ZOODISCOVERY_PREFIX = "zoodiscovery.";            
  static {

        // Check for configuration within system properties
        defaultConfigProperties.put(ZOOKEEPER_TEMPDIR, System.getProperty(
                    ZOODISCOVERY_PREFIX + ZOOKEEPER_TEMPDIR, TEMPDIR_DEFAULT));

        defaultConfigProperties.put(ZOOKEEPER_DATADIR, System.getProperty(
                    ZOODISCOVERY_PREFIX + ZOOKEEPER_DATADIR, DATADIR_DEFAULT));

        defaultConfigProperties.put(ZOOKEEPER_DATALOGDIR, System.getProperty(
                    ZOODISCOVERY_PREFIX + ZOOKEEPER_DATALOGDIR, ZOOKEEPER_DATADIR));

        defaultConfigProperties.put(ZOOKEEPER_CLIENTPORT, System
                    .getProperty(ZOODISCOVERY_PREFIX + ZOOKEEPER_CLIENTPORT,
                                CLIENT_PORT_DEFAULT));

        defaultConfigProperties.put(ZOOKEEPER_TICKTIME, System.getProperty(
                    ZOODISCOVERY_PREFIX + ZOOKEEPER_TICKTIME, TICKTIME_DEFAULT));

        defaultConfigProperties.put(ZOOKEEPER_INITLIMIT, System.getProperty(
                    ZOODISCOVERY_PREFIX + ZOOKEEPER_INITLIMIT, INITLIMIT_DEFAULT));

        defaultConfigProperties.put(ZOOKEEPER_SYNCLIMIT, System.getProperty(
                    ZOODISCOVERY_PREFIX + ZOOKEEPER_SYNCLIMIT, SYNCLIMIT_DEFAULT));

        defaultConfigProperties.put(ZOOKEEPER_SERVER_PORT, System.getProperty(
                    ZOODISCOVERY_PREFIX + ZOOKEEPER_SERVER_PORT,
                    SERVER_PORT_DEFAULT));

        defaultConfigProperties.put(ZOOKEEPER_ELECTION_PORT, System
                    .getProperty(ZOODISCOVERY_PREFIX + ZOOKEEPER_ELECTION_PORT,
                                ELECTION_PORT_DEFAULT));

        defaultConfigProperties.put("preAllocSize", 1);            

        defaultConfigProperties.put(ZOODISCOVERY_CONSOLELOG, System.getProperty(ZOODISCOVERY_PREFIX + ZOODISCOVERY_CONSOLELOG, null));
  }
  public static URI getLocation() {
      try {
            return URI.create(InetAddress.getLocalHost().getHostAddress());
      } catch (UnknownHostException e) {
            e.printStackTrace();
      }
      return null;
}
  public static String getHost() {
      String host;
      try {
            host = InetAddress.getLocalHost().getHostAddress();
      } catch (UnknownHostException e) {
            host = "localhost"; 
      }
      return host;
}
    public Configuration(ID id)
    {
        this(id.getName());
    }

    private File zooConfFile;
    private File zookeeperDataFile;
    private List<String> serverIps = new ArrayList<String>();
    private FLAVOR flavor;
    private static final String LOCALHOST = "localhost";

    public enum FLAVOR {
        STANDALONE, CENTRALIZED, REPLICATED;

        @Override
        public String toString() {
              switch (this) {
              case STANDALONE:
                    return ZOODISCOVERY_FLAVOR_STANDALONE;
              case CENTRALIZED:
                    return ZOODISCOVERY_FLAVOR_CENTRALIZED;
              case REPLICATED:
                    return ZOODISCOVERY_FLAVOR_REPLICATED;
              }
              throw new AssertionError("Unsupported configuration");
        }
  }
    public Map<String, Object> getConfigProperties() {
          return Collections.unmodifiableMap(defaultConfigProperties);
    }

    public Configuration(String propsAsString) {
          Assert.isNotNull(propsAsString);
          String ss[] = propsAsString.split(";");
          for (String s : ss) {
                String key_value[] = s.split("=");
                if (key_value.length == 2)
                      defaultConfigProperties.put(key_value[0], key_value[1]);
          }
    }

    public Configuration configure() {
          PrintWriter writer = null;
          boolean isNewZookeeperData = false;
          try {
                String dataDirName = (String) getConfigProperties().get(
                            ZOOKEEPER_DATADIR);
                // if no data directory name is specified, we randomly pick one.
                if (DATADIR_DEFAULT.equals(dataDirName)) {
                      dataDirName = randomDirName();
                }
                this.zookeeperDataFile = new File(new File(getConfigProperties()
                            .get(ZOOKEEPER_TEMPDIR).toString()), dataDirName);
                isNewZookeeperData = this.zookeeperDataFile.mkdir();
                this.zookeeperDataFile.deleteOnExit();
                if (!isNewZookeeperData) {
                      /*
                       * the same data directory is being reused, we try emptying it
                       * to avoid data corruption
                       */
                      clean();
                }
                this.zooConfFile = new File(this.zookeeperDataFile, "zoo.cfg");
                this.zooConfFile.createNewFile();
                this.zooConfFile.deleteOnExit();
                if (getConfigProperties().containsKey(
                            ZOODISCOVERY_FLAVOR_CENTRALIZED)) {
                      this.setFlavor(FLAVOR.CENTRALIZED);
                      this.serverIps = parseIps();
                      if (this.serverIps.size() != 1) {
                            String msg = "ZooDiscovery property "
                                        + ZOODISCOVERY_FLAVOR_CENTRALIZED
                                        + " must contain exactly one IP address designating the location of the ZooDiscovery instance playing this central role.";
                            LOG.error( msg);
                            throw new ServiceException(msg);
                      }

                } else if (getConfigProperties().containsKey(
                            ZOODISCOVERY_FLAVOR_REPLICATED)) {
                      this.setFlavor(FLAVOR.REPLICATED);
                      this.serverIps = parseIps();
                      if (!this.serverIps.contains(getHost())) {
                            this.serverIps.add(getHost());
                      }
                      if (this.serverIps.size() < 2) {
                            String msg = "Industrial Discovery property "
                                        + ZOODISCOVERY_FLAVOR_REPLICATED
                                        + " must contain at least one IP address which is not localhost.";
                            LOG.error( msg);
                            throw new ServiceException(msg);
                      }

                } else if (getConfigProperties().containsKey(
                            ZOODISCOVERY_FLAVOR_STANDALONE)) {
                      this.setFlavor(FLAVOR.STANDALONE);
                      this.serverIps = parseIps();
                }
                Collections.sort(this.serverIps);
                if (this.isQuorum()) {
                      String myip = getHost();
                      int myId = this.serverIps.indexOf(myip);
                      File myIdFile = new File(getZookeeperDataFile(), "myid");
                      myIdFile.createNewFile();
                      myIdFile.deleteOnExit();
                      writer = new PrintWriter(myIdFile);
                      writer.print(myId);
                      writer.flush();
                      writer.close();
                }
                writer = new PrintWriter(this.zooConfFile);
                if (this.isQuorum()) {
                      for (int i = 0; i < this.serverIps.size(); i++) {
                            writer.println("server."
                                        + i + "="
                                        + this.serverIps.get(i) + ":"
                                        + getServerPort() + ":"
                                        + getElectionPort());
                      }
                }
                for (String k : getConfigProperties().keySet()) {
                      if (k.startsWith(ZOODISCOVERY_PREFIX)) {
                            /*
                             * Ignore properties that are not intended for ZooKeeper
                             * internal configuration
                             */
                            continue;
                      }
                      writer.println(k + "=" + getConfigProperties().get(k));
                }
                writer.flush();
                writer.close();

          } catch (IOException e) {
              LOG.error( e.getMessage(), e);
          } finally {
                if (writer != null)
                      writer.close();
          }
          return this;
    }

    private String randomDirName() {
          String name = UUID.randomUUID() + "";
          name = name.replaceAll("-", "");
          return "zdd" + name;
    }

    public int getElectionPort() {
          return Integer.parseInt((String) getConfigProperties().get(
                      ZOOKEEPER_ELECTION_PORT));
    }

    public String getConfFile() {
          return this.zooConfFile.toString();
    }

    public String getServerIps() {
          String ipsString = ""; 
          for (String i : this.serverIps) {
                ipsString += i + ","; 
          }
          return ipsString.substring(0, ipsString.lastIndexOf(","));
    }

    public int getClientPort() {
          return Integer.parseInt((String) getConfigProperties().get(
                      ZOOKEEPER_CLIENTPORT));
    }

    public List<String> getServerIpsAsList() {
          return this.serverIps;
    }

    public File getZookeeperDataFile() {
          return this.zookeeperDataFile;
    }

    public void setFlavor(FLAVOR flavor) {
          this.flavor = flavor;
    }

    public FLAVOR getFlavor() {
          return this.flavor;
    }

    public boolean isQuorum() {
          return this.flavor == FLAVOR.REPLICATED;
    }

    public boolean isCentralized() {
          return this.flavor == FLAVOR.CENTRALIZED;
    }

    public boolean isStandAlone() {
          return this.flavor == FLAVOR.STANDALONE;
    }


    private void clean() {
          for (File file : this.zookeeperDataFile.listFiles()) {
                try {
                      if (file.isDirectory()) {
                            for (File f : file.listFiles())
                                  f.delete();
                      }
                      file.delete();
                } catch (Throwable t) {
                    LOG.error(  t.getMessage());
                }
          }
    }

    private List<String> parseIps() {
          List<String> ips = Arrays.asList(((String) getConfigProperties().get(
                      flavor.toString())).split(","));
          List<String> unfixedSize = new ArrayList<String>();
          for (String ip : ips) {
                if (ip.contains(LOCALHOST))
                      ip = ip.replace(LOCALHOST, getHost());
                unfixedSize.add(ip);
          }
          Collections.sort(unfixedSize);
          return unfixedSize;
    }

    @Override
    public String toString() {
          String s = flavor.name();
          for (Object o : parseIps())
                s += o;
          return s;
    }

    public int getTickTime() {
          return Integer.parseInt((String) getConfigProperties().get(
                      ZOOKEEPER_TICKTIME));
    }

    public int getServerPort() {
          return Integer.parseInt((String) getConfigProperties().get(
                      ZOOKEEPER_SERVER_PORT));
    }
}
