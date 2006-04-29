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
package org.carion.s3.impl;

import java.io.IOException;
import java.io.InputStream;

import org.carion.s3.Credential;
import org.carion.s3.S3Resource;
import org.carion.s3.S3UploadManager;
import org.carion.s3.S3UrlName;
import org.carion.s3.operations.ObjectDELETE;
import org.carion.s3.operations.ObjectGET;
import org.carion.s3.operations.ObjectHEAD;
import org.carion.s3.util.MimeTypes;

public class S3ResourceImpl extends S3ObjectImpl implements S3Resource {

    S3ResourceImpl(S3UrlName name, Credential credential,
            S3RepositoryImpl repository) {
        super(name, credential, repository);
    }

    public InputStream getContent() throws IOException {
        String key = _name.getResourceKey();
        ObjectGET ope;
        ope = _repository.mkObjectGET(key);

        if (!ope.execute()) {
            throw new IOException("Can't GET:" + key);
        }
        return ope.getInputStream();
    }

    public String getContentType() {
        return MimeTypes.ext2mimeType(_name.getExt());
    }

    public long getLength() throws IOException {
        String key = _name.getResourceKey();
        ObjectHEAD ope = _repository.mkObjectHEAD(key);
        if (!ope.execute()) {
            throw new IOException("Can't HEAD:" + key);
        }
        return ope.getContentLength();
    }

    public void setResourceContent(InputStream content, String contentType,
            long length) throws IOException {
        S3UploadManager uploadManager = _repository.getUploadManager();

        uploadManager.upload(_name, content, contentType, length);
    }

    public void remove() throws IOException {
        String key = _name.getResourceKey();
        ObjectDELETE ope;
        ope = _repository.mkObjectDELETE(key);

        if (!ope.execute()) {
            throw new IOException("Can't DELETE:" + key);
        }
    }
}
