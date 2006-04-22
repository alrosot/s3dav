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
import java.net.HttpURLConnection;

import org.carion.s3.Credential;
import org.carion.s3.S3Log;

/**
 * You fetch objects from S3 using the GET operation.
 * This operation returns the object directly from S3 using
 * a client/server delivery mechanism.
 * If you want to distribute big files to a large number of people,
 * you may find BitTorrent delivery to be preferable since it
 * uses less bandwidth.
 * Please see the section on Using BitTorrent with S3 for details.
 *
 * @author pcarion
 */
public class ObjectGET extends BaseS3Operation {
    private final String _uri;

    HttpURLConnection _conn;

    public ObjectGET(String uri, Credential credential, S3Log log) {
        super(credential, log);
        _uri = uri;
    }

    public boolean execute() throws IOException {
        S3Request X = S3Request.mkGetRequest(_uri, _log);
        return process(X, false);
    }
}
