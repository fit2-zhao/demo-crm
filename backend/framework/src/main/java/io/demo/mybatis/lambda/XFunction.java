package io.demo.mybatis.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * A serializable Function.
 */
@FunctionalInterface
public interface XFunction<T, R> extends Function<T, R>, Serializable {
}