package io.demo.common.response.handler;

import io.demo.common.util.JSON;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * <p>Unified response body enhancement class for handling return result sets.</p>
 * <p>This class is used to uniformly wrap the returned results before returning the response,
 * ensuring that all responses follow a unified format. If the return value is null, it is automatically
 * wrapped as a successful response.</p>
 *
 * <p>Supported message converter types are: MappingJackson2HttpMessageConverter and StringHttpMessageConverter.</p>
 */
@RestControllerAdvice(value = {"io.demo.crm"})
public class ResultResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    /**
     * Determines whether the current handler supports the given converter.
     *
     * @param methodParameter Current request method parameter
     * @param converterType   Converter type
     * @return true if supported, otherwise false
     */
    @Override
    public boolean supports(@NotNull MethodParameter methodParameter,
                            @NotNull Class<? extends HttpMessageConverter<?>> converterType) {
        return MappingJackson2HttpMessageConverter.class.isAssignableFrom(converterType) ||
                StringHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    /**
     * Processes the response result before writing the response body.
     *
     * @param body               Response body content
     * @param methodParameter    Current method parameter
     * @param mediaType          Response media type
     * @param converterType      Current message converter type
     * @param serverHttpRequest  Current HTTP request
     * @param serverHttpResponse Current HTTP response
     * @return Processed response body content
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                  @NotNull MethodParameter methodParameter,
                                  @NotNull MediaType mediaType,
                                  @NotNull Class<? extends HttpMessageConverter<?>> converterType,
                                  @NotNull ServerHttpRequest serverHttpRequest,
                                  @NotNull ServerHttpResponse serverHttpResponse) {
        // Handle null response, convert to JSON format success response
        if (body == null && StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
            serverHttpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return JSON.toJSONString(ResultHolder.success(body));
        }

        // If the method is annotated with NoResultHolder, do not wrap
        if (methodParameter.hasMethodAnnotation(NoResultHolder.class)) {
            return body;
        }

        // If the response body is not of type ResultHolder, wrap it as ResultHolder
        if (!(body instanceof ResultHolder)) {
            if (body instanceof String) {
                serverHttpResponse.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return JSON.toJSONString(ResultHolder.success(body));
            }
            return ResultHolder.success(body);
        }

        // If the response body is already of type ResultHolder, return it directly
        return body;
    }
}