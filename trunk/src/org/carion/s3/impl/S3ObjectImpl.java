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
import java.util.Date;

import org.carion.s3.Credential;
import org.carion.s3.S3Object;
import org.carion.s3.S3UrlName;
import org.carion.s3.operations.ObjectHEAD;

public abstract class S3ObjectImpl implements S3Object {
    protected final S3UrlName _name;

    protected final String _s3Key;

    protected final Credential _credential;

    protected final S3RepositoryImpl _repository;

    //    WebdavObjectImpl(String uri, Credential credential,
    //            WebdavRepositoryImpl repository) {
    //        this(new S3ResourceName(uri), credential, repository);
    //    }

    S3ObjectImpl(S3UrlName name, Credential credential,
            S3RepositoryImpl repository) {
        _name = name;
        _credential = credential;
        _repository = repository;
        if (_name.isRoot()) {
            _s3Key = "/";
        } else if (_name.isBucket()) {
            _s3Key = _name.getUri();
        } else {
            _s3Key = _name.getResourceKey();
        }
    }

    public S3UrlName getUrl() {
        return _name;
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
            ObjectHEAD ope = _repository.mkObjectHEAD(_s3Key);
            if (!ope.execute()) {
                throw new IOException("Can't get info for:" + _s3Key);
            }
            return ope.getLastModifiedDate();
        }
    }

    public String getName() {
        return _name.getName();
    }

    //    public String getURI() {
    //        return _name.getUri();
    //    }
}
