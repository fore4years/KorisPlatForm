package com.generator.rental.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Set cache expiration time
     * @param key Key
     * @param time Time in seconds
     * @return true if successful
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get expiration time
     * @param key Key
     * @return Time in seconds, 0 means permanent
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * Check if key exists
     * @param key Key
     * @return true if exists
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete cache
     * @param key Can be one or multiple keys
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    //String Operations
    
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Set cache with random expiration time to prevent avalanche
     * @param key Key
     * @param value Value
     * @param time Base time in seconds
     * @return true if successful
     */
    public boolean setWithRandomExpire(String key, Object value, long time) {
        try {
            if (time > 0) {
                // Add random time between 0-300 seconds (5 minutes)
                long randomTime = (long) (Math.random() * 300);
                redisTemplate.opsForValue().set(key, value, time + randomTime, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("Delta must be greater than 0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("Delta must be greater than 0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // Map Operations
    
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    public boolean hset(String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Set Operations
    
    public Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // List Operations
    
    public List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public boolean lSet(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- Distributed Lock ---

    /**
     * Acquire lock
     * @param key Lock key
     * @param value Lock value (usually UUID to identify owner)
     * @param timeout Expiration time in seconds
     * @return true if acquired
     */
    public boolean lock(String key, String value, long timeout) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS));
    }

    /**
     * Release lock
     * @param key Lock key
     * @param value Lock value (to ensure owner releases it)
     */
    public void unlock(String key, String value) {
        Object currentValue = redisTemplate.opsForValue().get(key);
        if (value.equals(currentValue)) {
            redisTemplate.delete(key);
        }
    }
}
