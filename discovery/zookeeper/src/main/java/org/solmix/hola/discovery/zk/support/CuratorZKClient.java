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
package org.solmix.hola.discovery.zk.support;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;
import org.solmix.hola.core.model.DiscoveryInfo;
import org.solmix.hola.discovery.zk.ChildListener;
import org.solmix.hola.discovery.zk.StateListener;
import org.solmix.hola.discovery.zk.ZKClient;


/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$  2014年9月15日
 */

public class CuratorZKClient implements ZKClient
{

    private final DiscoveryInfo info;
    private final CuratorFramework client;
    private final ConcurrentMap<String, ConcurrentMap<ChildListener, CuratorWatcher>> childListeners = new ConcurrentHashMap<String, ConcurrentMap<ChildListener, CuratorWatcher>>();
    private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<StateListener>();

    /**
     * @param info
     */
    public CuratorZKClient(DiscoveryInfo info)
    {
        this.info = info;
        try {
        Builder builder = CuratorFrameworkFactory.builder().connectString(
            info.getBackupAddress()).retryPolicy(
            new RetryNTimes(Integer.MAX_VALUE, 1000)).connectionTimeoutMs(5000);
        String authority = info.getAuthority();
        if (authority != null && authority.length() > 0) {
            builder = builder.authorization("digest", authority.getBytes());
        }
        client = builder.build();
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
              @Override
            public void stateChanged(CuratorFramework client, ConnectionState state) {
                    if (state == ConnectionState.LOST) {
                        CuratorZKClient.this.stateChanged(StateListener.DISCONNECTED);
                    } else if (state == ConnectionState.CONNECTED) {
                        CuratorZKClient.this.stateChanged(StateListener.CONNECTED);
                    } else if (state == ConnectionState.RECONNECTED) {
                        CuratorZKClient.this.stateChanged(StateListener.RECONNECTED);
                    }
              }
        });
        client.start();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
      }
    }
    protected void stateChanged(int state) {
        for (StateListener sessionListener : getSessionListeners()) {
              sessionListener.stateChanged(state);
        }
  }
    public Set<StateListener> getSessionListeners() {
        return stateListeners;
  }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.zk.ZKClient#create(java.lang.String, boolean)
     */
    @Override
    public void create(String path, boolean ephemeral) {
        int i = path.lastIndexOf('/');
        if (i > 0) {
              create(path.substring(0, i), false);
        }
        if (ephemeral) {
              createEphemeral(path);
        } else {
              createPersistent(path);
        }
    }
    public void createPersistent(String path) {
        try {
              client.create().forPath(path);
        } catch (NodeExistsException e) {
        } catch (Exception e) {
              throw new IllegalStateException(e.getMessage(), e);
        }
  }
    public void createEphemeral(String path) {
        try {
              client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (NodeExistsException e) {
        } catch (Exception e) {
              throw new IllegalStateException(e.getMessage(), e);
        }
  }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.zk.ZKClient#delete(java.lang.String)
     */
    @Override
    public void delete(String path) {
        try {
            client.delete().forPath(path);
      } catch (NoNodeException e) {
      } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
      }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.zk.ZKClient#getChildren(java.lang.String)
     */
    @Override
    public List<String> getChildren(String path) {
        try {
            return client.getChildren().forPath(path);
      } catch (NoNodeException e) {
            return null;
      } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
      }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.zk.ZKClient#addChildListener(java.lang.String, org.solmix.hola.discovery.zk.ChildListener)
     */
    @Override
    public List<String> addChildListener(String path, ChildListener listener) {
        ConcurrentMap<ChildListener, CuratorWatcher> listeners = childListeners.get(path);
        if (listeners == null) {
            childListeners.putIfAbsent(path,
                new ConcurrentHashMap<ChildListener, CuratorWatcher>());
            listeners = childListeners.get(path);
        }
        CuratorWatcher targetListener = listeners.get(listener);
        if (targetListener == null) {
            listeners.putIfAbsent(listener,
                createTargetChildListener(path, listener));
            targetListener = listeners.get(listener);
        }
        return addTargetChildListener(path, targetListener);
    }

    public List<String> addTargetChildListener(String path,
        CuratorWatcher listener) {
        try {
            return client.getChildren().usingWatcher(listener).forPath(path);
        } catch (NoNodeException e) {
            return null;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public CuratorWatcher createTargetChildListener(String path,
        ChildListener listener) {
        return new CuratorWatcherImpl(listener);
    }
    private class CuratorWatcherImpl implements CuratorWatcher {
        
        private volatile ChildListener listener;
        
        public CuratorWatcherImpl(ChildListener listener) {
              this.listener = listener;
        }
        
        public void unwatch() {
              this.listener = null;
        }
        
        @Override
        public void process(WatchedEvent event) throws Exception {
              if (listener != null) {
                    listener.childChanged(event.getPath(), client.getChildren().usingWatcher(this).forPath(event.getPath()));
              }
        }
  }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.zk.ZKClient#removeChildListener(java.lang.String, org.solmix.hola.discovery.zk.ChildListener)
     */
    @Override
    public void removeChildListener(String path, ChildListener listener) {
        ConcurrentMap<ChildListener, CuratorWatcher> listeners = childListeners.get(path);
        if (listeners != null) {
            CuratorWatcher targetListener = listeners.remove(listener);
              if (targetListener != null) {
                    removeTargetChildListener(path, targetListener);
              }
        }
    }

    public void removeTargetChildListener(String path, CuratorWatcher listener) {
        ((CuratorWatcherImpl) listener).unwatch();
    }
    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.zk.ZKClient#addStateListener(org.solmix.hola.discovery.zk.StateListener)
     */
    @Override
    public void addStateListener(StateListener listener) {
        stateListeners.add(listener);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.zk.ZKClient#removeStateListener(org.solmix.hola.discovery.zk.StateListener)
     */
    @Override
    public void removeStateListener(StateListener listener) {
        stateListeners.remove(listener);

    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.zk.ZKClient#isConnected()
     */
    @Override
    public boolean isConnected() {
            return client.getZookeeperClient().isConnected();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.discovery.zk.ZKClient#close()
     */
    @Override
    public void close() {
        client.close();
    }

}
