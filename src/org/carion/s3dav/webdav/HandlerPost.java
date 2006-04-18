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
import org.carion.s3dav.util.Util;
import org.carion.s3dav.webdav.htmlPages.AdminPage;

/**
 * Handles 'POST' request.
 *
 * @author pcarion
 *
 */
public class HandlerPost extends HandlerBase {
    HandlerPost(WebdavRepository repository) {
        super(repository);
    }

    void process(WebdavRequest request, WebdavResponse response)
            throws IOException {
        S3UrlName href = request.getUrl();

        // catch request to admin pages
        if (href.getUri().startsWith("/index.html")) {
            AdminPage page = new AdminPage(request, _repository);
            String body = page.getHtmlPage();

            response.setResponseHeader("last-modified", Util.getHttpDate());

            response.setResponseBody(body, "text/html");
        } else {
            response.setResponseStatus(WebdavResponse.SC_BAD_REQUEST);
        }
    }

}
