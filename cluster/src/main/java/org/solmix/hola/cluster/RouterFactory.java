package org.solmix.hola.cluster;

import java.util.Dictionary;

import org.solmix.runtime.Extension;

@Extension
public interface RouterFactory
{

    Router createRouter(Dictionary<String, ?> properties);
}
