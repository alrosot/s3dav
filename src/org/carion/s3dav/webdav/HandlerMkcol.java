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
 * Handles 'MKCOL' request rfc 2518 8.3 MKCOL creates a new collection resource
 * at the location specified by the Request-URI. If the resource identified by
 * the Request-URI is non-null then the MKCOL MUST fail. During MKCOL
 * processing, a server MUST make the Request-URI a member of its parent
 * collection, unless the Request-URI is "/". If no such ancestor exists, the
 * method MUST fail. When the MKCOL operation creates a new collection resource,
 * all ancestors MUST already exist, or the method MUST fail with a 409
 * (Conflict) status code. For example, if a request to create collection
 * /a/b/c/d/ is made, and neither /a/b/ nor /a/b/c/ exists, the request must
 * fail. When MKCOL is invoked without a request body, the newly created
 * collection SHOULD have no members. A MKCOL request message may contain a
 * message body. The behavior of a MKCOL request when the body is present is
 * limited to creating collections, members of a collection, bodies of members
 * and properties on the collections or members. If the server receives a MKCOL
 * request entity type it does not support or understand it MUST respond with a
 * 415 (Unsupported Media Type) status code. The exact behavior of MKCOL for
 * various request media types is undefined in this document, and will be
 * specified in separate documents.
 * 
 * @author pcarion
 */
public class HandlerMkcol extends HandlerBase {
    HandlerMkcol(S3Repository repository) {
        super(repository);
    }

    public void process(HttpRequest request, HttpResponse response)
            throws IOException {
        S3UrlName uri = request.getUrl();
        
        if (request.hasBody()) {
            response.setResponseStatus(HttpResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        if (_repository.objectExists(uri)) {
            response.setResponseStatus(HttpResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        S3UrlName parent = uri.getParent();
        if (parent != null) {
            if (!_repository.isFolder(parent)) {
                response.setResponseStatus(HttpResponse.SC_CONFLICT);
                return;
            }
        }

        try {
            _repository.createFolder(uri);
        } catch (IOException ex) {
            response.setResponseStatus(HttpResponse.SC_FORBIDDEN);
            return;
        }
        response.setResponseStatus(HttpResponse.SC_CREATED);
    }
}
