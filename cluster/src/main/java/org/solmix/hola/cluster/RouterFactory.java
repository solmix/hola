package org.solmix.hola.cluster;

import org.solmix.runtime.Extension;

@Extension("expression")
public interface RouterFactory
{

    Router createRouter(String rule);
}
