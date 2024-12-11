package io.demo.common.response.handler;

import java.lang.annotation.*;

/**
 * Annotation to mark a method as a "no result required" method.
 * <p>
 * This annotation is used to mark methods that do not need to return any result or handle the return result.
 * It is commonly used in controller methods to indicate that no data needs to be returned to the client after the method is called.
 * </p>
 * <p>
 * Methods using this annotation can avoid unnecessary result processing by the framework or handling mechanism.
 * </p>
 *
 * @see java.lang.annotation.Annotation
 */
@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoResultHolder {
}