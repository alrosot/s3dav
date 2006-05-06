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
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.carion.s3.Credential;
import org.carion.s3.S3Log;

/**
 * The PUT request operation is used to add an object to a bucket. The response
 * indicates that the object has been successfully stored. S3 never stores
 * partial objects: if you receive a successful response, then you can be
 * confident that the entire object was stored. If the object already exists in
 * the bucket, the new object overwrites the existing object. S3 orders all of
 * the requests that it receives. it is possible that if you send two requests
 * nearly simultaneously, we will receive them in a different order than they
 * were sent. The last request received is the one which is stored in S3. Note
 * that this means if multiple parties are simultaneously writing to the same
 * object, they may all get a successful response even though only one of them
 * wins in the end. This is because S3 is a distributed system and it may take a
 * few seconds for one part of the system to realize that another part has
 * received an object update. In this release of Amazon S3, there is no ability
 * to lock an object for writing -- such functionality, if required, should be
 * provided at the application layer.
 * 
 * @author pcarion
 */
public class ObjectPUT extends BaseS3Operation {
    private final String _uri;

    public ObjectPUT(String uri, Credential credential, S3Log log) {
        super(credential, log);
        _uri = uri;
    }

    public boolean execute() throws IOException {
        return execute(null, null, null, -1, null);
    }

    public boolean execute(InputStream content, String contentType,
            String contentMd5, long contentLength, UploadNotification notify)
            throws IOException {
        S3Request X = S3Request.mkPutRequest(_uri, _log);
        if (content != null) {
            if (notify != null) {
                X.setUploadNotification(notify);
            }
            X.setContent(content, contentMd5, contentType, contentLength);
        }
        return process(X);
    }
}
