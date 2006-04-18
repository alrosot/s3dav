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
package org.carion.s3dav.repository;

import java.io.IOException;

import org.carion.s3dav.s3.naming.S3UrlName;

public interface WebdavRepository {
    boolean isAvailable();

    S3Log getLog();

    void deleteObject(S3UrlName resource) throws IOException;

    boolean objectExists(S3UrlName resource) throws IOException;

    boolean isFolder(S3UrlName resource) throws IOException;

    boolean isResource(S3UrlName resource) throws IOException;

    //String getParentUri(S3UrlName resource) throws IOException;

    WebdavFolder createFolder(S3UrlName resource) throws IOException;

    WebdavResource createResource(S3UrlName resource) throws IOException;

    WebdavFolder getFolder(S3UrlName resource) throws IOException;

    WebdavResource getResource(S3UrlName resource) throws IOException;

    void copy(S3UrlName source, S3UrlName destination) throws IOException;
}