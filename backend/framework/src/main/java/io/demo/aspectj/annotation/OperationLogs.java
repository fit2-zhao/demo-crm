package io.demo.aspectj.annotation;

import java.lang.annotation.*;

/**
 * @author wulang
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface OperationLogs {
    OperationLog[] value();
}
