package io.demo.aspectj.support.aop;

import io.demo.aspectj.builder.LogRecordBuilder;
import io.demo.aspectj.annotation.LogRecord;
import io.demo.aspectj.annotation.LogRecords;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 该类负责解析和计算基于注解的日志记录操作。
 */
public class LogRecordOperationSource {

    /**
     * 缓存方法与其对应接口方法的映射关系。
     */
    private static final Map<Method, Method> INTERFACE_METHOD_CACHE = new ConcurrentReferenceHashMap<>(256);

    /**
     * 根据指定的方法和目标类计算日志记录操作。
     *
     * @param method      要分析的方法
     * @param targetClass 目标类
     * @return 日志记录构建器的集合
     */
    public Collection<LogRecordBuilder> computeLogRecordOperations(Method method, Class<?> targetClass) {
        if (!Modifier.isPublic(method.getModifiers())) {
            return Collections.emptyList();
        }

        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

        Collection<LogRecordBuilder> logRecordOps = parseLogRecordAnnotations(specificMethod);
        Collection<LogRecordBuilder> logRecordsOps = parseLogRecordsAnnotations(specificMethod);
        Collection<LogRecordBuilder> abstractLogRecordOps = parseLogRecordAnnotations(getInterfaceMethodIfPossible(method));
        Collection<LogRecordBuilder> abstractLogRecordsOps = parseLogRecordsAnnotations(getInterfaceMethodIfPossible(method));

        Set<LogRecordBuilder> result = new HashSet<>();
        result.addAll(logRecordOps);
        result.addAll(abstractLogRecordOps);
        result.addAll(logRecordsOps);
        result.addAll(abstractLogRecordsOps);
        return result;
    }

    /**
     * 确定给定方法对应的接口方法（如果可能）。
     *
     * @param method 要处理的方法
     * @return 对应的接口方法，如果不存在则返回原方法
     */
    public static Method getInterfaceMethodIfPossible(Method method) {
        if (!Modifier.isPublic(method.getModifiers()) || method.getDeclaringClass().isInterface()) {
            return method;
        }
        return INTERFACE_METHOD_CACHE.computeIfAbsent(method, key -> {
            Class<?> current = key.getDeclaringClass();
            while (current != null && current != Object.class) {
                for (Class<?> ifc : current.getInterfaces()) {
                    try {
                        return ifc.getMethod(key.getName(), key.getParameterTypes());
                    } catch (NoSuchMethodException ex) {
                        // 忽略异常
                    }
                }
                current = current.getSuperclass();
            }
            return key;
        });
    }

    /**
     * 解析方法上的 {@code LogRecords} 注解。
     *
     * @param ae 被注解的元素
     * @return 日志记录构建器的集合
     */
    private Collection<LogRecordBuilder> parseLogRecordsAnnotations(AnnotatedElement ae) {
        return AnnotatedElementUtils.findAllMergedAnnotations(ae, LogRecords.class).stream()
                .flatMap(logRecords -> Arrays.stream(logRecords.value())
                        .map(logRecord -> parseLogRecordAnnotation(ae, logRecord)))
                .collect(Collectors.toList());
    }

    /**
     * 解析方法上的 {@code LogRecord} 注解。
     *
     * @param ae 被注解的元素
     * @return 日志记录构建器的集合
     */
    private Collection<LogRecordBuilder> parseLogRecordAnnotations(AnnotatedElement ae) {
        return AnnotatedElementUtils.findAllMergedAnnotations(ae, LogRecord.class).stream()
                .map(recordAnnotation -> parseLogRecordAnnotation(ae, recordAnnotation))
                .collect(Collectors.toList());
    }

    /**
     * 将 {@code LogRecord} 注解转换为日志记录构建器。
     *
     * @param ae              被注解的元素
     * @param recordAnnotation {@code LogRecord} 注解
     * @return 日志记录构建器
     */
    private LogRecordBuilder parseLogRecordAnnotation(AnnotatedElement ae, LogRecord recordAnnotation) {
        LogRecordBuilder recordOps = LogRecordBuilder.builder()
                .successLogTemplate(recordAnnotation.success())
                .failLogTemplate(recordAnnotation.fail())
                .type(recordAnnotation.type())
                .bizNo(recordAnnotation.bizNo())
                .operatorId(recordAnnotation.operator())
                .subType(recordAnnotation.subType())
                .extra(recordAnnotation.extra())
                .condition(recordAnnotation.condition())
                .isSuccess(recordAnnotation.successCondition())
                .build();
        validateLogRecordOperation(ae, recordOps);
        return recordOps;
    }

    /**
     * 验证日志记录操作配置是否合法。
     *
     * @param ae        被注解的元素
     * @param recordOps 日志记录构建器
     * @throws IllegalStateException 如果配置不合法
     */
    private void validateLogRecordOperation(AnnotatedElement ae, LogRecordBuilder recordOps) {
        if (!StringUtils.hasText(recordOps.getSuccessLogTemplate()) && !StringUtils.hasText(recordOps.getFailLogTemplate())) {
            throw new IllegalStateException("无效的日志记录注解配置: '" +
                    ae.toString() + "'. 必须设置 'successTemplate' 或 'failLogTemplate' 属性。");
        }
    }
}