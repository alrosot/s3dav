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

/**
 * Handles 'MOVE' request.
 * 
 * @author pcarion
 */
public class HandlerMove extends HandlerBase {
    HandlerMove(S3Repository repository) {
        super(repository);
    }

    public void process(HttpRequest request, HttpResponse response)
            throws IOException {
        boolean overwrite = request.getOverwrite();
        boolean noContent = false;
        S3UrlName destination = request.getDestination();

        if (destination == null) {
            response.setResponseStatus(HttpResponse.SC_BAD_REQUEST);
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
                noContent = true;
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
        _repository.copy(request.getUrl(), destination);
        _repository.deleteObject(request.getUrl());

        response.setResponseStatus(noContent ? HttpResponse.SC_NO_CONTENT
                : HttpResponse.SC_CREATED);
        
    }
}
