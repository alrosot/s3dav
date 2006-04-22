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
 * The DELETE request operation deletes the bucket named in the URI.
 * All objects in the bucket must be deleted before the bucket itself can be deleted.
 *
 * @author pcarion
 */
public class BucketDELETE extends BaseS3Operation {
    private final String _bucket;

    public BucketDELETE(String bucket, Credential credential, S3Log log) {
        super(credential,log);
        _bucket = bucket;
    }

    public boolean execute() throws IOException {
        S3Request X = S3Request.mkDeleteRequest("/" + _bucket, _log);
        return process(X);
    }
}
