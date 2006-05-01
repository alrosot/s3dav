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
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.carion.s3.Credential;
import org.carion.s3.S3Folder;
import org.carion.s3.S3Log;
import org.carion.s3.S3Repository;
import org.carion.s3.S3Resource;
import org.carion.s3.S3UploadManager;
import org.carion.s3.S3UrlName;
import org.carion.s3.operations.BucketDELETE;
import org.carion.s3.operations.BucketGET;
import org.carion.s3.operations.BucketPUT;
import org.carion.s3.operations.ObjectDELETE;
import org.carion.s3.operations.ObjectGET;
import org.carion.s3.operations.ObjectHEAD;
import org.carion.s3.operations.ObjectPUT;
import org.carion.s3.operations.ServiceGET;
import org.carion.s3.util.Util;

/*
 * 
 * Let's consider this file system /a/f.txt /a/b/x1 /a/b/x2 /a/b/x3 /a/b/x4
 * /a/b/x5
 * 
 * If we check the content of /a we will get 6 items !
 * 
 * /a//f.txt /a//b
 * 
 * /a/b// /a/b//x1 /a/b//x2 /a/b//x3
 * 
 * If we check the content of /a// , we get 2 items
 * 
 */
public class S3RepositoryImpl implements S3Repository {
    private final File _s3DavDirectory;

    private Credential _credential;

    private final S3Log _log;

    private final Cache _s3ObjectCache;

    private final S3UploadManager _uploadManager;

    private List _buckets = null;

    public S3RepositoryImpl(Credential credential, File s3DavDirectory,
            File uploadDirectory, S3Log log) {
        _s3DavDirectory = s3DavDirectory;
        _credential = credential;
        _log = log;
        _s3ObjectCache = new Cache(32);

        _uploadManager = new S3UploadManagerImpl(this, uploadDirectory);
    }

    public S3Log getLog() {
        return _log;
    }

    public boolean isAvailable() {
        return _credential.isAccessAllowed();
    }

    public S3UploadManager getUploadManager() {
        return _uploadManager;
    }

    public Cache getS3Cache() {
        return _s3ObjectCache;
    }

    /**
     * This method is called by the HTML admin page when the user enters his
     * account information
     * 
     * @param key
     * @param secret
     */
    public void newCredentialInformation(String key, String secret) {
        _credential = CredentialFactory.newCredential(_s3DavDirectory, key,
                secret);
    }

    public void deleteCredential() {
        _credential = CredentialFactory.deleteCredential(_s3DavDirectory);
    }

    public String getAccessKey() {
        String key = _credential.getAwsAccessKeyId();
        return (key != null) ? key : "";
    }

    public void deleteObject(S3UrlName resource) throws IOException {
        if (isFolder(resource)) {
            S3Folder folder = getFolder(resource);
            folder.remove();
        } else {
            S3Resource res = getResource(resource);
            res.remove();
        }
    }

    public boolean objectExists(S3UrlName resource) throws IOException {
        boolean result;
        if (resource.isRoot()) {
            result = true;
        } else if (resource.isBucket()) {
            result = isBucketName(resource.getName());
        } else {
            // that's a regular resource
            ObjectHEAD ope = mkObjectHEAD(resource.getResourceKey());

            result = ope.execute();
        }
        return result;
    }

    public boolean isFolder(S3UrlName resource) throws IOException {
        boolean result;
        if (resource.isRoot()) {
            result = true;
        } else if (resource.isBucket()) {
            result = isBucketName(resource.getName());
        } else {
            ObjectHEAD ope = mkObjectHEAD(resource.getResourceKey());
            boolean res = ope.execute();
            if (res) {
                result = ope.getMeta("dir") != null;
            } else {
                result = false;
            }
        }
        return result;
    }

    public boolean isResource(S3UrlName uri) throws IOException {
        boolean result;
        if (uri.isRoot()) {
            result = false;
        } else if (uri.isBucket()) {
            result = false;
        } else {
            ObjectHEAD ope = mkObjectHEAD(uri.getResourceKey());
            boolean res = ope.execute();
            if (res) {
                result = ope.getMeta("dir") == null;
            } else {
                result = false;
            }
        }
        return result;
    }

    public S3Folder createFolder(S3UrlName uri) throws IOException {
        S3Folder result;
        if (uri.isRoot()) {
            throw new IOException("Can't create /");
        } else if (uri.isBucket()) {
            BucketPUT ope = new BucketPUT(uri.getName(), _credential, getLog());
            if (!ope.execute()) {
                throw new IOException("can't create:" + uri.getUri());
            }
            result = new S3FolderImpl(uri, _credential, this);
        } else {
            // that's a 'regular' directory
            ObjectPUT ope = mkObjectPUT(uri.getResourceKey());
            ope.addMeta("dir", "true");
            if (!ope.execute()) {
                throw new IOException("can't create:" + uri.getUri());
            }
            result = new S3FolderImpl(uri, _credential, this);
        }
        return result;
    }

    public S3Resource createResource(S3UrlName uri) throws IOException {
        S3Resource result;
        if (uri.isRoot()) {
            throw new IOException("Can't create /");
        }

        if (uri.isBucket()) {
            // OK, we want to create a bucket here
            // bucket are always directories
            throw new IOException("can't create file in /:" + uri.getUri());
        }

        ObjectPUT ope = mkObjectPUT(uri.getResourceKey());
        if (!ope.execute()) {
            throw new IOException("can't create:" + uri.getUri());
        }
        result = new S3ResourceImpl(uri, _credential, this);

        return result;
    }

    public S3Folder getFolder(S3UrlName uri) throws IOException {
        return new S3FolderImpl(uri, _credential, this);
    }

    public S3Resource getResource(S3UrlName uri) throws IOException {
        return new S3ResourceImpl(uri, _credential, this);
    }

    private boolean isBucketName(String name) throws IOException {
        List buckets = getBuckets();

        for (Iterator iter = buckets.iterator(); iter.hasNext();) {
            Bucket bucket = (Bucket) iter.next();
            if (name.equals(bucket.getName())) {
                return true;
            }
        }
        return false;
    }

    public List getBuckets() throws IOException {
        if (_buckets == null) {
            ServiceGET ope = new ServiceGET(_credential, getLog());
            _buckets = ope.execute();
        }
        return _buckets;
    }

    public void createBucket(String bucketName) throws IOException {
        _buckets = null;
        BucketPUT ope = new BucketPUT(bucketName, _credential, getLog());
        if (!ope.execute()) {
            if (ope.getResponseCode() == 409) {
                throw new IOException("the bucket )" + bucketName
                        + ") you tried to create already exists");
            } else {
                throw new IOException("can't create bucket:" + bucketName + "("
                        + ope.getResponseCode() + ")");
            }
        }
    }

    protected void deleteBucket(String name) throws IOException {
        _buckets = null;
        BucketDELETE ope = new BucketDELETE(name, _credential, _log);
        if (!ope.execute()) {
            throw new IOException("Can't delete bucket:" + name);
        }
    }

    public ObjectDELETE mkObjectDELETE(String uri) {
        _s3ObjectCache.delete(uri);
        return new ObjectDELETE(uri, _credential, _log);
    }

    public ObjectGET mkObjectGET(String uri) {
        _s3ObjectCache.delete(uri);
        return new ObjectGET(uri, _credential, _log);
    }

    public ObjectHEAD mkObjectHEAD(String uri) {
        ObjectHEAD result = _s3ObjectCache.get(uri);
        if (result == null) {
            result = new ObjectHEAD(uri, _credential, _log);
            _s3ObjectCache.put(uri, result);
        } else {
            _log.log("Cache hit for:" + uri);
        }
        return result;
    }

    public ObjectPUT mkObjectPUT(String uri) {
        _s3ObjectCache.delete(uri);
        return new ObjectPUT(uri, _credential, _log);
    }

    public List getRawListing(String bucket) throws IOException {
        BucketGET ope = new BucketGET(bucket, _credential, _log);

        List objects = ope.execute("");
        return objects;
    }

    public void deleteObject(String bucket, String key) throws IOException {
        StringBuffer uri = new StringBuffer();
        uri.append("/");
        uri.append(Util.urlEncode(bucket));
        uri.append("/");
        StringTokenizer st = new StringTokenizer(key, "/", true);
        while (st.hasMoreTokens()) {
            String tk = st.nextToken();
            if ("/".equals(tk)) {
                uri.append(tk);
            } else {
                uri.append(Util.urlEncode(tk));
            }
        }
        _log.log("Delete object bucket=(" + bucket + ") key=(" + key + "):("
                + uri.toString() + ")");
        ObjectDELETE ope = mkObjectDELETE(uri.toString());
        if (!ope.execute()) {
            throw new IOException("Can't delete object:" + uri.toString());
        }
    }

    // public String getParentUri(String uri) throws IOException {
    // when trying to get the parent URI, we consider that the
    // following URI is already encoded, so no need to
    // reencode the parent URI
    // ResourceName name = new ResourceName(uri, false);
    // return name.getParentUri();
    // }

    public void copy(S3UrlName source, S3UrlName destination)
            throws IOException {
        if (isFolder(source)) {
            S3Folder src = getFolder(source);
            S3Folder dest = createFolder(destination);
            copyDirectory(src, dest);
        } else {
            S3Resource src = getResource(source);
            S3Resource dest = createResource(destination);
            copyResource(src, dest);
        }
    }

    private void copyDirectory(S3Folder src, S3Folder dest) throws IOException {
        S3UrlName[] children = src.getChildrenUris();

        _log.log("Copy directory from: (" + src.getUrl().getUri() + ") to ("
                + dest.getUrl().getUri() + ")");

        for (int i = 0; i < children.length; i++) {
            S3UrlName uri = children[i];
            if (isFolder(uri)) {
                S3Folder s = getFolder(uri);
                S3Folder d = dest.createFolder(s.getName());
                copyDirectory(s, d);
            } else {
                S3Resource s = getResource(uri);
                S3Resource d = dest.createResource(s.getName());
                copyResource(s, d);
            }
        }
    }

    private void copyResource(S3Resource src, S3Resource dest)
            throws IOException {
        _log.log("Copy resource from: (" + src.getUrl().getUri() + ") to ("
                + dest.getUrl().getUri() + ")");
        dest.setResourceContent(src.getContent(), src.getContentType(), src
                .getLength());
    }

}
