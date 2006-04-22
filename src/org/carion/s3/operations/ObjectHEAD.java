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
 * The HEAD operation is used to retrieve information about a
 * specific object, without actually fetching the object itself.
 * This is useful if you're only interested in the object metadata,
 * and don't want to waste bandwidth on the object data.
 * A HEAD request has the same options as a GET operation on an object.
 * The response is identical to the GET response, except that there is
 * no response body. For details, please see GET Object.
 *
 * @author pcarion
 */
public class ObjectHEAD extends BaseS3Operation {
    private final String _uri;

    public ObjectHEAD(String uri, Credential credential, S3Log log) {
        super(credential, log);
        _uri = uri;
    }

    public boolean execute() throws IOException {
        S3Request X = S3Request.mkHeadRequest(_uri, _log);
        return process(X);
    }
}
