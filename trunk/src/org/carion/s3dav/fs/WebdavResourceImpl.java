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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.carion.s3dav.repository.WebdavResource;
import org.carion.s3dav.util.MimeTypes;

public class WebdavResourceImpl extends WebdavObjectImpl implements
        WebdavResource {
    private static final int BUF_SIZE = 50000;

    WebdavResourceImpl(File file, String uri) {
        super(file, uri);
    }

    public InputStream getContent() throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(_file));
        return in;
    }

    public long getLength() throws IOException {
        return _file.length();
    }

    public void setResourceContent(InputStream is, String contentType, long length)
            throws IOException {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(_file));
        try {
            int read = -1;
            byte[] copyBuffer = new byte[BUF_SIZE];

            while ((read = is.read(copyBuffer, 0, copyBuffer.length)) != -1) {
                os.write(copyBuffer, 0, read);
            }
        } finally {
            try {
                is.close();
            } finally {
                os.close();
            }
        }
    }

    public String getContentType() {
        int dot = getName().lastIndexOf('.');
        if (dot < 0) {
            return "application/octet-stream";
        }

        String ext = getName().substring(dot + 1);
        return MimeTypes.ext2mimeType(ext);
    }

    public void remove() throws IOException {
        boolean success = _file.delete();
        if (!success) {
            throw new IOException("cannot delete object: " + _uri);
        }
    }
}
