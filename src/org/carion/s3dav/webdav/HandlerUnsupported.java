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
package org.carion.s3dav.webdav;

import java.io.IOException;

import org.carion.s3.S3Repository;
import org.carion.s3.http.HttpRequest;
import org.carion.s3.http.HttpResponse;

/**
 * Handles unsupported request
 * 
 * @author pcarion
 */
public class HandlerUnsupported extends HandlerBase {
    HandlerUnsupported(S3Repository repository) {
        super(repository);
    }

    public void process(HttpRequest request, HttpResponse response)
            throws IOException {
        response.setResponseStatus(HttpResponse.SC_NOT_IMPLEMENTED);
    }
}
