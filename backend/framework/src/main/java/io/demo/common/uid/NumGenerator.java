package io.demo.common.uid;

import io.demo.common.constants.ApplicationNumScope;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RIdGenerator;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Generator class for generating numeric IDs, supporting unique ID generation based on different application scopes.
 *
 * <p>This class uses Redisson's distributed ID generator and Redis for management.</p>
 */
@Component
public class NumGenerator {

    // Initial value, representing the starting point for ID generation from 100001
    private static final long INIT = 100001L;

    // Limit for the maximum number of IDs generated at a time
    private static final long LIMIT = 1;

    // Redisson instance for obtaining the distributed ID generator
    private static Redisson redisson;

    // StringRedisTemplate for operating Redis
    private static StringRedisTemplate stringRedisTemplate;

    // Specific sub-scope, representing secondary use cases
    private static final List<ApplicationNumScope> SUB_NUM = List.of(ApplicationNumScope.SYSTEM);

    /**
     * Generates a unique numeric ID based on the specified application scope.
     *
     * @param scope Application scope (e.g., interface use case)
     * @return Unique numeric ID
     */
    public static long nextNum(ApplicationNumScope scope) {
        return nextNum(scope.name(), scope);
    }

    /**
     * Generates a unique numeric ID based on the specified prefix and application scope.
     *
     * @param prefix Prefix, e.g., PROJECT_ID or PROJECT_ID + "_" + DOMAIN
     * @param scope  Application scope (e.g., interface use case)
     * @return Unique numeric ID
     */
    public static long nextNum(String prefix, ApplicationNumScope scope) {
        // Obtain the distributed ID generator
        RIdGenerator idGenerator = redisson.getIdGenerator(prefix + "_" + scope.name());

        // Handle sub-scope use cases (e.g., SYSTEM)
        if (SUB_NUM.contains(scope)) {
            // Ensure the ID generator exists, initialize if not
            if (!idGenerator.isExists()) {
                idGenerator.tryInit(1, LIMIT);
            }
            // Return formatted ID, keeping 3 digits
            return Long.parseLong(prefix.split("_")[1] + StringUtils.leftPad(String.valueOf(idGenerator.nextId()), 3, "0"));
        } else {
            // Other scope use cases, initialize the ID generator
            if (!idGenerator.isExists()) {
                idGenerator.tryInit(INIT, LIMIT);
            }
            return idGenerator.nextId();
        }
    }

    /**
     * Sets the Redisson instance for the distributed ID generator.
     *
     * @param redisson Redisson instance
     */
    @Resource
    public void setRedisson(Redisson redisson) {
        NumGenerator.redisson = redisson;
    }

    /**
     * Sets the StringRedisTemplate instance for operating Redis.
     *
     * @param stringRedisTemplate StringRedisTemplate instance
     */
    @Resource
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        NumGenerator.stringRedisTemplate = stringRedisTemplate;
    }
}