/*
 * Copyright (c) 2006, Pierre Carion.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.carion.s3.keymanagement;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;

import org.carion.s3.Credential;
import org.carion.s3.keymanagement.Encryptor.EncryptionException;

/**
 * Yes, this class violates the security 'trick' described in AwsAuthorization.
 * 
 * @author pcarion
 * 
 */
public class CredentialImpl implements Credential {
    private static final String UNICODE_FORMAT = "UTF8";

    // this is just a sample key base, you are encouraged to use a
    // different one that is not human readable. If you really want
    // to be secure, you might not want to hardcode this here.
    private static String KEY_BASE = "0x01z8*oneplusonequalstwo89xdrftyy";

    private final boolean _accessAllowed;

    private final String _host = "s3.amazonaws.com";

    private String _awsAccessKeyId;

    private String _awsSecretAccessKey;

    private final File _accessFile;

    public CredentialImpl(File s3davDirectory) throws KeyStoreException {
        _accessFile = new File(s3davDirectory, "authorization");
        renameOldAuthFile(_accessFile);

        if (!_accessFile.exists() || !_accessFile.isFile()) {
            throw new KeyStoreException(
                    "No encrypted authorization file found.");
        }

        try {
            readKeyFile();
            _accessAllowed = true;
        } catch (IOException ex) {
            throw new KeyStoreException("can't get keys:" + ex.getMessage());
        } catch (EncryptionException ex) {
            throw new KeyStoreException("can't get keys:" + ex.getMessage());
        } catch (AwsAuthorizationException ex) {
            throw new KeyStoreException("can't get keys:" + ex.getMessage());
        }
    }

    public CredentialImpl(File s3davDirectory, String accessKey,
            String secretKey) throws KeyStoreException {
        _accessFile = new File(s3davDirectory, "authorization");
        try {
            writeKeyFile(accessKey, secretKey.toCharArray());
            _accessAllowed = true;
        } catch (Exception e) {
            throw new KeyStoreException("can't set keys:" + e.getMessage());
        }
    }

    public static void deleteCredential(File s3davDirectory) {
        File accessFile = new File(s3davDirectory, "authorization");
        renameOldAuthFile(accessFile);
        accessFile.delete();
    }

    public String getAwsAccessKeyId() {
        return _awsAccessKeyId;
    }

    public String getAwsSecretAccessKey() {
        return _awsSecretAccessKey;
    }

    public String getHost() {
        return _host;
    }

    public boolean isAccessAllowed() {
        return _accessAllowed;
    }

    private void readKeyFile() throws IOException, EncryptionException,
            AwsAuthorizationException {
        System.out.println("@@ reading cypher from:"+_accessFile);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(
                _accessFile));
        int len = in.available();
        byte[] buffer = new byte[len];
        in.read(buffer);
        in.close();
        System.out.println("@@ len read is:"+len);

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

        // Get the Access Key
        int offset = 2 + nameLen;
        byte[] accessKeyRaw = new byte[accessKeyLen];
        for (int i = 0; i < accessKeyLen; i++) {
            accessKeyRaw[i] = cleartext[i + offset];
        }
        _awsAccessKeyId = new String(accessKeyRaw, "UTF8");

        // Get the Secret Key
        char[] secretKey = new char[secretKeyLen / 2];
        offset += accessKeyLen;
        for (int i = 0; i < secretKeyLen / 2; i++) {
            int spot = i + i + offset;
            char ch = (char) ((cleartext[spot] << 8) + cleartext[spot + 1]);
            secretKey[i] = ch;
        }
        _awsSecretAccessKey = new String(secretKey);
    }

    private void writeKeyFile(String accessKey, char[] secretKey)
            throws UnsupportedEncodingException, EncryptionException,
            IOException, AwsAuthorizationException {
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

        System.out.println("@@ writing cypher in:"+_accessFile);
        System.out.println("@@ len written is:"+cipher.length);

        _accessFile.createNewFile();
        OutputStream out = new FileOutputStream(_accessFile);
        out.write(cipher);
        out.close();
        
        readKeyFile();
    }

    private static void renameOldAuthFile(File newFile) {
        File oldFile = new File(System.getProperty("user.home"), System
                .getProperty("user.name")
                + "_aws_authorization");
        if (oldFile.exists() && oldFile.isFile()) {
            oldFile.renameTo(newFile);
        }
    }
}
