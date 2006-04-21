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
package org.carion.s3.impl;

import java.security.KeyStoreException;

import org.carion.s3.Credential;
import org.carion.s3.keymanagement.CredentialImpl;

public class CredentialFactory {
    public static Credential getCredential() {
        Credential credential;

        try {
            credential = new CredentialImpl();
        } catch (KeyStoreException ex) {
            ex.printStackTrace();
            credential = new NoCredential();
        }
        return credential;
    }

    public static Credential newCredential(String accessKey, String secretKey) {
        Credential credential;

        try {
            credential = CredentialImpl.mkNewCredential(accessKey, secretKey);
        } catch (KeyStoreException ex) {
            ex.printStackTrace();
            credential = new NoCredential();
        }
        return credential;

    }

    public static Credential deleteCredential() {
        CredentialImpl.deleteCredential();
        return new NoCredential();
    }

    private static class NoCredential implements Credential {
        public String getAwsAccessKeyId() {
            return null;
        }

        public String getAwsSecretAccessKey() {
            return null;
        }

        public String getHost() {
            return null;
        }

        public boolean isAccessAllowed() {
            return false;
        }

    }

}
