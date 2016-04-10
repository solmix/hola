package org.solmix.hola.rs.filter;

import java.util.Dictionary;

import org.solmix.runtime.Extension;

@Extension
public interface InvokeFilterFactory
{

    InvokeFilter create(Dictionary<String, ?> serviceProperties);
}
