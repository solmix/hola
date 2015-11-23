package org.solmix.hola.cluster.loadbalance;

import java.util.List;

import org.solmix.hola.cluster.ClusterException;
import org.solmix.hola.cluster.LoadBalance;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;


public abstract class AbstractLoadBalance implements LoadBalance
{

    @Override
    public <T> RemoteService<T> select(List<RemoteService<T>> services, RemoteRequest request) throws ClusterException {
        if(services==null||services.size()==0){
            return null;
        }
        if(services.size()==1){
            return services.get(0);
        }
        return doSelect(services,request);
    }

    protected abstract <T>  RemoteService<T> doSelect(List<RemoteService<T>> services, RemoteRequest request);
    
    protected int getWeight(RemoteService<?> service,RemoteRequest request){
        int weight =PropertiesUtils.getInt(service.getServiceProperties(), HOLA.WEIGHT_KEY, HOLA.DEFAULT_WEIGHT);
        if (weight > 0) {
              long timestamp = PropertiesUtils.getLong(service.getServiceProperties(), HOLA.TIMESTAMP_KEY, 0L);
            if (timestamp > 0L) {
                  int uptime = (int) (System.currentTimeMillis() - timestamp);
                  int warmup =PropertiesUtils.getInt(service.getServiceProperties(), HOLA.WARMUP_KEY, HOLA.DEFAULT_WARMUP);
                  if (uptime > 0 && uptime < warmup) {
                        weight = calculateWarmupWeight(uptime, warmup, weight);
                  }
            }
        }
      return weight;
    }
    
    static int calculateWarmupWeight(int uptime, int warmup, int weight) {
      int ww = (int) ( uptime / ( (float) warmup / (float) weight ) );
      return ww < 1 ? 1 : (ww > weight ? weight : ww);
    }
}
