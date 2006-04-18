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

public interface AwsAuthorization {
    /**
     * @return access key
     * @throws AwsException
     */
    public String getAccessKey() throws AwsAuthorizationException;

    /**
     * This uses a char[] instead of a String because the contents of a String
     * will be kicking around in memory for the remainder of the execution
     * of the application (and thereby discoverable by clever, inimical bad
     * people).
     * @return secret key
     * @throws AwsException
     */
    public char[] getSecretKey() throws AwsAuthorizationException;

}
