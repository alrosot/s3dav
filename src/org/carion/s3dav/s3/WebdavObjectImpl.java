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
import java.util.Date;

import org.carion.s3dav.repository.WebdavObject;
import org.carion.s3dav.s3.operations.ObjectHEAD;

public abstract class WebdavObjectImpl implements WebdavObject {
    protected final S3ResourceName _name;

    protected final String _s3Uri;

    protected Credential _credential;

    protected final WebdavRepositoryImpl _repository;

    WebdavObjectImpl(String uri, Credential credential,
            WebdavRepositoryImpl repository) {
        this(new S3ResourceName(uri), credential, repository);
    }

    WebdavObjectImpl(S3ResourceName name, Credential credential,
            WebdavRepositoryImpl repository) {
        _name = name;
        _credential = credential;
        _repository = repository;
        if (_name.isRoot()) {
            _s3Uri = "/";
        } else if (_name.isBucket()) {
            _s3Uri = _name.getUri();
        } else {
            _s3Uri = _name.getResourceKey();
        }
    }

    public Date getCreationDate() throws IOException {
        // TODO: do we add the creation date in the
        return getLastModified();
    }

    public Date getLastModified() throws IOException {
        if (_name.isRoot()) {
            // TODO
            return new Date();
        } else if (_name.isBucket()) {
            // TODO
            return new Date();
        } else {
            String key = _name.getResourceKey();
            ObjectHEAD ope = _repository.mkObjectHEAD(key);
            if (!ope.execute()) {
                throw new IOException("Can't get info for:" + key);
            }
            return ope.getLastModifiedDate();
        }
    }

    public String getName() {
        return _name.getName();
    }

    public String getURI() {
        return _name.getUri();
    }
}
