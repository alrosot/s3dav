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
 * The DELETE request operation removes the specified object from Amazon S3.
 * Once you've deleted it, there is no going back: Amazon S3 does not
 * support any form of undelete.
 *
 * @author pcarion
 */
public class ObjectDELETE extends BaseS3Operation{
    private final String _uri;

    public ObjectDELETE(String uri, Credential credential, S3Log log) {
        super(credential, log);
        _uri = uri;
    }

    public boolean execute() throws IOException {
        S3Request X = S3Request.mkDeleteRequest(_uri, _log);
        return process(X);
    }
}
