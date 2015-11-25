
package org.solmix.hola.cluster.router;

import java.util.List;

import org.solmix.hola.cluster.Router;
import org.solmix.hola.common.model.ServiceID;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;

public class ExpressionRouter implements Router
{

    public ExpressionRouter(String rule)
    {
        parseRule(rule);
    }

    protected void parseRule(String rule) {
        // TODO Auto-generated method stub

    }

    @Override
    public int compareTo(Router o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public <T> List<RemoteService<T>> route(List<RemoteService<T>> routes, ServiceID consumer, RemoteRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

}
