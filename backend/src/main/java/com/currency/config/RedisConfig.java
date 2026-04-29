package com.currency.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.url:}")
    private String redisUrl;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = buildRedisConfiguration();
        log.info(
            "Initializing Redis connection factory for {}:{}",
            configuration.getHostName(),
            configuration.getPort()
        );
        return new JedisConnectionFactory(configuration);
    }

    private RedisStandaloneConfiguration buildRedisConfiguration() {
        if (redisUrl != null && !redisUrl.trim().isEmpty()) {
            return buildRedisConfigurationFromUrl(redisUrl.trim());
        }

        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        configuration.setDatabase(redisDatabase);
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            configuration.setPassword(redisPassword);
        }
        return configuration;
    }

    private RedisStandaloneConfiguration buildRedisConfigurationFromUrl(String url) {
        try {
            URI uri = URI.create(url);
            int port = uri.getPort() > 0 ? uri.getPort() : 6379;
            int database = parseDatabase(uri.getPath());
            RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(uri.getHost(), port);
            configuration.setDatabase(database);

            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.trim().isEmpty()) {
                String[] credentials = userInfo.split(":", 2);
                if (credentials.length == 2 && !credentials[1].isEmpty()) {
                    configuration.setPassword(credentials[1]);
                } else if (credentials.length == 1 && !credentials[0].isEmpty()) {
                    configuration.setPassword(credentials[0]);
                }
            }

            return configuration;
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid Redis URL configured. Falling back to host and port properties.", ex);
            return buildFallbackRedisConfiguration();
        }
    }

    private RedisStandaloneConfiguration buildFallbackRedisConfiguration() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(redisHost, redisPort);
        configuration.setDatabase(redisDatabase);
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            configuration.setPassword(redisPassword);
        }
        return configuration;
    }

    private int parseDatabase(String path) {
        if (path == null || path.trim().isEmpty() || "/".equals(path)) {
            return redisDatabase;
        }

        try {
            return Integer.parseInt(path.substring(1));
        } catch (NumberFormatException ex) {
            log.warn("Invalid Redis database path '{}'. Falling back to database {}.", path, redisDatabase);
            return redisDatabase;
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        log.info("Configuring Redis template");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Create Jackson serializer
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = 
            new Jackson2JsonRedisSerializer<>(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // Set key-value serialization
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);

        // Set hash key-value serialization
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        log.info("Redis template configured successfully");
        return template;
    }
}
