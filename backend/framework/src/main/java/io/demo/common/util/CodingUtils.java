package io.demo.common.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Encryption and decryption utility class, providing MD5, BASE64, and AES encryption and decryption operations.
 * Supports common encryption and decryption algorithms, simplifying the encryption process.
 */
public class CodingUtils {

    private static final String UTF_8 = "UTF-8";
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    /**
     * Encryption offset, initialization vector for AES encryption.
     */
    private static final String GCM_IV = "1Av7hf9PgHusUHRm";
    private static final int GCM_TAG_LENGTH = 128; // GCM tag length (in bits)

    /**
     * MD5 encryption (default UTF-8 charset)
     *
     * @param src String to be encrypted
     * @return Encrypted MD5 string
     */
    public static String md5(String src) {
        return md5(src, UTF_8);
    }

    /**
     * MD5 encryption
     *
     * @param src     String to be encrypted
     * @param charset Charset to be used
     * @return Encrypted MD5 string
     */
    public static String md5(String src, String charset) {
        if (StringUtils.isBlank(src)) {
            throw new IllegalArgumentException("Input for MD5 cannot be null or empty");
        }

        try {
            byte[] strTemp = src.getBytes(StringUtils.defaultIfBlank(charset, UTF_8));
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
     * BASE64 decoding
     *
     * @param src String to be decoded
     * @return Decoded string
     */
    public static String base64Decoding(String src) {
        if (StringUtils.isBlank(src)) {
            throw new IllegalArgumentException("Input for BASE64 decoding cannot be null or empty");
        }

        try {
            byte[] decodedBytes = Base64.decodeBase64(src);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("BASE64 decoding error:", e);
        }
    }

    /**
     * BASE64 encoding
     *
     * @param src String to be encoded
     * @return Encoded string
     */
    public static String base64Encoding(String src) {
        if (StringUtils.isBlank(src)) {
            throw new IllegalArgumentException("Input for BASE64 encoding cannot be null or empty");
        }

        try {
            return Base64.encodeBase64String(src.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("BASE64 encoding error:", e);
        }
    }

    /**
     * AES-GCM encryption
     *
     * @param src       String to be encrypted
     * @param secretKey Encryption key (16 bytes)
     * @param iv        Initialization vector (12 bytes)
     * @return Encrypted string
     */
    public static String aesEncrypt(String src, String secretKey, byte[] iv) {
        if (StringUtils.isBlank(src) || StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("Input or secretKey cannot be null or empty");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

            byte[] encryptedBytes = cipher.doFinal(src.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM encrypt error:", e);
        }
    }

    /**
     * AES-GCM decryption
     *
     * @param src       String to be decrypted
     * @param secretKey Decryption key (16 bytes)
     * @param iv        Initialization vector (12 bytes)
     * @return Decrypted string
     */
    public static String aesDecrypt(String src, String secretKey, byte[] iv) {
        if (StringUtils.isBlank(src) || StringUtils.isBlank(secretKey)) {
            throw new IllegalArgumentException("Input or secretKey cannot be null or empty");
        }

        try {
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);

            byte[] decodedBytes = Base64.decodeBase64(src);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM decrypt error:", e);
        }
    }

    /**
     * Generates a new AES key
     *
     * @return Generated AES key (Base64 encoded)
     */
    public static String generateSecretKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey secretKey = keyGen.generateKey();
            return Base64.encodeBase64String(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Generate AES secret key error:", e);
        }
    }

    /**
     * Generates a random IV (for AES-GCM)
     *
     * @return Randomly generated IV
     */
    public static byte[] generateIv() {
        return GCM_IV.getBytes();
    }
}