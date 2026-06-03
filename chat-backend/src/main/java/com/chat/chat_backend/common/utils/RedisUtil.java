package com.chat.chat_backend.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis工具类，提供String、Set、Hash数据结构的常用操作及过期管理
 * @author chat-backend
 * @since 2026-05-12
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    /** Redis模板，用于执行键值操作 */
    private final RedisTemplate<String, Object> redisTemplate;

    // ========== String 操作 ==========

    /**
     * 设置键值对（无过期时间）
     * @param key Redis键
     * @param value 存储的值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置键值对并指定过期时间
     * @param key Redis键
     * @param value 存储的值
     * @param timeout 过期时长
     * @param unit 时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 根据键获取值
     * @param key Redis键
     * @return 存储的值，不存在时返回null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定键
     * @param key 待删除的Redis键
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    // ========== Set 操作（如在线用户集合） ==========

    /**
     * 向Set中添加一个或多个值
     * @param key Redis键
     * @param values 待添加的值
     */
    public void addToSet(String key, Object... values) {
        redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 从Set中移除一个或多个值
     * @param key Redis键
     * @param values 待移除的值
     */
    public void removeFromSet(String key, Object... values) {
        redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 检查某个值是否是Set的成员
     * @param key Redis键
     * @param value 待检查的值
     * @return 如果值存在于集合中返回true
     */
    public Boolean isMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 返回Set中的所有成员
     * @param key Redis键
     * @return 成员集合
     */
    public Set<Object> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    // ========== Hash 操作（如未读计数器） ==========

    /**
     * 向Hash中存入字段-值对
     * @param key Redis键
     * @param hashKey Hash字段键
     * @param value 存储的值
     */
    public void hashPut(String key, Object hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 获取指定Hash字段的值
     * @param key Redis键
     * @param hashKey Hash字段键
     * @return 字段值，不存在时返回null
     */
    public Object hashGet(String key, Object hashKey) {
        return redisTemplate.opsForHash().get(key, hashKey);
    }

    /**
     * 对Hash中的某个字段执行增量操作
     * @param key Redis键
     * @param hashKey Hash字段键
     * @param delta 增量值
     * @return 增加后的新值
     */
    public Long hashIncrement(String key, Object hashKey, long delta) {
        return redisTemplate.opsForHash().increment(key, hashKey, delta);
    }

    /**
     * 删除Hash中的一个或多个字段
     * @param key Redis键
     * @param hashKeys 待删除的Hash字段键
     */
    public void hashDelete(String key, Object... hashKeys) {
        redisTemplate.opsForHash().delete(key, hashKeys);
    }

    // ========== 过期操作 ==========

    /**
     * 设置键的存活时间（TTL）
     * @param key Redis键
     * @param timeout 过期时长
     * @param unit 时间单位
     * @return 是否设置成功
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 检查键是否存在
     * @param key Redis键
     * @return 键是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}