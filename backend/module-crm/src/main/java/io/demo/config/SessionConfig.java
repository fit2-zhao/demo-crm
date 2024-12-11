package io.demo.config;

import com.fit2cloud.quartz.anno.QuartzScheduled;
import io.demo.security.SessionConstants;
import io.demo.common.util.JSON;
import io.demo.common.util.LogUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for managing session-related settings and cleanup operations.
 * <p>
 * This class is mainly used to configure the session ID resolver and clean up sessions that are not bound to users through scheduled tasks.
 * </p>
 *
 * @version 1.0
 */
@Configuration
public class SessionConfig {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedisIndexedSessionRepository redisIndexedSessionRepository;

    /**
     * Creates a {@link HeaderHttpSessionIdResolver} Bean.
     * <p>
     * This method configures the session ID resolution method, using the {@link SessionConstants#HEADER_TOKEN} field in the request header as the session ID.
     * </p>
     *
     * @return Configured {@link HeaderHttpSessionIdResolver} instance
     */
    @Bean
    public HeaderHttpSessionIdResolver sessionIdResolver() {
        return new HeaderHttpSessionIdResolver(SessionConstants.HEADER_TOKEN);
    }

    /**
     * Periodically cleans up sessions that are not bound to users.
     * <p>
     * This method is executed at 2 minutes past midnight every day, scanning session data in Redis and deleting sessions that are not bound to user information.
     * Additionally, it handles special cases such as manually setting the expiration time when Redisson sets it to -1.
     * </p>
     *
     * <p>
     * This method uses the {@link QuartzScheduled} annotation to execute periodically and uses {@link ScanOptions} to scan sessions in Redis.
     * </p>
     */
    @QuartzScheduled(cron = "0 2 0 * * ?")
    public void cleanSession() {
        Map<String, Long> userCount = new HashMap<>();
        ScanOptions options = ScanOptions.scanOptions().match("spring:session:sessions:*").count(1000).build();

        try (Cursor<String> scan = stringRedisTemplate.scan(options)) {
            while (scan.hasNext()) {
                String key = scan.next();
                if (key.contains("spring:session:sessions:expires:")) {
                    continue;
                }

                String sessionId = key.substring(key.lastIndexOf(":") + 1);
                Boolean exists = stringRedisTemplate.opsForHash().hasKey(key, "sessionAttr:user");

                // Delete sessions that are not bound to users
                if (!exists) {
                    redisIndexedSessionRepository.deleteById(sessionId);
                } else {
                    // Get user information and check session expiration time
                    Object user = redisIndexedSessionRepository.getSessionRedisOperations().opsForHash().get(key, "sessionAttr:user");
                    Long expire = redisIndexedSessionRepository.getSessionRedisOperations().getExpire(key);

                    assert user != null;
                    String userId = (String) MethodUtils.invokeMethod(user, "getId");
                    userCount.merge(userId, 1L, Long::sum);

                    // Log and check session expiration time
                    LogUtils.info("{} : {} expiration time: {}", key, userId, expire);

                    // If the expiration time is -1, manually set the expiration time to 30 seconds
                    if (expire != null && expire == -1) {
                        redisIndexedSessionRepository.getSessionRedisOperations().expire(key, Duration.of(30, ChronoUnit.SECONDS));
                    }
                }
            }
            LogUtils.info("User session statistics: {}", JSON.toJSONString(userCount));
        } catch (Exception e) {
            LogUtils.error(e);
        }
    }
}