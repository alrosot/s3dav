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
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.carion.s3.Credential;
import org.carion.s3.S3Log;
import org.carion.s3.util.Util;

abstract public class BaseS3Operation implements S3Processing {
    private final Credential _credential;

    protected final S3Log _log;

    private InputStream _inputStream;

    private final Map _meta = new HashMap();

    private final Map _headers = new HashMap();

    private final Map _metaToAddInRequest = new HashMap();

    private int _responseCode;

    String _request;

    BaseS3Operation(Credential credential, S3Log log) {
        _credential = credential;
        _log = log;
        _responseCode = -1;
    }

    public void amzError(int responseCode, S3Error error, String amzRequestId,
            String amzId2) {
        _responseCode = responseCode;
        _log.log("ERROR: code=" + responseCode + " amzRequestId="
                + amzRequestId + ", amxId2=" + amzId2);
    }

    public void amzOk(int responseCode, String amzRequestId, String amzId2) {
        _responseCode = responseCode;
        _log.log("OK {" + responseCode + "," + getContentLength() + "}");
    }

    public void amzException(Exception ex) {
        _log.log("ERROR: exception", ex);
    }

    public void amzHeader(String name, String value) {
        _headers.put(name, value);
    }

    public void amzMeta(String name, String value) {
        _meta.put(name, value);
    }

    public void amzInputStream(InputStream in) {
        _inputStream = in;
    }

    public void addMeta(String key, String value) {
        _metaToAddInRequest.put(key, value);
    }

    protected boolean process(S3Request s3Request) {
        return process(s3Request, true);
    }

    protected boolean process(S3Request s3Request, boolean doCloseConnection) {
        _request = "Request:" + s3Request.getMethod() + " "
                + s3Request.getPath() + " " + s3Request.getQueryString();
        _log.log("Request:" + s3Request.getMethod() + " " + s3Request.getPath()
                + " " + s3Request.getQueryString());
        for (Iterator iter = _metaToAddInRequest.keySet().iterator(); iter
                .hasNext();) {
            String key = (String) iter.next();
            String value = (String) _metaToAddInRequest.get(key);
            s3Request.addMetaInformation(key, value);
        }
        return s3Request.process(_credential, this, doCloseConnection);
    }

    public String getMeta(String meta) {
        return (String) _meta.get(meta);
    }

    public String getHeader(String header) {
        return (String) _headers.get(header);
    }

    public Date getLastModifiedDate() {
        String v = getHeader("Last-Modified");
        if (v == null) {
            return null;
        }
        return Util.parseHttpdate(v);
    }

    public long getContentLength() {
        String v = getHeader("Content-Length");
        if (v == null) {
            return 0;
        }
        return Long.parseLong(v);
    }

    public int getResponseCode() {
        return _responseCode;
    }

    public InputStream getInputStream() {
        return _inputStream;
    }

    /**
     * Get the body as a string
     * We can then close the InputStream
     * @return
     * @throws IOException
     */
    protected String getXmldata() throws IOException {
        String xmlData = Util.readInputStreamAsString(_inputStream);
        _log.log("xmlData is:(" + xmlData + ")");
        _inputStream.close();
        return xmlData;
    }

}
