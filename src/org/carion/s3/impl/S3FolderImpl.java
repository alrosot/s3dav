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
import java.util.Iterator;
import java.util.List;

import org.carion.s3.Credential;
import org.carion.s3.S3Folder;
import org.carion.s3.S3Resource;
import org.carion.s3.S3UrlName;
import org.carion.s3.operations.BucketGET;
import org.carion.s3.operations.ObjectDELETE;

public class S3FolderImpl extends S3ObjectImpl implements S3Folder {
    S3FolderImpl(S3UrlName uri, Credential credential,
            S3RepositoryImpl repository) {
        super(uri, credential, repository);
    }

    public S3Folder createFolder(String name) throws IOException {
        return _repository.createFolder(_name.getChild(name));
    }

    public S3Resource createResource(String name) throws IOException {
        return _repository.createResource(_name.getChild(name));
    }

    public S3UrlName[] getChildrenUris() throws IOException {
        S3UrlName[] result;

        if (_name.isRoot()) {
            // we want the buckets here
            List buckets = _repository.getBuckets();
            result = new S3UrlName[buckets.size()];
            int index = 0;
            for (Iterator iter = buckets.iterator(); iter.hasNext();) {
                Bucket bucket = (Bucket) iter.next();
                result[index++] = new S3UrlNameImpl(
                        "/" + bucket.getName(), false);
            }
        } else {
            // we want the resources inside a directory
            String bucket = _name.getBucket();
            String prefix = _name.getPrefixKey();
            BucketGET ope = new BucketGET(bucket, _credential, _repository
                    .getLog());

            List objects = ope.execute(prefix);
            result = new S3UrlName[objects.size()];
            
            int count = 0;
            for (Iterator iter = objects.iterator(); iter.hasNext();) {
                Object obj = (Object) iter.next();
                result[count++] = _name.getChild(obj.getName());
            }
        }
        return result;
    }

    public void remove() throws IOException {
        deleteFolder(this);
    }

    private void deleteFolder(S3Folder folder) throws IOException {
        deleteFolderContent(folder);
        ((S3FolderImpl) folder).doRemove();
    }

    private void deleteFolderContent(S3Folder folder) throws IOException {
        S3UrlName[] files = folder.getChildrenUris();

        for (int i = 0; i < files.length; i++) {
            S3UrlName uri = files[i];

            if (_repository.isFolder(uri)) {
                deleteFolder(_repository.getFolder(uri));
            } else {
                S3Resource res = _repository.getResource(uri);
                res.remove();
            }
        }
    }

    void doRemove() throws IOException {
        if (_name.isRoot()) {
            throw new IOException("Can't delete /");
        }

        if (_name.isBucket()) {
            _repository.deleteBucket(getName());
        } else {
            ObjectDELETE ope;
            ope = _repository.mkObjectDELETE(_name.getResourceKey());

            if (!ope.execute()) {
                throw new IOException("Can't delete :" + _name.getResourceKey());
            }
        }
    }
}
