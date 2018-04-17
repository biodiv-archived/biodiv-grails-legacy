package org.hibernate.cache.redis.hibernate4;


import org.hibernate.cache.CacheException;
import org.hibernate.cfg.Settings;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import grails.util.Holders;
import org.hibernate.cache.redis.hibernate4.SingletonRedisRegionFactory;

/**
 *  * Grails-properties region factory based on the Singleton Implementation
 *   */
public class GrailsRedisRegionFactory extends SingletonRedisRegionFactory {
    
    public GrailsRedisRegionFactory(Properties props) {
        super(props);
        System.out.println("create GrailsRedisRegionFactory instance.");
    }

    @Override
    public synchronized void start(Settings settings, Properties properties) throws CacheException {
        System.out.println("starting GrailsRedisRegionFactory...");
        Properties props = Holders.getConfig().toProperties();
        props.setProperty("redisson-config", props.getProperty("redissonConfig"));
        System.out.println(props);
        super.start(settings, props);
   }
}
