package io.demo.aspectj.annotation;

import java.lang.annotation.*;

@Repeatable(OperationLogs.class)
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface OperationLog {
    /**
     * @return Log template after the method executes successfully
     */
    String success();

    /**
     * @return Log template after the method execution fails
     */
    String fail() default "";

    /**
     * @return Operator of the log
     */
    String operator() default "";

    /**
     * @return Type of operation log, such as: add, modify, delete
     */
    String type();

    /**
     * @return Name of the business module
     */
    String module() default "";

    /**
     * @return Business identifier bound to the log
     */
    String resourceId();

    /**
     * @return Additional information of the log
     */
    String extra() default "";

}