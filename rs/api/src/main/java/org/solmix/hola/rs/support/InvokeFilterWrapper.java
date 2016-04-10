
package org.solmix.hola.rs.support;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.solmix.exchange.interceptor.Fault;
import org.solmix.hola.rs.call.DefaultRemoteRequest;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.filter.InvokeFilter;
import org.solmix.hola.rs.filter.InvokeFilterChain;

public class InvokeFilterWrapper implements InvocationHandler, InvokeFilterChain
{

    private final Object instance;

    private InvokeFilterChain last;

    private final List<InvokeFilter> filters;

    public InvokeFilterWrapper(Object instance, List<InvokeFilter> filters,Dictionary<String, Object> properties)
    {
        this.instance = instance;
        this.filters = filters;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Map<String, Object> requestContext = new HashMap<String, Object>();
        DefaultRemoteRequest request = new DefaultRemoteRequest(method, args, requestContext);
        request.getRequestContext().put(Method.class.getName(), method);
        if (filters != null) {
            Object response;
            last = this;
            for (final InvokeFilter filter : filters) {
                final InvokeFilterChain next = last;
                last = new InvokeFilterChain() {

                    @Override
                    public Object doFilter(RemoteRequest request) throws Throwable {
                        return filter.doFilter(request, next);
                    }
                };
            }
            response = last.doFilter(request);
            return response;
        }
        return null;
    }

    @Override
    public Object doFilter(RemoteRequest request) throws Throwable {
        Method method = (Method) request.getRequestContext().get(Method.class.getName());

        Object obj=null;
        try {
            obj = method.invoke(instance, request.getParameters());
        } catch (InvocationTargetException e) {
            Throwable t =e.getCause();
           for (Class<?> cl : method.getExceptionTypes()) {
               if (cl.isInstance(t)) {
                   throw new Fault(t);
               }
           }
           throw e;
        }
        return obj;
    }

}
