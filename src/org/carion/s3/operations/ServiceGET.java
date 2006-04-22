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
package org.carion.s3.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.carion.s3.Credential;
import org.carion.s3.S3Log;
import org.carion.s3.impl.Bucket;
import org.carion.s3.util.BaseXmlParser;
import org.carion.s3.util.Util;

/**
 * The GET operation on the Service endpoint returns a list of
 * all of the buckets owned by the authenticated sender of the request.
 *
 * @author pcarion
 */
public class ServiceGET extends BaseS3Operation {
    private final List _buckets = new ArrayList();

    public ServiceGET(Credential credential, S3Log log) {
        super(credential, log);
    }

    /**
     * Returns the list of buckets
     * @return list of buckets (Bucket)
     * @throws IOException
     */
    public List execute() throws IOException {
        // we want the buckets here
        S3Request X = S3Request.mkGetRequest("/", _log);
        if (!process(X, false)) {
            throw new IOException("Can't get buckets");
        }
        String xmlData = getXmldata();
        S3ResponseParser parser = new S3ResponseParser(xmlData);
        try {
            parser.parse(new Handler());
            return _buckets;
        } catch (Exception ex) {
            throw new IOException("Can't parse buckets:" + ex);
        }
    }

    void addBucket(String name, Date creationdate) {
        _buckets.add(new Bucket(name, creationdate));
    }

    private class Handler extends BaseXmlParser {
        private String _bucket;

        private Date _creationDate;

        protected void processData(String elementName, String fullName,
                String data) {
            if (fullName.endsWith("Bucket.Name")) {
                _bucket = data;
            } else if (fullName.endsWith("Bucket.CreationDate")) {
                _creationDate = Util.parseIsoDate(data);
            }
        }

        protected void processEndElement(String elementName, String fullName) {
            if (elementName.equals("Bucket")) {
                addBucket(_bucket, _creationDate);
            }
        }
    }
}
