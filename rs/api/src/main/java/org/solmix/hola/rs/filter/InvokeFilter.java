package org.solmix.hola.rs.filter;

import org.solmix.hola.rs.call.RemoteRequest;

public interface InvokeFilter
{
    
    public Object doFilter ( RemoteRequest request,InvokeFilterChain chain) throws Throwable;


}
