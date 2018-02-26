package com.yscredit.pgp.gateway.util;

import com.yscredit.pgp.gateway.exception.BizException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by 子华 on 2017/5/23.
 * 使用 jedis 操作缓存
 */
public class JedisUtils {

    private static transient volatile boolean isInit = false;

    //连接池
    private static transient volatile JedisPool pool;

    public static final int SECOND = 1000;//一秒

    public static final int MINUTE = 60*SECOND;//一分钟

    private static final Logger logger = LoggerFactory.getLogger(JedisUtils.class);
    /**
     *
     */
    public JedisUtils(JedisPoolConfig config,String host,Integer port){
        init(config,host,port);
    }

    /**
     *  初始化jedis连接池
     * @param config 不能为空
     * @param host 如果为空，设置成 127.0.0.1
     * @param port 如果为空，设置成 6379
     */
    public static synchronized void init(JedisPoolConfig config,String host,Integer port){
        if(!isInit){//没有被初始化过，如果已被初始化过，就不会再初始化
            if(null == config)
                throw new BizException("03","连接池配置不能为空");
            if(StringUtils.isEmpty(host))host = "127.0.0.1";
            if(null == port) port = 6379;
            pool = new JedisPool(config,host,port);
            isInit = true;
        }
    }

    /**
     * 设置是否可以重新初始化数据
     * @param init
     */
    public static void setInit(boolean init){
        isInit = init;
    }

    /**
     * 回收Jedis对象资源
     * @param jedis
     */
    public static void returnResource(Jedis jedis){
        if(jedis != null){
            pool.returnResource(jedis);
        }
    }

    /**
     * Jedis对象出异常的时候，回收Jedis对象资源
     * @param jedis
     */
    public static void returnBrokenResource(Jedis jedis){
        if(jedis != null){
            pool.returnBrokenResource(jedis);
        }

    }

    /**
     *关闭Jedis资源
     * @param jedis
     */
    public static void close(Jedis jedis){
        if(null!=jedis)
            jedis.close();
    }

    /**
     * 获取Jedis资源
     * @return
     */
    public static Jedis getResource(){
        isInit();
        return pool.getResource();
    }

    /**
     * 获取Jedis资源池
     * @return
     */
    public static JedisPool getPool(){
        return pool;
    }

    /**
     * 设置缓存
     * @param key
     * @param value
     * @return
     */
    public static String set(String key,String value){
        return set(key,value,-1);
    }

    /**
     * 设置缓存被设置过期
     * @param key
     * @param field
     * @param value
     * @return
     */
    public static Long hset(String key,String field,String value){
        return hset(key,field,value,-1);
    }

    /**
     * 同时将多个 field-value (域-值)对设置到哈希表 key 中。 此命令会覆盖哈希表中已存在的域。如果 key
     * 不存在，一个空哈希表被创建并执行 HMSET 操作。同时设置过期时间
     * @param key
     * @param hash
     * @param seconds
     * @return
     */
    public static String hmset(String key, Map<String, String> hash, int seconds){
        String result = null;
        Jedis jedis = getResource();
        try {
            result = jedis.hmset(key, hash);
            if(seconds > 0){
                jedis.expire(key, seconds);
            }
        } catch (Exception e) {
            logger.warn("设置缓存失败:key={},value={},timeout={}",key,hash,seconds+"");
        }finally {
            returnResource(jedis);
        }
        return result;
    }


    /**
     * 设置值
     * @param key
     * @param field
     * @param value
     * @param seconds
     * @return
     */
    public static Long hset(String key,String field,String value,int seconds){
        Jedis jedis = null;
        Long v = null;
        try{
            jedis = getResource();
            v = jedis.hset(key,field,value);
            if(seconds>0)
                jedis.expire(key,seconds);
        }catch (Exception e){
            logger.warn("设置缓存失败:key={},value={},timeout={}",key,value,seconds+"");
        }finally {
            returnResource(jedis);
        }
        return v;
    }

    /**
     * 设置某个key的
     * @param key
     * @return
     */
    public static Long setTimeOut(String key){
        Jedis jedis = null;
        Long v = null;
        try {
            jedis = getResource();
            v = jedis.expire(key, 0);
        } catch (Exception e) {
            logger.warn("设置缓存失效失败：key={}", key);
        } finally {
            returnResource(jedis);
        }
        return v;
    }

    /**
     * 设置值并设置过期时间
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public static String set(String key,String value,int seconds){
        Jedis jedis = null;
        String v = null;
        try{
            jedis = getResource();
            v= jedis.set(key,value);
            if(seconds>0)
                jedis.expire(key,seconds);
        }catch (Exception e){
            logger.warn("设置缓存失败:key={},value={},timeout={}",key,value,seconds+"");
        }finally {
            returnResource(jedis);
        }
        return v;
    }

    /**
     * 获取值
     * @param key
     * @return
     */
    public static String get(String key){
        Jedis jedis = null;
        String v = null;
        try{
            jedis = getResource();
            v= jedis.get(key);
        }catch (Exception e){
            logger.warn("获取缓存失败:key={},",key);
        }finally {
            returnResource(jedis);
        }
        return v;
    }

    /**
     * 获取值
     * @param key
     * @param field
     * @return
     */
    public static String hget(String key,String field){
        Jedis jedis = null;
        String v = null;
        try{
            jedis = getResource();
            v= jedis.hget(key,field);
        }catch (Exception e){
            logger.warn("获取缓存失败:key={},field = {}",key,field);
        }finally {
            returnResource(jedis);
        }
        return v;
    }

    /**
     * 获取指定字段的值
     * @param key
     * @param fields
     * @return
     */
    public static List<String> hmget(String key,String... fields){
        Jedis jedis = null;
        List<String> v = null;
        try{
            jedis = getResource();
            v= jedis.hmget(key,fields);
        }catch (Exception e){
            logger.warn("获取缓存失败:key={},",key);
        }finally {
            returnResource(jedis);
        }
        return v;
    }

    /**
     * 获取key对应所有字段的值
     * @param key
     * @return
     */
    public static Set<String> hkeys(String key){
        Jedis jedis = null;
        Set<String> v = null;
        try{
            jedis = getResource();
            v= jedis.hkeys(key);
        }catch (Exception e){
            logger.warn("获取缓存失败:key={},",key);
        }finally {
            returnResource(jedis);
        }
        return v;
    }

    /**
     * 获取缓存数据并转出成对象
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getJsonToObj(String key,Class<T> clazz){
        String json = get(key);
        if(null!=json) {
            return JsonUtil.fromString(json, clazz);
        } else {
            return null;
        }
    }

    /**
     *获取缓存数据并转出成对象
     * @param key
     * @param field
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getJsonToObj(String key,String field,Class<T> clazz){
        String json = hget(key,field);
        if(null!=json)
            return JsonUtil.fromString(json,clazz);
        else
            return null;
    }

    /**
     *获取缓存中存放的List json
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> getJsonToListObj(String key,Class<T> clazz){
        String json = get(key);
        if(null!=json && json.length()>0)
            return JsonUtil.toList(json,clazz);
        else
            return null;
    }

    /**
     * 获取缓存中存放的List json
     * @param key
     * @param field
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> getJsonToListObj(String key,String field,Class<T> clazz){
        String json = hget(key,field);
        if(null!=json)
            return JsonUtil.toList(json,clazz);
        else
            return null;
    }


    /**
     * 递增数据
     * @param key
     * @param incr
     * @return
     */
    public static Long incrBy(String key,long incr){
        Jedis jedis = null;
        Long v = null;
        try{
            jedis = getResource();
            v= jedis.incrBy(key,incr);
        }catch (Exception e){
            logger.warn("递增失败:key={},incr = {}",key,incr);
        }finally {
            returnResource(jedis);
        }
        return v;
    }

    /**
     * 递增数据
     * @param key
     * @param field
     * @param incr
     * @return
     */
    public static Long hincrBy(String key,String field,long incr){
        Jedis jedis = null;
        Long v = null;
        try{
            jedis = getResource();
            v= jedis.hincrBy(key,field,incr);
        }catch (Exception e){
            logger.warn("递增失败:key={},incr = {}",key,incr);
        }finally {
            returnResource(jedis);
        }
        return v;
    }

    /**
     * 删除缓存KEY
     * @param keys
     * @return
     */
    public static Long del(String... keys){
        Jedis jedis = null;
        Long v = null;
        try{
            jedis = getResource();
            v= jedis.del(keys);
        }catch (Exception e){
            logger.warn("删除缓存失败:key={},",keys);
        }finally {
            returnResource(jedis);
        }
        return v;
    }

    public static Long hdel(String key,String... fields){
        Jedis jedis = null;
        Long v = null;
        try{
            jedis = getResource();
            v= jedis.hdel(key,fields);
        }catch (Exception e){
            logger.warn("删除缓存失败:key={},fields = {}",key,fields);
        }finally {
            returnResource(jedis);
        }
        return v;
    }


    public static  Set<String> keys(String pattern){
        Jedis jedis = null;
        Set<String> v = null;
        try{
            jedis = getResource();
            v= jedis.keys(pattern);
        }catch (Exception e){
            logger.warn("获取KEY失败:pattern={}",pattern);
        }finally {
            returnResource(jedis);
        }
        return v;
    }


    public static void isInit(){
        if(!isInit){
            logger.warn("缓存未初始化");
            throw  new BizException("03","缓存未初始化");
        }
    }

    public static String _type(String key){
        Jedis jedis = null;
        try{
            jedis = getResource();
            return jedis.type(key);
        }catch (Exception e){
            logger.warn("获取KEY类型失败，KEY = {}",key);
        }finally {
            returnResource(jedis);
        }
        return KEYTYPE.NONE;
    }


    public interface KEYTYPE{
        String NONE = "none";
        String STRING ="string";
        String LIST ="list";
        String SET ="set";
        String ZSET="zset";
        String HASH ="hash";
    }
}
