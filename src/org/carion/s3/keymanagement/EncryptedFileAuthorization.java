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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.carion.s3.keymanagement.Encryptor.EncryptionException;

/**
 * Uses an encrypted file to store the user access key and secret key.
 * When an instance of this class is created, it will read the access key
 * and secret key from the file. If the file does not exist, an exception
 * is throw. A static method is provided to create the encrypted file
 * initially.
 *
 * In addition, the access key and secret key can only be read from the
 * encrypted file if the user that created it is the one reading it.
 *
 * <p>
 * Possible feature extention is to add expiration to the data stored in the
 * file, so it will only work for a limited time.
 */
public class EncryptedFileAuthorization implements AwsAuthorization {

    private static final String UNICODE_FORMAT = "UTF8";

    public static final String ACCESS_FILE_PATH = System
            .getProperty("user.home");

    public static final String ACCESS_FILE_NAME = System
            .getProperty("user.name")
            + "_aws_authorization";

    //this is just a sample key base, you are encouraged to use a
    //different one that is not human readable. If you really want
    //to be secure, you might not want to hardcode this here.
    private static String KEY_BASE = "0x01z8*oneplusonequalstwo89xdrftyy";

    private String accessKey;

    private File file;

    private char[] secretKey;

    /**
     * Creates the encrypted file and stores the access key and secret key
     * using a 3DES Encryptor. Note that the private key used by the
     * 3DES Encryptor has the following format: userName + KEY_BASE
     *
     * @param accessKey
     * @param secretKey
     * @throws UnsupportedEncodingException
     * @throws EncryptionException
     * @throws IOException
     */
    public static void writeKeyFile(String accessKey, char[] secretKey)
            throws UnsupportedEncodingException, EncryptionException,
            IOException {
        String userName = System.getProperty("user.name");
        byte[] nameRaw = userName.getBytes(UNICODE_FORMAT);
        byte[] accessKeyRaw = accessKey.getBytes(UNICODE_FORMAT);

        if (nameRaw.length > 0xff) {
            throw new EncryptionException("name too long");
        }
        if (accessKeyRaw.length > 0xff) {
            throw new EncryptionException("accessKey too long");
        }

        byte[] cleartext = new byte[2 + nameRaw.length + accessKeyRaw.length
                + secretKey.length * 2];
        cleartext[0] = (byte) nameRaw.length;
        cleartext[1] = (byte) accessKeyRaw.length;
        for (int i = 0; i < nameRaw.length; i++) {
            cleartext[i + 2] = nameRaw[i];
        }
        int offset = nameRaw.length + 2;
        for (int i = 0; i < accessKeyRaw.length; i++) {
            cleartext[i + offset] = accessKeyRaw[i];
        }
        offset += accessKeyRaw.length;
        for (int i = 0; i < secretKey.length; i++) {
            char ch = secretKey[i];
            int spot = i + i;
            cleartext[offset + spot] = (byte) (ch >> 8); // msb
            cleartext[offset + spot + 1] = (byte) (ch); // lsb
        }

        Encryptor crypt = new ThreeDESEncryptor(userName + KEY_BASE);
        byte[] cipher = crypt.encrypt(cleartext);

        File file = new File(ACCESS_FILE_PATH, ACCESS_FILE_NAME);
        file.createNewFile();
        OutputStream out = new FileOutputStream(file);
        out.write(cipher);
        out.close();
    }

    /**
     * Open the file and read the keys.  If the file doesn't exist,
     * throw an exception.
     * @throws IOException
     * @throws EncryptionException
     * @throws AwsAuthorizationException
     */
    public EncryptedFileAuthorization() throws IOException,
            EncryptionException, AwsAuthorizationException {
        file = new File(ACCESS_FILE_PATH, ACCESS_FILE_NAME);
        if (!file.exists() || !file.isFile()) {
            throw new AwsAuthorizationException(
                    "No encrypted authorization file found.");
        }
        readKeyFile();
    }

    public static void delete() {
        File file = new File(ACCESS_FILE_PATH, ACCESS_FILE_NAME);
        file.delete();
    }

    /* (non-Javadoc)
     * @see authorization.AwsAuthorization#getAccessKey()
     */
    public String getAccessKey() throws AwsAuthorizationException {
        return accessKey;
    }

    /* (non-Javadoc)
     * @see authorization.AwsAuthorization#getSecretKey()
     */
    public char[] getSecretKey() throws AwsAuthorizationException {
        return secretKey;
    }

    /**
     * Do the work of reading the keyfile.
     * @throws IOException
     * @throws EncryptionException
     * @throws AwsAuthorizationException
     */
    private void readKeyFile() throws IOException, EncryptionException,
            AwsAuthorizationException {
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                file));
        int len = in.available();
        byte[] buffer = new byte[len];
        in.read(buffer);
        in.close();

        String userName = System.getProperty("user.name");
        byte[] cleartext;

        Encryptor crypt = new ThreeDESEncryptor(userName + KEY_BASE);
        cleartext = crypt.decrypt(buffer);

        int nameLen = cleartext[0];
        int accessKeyLen = cleartext[1];
        int secretKeyLen = cleartext.length - 2 - nameLen - accessKeyLen;
        if (secretKeyLen % 2 != 0) {
            throw new AwsAuthorizationException(
                    "internal error: secret key has odd bytecount");
        }

        byte[] nameRaw = new byte[nameLen];
        for (int i = 0; i < nameLen; i++) {
            nameRaw[i] = cleartext[i + 2];
        }
        String nameCheck = new String(nameRaw, "UTF8");
        if (!userName.equals(nameCheck)) {
            throw new AwsAuthorizationException(
                    "internal error: secret key name mismatch");
        }

        //Get the Access Key
        int offset = 2 + nameLen;
        byte[] accessKeyRaw = new byte[accessKeyLen];
        for (int i = 0; i < accessKeyLen; i++) {
            accessKeyRaw[i] = cleartext[i + offset];
        }
        accessKey = new String(accessKeyRaw, "UTF8");

        //Get the Secret Key
        secretKey = new char[secretKeyLen / 2];
        offset += accessKeyLen;
        for (int i = 0; i < secretKeyLen / 2; i++) {
            int spot = i + i + offset;
            char ch = (char) ((cleartext[spot] << 8) + cleartext[spot + 1]);
            secretKey[i] = ch;
        }
    }

}
