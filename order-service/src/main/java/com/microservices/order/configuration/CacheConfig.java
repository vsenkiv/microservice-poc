package com.microservices.order.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

  @Bean
  public ObjectMapper redisObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    // ✅ Configure type information to avoid LinkedHashMap issue
    mapper.activateDefaultTyping(
        LaissezFaireSubTypeValidator.instance,
        ObjectMapper.DefaultTyping.NON_FINAL,
        JsonTypeInfo.As.PROPERTY);

    mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
    mapper.registerModule(new JavaTimeModule());

    log.info("✅ Redis ObjectMapper configured with type information");
    return mapper;
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);

    // Use the configured ObjectMapper
    GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(
        redisObjectMapper());

    template.setKeySerializer(new StringRedisSerializer());
    template.setHashKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(serializer);
    template.setHashValueSerializer(serializer);

    template.setDefaultSerializer(serializer);
    template.afterPropertiesSet();

    log.info("✅ Redis template configured successfully with type-aware serialization");
    return template;
  }

  @Bean
  public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
    return (builder) -> {
      // Use the same ObjectMapper for cache serialization
      GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(
          redisObjectMapper());

      // Default cache configuration
      RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
          .entryTtl(Duration.ofMinutes(10))
          .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
              new StringRedisSerializer()))
          .serializeValuesWith(
              RedisSerializationContext.SerializationPair.fromSerializer(serializer))
          .disableCachingNullValues();

      // Specific cache configurations
      builder
          .cacheDefaults(defaultConfig)
          .withCacheConfiguration("orders",
              RedisCacheConfiguration.defaultCacheConfig()
                  .entryTtl(Duration.ofMinutes(15))  // Orders cached for 15 minutes
                  .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                      new StringRedisSerializer()))
                  .serializeValuesWith(
                      RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                  .prefixCacheNameWith("order-service:")
                  .disableCachingNullValues())
          .withCacheConfiguration("users",
              RedisCacheConfiguration.defaultCacheConfig()
                  .entryTtl(Duration.ofMinutes(30))  // Users cached for 30 minutes
                  .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                      new StringRedisSerializer()))
                  .serializeValuesWith(
                      RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                  .prefixCacheNameWith("order-service:")
                  .disableCachingNullValues());

      log.info("✅ Redis cache manager configured with type-aware serialization");
    };
  }
}
