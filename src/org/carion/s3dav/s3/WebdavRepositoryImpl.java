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
import java.util.StringTokenizer;

import org.carion.s3dav.log.S3Log;
import org.carion.s3dav.repository.BaseWebdavRespository;
import org.carion.s3dav.repository.ResourceName;
import org.carion.s3dav.repository.WebdavFolder;
import org.carion.s3dav.repository.WebdavResource;
import org.carion.s3dav.s3.operations.BucketDELETE;
import org.carion.s3dav.s3.operations.BucketGET;
import org.carion.s3dav.s3.operations.BucketPUT;
import org.carion.s3dav.s3.operations.ObjectDELETE;
import org.carion.s3dav.s3.operations.ObjectGET;
import org.carion.s3dav.s3.operations.ObjectHEAD;
import org.carion.s3dav.s3.operations.ObjectPUT;
import org.carion.s3dav.s3.operations.ServiceGET;
import org.carion.s3dav.util.Util;

/*

 Let's consider this file system
 /a/f.txt
 /a/b/x1
 /a/b/x2
 /a/b/x3
 /a/b/x4
 /a/b/x5

 If we check the content of /a we will get 6 items !

 /a//f.txt
 /a//b

 /a/b//
 /a/b//x1
 /a/b//x2
 /a/b//x3

 If we check the content of /a// , we get 2 items

 */
public class WebdavRepositoryImpl extends BaseWebdavRespository {
    private Credential _credential;

    private final S3Log _log;

    private final Cache _s3ObjectCache;

    private List _buckets = null;

    private long _bucketsTimeStamp = 0;

    private final static long REFRESH_BUCKETS_DELAY = 10000L;

    public WebdavRepositoryImpl(Credential credential, S3Log log) {
        _credential = credential;
        _log = log;
        _s3ObjectCache = new Cache(32);
    }

    public S3Log getLog() {
        return _log;
    }

    public String getRawLogs() {
        return _log.getRawLogs();
    }

    public boolean isAvailable() {
        return _credential.isAccessAllowed();
    }

    /**
     * This method is called by the HTML admin page when the user
     * enters his account information
     *
     * @param key
     * @param secret
     */
    public void newCredentialInformation(String key, String secret) {
        _credential = CredentialFactory.newCredential(key, secret);
    }

    public void deleteCredential() {
        _credential = CredentialFactory.deleteCredential();
    }

    public String getAccessKey() {
        String key = _credential.getAwsAccessKeyId();
        return (key != null) ? key : "";
    }

    public void deleteObject(String uri) throws IOException {
        if (isFolder(uri)) {
            WebdavFolder folder = getFolder(uri);
            folder.remove();
        } else {
            WebdavResource res = getResource(uri);
            res.remove();
        }
    }

    public boolean objectExists(String uri) throws IOException {
        S3ResourceName name = new S3ResourceName(uri);
        boolean result;
        if (name.isRoot()) {
            result = true;
        } else if (name.isBucket()) {
            result = isBucketName(name.getName());
        } else {
            // that's a regular resource
            ObjectHEAD ope = mkObjectHEAD(name.getResourceKey());

            result = ope.execute();
        }
        return result;
    }

    public boolean isFolder(String uri) throws IOException {
        boolean result;
        S3ResourceName name = new S3ResourceName(uri);
        if (name.isRoot()) {
            result = true;
        } else if (name.isBucket()) {
            result = isBucketName(name.getName());
        } else {
            ObjectHEAD ope = mkObjectHEAD(name.getResourceKey());
            boolean res = ope.execute();
            if (res) {
                result = ope.getMeta("dir") != null;
            } else {
                result = false;
            }
        }
        return result;
    }

    public boolean isResource(String uri) throws IOException {
        boolean result;
        S3ResourceName name = new S3ResourceName(uri);
        if (name.isRoot()) {
            result = false;
        } else if (name.isBucket()) {
            result = false;
        } else {
            ObjectHEAD ope = mkObjectHEAD(name.getResourceKey());
            boolean res = ope.execute();
            if (res) {
                result = ope.getMeta("dir") == null;
            } else {
                result = false;
            }
        }
        return result;
    }

    public WebdavFolder createFolder(String uri) throws IOException {
        WebdavFolder result;
        S3ResourceName name = new S3ResourceName(uri);
        if (name.isRoot()) {
            throw new IOException("Can't create /");
        } else if (name.isBucket()) {
            BucketPUT ope = new BucketPUT(name.getName(), _credential, getLog());
            if (!ope.execute()) {
                throw new IOException("can't create:" + name.getUri());
            }
            result = new WebdavFolderImpl(name, _credential, this);
        } else {
            // that's a 'regular' directory
            ObjectPUT ope = mkObjectPUT(name.getResourceKey());
            ope.addMeta("dir", "true");
            if (!ope.execute()) {
                throw new IOException("can't create:" + name.getUri());
            }
            result = new WebdavFolderImpl(name, _credential, this);
        }
        return result;
    }

    public WebdavResource createResource(String uri) throws IOException {
        WebdavResource result;
        S3ResourceName name = new S3ResourceName(uri);
        if (name.isRoot()) {
            throw new IOException("Can't create /");
        }

        if (name.isBucket()) {
            // OK, we want to create a bucket here
            // bucket are always directories
            throw new IOException("can't create file in /:" + name.getUri());
        }

        ObjectPUT ope = mkObjectPUT(name.getResourceKey());
        if (!ope.execute()) {
            throw new IOException("can't create:" + name.getUri());
        }
        result = new WebdavResourceImpl(name, _credential, this);

        return result;
    }

    public WebdavFolder getFolder(String uri) throws IOException {
        return new WebdavFolderImpl(uri, _credential, this);
    }

    public WebdavResource getResource(String uri) throws IOException {
        return new WebdavResourceImpl(uri, _credential, this);
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
        ObjectHEAD result = null; //_s3ObjectCache.get(uri);
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

}
