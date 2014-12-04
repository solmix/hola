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

package org.solmix.hola.rm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.solmix.hola.core.model.RemoteServiceInfo;
import org.solmix.runtime.exchange.Exchange;
import org.solmix.runtime.exchange.Service;
import org.solmix.runtime.exchange.event.ServiceFactoryEvent;
import org.solmix.runtime.exchange.model.ArgumentInfo;
import org.solmix.runtime.exchange.model.FaultInfo;
import org.solmix.runtime.exchange.model.InterfaceInfo;
import org.solmix.runtime.exchange.model.MessageInfo;
import org.solmix.runtime.exchange.model.NamedID;
import org.solmix.runtime.exchange.model.OperationInfo;
import org.solmix.runtime.exchange.model.ServiceInfo;
import org.solmix.runtime.exchange.support.DefaultService;
import org.solmix.runtime.exchange.support.ReflectServiceFactory;
import org.solmix.runtime.interceptor.Fault;
import org.solmix.runtime.interceptor.phase.PhasePolicy;

/**
 * 
 * @author solmix.f@gmail.com
 * @version $Id$ 2014年12月2日
 */

public class RemoteServiceFactory extends ReflectServiceFactory {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteServiceFactory.class);

    private RemoteServiceInfo<?> remoteServiceInfo;
    
    private final RemotePhasePolicy phasePolicy;

    public RemoteServiceFactory(){
        phasePolicy=new RemotePhasePolicy();
    }
    @Override
    protected void buildService() {
        if (getRemoteServiceInfo() != null) {
            buildServiceFromInfo();
        } else if (getServiceClass() != null) {
            buildServiceFromClass();
        } else {
            throw new IllegalStateException("service class is null");
        }
    }

    /**
     * 
     */
    private void buildServiceFromInfo() {
        // TODO Auto-generated method stub

    }

    /**
     * 
     */
    protected void buildServiceFromClass() {
        if (LOG.isInfoEnabled()) {
            LOG.info("create Service from class :"
                + getServiceClass().getName());
        }
        if (Proxy.isProxyClass(this.getServiceClass())) {
            LOG.warn("USING_PROXY_FOR_SERVICE", getServiceClass());
        }
        pulishEvent(ServiceFactoryEvent.CREATE_FROM_CLASS, getServiceClass());

        ServiceInfo info = new ServiceInfo();

        Service service = new DefaultService(info);

        setService(service);
        info.setName(getServiceName());
        if (getProperties() != null) {
            service.putAll(getProperties());
        }
        createInterface(info);

    }

    /**
     * @param info
     */
    protected void createInterface(ServiceInfo serviceInfo) {
        NamedID intfName = getInterfaceName();
        InterfaceInfo intf = new InterfaceInfo(serviceInfo, intfName);
        Method[] methods = getServiceClass().getMethods();
        for (Method method : methods) {
            if (isValidMethod(method)) {
                createOperation(serviceInfo, intf, method);
            }
        }
    }

    protected void createOperation(ServiceInfo serviceInfo, InterfaceInfo intf,
        Method method) {
        OperationInfo op = intf.addOperation(getOperationName(intf, method));
        final Annotation[] annotations = method.getAnnotations();
        final Annotation[][] parAnnotations = method.getParameterAnnotations();
        op.setProperty(METHOD_ANNOTATIONS, annotations);
        op.setProperty(METHOD_PARAM_ANNOTATIONS, parAnnotations);
        createArgument(intf,op,method);
    }

   
    protected void createArgument(InterfaceInfo intf, OperationInfo op,
        Method method) {
      //输入
        final Class<?>[] paramClasses = method.getParameterTypes();
        op.setProperty(METHOD, method);
        MessageInfo inMi=op.createMessage(getInMessageName(op,method), MessageInfo.Type.INPUT);
        op.setInput(inMi.getName().getName(), inMi);
        final Annotation[][] parAnnotations = method.getParameterAnnotations();
        final Type[] genParTypes = method.getGenericParameterTypes();
        for (int j = 0; j < paramClasses.length; j++) {
            if (Exchange.class.equals(paramClasses[j])) {
                continue;
            }
            if(isInParam(method,j)){
                NamedID argumentName=getInArgumentName(op,method,j);
                ArgumentInfo argument=inMi.addArgument(argumentName);
                initializeParameter(argument, paramClasses[j], genParTypes[j]);
                argument.setProperty(METHOD_PARAM_ANNOTATIONS, parAnnotations);
                argument.setProperty(PARAM_ANNOTATION, parAnnotations[j]);
                argument.setIndex(j);
            }
        }
        pulishEvent(ServiceFactoryEvent.OPERATIONINFO_IN_MESSAGE_SET, op,method,inMi);
        //输出
        boolean hasOut = hasOutMessage(method);
        if (hasOut) {
            MessageInfo outMi=op.createMessage(getOutMessageName(op,method), MessageInfo.Type.OUTPUT);
            op.setInput(outMi.getName().getName(), outMi);
            final Class<?> returnType = method.getReturnType();
            if (!returnType.isAssignableFrom(void.class)) {
                final NamedID q = getOutArgumentName(op, method, -1);
                ArgumentInfo argument = outMi.addArgument(q);
                initializeParameter(argument, method.getReturnType(), method.getGenericReturnType());
            
                final Annotation[] annotations = method.getAnnotations();
                argument.setProperty(METHOD_ANNOTATIONS, annotations);
                argument.setProperty(PARAM_ANNOTATION, annotations);
                argument.setIndex(0);
            }
            pulishEvent(ServiceFactoryEvent.OPERATIONINFO_OUT_MESSAGE_SET, op,method,inMi);
        }
        if (hasOut) {
            // Faults are only valid if not a one-way operation
            initializeFaults(intf, op, method);
        }
    }

    protected FaultInfo addFault(final InterfaceInfo service,
        final OperationInfo op, Class<?> exClass) {
        Class<?> beanClass = getBeanClass(exClass);
        if (beanClass == null) {
            return null;
        }
        String faultMsgName =namedIDPolicy.getFaultMessageName(op,exClass,beanClass);
        
        if (faultMsgName == null) {
            faultMsgName = exClass.getSimpleName();
        }

        NamedID faultName = getFaultName(service, op, exClass, beanClass);
        FaultInfo fi = op.addFault(new NamedID(op.getName().getServiceNamespace(),
            faultMsgName), new NamedID(op.getName().getServiceNamespace(),
            faultMsgName));
        fi.setProperty(Class.class.getName(), exClass);
        fi.setProperty("elementName", faultName);
        ArgumentInfo mpi = fi.addArgument(new NamedID(
            faultName.getServiceNamespace(), exClass.getSimpleName()));
        mpi.setTypeClass(beanClass);
        pulishEvent(ServiceFactoryEvent.OPERATIONINFO_FAULT, op, exClass, fi);
        return fi;
    }
   
    private NamedID getFaultName(InterfaceInfo service, OperationInfo op,
        Class<?> exClass, Class<?> beanClass) {
        return namedIDPolicy.getFaultName(service,op,exClass,beanClass);
    }

    protected Class<?> getBeanClass(Class<?> exClass) {
        if (java.rmi.RemoteException.class.isAssignableFrom(exClass)) {
            return null;
        }
        return exClass;
    }

   
    protected void initializeFaults(InterfaceInfo intf, OperationInfo op,
        Method method) {
        final Class<?>[] exceptionClasses = method.getExceptionTypes();
        for (int i = 0; i < exceptionClasses.length; i++) {
            Class<?> exClazz = exceptionClasses[i];

            // Ignore XFireFaults because they don't need to be declared
            if (Fault.class.isAssignableFrom(exClazz)
                || exClazz.equals(RuntimeException.class) || exClazz.equals(Throwable.class)) {
                continue;
            }
            addFault(intf, op, exClazz);
        }
        
    }
    protected void initializeParameter(ArgumentInfo argument, Class<?> rawClass,
        Type type) {
        if (type instanceof TypeVariable) {
            if (parameterizedTypes == null) {
                processParameterizedTypes();
            }
            TypeVariable<?> var = (TypeVariable<?>)type;
            final Object gd = var.getGenericDeclaration();
            Map<String, Class<?>> mp = parameterizedTypes.get(gd);
            if (mp != null) {
                Class<?> c = parameterizedTypes.get(gd).get(var.getName());
                if (c != null) {
                    rawClass = c;
                    type = c;
                    argument.getMessageInfo().setProperty("parameterized", Boolean.TRUE);
                }
            }
        }
        argument.setProperty(GENERIC_TYPE, type);
        // if rawClass is List<String>, it will be converted to array
        // and set it to type class
        if (Collection.class.isAssignableFrom(rawClass)) {
            argument.setProperty(RAW_CLASS, rawClass);
        }
        argument.setTypeClass(rawClass);
        
    }

    protected NamedID getOutMessageName(OperationInfo op, Method method) {
        return namedIDPolicy.getOutMessageName(op,method);
    }

    protected boolean hasOutMessage(Method method) {
        return namedIDPolicy.hasOutMessage(method);
    }

    protected NamedID getInArgumentName(OperationInfo op, Method method, int j) {
        if (j == -1) {
            return null;
        }
        return namedIDPolicy.getInArgumentName(op,method,j);
    }
    protected NamedID getOutArgumentName(OperationInfo op, Method method, int j) {
        return namedIDPolicy.getOutArgumentName(op,method,j);
    }

    protected boolean isInParam(Method method, int j) {
        return namedIDPolicy.isInParam(method,j);
    }

    protected NamedID getInMessageName(final OperationInfo op, final Method method) {
        return namedIDPolicy.getInMessageName(op,method);
    }

    protected NamedID getOperationName(InterfaceInfo intf, Method method) {
        return namedIDPolicy.getOperationName(intf,method);
    }

    /**
     * @param method
     * @return
     */
    protected boolean isValidMethod(Method method) {
        if (namedIDPolicy != null) {
            return namedIDPolicy.isValidOperation(method);
        }
        return false;
    }

    /**   */
    public RemoteServiceInfo<?> getRemoteServiceInfo() {
        return remoteServiceInfo;
    }

    /**   */
    public void setRemoteServiceInfo(RemoteServiceInfo<?> remoteServiceInfo) {
        this.remoteServiceInfo = remoteServiceInfo;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.solmix.runtime.exchange.support.ReflectServiceFactory#getPhasePolicy()
     */
    @Override
    protected PhasePolicy getPhasePolicy() {
        return phasePolicy;
    }

}
