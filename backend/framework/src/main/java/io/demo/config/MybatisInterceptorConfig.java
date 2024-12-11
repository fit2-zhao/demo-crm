package io.demo.config;

import io.demo.common.util.EncryptUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * Mybatis interceptor configuration class, mainly used for configuring Mybatis encryption and decryption interceptors.
 * <p>
 * This class is used to define interceptor settings related to encryption/decryption, including model class, attribute name, interceptor class, and methods.
 * </p>
 */
@Getter
@Setter
public class MybatisInterceptorConfig {

    /**
     * Name of the model class.
     */
    private String modelName;

    /**
     * Name of the attribute to be intercepted.
     */
    private String attrName;

    /**
     * List of attribute names to be intercepted.
     */
    private String attrNameForList;

    /**
     * Name of the interceptor class.
     */
    private String interceptorClass;

    /**
     * Name of the interceptor method.
     */
    private String interceptorMethod;

    /**
     * Name of the undo class (e.g., decryption operation class).
     */
    private String undoClass;

    /**
     * Name of the undo method (e.g., decryption method).
     */
    private String undoMethod;

    /**
     * Default constructor, initializes an empty interceptor configuration.
     */
    public MybatisInterceptorConfig() {
    }

    /**
     * Constructor for configuring encryption interceptors.
     * <p>
     * By default, it uses the encryption and decryption methods of the {@link EncryptUtils} class.
     * </p>
     *
     * @param modelClass Model class
     * @param attrName   Name of the attribute to be encrypted
     */
    public MybatisInterceptorConfig(Class<?> modelClass, String attrName) {
        this.modelName = modelClass.getName();
        this.attrName = attrName;
        this.interceptorClass = EncryptUtils.class.getName();
        this.interceptorMethod = "aesEncrypt";
        this.undoClass = EncryptUtils.class.getName();
        this.undoMethod = "aesDecrypt";
    }

    /**
     * Constructor for custom interceptors.
     * <p>
     * This constructor allows passing custom interceptor classes and methods.
     * </p>
     *
     * @param modelClass        Model class
     * @param attrName          Name of the attribute to be intercepted
     * @param interceptorClass  Custom interceptor class
     * @param interceptorMethod Custom interceptor method
     * @param undoMethod        Custom undo method
     */
    public MybatisInterceptorConfig(Class<?> modelClass, String attrName, Class<?> interceptorClass, String interceptorMethod, String undoMethod) {
        this.modelName = modelClass.getName();
        this.attrName = attrName;
        this.interceptorClass = interceptorClass.getName();
        this.interceptorMethod = interceptorMethod;
        this.undoClass = interceptorClass.getName();
        this.undoMethod = undoMethod;
    }
}