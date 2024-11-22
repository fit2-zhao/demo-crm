package io.demo.crm.modules.system.logger.aspect;

import io.demo.crm.common.util.JSON;
import io.demo.crm.common.util.LogUtils;
import io.demo.crm.modules.system.logger.annotation.Log;
import io.demo.crm.modules.system.logger.constants.LogType;
import io.demo.crm.modules.system.logger.dto.LogDTO;
import io.demo.crm.modules.system.logger.service.LogService;
import io.demo.crm.common.util.SessionUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.core.StandardReflectionParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统操作日志切面处理类。
 * 用于拦截带有 @Log 注解的方法，并根据不同的操作类型（如 ADD, UPDATE, DELETE 等）记录操作日志。
 */
@Aspect
@Component
public class LogAspect {

    private static final String ID = "id";
    private final ExpressionParser parser = new SpelExpressionParser();
    private final StandardReflectionParameterNameDiscoverer discoverer = new StandardReflectionParameterNameDiscoverer();

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private LogService logService;

    // 保存方法执行前的日志数据
    private final ThreadLocal<List<LogDTO>> beforeValues = new ThreadLocal<>();

    // 保存当前用户信息
    private final ThreadLocal<String> localUser = new ThreadLocal<>();

    // 需要在操作前执行的日志类型
    private final LogType[] beforeMethodNames = {
            LogType.UPDATE, LogType.DELETE, LogType.COPY
    };

    // 需要在操作后执行并合并日志的类型
    private final LogType[] postMethodNames = {
            LogType.ADD, LogType.UPDATE
    };

    /**
     * 定义切点，拦截带有 @Log 注解的方法
     */
    @Pointcut("@annotation(io.demo.crm.modules.system.logger.annotation.Log)")
    public void logPointCut() {
    }

    /**
     * 异常处理，记录异常信息
     *
     * @param ex 异常信息
     */
    @AfterThrowing(pointcut = "logPointCut()", throwing = "ex")
    public void handleException(Exception ex) {
        localUser.remove();
        beforeValues.remove();
        LogUtils.error(ex);
    }

    /**
     * 执行方法前的处理，记录操作日志的前置内容
     *
     * @param joinPoint 连接点对象，包含方法和参数信息
     */
    @Before("logPointCut()")
    public void before(JoinPoint joinPoint) {
        try {
            localUser.set(SessionUtils.getUserId());

            // 获取当前执行的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log msLog = method.getAnnotation(Log.class);
            if (msLog != null && isMatch(msLog.type())) {
                // 获取方法参数和变量，并将其纳入 Spring 管理
                Object[] args = joinPoint.getArgs();
                String[] params = discoverer.getParameterNames(method);
                StandardEvaluationContext context = new StandardEvaluationContext();
                for (int i = 0; i < Objects.requireNonNull(params).length; i++) {
                    context.setVariable(params[i], args[i]);
                }

                // 将执行类传入上下文
                boolean isNext = false;
                for (Class<?> clazz : msLog.serviceClass()) {
                    context.setVariable("serviceClass", applicationContext.getBean(clazz));
                    isNext = true;
                }
                if (!isNext) {
                    return;
                }

                // 初始化前置日志详情
                initBeforeDetails(msLog, context);
            }
        } catch (Exception e) {
            LogUtils.error("操作日志写入异常：" + joinPoint.getSignature());
        }
    }

    /**
     * 判断当前操作是否与指定的日志类型匹配
     *
     * @param keyword 操作类型
     * @return 是否匹配
     */
    public boolean isMatch(LogType keyword) {
        return Arrays.stream(beforeMethodNames)
                .anyMatch(input -> input.contains(keyword));
    }

    /**
     * 初始化方法执行前的日志内容
     *
     * @param msLog   日志注解
     * @param context 表达式上下文
     */
    private void initBeforeDetails(Log msLog, StandardEvaluationContext context) {
        try {
            // 执行表达式并获取日志内容
            Object obj = parser.parseExpression(msLog.expression()).getValue(context);
            if (obj == null) return;

            List<LogDTO> logDTOList = (obj instanceof List<?>) ? (List<LogDTO>) obj : Collections.singletonList((LogDTO) obj);
            beforeValues.set(logDTOList);

        } catch (Exception e) {
            LogUtils.error("未获取到details内容", e);
        }
    }

    /**
     * 合并前后日志内容
     *
     * @param beforeLogs 前置日志
     * @param postLogs   后置日志
     */
    public void mergeLists(List<LogDTO> beforeLogs, List<LogDTO> postLogs) {
        if (CollectionUtils.isEmpty(beforeLogs) && CollectionUtils.isNotEmpty(postLogs)) {
            beforeValues.set(postLogs);
            return;
        }
        if (CollectionUtils.isEmpty(beforeLogs)) return;

        Map<String, LogDTO> postDto = postLogs.stream().collect(Collectors.toMap(LogDTO::getSourceId, item -> item));
        beforeLogs.forEach(item -> {
            LogDTO post = postDto.get(item.getSourceId());
            if (post != null) {
                item.setModifiedValue(post.getOriginalValue());
            }
        });
    }

    /**
     * 初始化方法执行后的日志内容
     *
     * @param msLog   日志注解
     * @param context 表达式上下文
     */
    private void initPostDetails(Log msLog, StandardEvaluationContext context) {
        try {
            if (StringUtils.isBlank(msLog.expression())) return;

            Object obj = parser.parseExpression(msLog.expression()).getValue(context);
            if (obj == null) return;

            if (obj instanceof List<?>) {
                mergeLists(beforeValues.get(), (List<LogDTO>) obj);
            } else {
                LogDTO log = (LogDTO) obj;
                if (CollectionUtils.isNotEmpty(beforeValues.get())) {
                    beforeValues.get().get(0).setModifiedValue(log.getOriginalValue());
                } else {
                    beforeValues.set(Collections.singletonList(log));
                }
            }
        } catch (Exception e) {
            LogUtils.error("未获取到details内容", e);
        }
    }

    /**
     * 从结果中获取资源 ID
     *
     * @param result 结果对象
     * @return 资源 ID
     */
    public String getId(Object result) {
        try {
            if (result != null) {
                String resultStr = JSON.toJSONString(result);
                Map<String, Object> object = JSON.parseMap(resultStr);
                if (MapUtils.isNotEmpty(object) && object.containsKey(ID)) {
                    Object id = object.get(ID);
                    if (ObjectUtils.isNotEmpty(id)) {
                        return id.toString();
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.error("未获取到响应资源Id");
        }
        return null;
    }

    /**
     * 保存操作日志
     *
     * @param result 操作结果
     */
    private void save(Object result) {
        List<LogDTO> logDTOList = beforeValues.get();
        if (CollectionUtils.isEmpty(logDTOList)) return;

        logDTOList.forEach(logDTO -> {
            logDTO.setSourceId(StringUtils.defaultIfBlank(logDTO.getSourceId(), getId(result)));
            logDTO.setCreateUser(StringUtils.defaultIfBlank(logDTO.getCreateUser(), localUser.get()));
            logDTO.setMethod(getMethod());
            logDTO.setPath(getPath());
        });

        // 根据日志数量决定是单条保存还是批量保存
        if (logDTOList.size() == 1) {
            logService.add(logDTOList.getFirst());
        } else {
            logService.batchAdd(logDTOList);
        }
    }

    /**
     * 切面后置通知，保存日志
     *
     * @param joinPoint 连接点对象
     * @param result    方法返回值
     */
    @AfterReturning(value = "logPointCut()", returning = "result")
    public void saveLog(JoinPoint joinPoint, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            Log msLog = method.getAnnotation(Log.class);
            if (msLog != null) {
                Object[] args = joinPoint.getArgs();
                String[] params = discoverer.getParameterNames(method);
                StandardEvaluationContext context = new StandardEvaluationContext();
                for (int i = 0; i < Objects.requireNonNull(params).length; i++) {
                    context.setVariable(params[i], args[i]);
                }

                boolean isNext = false;
                for (Class<?> clazz : msLog.serviceClass()) {
                    context.setVariable("serviceClass", applicationContext.getBean(clazz));
                    isNext = true;
                }
                if (!isNext) {
                    return;
                }

                // 执行操作日志内容更新
                initPostDetails(msLog, context);
                save(result);
            }
        } catch (Exception e) {
            LogUtils.error("操作日志写入异常：" + joinPoint.getSignature());
        }
    }

    /**
     * 获取请求路径
     *
     * @return 请求路径
     */
    private String getPath() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
            return request.getRequestURI();
        }
        return null;
    }

    /**
     * 获取方法名称
     *
     * @return 方法名称
     */
    private String getMethod() {
        return Thread.currentThread().getStackTrace()[2].getMethodName();
    }
}
