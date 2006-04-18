package org.carion.s3.impl;

import java.security.KeyStoreException;

import org.carion.s3.Credential;
import org.carion.s3dav.s3.keymanagement.CredentialImpl;

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
