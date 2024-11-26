package io.demo.common.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 加密解密工具类，提供 MD5、BASE64 和 AES 加密解密操作。
 * 支持常见的加密解密算法，简化了加密过程。
 */
public class CodingUtils {

    private static final String UTF_8 = "UTF-8";
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * MD5加密（默认UTF-8字符集）
     *
     * @param src 要加密的字符串
     * @return 加密后的MD5字符串
     */
    public static String md5(String src) {
        return md5(src, UTF_8);
    }

    /**
     * MD5加密
     *
     * @param src     要加密的字符串
     * @param charset 使用的字符集
     * @return 加密后的MD5字符串
     */
    public static String md5(String src, String charset) {
        try {
            byte[] strTemp = StringUtils.isEmpty(charset) ? src.getBytes() : src.getBytes(charset);
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);

            byte[] md = mdTemp.digest();
            char[] str = new char[md.length * 2];
            int k = 0;

            for (byte byte0 : md) {
                str[k++] = HEX_DIGITS[(byte0 >>> 4) & 0xf];
                str[k++] = HEX_DIGITS[byte0 & 0xf];
            }

            return new String(str);
        } catch (Exception e) {
            throw new RuntimeException("MD5 encrypt error:", e);
        }
    }

    /**
     * BASE64解密
     *
     * @param src 待解密的字符串
     * @return 解密后的字符串
     */
    public static String base64Decoding(String src) {
        try {
            if (src != null) {
                byte[] decodedBytes = Base64.decodeBase64(src);
                return new String(decodedBytes, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            throw new RuntimeException("BASE64 decoding error:", e);
        }
        return null;
    }

    /**
     * BASE64加密
     *
     * @param src 待加密的字符串
     * @return 加密后的字符串
     */
    public static String base64Encoding(String src) {
        try {
            if (src != null) {
                return Base64.encodeBase64String(src.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            throw new RuntimeException("BASE64 encoding error:", e);
        }
        return null;
    }

    /**
     * AES加密
     *
     * @param src       待加密的字符串
     * @param secretKey 加密密钥
     * @param iv        初始向量
     * @return 加密后的字符串
     */
    public static String aesEncrypt(String src, String secretKey, String iv) {
        if (StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("secretKey is empty");
        }

        try {
            byte[] raw = secretKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(src.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES encrypt error:", e);
        }
    }

    /**
     * AES解密
     *
     * @param src       待解密的字符串
     * @param secretKey 解密密钥
     * @param iv        初始向量
     * @return 解密后的字符串
     */
    public static String aesDecrypt(String src, String secretKey, String iv) {
        if (StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("secretKey is empty");
        }

        try {
            byte[] raw = secretKey.getBytes(StandardCharsets.UTF_8);
            SecretKeySpec secretKeySpec = new SecretKeySpec(raw, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivSpec = new IvParameterSpec(iv.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

            byte[] encrypted = Base64.decodeBase64(src);
            byte[] original = cipher.doFinal(encrypted);
            return new String(original, StandardCharsets.UTF_8);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            // 如果解密失败，可能是因为源字符串不是加密后的字符串，直接返回原字符串
            return src;
        } catch (Exception e) {
            throw new RuntimeException("AES decrypt error:", e);
        }
    }

    /**
     * 生成一个新的AES密钥
     *
     * @return 生成的AES密钥（Base64编码）
     */
    public static String secretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.encodeBase64String(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Generate AES secret key error", e);
        }
    }
}
