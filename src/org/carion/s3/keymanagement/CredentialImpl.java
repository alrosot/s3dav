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

import java.io.IOException;
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
    private final boolean _accessAllowed;

    private final String _host;

    private final String _awsAccessKeyId;

    private final String _awsSecretAccessKey;

    public static Credential mkNewCredential(String accessKey, String secretKey)
            throws KeyStoreException {
        try {
            EncryptedFileAuthorization.writeKeyFile(accessKey, secretKey
                    .toCharArray());
        } catch (IOException ex) {
            throw new KeyStoreException("can't set keys:" + ex.getMessage());
        } catch (EncryptionException ex) {
            throw new KeyStoreException("can't set keys:" + ex.getMessage());
        }
        return new CredentialImpl();
    }

    public static void deleteCredential() {
        EncryptedFileAuthorization.delete();
    }

    public CredentialImpl() throws KeyStoreException {
        _host = "s3.amazonaws.com";
        try {
            AwsAuthorization access = new EncryptedFileAuthorization();
            _awsAccessKeyId = access.getAccessKey();
            _awsSecretAccessKey = new String(access.getSecretKey());
            _accessAllowed = true;
        } catch (IOException ex) {
            throw new KeyStoreException("can't get keys:" + ex.getMessage());
        } catch (EncryptionException ex) {
            throw new KeyStoreException("can't get keys:" + ex.getMessage());
        } catch (AwsAuthorizationException ex) {
            throw new KeyStoreException("can't get keys:" + ex.getMessage());
        }
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

}
