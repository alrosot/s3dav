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

import org.carion.s3.S3Folder;
import org.carion.s3.S3Repository;
import org.carion.s3.S3Resource;
import org.carion.s3.S3UrlName;
import org.carion.s3.util.Util;

public class HandlerHead extends HandlerGet {
    HandlerHead(S3Repository repository) {
        super(repository);
    }

    void process(WebdavRequest request, WebdavResponse response)
            throws IOException {
        S3UrlName href = request.getUrl();

        boolean exists = _repository.objectExists(href);
        if (exists) {
            boolean isDirectory = _repository.isFolder(href);

            if (isDirectory) {
                S3Folder folder = _repository.getFolder(href);

                response.setResponseHeader("last-modified", Util.getHttpDate());

                String body = getFolderHtmlPage(folder, request);

                response.setResponseHeader("Content-Length", String
                        .valueOf(body.length()));

                response.setResponseHeader("Content-Type", "text/html");
            } else {
                S3Resource resource = _repository.getResource(href);
                setHeaders(resource, response);
            }
        } else {
            response.setResponseStatus(WebdavResponse.SC_NOT_FOUND);
        }
    }

}
