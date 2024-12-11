package io.demo.mybatis.lambda;

import io.demo.common.exception.GenericException;

import java.io.*;

/**
 * This class is a mirror of {@link java.lang.invoke.SerializedLambda}.
 * <p>
 * This class is used to serialize and extract metadata from Lambda expressions, providing the same functionality as the original {@link SerializedLambda} class,
 * but can be used in a custom serialization environment.
 * </p>
 */
@SuppressWarnings("ALL")
public class SerializedLambda implements Serializable {
    private static final long serialVersionUID = 8025925345765570181L;

    private Class<?> capturingClass;
    private String functionalInterfaceClass;
    private String functionalInterfaceMethodName;
    private String functionalInterfaceMethodSignature;
    private String implClass;
    private String implMethodName;
    private String implMethodSignature;
    private int implMethodKind;
    private String instantiatedMethodType;
    private Object[] capturedArgs;

    /**
     * Extracts a {@link SerializedLambda} object from a serialized Lambda expression.
     * This method extracts the metadata of the Lambda expression through the serialization and deserialization process.
     *
     * @param serializable A serializable object, usually a Lambda expression.
     * @return The extracted {@link SerializedLambda} object.
     * @throws GenericException If an exception occurs during the serialization or deserialization process.
     */
    public static SerializedLambda extract(Serializable serializable) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(serializable);
            oos.flush();

            try (ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                    Class<?> clazz = super.resolveClass(desc);
                    return clazz == java.lang.invoke.SerializedLambda.class ? SerializedLambda.class : clazz;
                }
            }) {
                return (SerializedLambda) objectInputStream.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new GenericException("Exception occurred while extracting Lambda expression", e);
        }
    }

    /**
     * Gets the string of the instantiated method type.
     *
     * @return The string of the instantiated method type.
     */
    public String getInstantiatedMethodType() {
        return instantiatedMethodType;
    }

    /**
     * Gets the {@link Class} object of the capturing class.
     *
     * @return The {@link Class} object of the capturing class.
     */
    public Class<?> getCapturingClass() {
        return capturingClass;
    }

    /**
     * Gets the name of the implementation method.
     *
     * @return The name of the implementation method.
     */
    public String getImplMethodName() {
        return implMethodName;
    }
}