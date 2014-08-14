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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.osgi.framework.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.HolaException;
import org.solmix.hola.rs.event.RemoteCallCompleteEvent;
import org.solmix.hola.rs.event.RemoteCallEvent;
import org.solmix.hola.rs.identity.RemoteServiceID;
import org.solmix.hola.rs.internal.Plugin;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年4月30日
 */

public abstract class AbstractRemoteService implements RemoteService,
    InvocationHandler
{

    protected  final Logger LOG= LoggerFactory.getLogger(this.getClass());
    protected static final Object[] EMPTY_ARGS = new Object[0];

    protected ExecutorService futureExecutorService;

    protected int futureExecutorServiceMaxThreads = Integer.parseInt(System.getProperty(
        "hola.remoteservice.futureExecutorServiceMaxThreads", "10"));

    protected abstract RemoteServiceID getRemoteServiceID();

    protected abstract String[] getInterfaceClassNames();

    protected abstract RemoteServiceReference<?> getRemoteServiceReference();

    /**
     * {@inheritDoc}
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     *      java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        try {

            Object resultObject = invokeObject(proxy, method, args);
            if (resultObject != null)
                return resultObject;
            if (isAsync(proxy, method, args))
                return invokeAsync(method, args);
            else {
                final String callMethod = getCallMethodNameForProxyInvoke(
                    method, args);
                final Object[] callParameters = getCallParametersForProxyInvoke(
                    callMethod, method, args);
                final long callTimeout = getCallTimeoutForProxyInvoke(
                    callMethod, method, args);
                final RemoteCall remoteCall = createRemoteCall(callMethod,
                    callParameters, callTimeout);
                return invokeSync(remoteCall);
            }
        } catch (Throwable t) {
            if (t instanceof ServiceException)
                throw t;
            // rethrow as service exception
            throw new ServiceException(
                "Service exception on remote service proxy rsid=" + getRemoteServiceID(), ServiceException.REMOTE, t); //$NON-NLS-1$
        }
    }

    protected Object[] getCallParametersForProxyInvoke(String callMethod,
        Method proxyMethod, Object[] args) {
        return args == null ? EMPTY_ARGS : args;
    }

    protected long getCallTimeoutForProxyInvoke(String callMethod,
        Method proxyMethod, Object[] args) {
        return RemoteCall.DEFAULT_TIMEOUT;
    }

    protected String getCallMethodNameForProxyInvoke(Method method,
        Object[] args) {
        return method.getName();
    }

    /**
     * @param remoteCall
     * @return
     */
    protected Object invokeSync(RemoteCall remoteCall) {
        return sync(remoteCall);
    }

    protected Object invokeObject(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();
        if (methodName.equals("toString")) {
            final String[] clazzes = getInterfaceClassNames();
            String proxyClass = (clazzes.length == 1) ? clazzes[0]
                : Arrays.asList(clazzes).toString();
            return proxyClass + ".proxy@" + getRemoteServiceID();
        } else if (methodName.equals("hashCode")) {
            return new Integer(hashCode());
        } else if (methodName.equals("equals")) {
            if (args == null || args.length == 0)
                return Boolean.FALSE;
            try {
                return new Boolean(Proxy.getInvocationHandler(args[0]).equals(
                    this));
            } catch (IllegalArgumentException e) {
                return Boolean.FALSE;
            }
            // 调用 RemoteServiceProxy中的方法
        } else if (methodName.equals("getRemoteService")) {
            return getRemoteService();
        } else if (methodName.equals("getRemoteServiceReference")) {
            return getRemoteServiceReference();
        }
        return null;
    }

    /**
     * @param method
     * @param args
     * @return
     */
    protected Object invokeAsync(Method method, Object[] args) {
        final String invokeMethodName = getAsyncInvokeMethodName(method);
        Class<?> returnType = method.getReturnType();
        RemoteCallListener listener = null;
        Object[] parameters;
        if (!Future.class.isAssignableFrom(returnType)) {
            if (args == null || args.length == 0)
                throw new IllegalArgumentException(
                    "Async calls must include a IRemoteCallListener instance as the last argument");
            Object lastArg = args[args.length - 1];
            if (lastArg instanceof RemoteCallListener) {
                listener = (RemoteCallListener) lastArg;
                int argsLength = args.length - 1;
                parameters = new Object[argsLength];
                System.arraycopy(args, 0, args, 0, argsLength);
            } else if (lastArg instanceof AsyncCallback<?>) {
                listener = new CallbackRemoteCallListener(
                    (AsyncCallback<?>) lastArg);
                int argsLength = args.length - 1;
                parameters = new Object[argsLength];
                System.arraycopy(args, 0, args, 0, argsLength);
            } else {
                throw new IllegalArgumentException(
                    "Last argument must be an instance of IRemoteCallListener");
            }
        } else {
            parameters = args;
        }
        return async(
            createRemoteCall(invokeMethodName, parameters,
                RemoteCall.DEFAULT_TIMEOUT), listener, returnType);
    }

    /**
     * @param method
     * @return
     */
    protected String getAsyncInvokeMethodName(Method method) {
        String methodName = method.getName();
        return methodName.endsWith(AsyncRemoteServiceProxy.ASYNC_METHOD_SUFFIX) ? methodName.substring(
            0, methodName.length()
                - AsyncRemoteServiceProxy.ASYNC_METHOD_SUFFIX.length())
            : methodName;

    }

    protected RemoteCall createRemoteCall(final String method,
        final Object[] parameters, final long timeOut) {
        return new RemoteCall() {

            @Override
            public String getMethod() {
                return method;
            }

            @Override
            public Object[] getParameters() {
                return parameters;
            }

            @Override
            public long getTimeout() {
                return timeOut;
            }

        };
    }

    /**
     * @param proxy
     * @param method
     * @param args
     * @return
     */
    private boolean isAsync(Object proxy, Method method, Object[] args) {
        return (Arrays.asList(method.getDeclaringClass().getInterfaces()).contains(
            AsyncRemoteServiceProxy.class) || method.getName().endsWith(
            AsyncRemoteServiceProxy.ASYNC_METHOD_SUFFIX));
    }

    protected Object getRemoteService() {
        return this;
    }

    public Object async(RemoteCall call, RemoteCallListener listener,
        Class<?> returnType) {
        return (listener != null) ? asyncWithResult(call, listener)
            : callFuture(call, returnType);

    }

    /**
     * @param call
     * @param returnType
     * @return
     */
    protected Object callFuture(RemoteCall call, Class<?> returnType) {
        if (Future.class.isAssignableFrom(returnType)) {
            return async(call);
        }
        return async(call);
    }

    protected ExecutorService getFutureExecutorService(RemoteCall call) {
        synchronized (this) {
            if (futureExecutorService == null)
                futureExecutorService = Executors.newFixedThreadPool(futureExecutorServiceMaxThreads);
        }
        return futureExecutorService;
    }

    /**
     * @param call
     * @param listener
     * @return
     */
    protected Object asyncWithResult(RemoteCall call,
        RemoteCallListener listener) {
        async(call, listener);
        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteService#async(org.solmix.hola.rs.RemoteCall)
     */
    @Override
    public Future<Object> async(final RemoteCall call) {
        ExecutorService executorService = getFutureExecutorService(call);
        if (executorService == null)
            throw new ServiceException(
                "future executor service is null.  .  Cannot callAsync remote method=" + call.getMethod()); //$NON-NLS-1$
        return executorService.submit(new Callable<Object>() {

            @Override
            public Object call() throws Exception {
                return sync(call);
            }
        });
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteService#getProxy()
     */
    @Override
    public Object getProxy() throws HolaException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        ClassLoader cl = this.getClass().getClassLoader();
        try {
              // Get clazz from reference
              final String[] clazzes = getInterfaceClassNames();
              for (int i = 0; i < clazzes.length; i++)
                    classes.add(loadInterfaceClass(cl, clazzes[i]));
        } catch (final Exception e) {
            HolaException except = new HolaException("Failed to create proxy", e); 
              LOG.warn("Exception in remote service getProxy", except); 
              throw except;
        } catch (final NoClassDefFoundError e) {
            HolaException except = new HolaException("Failed to load proxy interface class", e);
            LOG.warn("Could not load class for getProxy", except);
              throw except;
        }
        return getProxy(cl, classes.toArray(new Class[classes.size()]));
    }

    /**
     * @param cl
     * @param string
     * @return
     * @throws ClassNotFoundException 
     */
    private Class<?> loadInterfaceClass(ClassLoader cl, String className) throws ClassNotFoundException {
        return Class.forName(className, true, cl);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.hola.rs.RemoteService#getProxy(java.lang.ClassLoader,
     *      java.lang.Class[])
     */
    @Override
    public Object getProxy(ClassLoader classLoader, Class<?>[] interfaces)
        throws HolaException {
        List<Class<?>> classes = addAsyncProxyClasses(classLoader, interfaces);
        
        addRemoteServiceProxyToProxy(classes);
        try {
            return createProxy(classLoader, classes.toArray(new Class[classes.size()]));
      } catch (final Exception e) {
          HolaException except = new HolaException("Failed to create proxy", e);
            LOG.warn("Exception in remote service getProxy", except);
            throw except;
      } catch (final NoClassDefFoundError e) {
          HolaException except = new HolaException("Failed to load proxy interface class", e);
          LOG.warn("Could not load class for getProxy", except);
            throw except;
      }
    }
    /**
     * @param classLoader
     * @param array
     * @return
     */
    protected Object createProxy(ClassLoader classLoader, Class<?>[] interfaces) {
        RemoteServiceProxyFactory factory= Plugin.getDefault().getRemoteServiceProxyFactory();
        if(factory!=null)
            return factory.createProxy(new ProxyClassLoader(classLoader), interfaces, this);
        
        return Proxy.newProxyInstance(new ProxyClassLoader(classLoader), interfaces, this);
    }

    protected void addRemoteServiceProxyToProxy(List<Class<?>> classes) {
        RemoteServiceReference<?> rsReference = getRemoteServiceReference();
        // add IRemoteServiceProxy interface to set of interfaces supported by this proxy
        if (rsReference != null && rsReference.getProperty(org.solmix.hola.rs.RemoteConstants.SERVICE_PREVENT_ASYNCPROXY) == null)
              classes.add(RemoteServiceProxy.class);
  }
    /**
     *添加代理接口
     */
    protected List<Class<?>> addAsyncProxyClasses(ClassLoader classLoader,
        Class<?>[] interfaces) {
        List<Class<?>> intfs = Arrays.asList(interfaces);
        List<Class<?>> results = new ArrayList<Class<?>>();
        if (getRemoteServiceReference().getProperty(
            org.solmix.hola.rs.RemoteConstants.SERVICE_PREVENT_ASYNCPROXY) == null) {
            for (Iterator<Class<?>> i = intfs.iterator(); i.hasNext();) {
                Class<?> intf = i.next();
                String intfName = convertInterfaceNameToAsyncInterfaceName(intf.getName());
                if(intfName!=null){
                    boolean alreadyHad=false;
                    for(Class<?> it:intfs){
                        if(it.getName().equals(intfName)){
                            alreadyHad=true;
                            break;
                        }
                    }
                    if(!alreadyHad){
                        Class<?> asyncClass=findAsyncProxyClass(classLoader,intf,intfName);
                        if (asyncClass != null && !intfs.contains(asyncClass))
                            results.add(asyncClass);
                    }
                }
            }
        }
        results.addAll(intfs);
        return results;
    }

    /**
     * @param classLoader
     * @param intf
     * @return
     */
    protected Class<?> findAsyncProxyClass(ClassLoader classLoader,
        Class<?> intf, String proxyIntfName) {
        try {
            return Class.forName(proxyIntfName, true, classLoader);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoClassDefFoundError e) {
            LOG.warn(
                "Async remote service interface with name=" + proxyIntfName
                    + " could not be loaded for proxy service class="
                    + intf.getName(), e);
            return null;
        }
    }

    /**
     * 根据接口名生成代理接口的名称
     */
    protected String convertInterfaceNameToAsyncInterfaceName(String interfaceName) {
        if (interfaceName == null)
            return null;
      String asyncProxyName = (String) getRemoteServiceReference().getProperty(org.solmix.hola.rs.RemoteConstants.SERVICE_ASYNC_RSPROXY_CLASS_ + interfaceName);
      if (asyncProxyName != null)
            return asyncProxyName;
      if (interfaceName.endsWith(AsyncRemoteServiceProxy.ASYNC_INTERFACE_SUFFIX))
            return interfaceName;
      return interfaceName + AsyncRemoteServiceProxy.ASYNC_INTERFACE_SUFFIX;
    }
    public class ProxyClassLoader extends ClassLoader {

        private final ClassLoader cl;

        public ProxyClassLoader(ClassLoader cl) {
              this.cl = cl;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
              try {
                    return cl.loadClass(name);
              } catch (ClassNotFoundException e) {
                  ClassLoader loader= Plugin.getDefault().getPluginClassLoader();
                  if(loader!=null)
                      return loader.loadClass(name);
                  else
                      throw e;
              }
        }
  }
    public class CallbackRemoteCallListener implements RemoteCallListener
    {

        private final AsyncCallback callback;

        public CallbackRemoteCallListener(AsyncCallback callback)
        {
            this.callback = callback;
        }

        @Override
        public void onHandle(RemoteCallEvent event) {
            if (event.getType() == RemoteCallEvent.COMPLETE) {
                RemoteCallCompleteEvent e = (RemoteCallCompleteEvent) event;
                if (e.hadException())
                    callback.onFailure(e.getException());
                else
                    callback.onSuccess(e.getResponse());
            }

        }

    }
}