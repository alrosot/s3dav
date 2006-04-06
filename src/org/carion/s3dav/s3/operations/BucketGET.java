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
package org.carion.s3dav.s3.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.carion.s3dav.s3.Credential;
import org.carion.s3dav.s3.Object;
import org.carion.s3dav.s3.S3Log;
import org.carion.s3dav.util.BaseXmlParser;
import org.carion.s3dav.util.Util;

/**
 * A GET request operation using a bucket URI lists information about the objects
 * in the bucket.
 * The number of keys returned in a single request is limited by the server.
 * To list information about all objects in a bucket, you may need to use
 * multiple requests. You can paginate through the list using the
 * marker parameter as described below.
 *
 * @author pcarion
 *
 */
public class BucketGET extends BaseS3Operation {
    private final String _bucket;

    private String _lastKey;

    private final List _objects = new ArrayList();

    public BucketGET(String bucket, Credential credential, S3Log log) {
        super(credential, log);
        _bucket = bucket;
    }

    /**
     * Returns the list of buckets
     * @return list of buckets (Bucket)
     * @throws IOException
     */
    public List execute(String prefix) throws IOException {
        for (String marker = null;;) {
            S3Request X = S3Request.mkGetRequest("/" + _bucket);
            X.setQueryString("prefix="
                    + Util.urlEncode(prefix)
                    + ((marker == null) ? "" : "&marker="
                            + Util.urlEncode(marker)));
            if (!process(X)) {
                throw new IOException("Can't get list fo files");
            }

            S3ResponseParser parser = new S3ResponseParser(_xmlData);
            try {
                Handler handler = new Handler();
                parser.parse(handler);
                if (handler.isTruncated()) {
                    marker = _lastKey;
                } else {
                    break;
                }
            } catch (Exception ex) {
                throw new IOException("Can't parse buckets:" + ex);
            }
        }
        return _objects;
    }

    void addContent(String key, Date lastModified, int size) {
        _lastKey = key;
        _objects.add(new Object(key, lastModified, size));
        System.out.println(">>get>> key=(" + key + ")");
    }

    private class Handler extends BaseXmlParser {
        private String _bucketName;

        private String _prefix;

        private String _marker;

        private int _maxKeys;

        private boolean _isTruncated;

        private String _currentKey;

        private Date _currentLastModified;

        private int _currentSize;

        /**
         * Response Body
         * • Name: The name of the bucket.
         * • Prefix: The prefix you specified in your request, if any.
         * • Marker: The marker you specified in your request, if any.
         * • MaxKeys: The max-keys you specified in your request, if any.
         * • IsTruncated: To aid in pagination, this indicates whether
         *   where may be more objects in the bucket. If true, then there
         *   may be more objects in the bucket and calling GET again using
         *   the last key received as the marker may return more keys.
         *   If false, then the end of the bucket was reached
         *   and you do not need to call GET again.
         *
         * For each object the following information is returned:
         * • key: The object's key.
         * • LastModified: The time that the object was placed into S3.
         * • ETag: The object's entity tag. This is a hash of the object
         *   that can be used to do conditional gets.
         * • Size:The size of the object data in bytes.
         * • StorageClass: This value will always read STANDARD in this release.
         * • Owner: This indicates who put the object into Amazon S3.
         *   The owner is provided only if you are the owner, or if you own the bucket.
         */
        protected void processData(String elementName, String fullName,
                String data) {
            if (fullName.endsWith("ListBucketResult.Name")) {
                _bucketName = data;
            } else if (fullName.endsWith("ListBucketResult.Prefix")) {
                _prefix = data;
            } else if (fullName.endsWith("ListBucketResult.Marker")) {
                _marker = data;
            } else if (fullName.endsWith("ListBucketResult.MaxKeys")) {
                _maxKeys = Integer.parseInt(data);
            } else if (fullName.endsWith("ListBucketResult.IsTruncated")) {
                _isTruncated = data.equalsIgnoreCase("true");
            } else if (fullName.endsWith("Contents.Key")) {
                _currentKey = data;
            } else if (fullName.endsWith("Contents.LastModified")) {
                _currentLastModified = Util.parseIsoDate(data);
            } else if (fullName.endsWith("Contents.Size")) {
                _currentSize = Integer.parseInt(data);
            }
        }

        protected void processEndElement(String elementName, String fullName) {
            if (elementName.equals("Contents")) {
                addContent(_currentKey, _currentLastModified, _currentSize);
            }
        }

        public String getBucketName() {
            return _bucketName;
        }

        public boolean isTruncated() {
            return _isTruncated;
        }

        public String getMarker() {
            return _marker;
        }

        public int getMaxKeys() {
            return _maxKeys;
        }

        public String getPrefix() {
            return _prefix;
        }
    }
}
