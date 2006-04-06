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
package org.carion.s3dav.s3.keymanagement;

public class AwsAuthorizationException extends Exception {
    static final long serialVersionUID = -3287373151130769090L;

    AwsAuthorizationException() {
        super();
    }

    AwsAuthorizationException(Throwable e) {
        super(e);
    }

    public AwsAuthorizationException(String message, Throwable e) {
        super(message, e);
    }

    public AwsAuthorizationException(String message) {
        super(message);
    }
}
