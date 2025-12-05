package com.example.kwave.global.util;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * byte[] 그대로 Redis에 저장하도록
 * 가져올 때도 마찬가지로 그대로 가져오도록
 */
public class ByteArrayRedisSerializer implements RedisSerializer<byte[]> {

    @Override
    public byte[] serialize(byte[] bytes) throws SerializationException {
        return bytes;
    }

    @Override
    public byte[] deserialize(byte[] bytes) throws SerializationException {
        return bytes;
    }
}
