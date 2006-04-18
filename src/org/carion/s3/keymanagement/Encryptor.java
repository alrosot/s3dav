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

/**
 * Simple encryptor interface to abstract encryptor type.
 */
public interface Encryptor {
    public byte[] encrypt(byte[] cleartext) throws EncryptionException;

    public byte[] decrypt(byte[] ciphertext) throws EncryptionException;

    /**
     * Define custom exception type.
     */
    public static class EncryptionException extends Exception {

        private static final long serialVersionUID = 1L;

        public EncryptionException() {
            super();
        }

        public EncryptionException(String message) {
            super(message);
        }

        public EncryptionException(Throwable t) {
            super(t);
        }

        public EncryptionException(String message, Throwable t) {
            super(message, t);
        }
    }
}
