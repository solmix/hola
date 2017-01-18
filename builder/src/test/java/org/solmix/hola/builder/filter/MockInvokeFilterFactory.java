package org.solmix.hola.builder.filter;

import java.util.Dictionary;

import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.filter.InvokeFilter;
import org.solmix.hola.rs.filter.InvokeFilterChain;
import org.solmix.hola.rs.filter.InvokeFilterFactory;
import org.solmix.runtime.Extension;


@Extension("test-filter")
public class MockInvokeFilterFactory implements InvokeFilterFactory
{


    @Override
    public InvokeFilter create(Dictionary<String, ?> serviceProperties) {
        return new InvokeFilter() {
            
            
            @Override
            public Object doFilter(RemoteRequest request, InvokeFilterChain chain) throws Throwable {
                System.out.println("aaaaaaaaaaaaaaaaaaa");
                return chain.doFilter(request);
            }
        };
    }

}
