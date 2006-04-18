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
import org.carion.s3.impl.S3UrlNameImpl;

/**
 * Handles 'COPY' request.
 *
 * @author pcarion
 */
public class HandlerCopy extends HandlerBase {
    HandlerCopy(S3Repository repository) {
        super(repository);
    }

    void process(WebdavRequest request, WebdavResponse response)
            throws IOException {
        boolean overwrite = request.getOverwrite();
        S3UrlNameImpl destination = request.getDestination();

        if (destination == null) {
            response.setResponseStatus(WebdavResponse.SC_BAD_REQUEST);
            return;
        }

        if (_repository.objectExists(destination)) {
            if (overwrite) {
                _repository.deleteObject(destination);
            } else {
                response.setResponseStatus(WebdavResponse.SC_FORBIDDEN);
                return;
            }
        }
        _repository.copy(request.getResourceName(), destination);

        response.setResponseStatus(WebdavResponse.SC_CREATED);
    }

}
