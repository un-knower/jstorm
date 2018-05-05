package com.hollyvoc.helper.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;
import java.util.Set;

import static com.hollycrm.util.config.ConfigUtils.getIntVal;
import static com.hollycrm.util.config.ConfigUtils.getStrVal;

/**
 * Created by qianxm on 2017/8/4.
 * 对redis的操作
 */
public class RedisHelper {

    private static RedisHelper instance = new RedisHelper();
    public static RedisHelper getInstance(){
        return instance;
    }
    private JedisPool pool;
    private RedisHelper(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(getIntVal("redis.maxIdle", 10));
        config.setMaxWaitMillis(getIntVal("redis.maxWait", 2000));
        config.setMaxTotal(getIntVal("redis.maxTotal", 50));
        config.setTestOnBorrow(true);
        this.pool = new JedisPool(config, getStrVal("redis.host"), getIntVal("redis.port", 6379),
                getIntVal("redis.timeout", 2000), getStrVal("redis.password"), getIntVal("redis.database", 3));
    }

    /**
     * 获取redis中集合的值
     * @param key key
     * @return 集合
     */
    public Set<String> getSetByKey(String key) {
        try(Jedis jedis = pool.getResource()){
            return jedis.smembers(key);
        }
    }

    /**
     * 将值添加至指定set
     * @param key key
     * @param val 值
     * @return 数量
     */
    public Long add2Set(String key, String ... val) {
        try(Jedis jedis = pool.getResource()) {
            return jedis.sadd(key, val);
        }
    }

    /**
     * 向hash表中添加k-v
     * @param key 姑且理解为表名
     * @param field hash k
     * @param val val
     * @return 条数
     */
    public Long add2Hash(String key, String field, String val){
        try(Jedis jedis = pool.getResource()) {
            return jedis.hset(key, field, val);
        }
    }




    /**
     * 获取hash表的keys
     * @param key 表名
     * @return keys集合
     */
    public Set<String> getHashField(String key) {
        try(Jedis jedis = pool.getResource()) {
            return jedis.hkeys(key);
        }
    }

    /**
     * 获取hash表
     * @param key key
     * @return 所有数据
     */
    public Map<String, String> getHashByKey(String key) {
        try(Jedis jedis = pool.getResource()){
            return jedis.hgetAll(key);
        }
    }


}
