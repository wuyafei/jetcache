package com.alicp.jetcache.embedded;

import com.alicp.jetcache.AutoReleaseLock;
import com.alicp.jetcache.Cache;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/12/20.
 *
 * @author <a href="mailto:areyouok@gmail.com">huangli</a>
 */
public class SimpleLock implements AutoReleaseLock {

    private Cache cache;
    private Object key;
    private long expireTimestamp;

    private SimpleLock(Cache cache, Object key, long expireTimestamp) {
        this.cache = cache;
        this.key = key;
        this.expireTimestamp = expireTimestamp;
    }

    public static SimpleLock tryLock(Cache cache, Object key, long expire, TimeUnit timeUnit) {
        long expireTimestamp = System.currentTimeMillis() + timeUnit.toMillis(expire);
        synchronized (cache) {
            Object fromCache = cache.get(key);
            if (fromCache == null) {
                SimpleLock lock = new SimpleLock(cache, key, expireTimestamp);
                cache.put(key, Boolean.TRUE, expire, timeUnit);
                return lock;
            } else {
                return null;
            }
        }
    }

    @Override
    public void close() {
        long t = System.currentTimeMillis();
        if (t < expireTimestamp) {
            cache.remove(key);
        }
    }
}
