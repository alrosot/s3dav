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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import org.carion.s3dav.s3.Credential;
import org.carion.s3dav.s3.S3Log;

/**
 * You fetch objects from S3 using the GET operation.
 * This operation returns the object directly from S3 using
 * a client/server delivery mechanism.
 * If you want to distribute big files to a large number of people,
 * you may find BitTorrent delivery to be preferable since it
 * uses less bandwidth.
 * Please see the section on Using BitTorrent with S3 for details.
 *
 * @author pcarion
 */
public class ObjectGET extends BaseS3Operation {
    private final String _uri;

    HttpURLConnection _conn;

    public ObjectGET(String uri, Credential credential, S3Log log) {
        super(credential, log);
        _uri = uri;
    }

    public boolean execute() throws IOException {
        S3Request X = S3Request.mkGetRequest(_uri);
        return process(X);
    }

    public InputStream getInputStream() {
        return new ContentLengthInputStream(_inputStream, _contentLength);
    }

    public boolean doCloseConnection(HttpURLConnection conn) {
        _conn = conn;
        return false;
    }

    private void closeConnection() {
        if (_conn != null) {
            System.out.println("Connection closed");
            try {
                _conn.disconnect();
            } catch (Exception ex) {
            }
        }
    }

    /**
     * Right now, this class is just a wrapper of the InputStream
     * coming from S3 on a GET request.
     * Eventually, we may want to check the amount of data read
     * to ensure that we don't read more than 'content length' data.
     * It appears that S3 behaves properly and don't send garbage data
     * so we keep this class simple for now.
     *
     * @author pcarion
     */
    private class ContentLengthInputStream extends FilterInputStream {

        ContentLengthInputStream(InputStream in, int contentLength) {
            super(in);
        }

        public int read() throws IOException {
            return in.read();
        }

        public int read(byte[] bts) throws IOException {
            return in.read(bts);
        }

        public int read(byte[] bts, int st, int end) throws IOException {
            return in.read(bts, st, end);
        }

        public long skip(long ln) throws IOException {
            return in.skip(ln);
        }

        public int available() throws IOException {
            return in.available();
        }

        public void close() throws IOException {
            in.close();
            closeConnection();
        }

        public synchronized void mark(int idx) {
            in.mark(idx);
        }

        public synchronized void reset() throws IOException {
            in.reset();
        }

        public boolean markSupported() {
            return in.markSupported();
        }
    }
}
