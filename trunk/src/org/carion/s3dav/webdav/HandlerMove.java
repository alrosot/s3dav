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
import org.carion.s3dav.s3.naming.S3UrlName;

/**
 * Handles 'MOVE' request.
 *
 * @author pcarion
 */
public class HandlerMove extends HandlerBase {
    HandlerMove(WebdavRepository repository) {
        super(repository);
    }

    void process(WebdavRequest request, WebdavResponse response)
            throws IOException {
        boolean overwrite = request.getOverwrite();
        S3UrlName destination = request.getDestination();

        if (destination == null) {
            response.setResponseStatus(WebdavResponse.SC_BAD_REQUEST);
            return;
        }

        // rfc 2518 - 8.9.3
        // MOVE and the Overwrite Header
        // If a resource exists at the destination and
        // server MUST perform a DELETE with "Depth:
        // set to "F" then the operation will fail.

        if (_repository.objectExists(destination)) {
            if (overwrite) {
                // WARNING: we should backup the content
                // if something wrong afterwards ... the content is lost !
                _repository.deleteObject(destination);
            } else {
                response.setResponseStatus(WebdavResponse.SC_BAD_REQUEST);
                return;
            }
        }
        _repository.copy(request.getUrl(), destination);
        _repository.deleteObject(request.getUrl());

        response.setResponseStatus(WebdavResponse.SC_CREATED);
    }
}
