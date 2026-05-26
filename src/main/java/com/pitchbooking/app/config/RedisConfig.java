package com.pitchbooking.app.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pitchbooking.app.domain.dto.PendingBookingData;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis configuration for non-test profiles.
 *
 * <p>Provides a typed {@code RedisTemplate<String, PendingBookingData>} that
 * serialises values as JSON (with JavaTime + polymorphic type info) so the
 * {@link com.pitchbooking.app.service.RedisPendingBookingCache} can round-trip
 * a {@code PendingBookingData} snapshot.
 */
@Configuration
@Profile("!test")
public class RedisConfig {

    @Bean
    public RedisTemplate<String, PendingBookingData> pendingBookingRedisTemplate(
            RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, PendingBookingData> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        ObjectMapper mapper = buildObjectMapper();
        Jackson2JsonRedisSerializer<PendingBookingData> serializer =
                new Jackson2JsonRedisSerializer<>(mapper, PendingBookingData.class);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

    private ObjectMapper buildObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // Polymorphic type info, scoped to the PendingBookingData package so we
        // don't accept arbitrary class names from Redis.
        BasicPolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubTypeIsArray()
                .allowIfSubType("com.pitchbooking.app.")
                .allowIfSubType("java.")
                .build();
        mapper.activateDefaultTyping(validator, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return mapper;
    }
}
