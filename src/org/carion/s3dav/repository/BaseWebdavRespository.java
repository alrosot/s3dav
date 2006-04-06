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

public abstract class BaseWebdavRespository implements WebdavRepository {
    public String getParentUri(String uri) throws IOException {
        ResourceName name = new ResourceName(uri);
        return name.getParentUri();
    }

    public void copy(String uriSource, String uriDestination)
            throws IOException {
        if (isFolder(uriSource)) {
            WebdavFolder src = getFolder(uriSource);
            WebdavFolder dest = createFolder(uriDestination);
            copyDirectory(src, dest);
        } else {
            WebdavResource src = getResource(uriSource);
            WebdavResource dest = createResource(uriDestination);
            copyResource(src, dest);
        }
    }

    private void copyDirectory(WebdavFolder src, WebdavFolder dest)
            throws IOException {
        String[] children = src.getChildrenUris();

        for (int i = 0; i < children.length; i++) {
            String uri = children[i];
            if (isFolder(uri)) {
                WebdavFolder s = getFolder(uri);
                WebdavFolder d = dest.createFolder(s.getName());
                copyDirectory(s, d);
            } else {
                WebdavResource s = getResource(uri);
                WebdavResource d = dest.createResource(s.getName());
                copyResource(s, d);
            }
        }
    }

    private void copyResource(WebdavResource src, WebdavResource dest)
            throws IOException {
        dest.setResourceContent(src.getContent(), src.getContentType(), src.getLength());
    }

}
