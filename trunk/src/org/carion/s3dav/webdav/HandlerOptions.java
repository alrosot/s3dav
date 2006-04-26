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
import org.carion.s3.S3UrlName;
import org.carion.s3.http.HttpRequest;
import org.carion.s3.http.HttpResponse;

public class HandlerOptions extends HandlerBase {
    HandlerOptions(S3Repository repository) {
        super(repository);
    }

    public void process(HttpRequest request, HttpResponse response)
            throws IOException {
        S3UrlName href = request.getUrl();
        StringBuffer options = new StringBuffer();
        response.setResponseHeader("DAV", "1, 2");

        boolean exists = _repository.isResource(href);
        if (exists) {
            boolean isDirectory = _repository.isFolder(href);
            options
                    .append("OPTIONS, GET, HEAD, POST, DELETE, TRACE, COPY, MOVE, LOCK, UNLOCK, PROPFIND");
            if (isDirectory) {
                options.append(", PUT");
            }
        } else {
            options.append("OPTIONS, MKCOL, PUT, LOCK");
        }
        response.setResponseHeader("Allow", options.toString());

        // see: http://www-128.ibm.com/developerworks/rational/library/2089.html
        response.setResponseHeader("MS-Author-Via", "DAV");
    }
}
