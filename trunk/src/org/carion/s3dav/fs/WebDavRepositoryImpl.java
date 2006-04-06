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
package org.carion.s3dav.fs;

import java.io.File;
import java.io.IOException;

import org.carion.s3dav.repository.BaseWebdavRespository;
import org.carion.s3dav.repository.WebdavFolder;
import org.carion.s3dav.repository.WebdavResource;

public class WebDavRepositoryImpl extends BaseWebdavRespository {

    private final File _root;

    public WebDavRepositoryImpl(File root) {
        _root = root;
    }

    public boolean isAvailable() {
        return true;
    }

    public void deleteObject(String uri) throws IOException {
        if (!objectExists(uri)) {
            throw new IOException("Object does not exist:" + uri);
        }
        File file = uriToFile(uri);
        if (file.isDirectory()) {
            WebdavFolderImpl folder = new WebdavFolderImpl(file, uri);
            folder.remove();
        } else {
            WebdavResourceImpl res = new WebdavResourceImpl(file, uri);
            res.remove();
        }
    }

    public boolean objectExists(String uri) throws IOException {
        File file = uriToFile(uri);
        return file.exists();
    }

    public boolean isFolder(String uri) throws IOException {
        File file = uriToFile(uri);
        return file.isDirectory();
    }

    public boolean isResource(String uri) throws IOException {
        File file = uriToFile(uri);
        return file.isFile();
    }

    public WebdavFolder getFolder(String uri) throws IOException {
        File file = uriToFile(uri);
        if (!file.isDirectory()) {
            throw new IOException("URI is not a directory:" + uri);
        }
        return new WebdavFolderImpl(file, uri);
    }

    public WebdavResource getResource(String uri) throws IOException {
        File file = uriToFile(uri);
        if (!file.isFile()) {
            throw new IOException("URI is not a file:" + uri);
        }
        return new WebdavResourceImpl(file, uri);
    }

    public WebdavFolder createFolder(String uri) throws IOException {
        File file = uriToFile(uri);
        if (!file.mkdir()) {
            throw new IOException("cannot create folder: " + uri);
        }
        return new WebdavFolderImpl(file, uri);
    }

    public WebdavResource createResource(String uri) throws IOException {
        File file = uriToFile(uri);
        if (!file.createNewFile()) {
            throw new IOException("cannot create file: " + uri);
        }
        return new WebdavResourceImpl(file, uri);
    }

    private File uriToFile(String uri) {
        // we need to get rid of the first part of the path
        int pos = uri.indexOf('/', 1);
        String fName;
        if (pos < 0) {
            fName = "";
        } else {
            fName = uri.substring(pos + 1);
        }
        File file = new File(_root, fName);
        return file;
    }
}
