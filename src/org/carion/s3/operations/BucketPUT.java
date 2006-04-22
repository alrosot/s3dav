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
package org.carion.s3.operations;

import java.io.IOException;

import org.carion.s3.Credential;
import org.carion.s3.S3Log;

/**
 * The PUT request operation with a bucket URI creates a new bucket.
 * The length of the bucket name must be between 3 and 255 bytes.
 * It can contain letters, numbers, dashes, and underscores.
 *
 * @author pcarion
 */
public class BucketPUT extends BaseS3Operation {
    private final String _bucket;

    public BucketPUT(String bucket, Credential credential, S3Log log) {
        super(credential,log);
        _bucket = bucket;
    }

    public boolean execute() throws IOException {
        S3Request X = S3Request.mkPutRequest("/" + _bucket, _log);
        return process(X);
    }
}
