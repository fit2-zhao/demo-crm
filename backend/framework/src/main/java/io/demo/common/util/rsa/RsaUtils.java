package io.demo.common.util.rsa;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA encryption and decryption utility class, providing RSA algorithm-related encryption and decryption operations.
 * <p>
 * This class supports encryption, decryption, and key generation operations using RSA public and private keys.
 * </p>
 */
public class RsaUtils {

    /**
     * Default character set UTF-8.
     */
    public static final String CHARSET = StandardCharsets.UTF_8.name();

    /**
     * RSA encryption algorithm name.
     */
    public static final String RSA_ALGORITHM = "RSA/ECB/OAEPWITHSHA-256ANDMGF1PADDING";

    /**
     * RSA key pair, cached RSA public and private key objects.
     */
    private static RsaKey rsaKey;

    /**
     * Get the cached RSA key pair, generate it if not created.
     *
     * @return Cached RSA key pair
     * @throws NoSuchAlgorithmException If the specified algorithm is not found
     */
    public static RsaKey getRsaKey() throws NoSuchAlgorithmException {
        if (rsaKey == null) {
            rsaKey = createKeys();
        }
        return rsaKey;
    }

    /**
     * Set the RSA key pair cache.
     *
     * @param rsaKey RSA key pair
     * @throws NoSuchAlgorithmException If the specified algorithm is not found
     */
    public static void setRsaKey(RsaKey rsaKey) throws NoSuchAlgorithmException {
        RsaUtils.rsaKey = rsaKey;
    }

    /**
     * Create a new RSA key pair with a default key length of 1024 bits.
     *
     * @return Generated RSA key pair
     * @throws NoSuchAlgorithmException If the specified algorithm is not found
     */
    public static RsaKey createKeys() throws NoSuchAlgorithmException {
        return createKeys(1024);
    }

    /**
     * Create an RSA key pair with the specified key length.
     *
     * @param keySize Key length, recommended 1024 or 2048 bits
     * @return Generated RSA key pair
     * @throws NoSuchAlgorithmException If the specified algorithm is not found
     */
    public static RsaKey createKeys(int keySize) throws NoSuchAlgorithmException {
        // Create KeyPairGenerator object for RSA algorithm
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        kpg.initialize(keySize); // Initialize KeyPairGenerator object, set key length
        KeyPair keyPair = kpg.generateKeyPair(); // Generate key pair

        // Get public key and encode as Base64 string
        Key publicKey = keyPair.getPublic();
        String publicKeyStr = new String(Base64.encodeBase64(publicKey.getEncoded()));

        // Get private key and encode as Base64 string
        Key privateKey = keyPair.getPrivate();
        String privateKeyStr = new String(Base64.encodeBase64(privateKey.getEncoded()));

        // Create RsaKey object and set public and private keys
        RsaKey rsaKey = new RsaKey();
        rsaKey.setPublicKey(publicKeyStr);
        rsaKey.setPrivateKey(privateKeyStr);

        return rsaKey;
    }

    /**
     * Encrypt data using the public key.
     *
     * @param originalText Plain text
     * @param publicKey    Public key
     * @return Encrypted cipher text
     * @throws NoSuchAlgorithmException If the specified algorithm is not found
     */
    public static String publicEncrypt(String originalText, String publicKey) throws NoSuchAlgorithmException {
        RSAPublicKey rsaPublicKey = getPublicKey(publicKey);
        return publicEncrypt(originalText, rsaPublicKey);
    }

    /**
     * Decrypt data using the public key.
     *
     * @param cipherText Cipher text
     * @param publicKey  Public key
     * @return Decrypted plain text
     * @throws NoSuchAlgorithmException If the specified algorithm is not found
     */
    public static String publicDecrypt(String cipherText, String publicKey) throws NoSuchAlgorithmException {
        RSAPublicKey rsaPublicKey = getPublicKey(publicKey);
        return publicDecrypt(cipherText, rsaPublicKey);
    }

    private static String publicDecrypt(String cipherText, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            String v = new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(cipherText), publicKey.getModulus().bitLength()), CHARSET);
            if (StringUtils.isBlank(v)) {
                return cipherText;
            }
            return v;
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while decrypting string [" + cipherText + "]", e);
        }
    }

    /**
     * Encrypt data using the private key.
     *
     * @param originalText Plain text
     * @param privateKey   Private key
     * @return Encrypted cipher text
     * @throws NoSuchAlgorithmException If the specified algorithm is not found
     */
    public static String privateEncrypt(String originalText, String privateKey) throws NoSuchAlgorithmException {
        RSAPrivateKey rsaPrivateKey = getPrivateKey(privateKey);
        return privateEncrypt(originalText, rsaPrivateKey);
    }

    /**
     * Decrypt data using the private key.
     *
     * @param cipherText Cipher text
     * @param privateKey Private key
     * @return Decrypted plain text
     * @throws NoSuchAlgorithmException If the specified algorithm is not found
     */
    public static String privateDecrypt(String cipherText, String privateKey) throws NoSuchAlgorithmException {
        RSAPrivateKey rsaPrivateKey = getPrivateKey(privateKey);
        return privateDecrypt(cipherText, rsaPrivateKey);
    }

    /**
     * Get the public key object from the public key string.
     *
     * @param publicKey Public key string (Base64 encoded)
     * @return Public key object
     * @throws NoSuchAlgorithmException If the specified algorithm is not found
     */
    private static RSAPublicKey getPublicKey(String publicKey) throws NoSuchAlgorithmException {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKey));
        try {
            return (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Failed to parse public key", e);
        }
    }

    /**
     * Encrypt data using the public key.
     *
     * @param originalText Plain text
     * @param publicKey    Public key object
     * @return Encrypted cipher text
     */
    private static String publicEncrypt(String originalText, RSAPublicKey publicKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, originalText.getBytes(CHARSET), publicKey.getModulus().bitLength()));
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while encrypting", e);
        }
    }

    /**
     * Get the private key object from the private key string.
     *
     * @param privateKey Private key string (Base64 encoded)
     * @return Private key object
     * @throws NoSuchAlgorithmException If the specified algorithm is not found
     */
    private static RSAPrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException {
        KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKey));
        try {
            return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Failed to parse private key", e);
        }
    }

    /**
     * Decrypt data using the private key.
     *
     * @param cipherText Cipher text
     * @param privateKey Private key object
     * @return Decrypted plain text
     */
    private static String privateDecrypt(String cipherText, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            String result = new String(rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, Base64.decodeBase64(cipherText), privateKey.getModulus().bitLength()), CHARSET);
            return StringUtils.isBlank(result) ? cipherText : result;
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while decrypting", e);
        }
    }

    /**
     * Private key encryption operation.
     *
     * @param originalText Plain text
     * @param privateKey   Private key object
     * @return Encrypted cipher text
     */
    private static String privateEncrypt(String originalText, RSAPrivateKey privateKey) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return Base64.encodeBase64URLSafeString(rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, originalText.getBytes(CHARSET), privateKey.getModulus().bitLength()));
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while encrypting", e);
        }
    }

    /**
     * Perform encryption or decryption operations using the public or private key, processing data in blocks.
     *
     * @param cipher  Cipher object for encryption/decryption
     * @param opmode  Operation mode (encryption or decryption)
     * @param data    Data to be encrypted/decrypted
     * @param keySize Key length
     * @return Encrypted/decrypted byte array
     */
    private static byte[] rsaSplitCodec(Cipher cipher, int opmode, byte[] data, int keySize) {
        int maxBlock = (opmode == Cipher.DECRYPT_MODE) ? keySize / 8 : keySize / 8 - 11;
        int offSet = 0;
        byte[] buff;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            while (data.length > offSet) {
                int length = Math.min(data.length - offSet, maxBlock);
                buff = cipher.doFinal(data, offSet, length);
                out.write(buff, 0, buff.length);
                offSet += length;
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred during encryption/decryption", e);
        }
    }
}