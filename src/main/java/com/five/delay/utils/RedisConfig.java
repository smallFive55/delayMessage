package com.five.delay.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 
 * @author 小五老师-云析学院
 * @createTime 2018年11月11日 下午5:50:47
 * Jeids config
 */
@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;
    
    @Value("${spring.redis.jedis.pool.max-total}")
    private int maxTotal;
    
    @Value("${spring.redis.jedis.pool.max-wait}")
    private long maxWaitMillis;

	@Bean
    public JedisPool redisPoolFactory() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxTotal);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout);
        return jedisPool;
    }
}
