package io.demo.common.util;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Encryption and decryption utility, extending CodingUtils, providing AES and MD5 encryption and decryption methods.
 */
public class EncryptUtils extends CodingUtils {

    // Default encryption key and vector
    private static final String secretKey = "www.fit2cloud.cn";

    /**
     * AES encryption method
     *
     * @param o Object to be encrypted
     * @return Encrypted string, returns null if the input object is null
     */
    public static String aesEncrypt(Object o) {
        if (o == null) {
            return null;
        }
        return aesEncrypt(o.toString(), secretKey, generateIv());
    }

    /**
     * AES decryption method
     *
     * @param o Object to be decrypted
     * @return Decrypted string, returns null if the input object is null
     */
    public static String aesDecrypt(Object o) {
        if (o == null) {
            return null;
        }
        return aesDecrypt(o.toString(), secretKey, generateIv());
    }

    /**
     * AES decryption for object properties in a list
     *
     * @param o        List of objects to be decrypted
     * @param attrName Name of the attribute to be decrypted
     * @param <T>      Type of the object
     * @return List of decrypted objects
     */
    public static <T> Object aesDecrypt(List<T> o, String attrName) {
        if (o == null || attrName == null) {
            return null;
        }
        // Decrypt the attribute of each object in the list
        return o.stream()
                .filter(element -> BeanUtils.getFieldValueByName(attrName, element) != null)
                .peek(element -> {
                    Object fieldValue = BeanUtils.getFieldValueByName(attrName, element);
                    if (fieldValue != null) {
                        String decryptedValue = aesDecrypt(fieldValue.toString(), secretKey, generateIv());
                        BeanUtils.setFieldValueByName(element, attrName, decryptedValue, String.class);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * MD5 encryption method
     *
     * @param o Object to be encrypted
     * @return Encrypted MD5 string, returns null if the input object is null
     */
    public static String md5Encrypt(Object o) {
        if (o == null) {
            return null;
        }
        return md5(o.toString());
    }
}