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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.carion.s3dav.repository.WebdavFolder;
import org.carion.s3dav.repository.WebdavResource;

public class WebdavFolderImpl extends WebdavObjectImpl implements WebdavFolder {
    WebdavFolderImpl(File file, String uri) {
        super(file, uri);
    }

    public WebdavFolder createFolder(String name) throws IOException {
        String uri = _uri + name;
        File file = new File(_file, name);
        if (!file.mkdir()) {
            throw new IOException("cannot create folder: " + uri);
        }
        return new WebdavFolderImpl(file, uri);
    }

    public WebdavResource createResource(String name) throws IOException {
        String uri = _uri + "/" + name;
        File file = new File(_file, name);
        if (!file.createNewFile()) {
            throw new IOException("cannot create file: " + uri);
        }
        return new WebdavResourceImpl(file, uri);

    }

    public String[] getChildrenUris() throws IOException {
        String uriBase = _uri;
        if (!_uri.endsWith("/")) {
            uriBase = _uri + "/";
        }
        File[] children = _file.listFiles();
        List childList = new ArrayList();
        for (int i = 0; i < children.length; i++) {
            String name = children[i].getName();
            childList.add(uriBase + name);
        }
        Collections.sort(childList);
        String[] childrenNames = new String[childList.size()];
        childrenNames = (String[]) childList.toArray(childrenNames);
        return childrenNames;
    }

    public void remove() throws IOException {
        deleteDirectory(_file);
    }

    private void deleteDirectory(File file) throws IOException {
        deleteDirectoryContent(file);
        if (!file.delete()) {
            throw new IOException("can't delete directory:" + file);
        }
    }

    private void deleteDirectoryContent(File file) throws IOException {
        File[] files = file.listFiles();
        if (files == null) { // null if security restricted
            throw new IOException("Failed to list contents of " + file);
        }

        for (int i = 0; i < files.length; i++) {
            File aFile = files[i];

            if (aFile.isDirectory()) {
                deleteDirectory(aFile);
            } else {
                if (!aFile.delete()) {
                    throw new IOException("can't delete directory:" + aFile);
                }
            }
        }
    }

}
