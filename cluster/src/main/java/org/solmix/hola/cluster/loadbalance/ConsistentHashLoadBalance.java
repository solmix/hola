package org.solmix.hola.cluster.loadbalance;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.solmix.hola.common.HOLA;
import org.solmix.hola.common.model.PropertiesUtils;
import org.solmix.hola.common.model.ServiceProperties;
import org.solmix.hola.rs.RemoteService;
import org.solmix.hola.rs.call.RemoteRequest;


public class ConsistentHashLoadBalance extends AbstractLoadBalance
{

    private final ConcurrentMap<String, ConsistentHashSelector<?>> selectors = new ConcurrentHashMap<String, ConsistentHashSelector<?>>();

    @Override
    protected <T> RemoteService<T> doSelect(List<RemoteService<T>> services, RemoteRequest request) {
        String key =PropertiesUtils.getServiceKey(services.get(0).getServiceProperties()) + "." + request.getMethodName();
        int identityHashCode = System.identityHashCode(services);
        ConsistentHashSelector<T> selector = (ConsistentHashSelector<T>) selectors.get(key);
        if (selector == null || selector.getIdentityHashCode() != identityHashCode) {
            selectors.put(key, new ConsistentHashSelector<T>(services, request.getMethodName(), identityHashCode));
            selector = (ConsistentHashSelector<T>) selectors.get(key);
        }
        return selector.select(request);
    }
    private static final class ConsistentHashSelector<T> {

        private final TreeMap<Long, RemoteService<T>> virtualInvokers;

        private final int                       replicaNumber;
        
        private final int                       identityHashCode;
        
        private final int[]                     argumentIndex;

        public ConsistentHashSelector(List<RemoteService<T>> invokers, String methodName, int identityHashCode) {
            this.virtualInvokers = new TreeMap<Long, RemoteService<T>>();
            this.identityHashCode = System.identityHashCode(invokers);
            ServiceProperties sp = invokers.get(0).getServiceProperties();
            this.replicaNumber = PropertiesUtils.getInt(sp, methodName+".hash.nodes", 160);
            String[] index = HOLA.SPLIT_COMMA_PATTERN.split(PropertiesUtils.getString(sp, methodName+".hash.arguments", ""));
            argumentIndex = new int[index.length];
            for (int i = 0; i < index.length; i ++) {
                argumentIndex[i] = Integer.parseInt(index[i]);
            }
            for (RemoteService<T> invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker.getAddress() + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
        }

        public int getIdentityHashCode() {
            return identityHashCode;
        }

        public RemoteService<T> select(RemoteRequest invocation) {
            String key = toKey(invocation.getParameters());
            byte[] digest = md5(key);
            RemoteService<T> invoker = sekectForKey(hash(digest, 0));
            return invoker;
        }

        private String toKey(Object[] args) {
            StringBuilder buf = new StringBuilder();
            for (int i : argumentIndex) {
                if (i >= 0 && i < args.length) {
                    buf.append(args[i]);
                }
            }
            return buf.toString();
        }

        private RemoteService<T> sekectForKey(long hash) {
            RemoteService<T> invoker;
            Long key = hash;
            if (!virtualInvokers.containsKey(key)) {
                SortedMap<Long, RemoteService<T>> tailMap = virtualInvokers.tailMap(key);
                if (tailMap.isEmpty()) {
                    key = virtualInvokers.firstKey();
                } else {
                    key = tailMap.firstKey();
                }
            }
            invoker = virtualInvokers.get(key);
            return invoker;
        }

        private long hash(byte[] digest, int number) {
            return (((long) (digest[3 + number * 4] & 0xFF) << 24)
                    | ((long) (digest[2 + number * 4] & 0xFF) << 16)
                    | ((long) (digest[1 + number * 4] & 0xFF) << 8) 
                    | (digest[0 + number * 4] & 0xFF)) 
                    & 0xFFFFFFFFL;
        }

        private byte[] md5(String value) {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            md5.reset();
            byte[] bytes = null;
            try {
                bytes = value.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            md5.update(bytes);
            return md5.digest();
        }

    }
}
