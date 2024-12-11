package io.demo.common.util.rsa;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * RSA key pair class, encapsulating the public and private keys.
 * <p>
 * This class is used to store the public and private keys in the RSA encryption algorithm for encryption and decryption operations.
 * </p>
 */
@Setter
@Getter
public class RsaKey implements Serializable {

    /**
     * Public key, used in RSA encryption.
     */
    private String publicKey;

    /**
     * Private key, used in RSA decryption.
     */
    private String privateKey;
}