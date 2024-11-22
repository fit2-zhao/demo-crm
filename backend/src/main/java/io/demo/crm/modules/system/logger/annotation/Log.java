package io.demo.crm.modules.system.logger.annotation;

import io.demo.crm.modules.system.logger.constants.LogType;

import java.lang.annotation.*;

/**
 * 操作日志注解，用于记录操作日志的相关信息。
 * 该注解可以应用于类或方法级别，用于定义操作类型、表达式及执行类。
 *

 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作类型，默认为 SELECT。
     *
     * @return 操作类型
     */
    LogType type() default LogType.SELECT;

    /**
     * 操作表达式，用于描述操作内容。
     *
     * @return 操作的表达式
     */
    String expression();

    /**
     * 传入执行类，用于标识具体的执行上下文。
     * 默认为空数组。
     *
     * @return 执行类数组
     */
    Class<?>[] serviceClass() default {};
}
