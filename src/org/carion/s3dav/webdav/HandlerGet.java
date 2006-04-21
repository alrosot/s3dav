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
import org.carion.s3.S3Resource;
import org.carion.s3.S3UrlName;
import org.carion.s3.http.HttpRequest;
import org.carion.s3.http.HttpResponse;
import org.carion.s3.util.Util;

/**
 * Handles 'GET' request. rfc2518: <i>8.4 : The semantics of GET are unchanged
 * when applied to a collection, since GET is defined as, "retrieve whatever
 * information (in the form of an entity) is identified by the Request-URI"
 * [RFC2068]. GET when applied to a collection may return the contents of an
 * "index.html" resource, a human-readable view of the contents of the
 * collection, or something else altogether. Hence it is possible that the
 * result of a GET on a collection will bear no correlation to the membership of
 * the collection.</i>
 * 
 * @author pcarion
 * 
 */
public class HandlerGet extends HandlerBase {
    HandlerGet(S3Repository repository) {
        super(repository);
    }

    public void process(HttpRequest request, HttpResponse response)
            throws IOException {
        S3UrlName href = request.getUrl();

        if (_repository.isAvailable()) {
            boolean exists = _repository.objectExists(href);
            if (exists) {
                boolean isDirectory = _repository.isFolder(href);

                if (isDirectory) {
                    response.setResponseStatus(HttpResponse.SC_FORBIDDEN);
                } else {
                    S3Resource resource = _repository.getResource(href);
                    setHeaders(resource, response);
                    response.setContentStream(resource.getContent());
                }
            } else {
                response.setResponseStatus(HttpResponse.SC_NOT_FOUND);
            }
        } else {
            response.setResponseStatus(HttpResponse.SC_FORBIDDEN);
        }
    }

    /**
     * Set the headers associated to the GET on the resource FYI: this method
     * will be reused in the HEAD request, that's why we have a separate method
     * to set the headers
     * 
     * @param resource
     *            resource to process
     * @param response
     *            response to set
     * @throws IOException
     */
    protected void setHeaders(S3Resource resource, HttpResponse response)
            throws IOException {
        // set HTTP headers
        response.setResponseHeader("last-modified", Util.getHttpDate(resource
                .getLastModified()));

        response.setResponseHeader("Content-Length", String.valueOf(resource
                .getLength()));

        response.setContentType(resource.getContentType());
    }
}
