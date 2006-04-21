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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.carion.s3.S3UploadManager;
import org.carion.s3.S3UrlName;
import org.carion.s3.operations.ObjectPUT;

public class S3UploadManagerImpl implements S3UploadManager {
    private final int SIZE_LIMIT_ASYNCHRONOUS = 1024 * 1024;

    private final S3RepositoryImpl _repository;

    private final File _baseDirectory;

    private final List _uploads = new ArrayList();

    public S3UploadManagerImpl(S3RepositoryImpl repository, File baseDirectory) {
        _repository = repository;
        _baseDirectory = baseDirectory;
    }

    public void upload(S3UrlName name, InputStream content, String contentType,
            long length) throws IOException {
        ObjectPUT ope = _repository.mkObjectPUT(name.getResourceKey());

        S3UploadImpl upload = new S3UploadImpl(name, _baseDirectory, _repository.getS3Cache(), this);

        try {
            upload.loadContent(content, length);

            if (length > SIZE_LIMIT_ASYNCHRONOUS) {
                _uploads.add(upload);
                upload.asynchronousUpload(ope, contentType, _repository
                        .getLog());
            } else {
                try {
                    if (!ope.execute(upload.getByteBuffer(), contentType)) {
                        throw new IOException("Can't PUT:"
                                + name.getResourceKey());
                    }
                } finally {
                    upload.close();
                }
            }
        } finally {
            content.close();
        }
    }

    public List getCurrentUploads() {
        return _uploads;
    }

    public void shutdown() {
        for (Iterator iter = _uploads.iterator(); iter.hasNext();) {
            S3UploadImpl upload = (S3UploadImpl) iter.next();
            upload.abort();
        }
    }

    void uploadDone(S3UploadImpl upload, int state) {
        if (state == S3UploadManager.Upload.STATE_FINISHED) {
            _uploads.remove(upload);
            upload.close();
        }
    }
}
