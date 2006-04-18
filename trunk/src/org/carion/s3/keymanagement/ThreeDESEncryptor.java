/*
 * This software code is made available "AS IS" without warranties of any
 * kind.  You may copy, display, modify and redistribute the software
 * code either by itself or as incorporated into your code; provided that
 * you do not remove any proprietary notices.  Your use of this software
 * code is at your own risk and you waive any claim against Amazon
 * Digital Services, Inc. or its affiliates with respect to your use of
 * this software code. (c) 2006 Amazon Digital Services, Inc. or its
 * affiliates.
 */
package org.carion.s3.keymanagement;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;

/**
 * Implements the Encryptor interface. This implementation uses the Triple Data
 * Encryption Standard (DES). This encryption scheme requires using a private
 * (secret) key.
 *
 */
public class ThreeDESEncryptor implements Encryptor {

    public static final String THREE_DES_ENCRYPTION_SCHEME = "DESede";

    public static final String THREE_DES_BLOCK_MODE = "CBC";

    public static final String THREE_DES_PADDING_MODE = "PKCS5Padding";

    private static final String UNICODE_FORMAT = "UTF8";

    private IvParameterSpec ivSpec;

    private KeySpec keySpec;

    private SecretKeyFactory keyFactory;

    private Cipher cipher;

    public ThreeDESEncryptor(String encryptionKey) throws EncryptionException {
        this(encryptionKey, THREE_DES_ENCRYPTION_SCHEME, THREE_DES_BLOCK_MODE,
                THREE_DES_PADDING_MODE);
    }

    public ThreeDESEncryptor(String encryptionKey, String encryptionScheme,
            String blockMode, String paddingMode) throws EncryptionException {
        try {
            byte[] keyAsBytes = encryptionKey.getBytes(UNICODE_FORMAT);
            ivSpec = new IvParameterSpec(encryptionKey.substring(1, 9)
                    .getBytes(UNICODE_FORMAT));
            keySpec = new DESedeKeySpec(keyAsBytes);
            keyFactory = SecretKeyFactory.getInstance(encryptionScheme);
            cipher = Cipher.getInstance(encryptionScheme + "/" + blockMode
                    + "/" + paddingMode);
        } catch (GeneralSecurityException e) {
            throw new EncryptionException(e);
        } catch (UnsupportedEncodingException e) {
            throw new EncryptionException(e);
        }
    }

    public byte[] encrypt(byte[] cleartext) throws EncryptionException {
        if (cleartext == null) {
            throw new IllegalArgumentException("cleartext was null");
        }

        try {
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] ciphertext = cipher.doFinal(cleartext);
            return ciphertext;
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }

    public byte[] decrypt(byte[] ciphertext) throws EncryptionException {
        if (ciphertext == null)
            throw new IllegalArgumentException("ciphertext was null");

        try {
            SecretKey key = keyFactory.generateSecret(keySpec);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] cleartext = cipher.doFinal(ciphertext);
            return cleartext;
        } catch (Exception e) {
            throw new EncryptionException(e);
        }
    }
}
