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

import org.carion.s3dav.repository.WebdavRepository;
import org.carion.s3dav.repository.WebdavResource;
import org.carion.s3dav.s3.naming.S3UrlName;

/**
 * Handles 'PUT' request
 *
 * @author pcarion
 */
public class HandlerPut extends HandlerBase {
    HandlerPut(WebdavRepository repository) {
        super(repository);
    }

    void process(WebdavRequest request, WebdavResponse response)
            throws IOException {
        S3UrlName url = request.getUrl();

        if (_repository.objectExists(url)) {
            if (_repository.isFolder(url)) {
                response.setResponseStatus(WebdavResponse.SC_FORBIDDEN);
            } else {
                WebdavResource res = _repository.getResource(url);
                res.setResourceContent(request.getInputStream(), request
                        .getContentType(), request.getContentLength());
                response.setResponseStatus(WebdavResponse.SC_CREATED);
            }
        } else {
            S3UrlName parent = url.getParent();
            if (parent != null) {
                if (!_repository.isFolder(parent)) {
                    response.setResponseStatus(WebdavResponse.SC_CONFLICT);
                    return;
                }
            }
            WebdavResource res = _repository.createResource(url);
            res.setResourceContent(request.getInputStream(), request
                    .getContentType(), request.getContentLength());
            response.setResponseStatus(WebdavResponse.SC_CREATED);
        }
    }
}
