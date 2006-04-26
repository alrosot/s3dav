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

    public void process(HttpRequest request, HttpResponse response)
            throws IOException {
        boolean overwrite = request.getOverwrite();
        S3UrlNameImpl destination = request.getDestination();
        boolean noContent = false;

        if (destination == null) {
            response.setResponseStatus(HttpResponse.SC_BAD_REQUEST);
            return;
        }

        if (destination.equals(request.getResourceName())) {
            response.setResponseStatus(HttpResponse.SC_FORBIDDEN);
            return;
        }

        boolean destinationExists = _repository.objectExists(destination);

        _log.log("COPY from (" + request.getResourceName().getUri() + ") to ("
                + destination.getUri() + ")");
        _log.log("---- overwrite? " + overwrite + " destination exists? "
                + destinationExists);

        if (destinationExists) {
            if (overwrite) {
                noContent = true;
                _repository.deleteObject(destination);
            } else {
                response.setResponseStatus(HttpResponse.SC_PRECONDITION_FAILED);
                return;
            }
        } else {
            // we must check that the parent directory exist
            S3UrlName parent = destination.getParent();
            if (parent != null) {
                if (! _repository.objectExists(parent)) {
                    response.setResponseStatus(HttpResponse.SC_CONFLICT);
                    return;
                }
            }
        }
        _repository.copy(request.getResourceName(), destination);

        response.setResponseStatus(noContent ? HttpResponse.SC_NO_CONTENT
                : HttpResponse.SC_CREATED);
    }

}
