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
import java.util.Iterator;
import java.util.List;

import org.carion.s3dav.repository.WebdavFolder;
import org.carion.s3dav.repository.WebdavResource;
import org.carion.s3dav.s3.operations.BucketDELETE;
import org.carion.s3dav.s3.operations.BucketGET;
import org.carion.s3dav.s3.operations.ObjectDELETE;
import org.carion.s3dav.s3.operations.ServiceGET;

public class WebdavFolderImpl extends WebdavObjectImpl implements WebdavFolder {
    WebdavFolderImpl(String uri, Credential credential,
            WebdavRepositoryImpl repository) {
        super(uri, credential, repository);
    }

    WebdavFolderImpl(S3ResourceName name, Credential credential,
            WebdavRepositoryImpl repository) {
        super(name, credential, repository);
    }

    public WebdavFolder createFolder(String name) throws IOException {
        return _repository.createFolder(_name.getUri() + "/" + name);
    }

    public WebdavResource createResource(String name) throws IOException {
        return _repository.createResource(_name.getUri() + "/" + name);
    }

    public String[] getChildrenUris() throws IOException {
        String[] result;

        if (_name.isRoot()) {
            // we want the buckets here
            ServiceGET ope = new ServiceGET(_credential, _repository.getLog());
            List buckets = ope.execute();
            result = new String[buckets.size()];
            int index = 0;
            for (Iterator iter = buckets.iterator(); iter.hasNext();) {
                Bucket bucket = (Bucket) iter.next();
                result[index++] = "/" + bucket.getName();
            }
            //result[index] = "/New Bucket";
        } else {
            // we want the resources inside a directory
            String bucket = _name.getBucket();
            String prefix;
            if (_name.isBucket()) {
                prefix = "/";
            } else {
                prefix = _name.getUriWithoutBucket() + "//";
            }
            BucketGET ope = new BucketGET(bucket, _credential, _repository
                    .getLog());

            List objects = ope.execute(prefix);
            result = new String[objects.size()];

            int count = 0;
            for (Iterator iter = objects.iterator(); iter.hasNext();) {
                Object obj = (Object) iter.next();
                result[count++] = _name.getUri() + "/"
                        + obj.getKey().substring(prefix.length());
            }
        }
        return result;
    }

    public void remove() throws IOException {
        deleteFolder(this);
    }

    private void deleteFolder(WebdavFolder folder) throws IOException {
        deleteFolderContent(folder);
        ((WebdavFolderImpl) folder).doRemove();
    }

    private void deleteFolderContent(WebdavFolder folder) throws IOException {
        String[] files = folder.getChildrenUris();

        for (int i = 0; i < files.length; i++) {
            String uri = files[i];

            if (_repository.isFolder(uri)) {
                deleteFolder(_repository.getFolder(uri));
            } else {
                WebdavResource res = _repository.getResource(uri);
                res.remove();
            }
        }
    }

    void doRemove() throws IOException {
        if (_name.isRoot()) {
            throw new IOException("Can't delete /");
        }

        if (_name.isBucket()) {
            BucketDELETE ope = new BucketDELETE(getName(), _credential,
                    _repository.getLog());
            if (!ope.execute()) {
                throw new IOException("Can't delete bucket:" + getName());
            }
        } else {
            ObjectDELETE ope = new ObjectDELETE(_name.getResourceKey(),
                    _credential, _repository.getLog());
            if (!ope.execute()) {
                throw new IOException("Can't delete :" + _name.getResourceKey());
            }
        }
    }
}
