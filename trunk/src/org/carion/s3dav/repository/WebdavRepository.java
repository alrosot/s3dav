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

public interface WebdavRepository {
    boolean isAvailable();

    void deleteObject(String uri) throws IOException;

    boolean objectExists(String uri) throws IOException;

    boolean isFolder(String uri) throws IOException;

    boolean isResource(String uri) throws IOException;

    String getParentUri(String uri) throws IOException;

    WebdavFolder createFolder(String uri) throws IOException;

    WebdavResource createResource(String uri) throws IOException;

    WebdavFolder getFolder(String uri) throws IOException;

    WebdavResource getResource(String uri) throws IOException;

    void copy(String uriSource, String uriDestination) throws IOException;
}