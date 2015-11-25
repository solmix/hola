
package org.solmix.hola.cluster.router;

import org.solmix.hola.cluster.Router;
import org.solmix.hola.cluster.RouterFactory;
import org.solmix.runtime.Extension;

@Extension(name = ExpressionRouterFactory.NAME)
public class ExpressionRouterFactory implements RouterFactory
{

    public static final String NAME = "expression";

    @Override
    public Router createRouter(String rule) {
        return new ExpressionRouter(rule);
    }

}
