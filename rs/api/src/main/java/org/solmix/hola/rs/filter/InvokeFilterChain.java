package org.solmix.hola.rs.filter;

import org.solmix.hola.rs.call.RemoteRequest;

public interface InvokeFilterChain
{

    public Object doFilter ( RemoteRequest request)  throws Throwable;
}
