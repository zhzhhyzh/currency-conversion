package com.currency.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    @Test
    @DisplayName("Should build Redis configuration from Render connection string")
    void shouldBuildRedisConfigurationFromUrl() {
        RedisConfig redisConfig = createRedisConfig();
        ReflectionTestUtils.setField(redisConfig, "redisUrl", "redis://default:secret@render-redis.internal:6380/2");

        RedisStandaloneConfiguration configuration = ReflectionTestUtils.invokeMethod(
            redisConfig,
            "buildRedisConfiguration"
        );

        assertNotNull(configuration);
        assertEquals("render-redis.internal", configuration.getHostName());
        assertEquals(6380, configuration.getPort());
        assertEquals(2, configuration.getDatabase());
        assertTrue(configuration.getPassword().isPresent());
    }

    @Test
    @DisplayName("Should use host and port properties when Redis URL is missing")
    void shouldBuildRedisConfigurationFromHostAndPort() {
        RedisConfig redisConfig = createRedisConfig();

        RedisStandaloneConfiguration configuration = ReflectionTestUtils.invokeMethod(
            redisConfig,
            "buildRedisConfiguration"
        );

        assertNotNull(configuration);
        assertEquals("localhost", configuration.getHostName());
        assertEquals(6379, configuration.getPort());
        assertEquals(0, configuration.getDatabase());
    }

    @Test
    @DisplayName("Should fall back to host and port when Redis URL is invalid")
    void shouldFallbackWhenRedisUrlIsInvalid() {
        RedisConfig redisConfig = createRedisConfig();
        ReflectionTestUtils.setField(redisConfig, "redisUrl", "://invalid-url");

        RedisStandaloneConfiguration configuration = ReflectionTestUtils.invokeMethod(
            redisConfig,
            "buildRedisConfiguration"
        );

        assertNotNull(configuration);
        assertEquals("localhost", configuration.getHostName());
        assertEquals(6379, configuration.getPort());
    }

    @Test
    @DisplayName("Should create Redis template")
    void shouldCreateRedisTemplate() {
        RedisConfig redisConfig = createRedisConfig();
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);

        RedisTemplate<String, Object> template = redisConfig.redisTemplate(connectionFactory);

        assertNotNull(template);
    }

    private RedisConfig createRedisConfig() {
        RedisConfig redisConfig = new RedisConfig();
        ReflectionTestUtils.setField(redisConfig, "redisUrl", "");
        ReflectionTestUtils.setField(redisConfig, "redisHost", "localhost");
        ReflectionTestUtils.setField(redisConfig, "redisPort", 6379);
        ReflectionTestUtils.setField(redisConfig, "redisPassword", "");
        ReflectionTestUtils.setField(redisConfig, "redisDatabase", 0);
        return redisConfig;
    }
}
