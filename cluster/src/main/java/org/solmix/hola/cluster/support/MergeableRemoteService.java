
package org.solmix.hola.cluster.support;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.solmix.commons.util.NamedThreadFactory;
import org.solmix.commons.util.ObjectUtils;
import org.solmix.commons.util.StringUtils;
import org.solmix.exchange.ClientCallback;
import org.solmix.hola.cluster.Directory;
import org.solmix.hola.cluster.Merger;
import org.solmix.hola.cluster.MergerFactory;
import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.rs.RemoteException;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;
import org.solmix.hola.rs.support.AbstractRemoteService;
import org.solmix.runtime.Container;

public class MergeableRemoteService<T> extends AbstractRemoteService<T> implements RemoteService<T>
{

    private Container container;

    private final Directory<T> directory;

    private ExecutorService executor = Executors.newCachedThreadPool(new NamedThreadFactory("mergeable-cluster-executor", true));

    public MergeableRemoteService(Directory<T> directory, Container container)
    {
        this.container = container;
        this.directory = directory;
    }

    @Override
    public String getAddress() {
        return directory.getAddress();
    }

    @Override
    public boolean isAvailable() {
        return directory.isAvailable();
    }

    @Override
    public void destroy() {
        directory.destroy();
    }

    @Override
    public Class<T> getServiceClass() {
        return directory.getServiceClass();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object[] invoke(final ClientCallback callback, final RemoteRequest request, final boolean oneway) throws RemoteException {
        List<RemoteService<T>> services =directory.list(request);
        ServiceProperties sp= directory.getServiceProperties();
        String merger = PropertiesUtils.getString(sp, request.getMethodName()+"."+HOLA.MERGER_KEY);
        if(StringUtils.isEmpty(merger)){
            for(RemoteService<T> rs:services){
                if(rs.isAvailable()){
                   return rs.invoke(callback, request, oneway);
                }
            }
        }
        
        Class<?> returnType;
        try {
            returnType = getServiceClass().getMethod(
                    request.getMethodName(), request.getParameterTypes() ).getReturnType();
        } catch ( NoSuchMethodException e ) {
            returnType = null;
        }
        List<Future<Object>> futures = new ArrayList<Future<Object>>();
        for(final RemoteService<T> rs:services){
            Future<Object> future = executor.submit( new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    Object[] objs= rs.invoke(callback,request,oneway);
                    return (objs==null||objs.length==0)?null:objs[0];
                }
            } );
            futures.add(future);
        }
        List<Object> resultList = new ArrayList<Object>(futures.size());
        final int timeout =  PropertiesUtils.getInt(directory.getServiceProperties(), HOLA.TIMEOUT_KEY,HOLA.DEFAULT_TIMEOUT);
        for(Future<Object> futurue:futures){
            try{
                Object obj =futurue.get(timeout, TimeUnit.MILLISECONDS);
                resultList.add(obj);
            }catch(Exception e){
                throw new RemoteException("Failed to invoke server",e);
            }
        }
        if(resultList.size()==0){
            return ObjectUtils.EMPTY_OBJECT_ARRAY;
        }else if(resultList.size()==1){
            return new Object[]{resultList.get(0)};
        }
        if(returnType==void.class){
            return ObjectUtils.EMPTY_OBJECT_ARRAY;
        }
        Object result =null;
        if (merger.startsWith(".")) {
            merger = merger.substring(1);
            Method method;
            try {
                method = returnType.getMethod(merger, returnType);
            } catch (NoSuchMethodException e) {
                throw new RemoteException(
                    new StringBuilder(32).append("Can not merge result because missing method [ ").append(merger).append(" ] in class [ ").append(
                        returnType.getClass().getName()).append(" ]").toString());
            }
            if(method!=null){
                if ( !Modifier.isPublic( method.getModifiers() ) ) {
                    method.setAccessible( true );
                }
                result = resultList.remove( 0 );
                try {
                    if ( method.getReturnType() != void.class
                            && method.getReturnType().isAssignableFrom( result.getClass() ) ) {
                        for ( Object r : resultList ) {
                            result = method.invoke( result, r );
                        }
                    } else {
                        for ( Object r : resultList ) {
                            method.invoke( result, r );
                        }
                    }
                } catch ( Exception e ) {
                    throw new RemoteException( 
                            new StringBuilder( 32 )
                                    .append( "Can not merge result: " )
                                    .append( e.getMessage() ).toString(), 
                            e );
                }
            }else{

                throw new RemoteException(
                        new StringBuilder( 32 )
                                .append( "Can not merge result because missing method [ " )
                                .append( merger )
                                .append( " ] in class [ " )
                                .append( returnType.getClass().getName() )
                                .append( " ]" )
                                .toString() );
            }
        }else{
            Merger resultMerger;
            if ( "true".equalsIgnoreCase(merger) 
                || "default".equalsIgnoreCase(merger)) {
                resultMerger = container.getExtension(MergerFactory.class).getMerger(returnType);
            } else {
                resultMerger = container.getExtensionLoader(Merger.class).getExtension(merger);
            }
            if (resultMerger != null) {
                List<Object> rets = new ArrayList<Object>(resultList.size());
                for ( Object r : resultList ) {
                    rets.add(r);
                }
                result = resultMerger.merge(
                        rets.toArray((Object[])Array.newInstance(returnType, 0)));
            } else {
                throw new RemoteException( "There is no merger to merge result." );
            }
        }
        return new Object[]{result};
}

    @Override
    public ServiceProperties getServiceProperties() {
        return directory.getServiceProperties();
    }

}
