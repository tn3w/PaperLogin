package com.example.paperlogin.services;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class RedisService {
    private final JedisPool jedisPool;

    public RedisService(String host, int port, String password) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMaxWait(Duration.ofSeconds(10));
        
        if (password != null && !password.isEmpty()) {
            this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        } else {
            this.jedisPool = new JedisPool(poolConfig, host, port);
        }
    }

    public void set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set(key, value);
        }
    }

    public void setWithExpiry(String key, String value, int expirySeconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.setex(key, expirySeconds, value);
        }
    }

    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.get(key);
        }
    }

    public void delete(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(key);
        }
    }

    public boolean exists(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.exists(key);
        }
    }
    
    public void hset(String key, String field, String value) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(key, field, value);
        }
    }
    
    public void hmset(String key, Map<String, String> hash) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hmset(key, hash);
        }
    }
    
    public String hget(String key, String field) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hget(key, field);
        }
    }
    
    public Map<String, String> hgetAll(String key) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.hgetAll(key);
        }
    }
    
    /**
     * Scans for keys matching a pattern
     * @param pattern the key pattern to scan for
     * @return A set of keys matching the pattern
     */
    public Set<String> scanKeys(String pattern) {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.keys(pattern);
        }
    }
    
    public void expire(String key, int seconds) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.expire(key, seconds);
        }
    }

    public void shutdown() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }
} 