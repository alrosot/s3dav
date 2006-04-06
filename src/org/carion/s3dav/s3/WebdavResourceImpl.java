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
package org.carion.s3dav.s3;

import java.io.IOException;
import java.io.InputStream;

import org.carion.s3dav.repository.WebdavResource;
import org.carion.s3dav.s3.operations.MemoryMappedFile;
import org.carion.s3dav.s3.operations.ObjectDELETE;
import org.carion.s3dav.s3.operations.ObjectGET;
import org.carion.s3dav.s3.operations.ObjectHEAD;
import org.carion.s3dav.s3.operations.ObjectPUT;
import org.carion.s3dav.util.MimeTypes;
import org.carion.s3dav.util.Util;

public class WebdavResourceImpl extends WebdavObjectImpl implements
        WebdavResource {
    WebdavResourceImpl(String uri, Credential credential,
            WebdavRepositoryImpl repository) {
        super(uri, credential, repository);
    }

    WebdavResourceImpl(S3ResourceName name, Credential credential,
            WebdavRepositoryImpl repository) {
        super(name, credential, repository);
    }

    public InputStream getContent() throws IOException {
        String key = _name.getResourceKey();
        ObjectGET ope = new ObjectGET(key, _credential, _repository.getLog());
        if (!ope.execute()) {
            throw new IOException("Can't DELETE:" + key);
        }
        return ope.getInputStream();
    }

    public String getContentType() {
        return MimeTypes.ext2mimeType(_name.getExt());
    }

    public long getLength() throws IOException {
        String key = _name.getResourceKey();
        ObjectHEAD ope = new ObjectHEAD(key, _credential, _repository.getLog());
        if (!ope.execute()) {
            throw new IOException("Can't HEAD:" + key);
        }
        return ope.getContentLength();
    }

    public void setResourceContent(InputStream content, String contentType,
            long length) throws IOException {
        ObjectPUT ope = new ObjectPUT(_name.getResourceKey(), _credential,
                _repository.getLog());

        MemoryMappedFile mappedFile = null;

        try {
            mappedFile = Util.mkMemoryMapFile(content, length);

            if (!ope.execute(mappedFile.getByteBuffer(), contentType)) {
                throw new IOException("Can't PUT:" + _name.getResourceKey());
            }
        } finally {
            if (mappedFile != null) {
                mappedFile.delete();
            }
        }
    }

    public void remove() throws IOException {
        String key = _name.getResourceKey();
        ObjectDELETE ope = new ObjectDELETE(key, _credential, _repository
                .getLog());
        if (!ope.execute()) {
            throw new IOException("Can't DELETE:" + key);
        }
    }
}
