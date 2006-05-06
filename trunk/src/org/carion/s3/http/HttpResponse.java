package org.carion.s3.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.carion.s3.util.XMLWriter;

public class HttpResponse {
    private Map _headers = new LinkedHashMap();

    private XMLWriter _xmlWriter = null;

    private String _responseBody;

    private int _responseStatus = 200;

    private InputStream _content = null;

    public final static int SC_OK = 200;

    public final static int SC_CREATED = 201;

    public final static int SC_NO_CONTENT = 204;

    public final static int SC_MULTI_STATUS = 207;

    public final static int SC_BAD_REQUEST = 400;

    public final static int SC_FORBIDDEN = 403;

    public final static int SC_NOT_FOUND = 404;

    public final static int SC_METHOD_NOT_ALLOWED = 405;

    public final static int SC_CONFLICT = 409;

    public final static int SC_PRECONDITION_FAILED = 412;

    public final static int SC_UNSUPPORTED_MEDIA_TYPE = 415;

    public final static int SC_INTERNAL_ERROR = 500;

    public final static int SC_NOT_IMPLEMENTED = 501;

    /**
     * This map associated the status code and the status message which will be
     * sent in the webdav response
     */
    private final static Map _statusMessages = new HashMap();

    static {
        _statusMessages.put(new Integer(SC_PRECONDITION_FAILED),
                "Precondition Failed");
        _statusMessages.put(new Integer(SC_FORBIDDEN), "Forbidden");
        _statusMessages.put(new Integer(SC_NOT_FOUND), "Not Found");
        _statusMessages.put(new Integer(SC_BAD_REQUEST), "Bad Request");
        _statusMessages.put(new Integer(SC_OK), "OK");
        _statusMessages.put(new Integer(SC_NO_CONTENT), "No Content");
        _statusMessages.put(new Integer(SC_MULTI_STATUS), "Multi-Status");
        _statusMessages.put(new Integer(SC_CREATED), "Created");
        _statusMessages.put(new Integer(SC_INTERNAL_ERROR),
                "Internal Server Error");
        _statusMessages.put(new Integer(SC_METHOD_NOT_ALLOWED),
                "Method Not Allowed");
        _statusMessages.put(new Integer(SC_UNSUPPORTED_MEDIA_TYPE),
                "Unsupported Media Type");
        _statusMessages.put(new Integer(SC_CONFLICT), "Conflict");
        _statusMessages.put(new Integer(SC_NOT_IMPLEMENTED), "Not implemented");
    }

    HttpResponse() {
    }

    public boolean hasContent() {
        return (_content != null);
    }

    public void setResponseHeader(String name, String value) {
        _headers.put(name, value);
    }

    public void setContentType(String contentType) {
        setResponseHeader("Content-Type", contentType);
    }

    public Iterator getHeaders() {
        return _headers.keySet().iterator();
    }

    public String getHeader(String header) {
        return (String) _headers.get(header);
    }

    public void setResponseBody(String data, String contentType) {
        _responseBody = data;
        setContentType(contentType);
    }

    public XMLWriter getXMLWriter(String root) {
        _xmlWriter = new XMLWriter(root, "DAV:");
        return _xmlWriter;
    }

    public void setResponseStatus(int code) {
        _responseStatus = code;
    }

    void finish() {
        if (_xmlWriter != null) {
            setContentType("text/xml;charser=utf-8");
            _xmlWriter.finish();
            String data = _xmlWriter.getData();

            setResponseHeader("Content-Length", String.valueOf(data.length()));
            _content = new ByteArrayInputStream(data.getBytes());
        } else if (_responseBody != null) {
            setResponseHeader("Content-Length", String.valueOf(_responseBody
                    .length()));
            _content = new ByteArrayInputStream(_responseBody.getBytes());
        }

        if (_content == null) {
            setResponseHeader("Content-Length", "0");
        }
    }

    public int getResponseStatus() {
        return _responseStatus;
    }

    public String getStatusMessage() {
        String status = (String) _statusMessages.get(new Integer(
                _responseStatus));
        if (status == null) {
            status = "code=" + _responseStatus;
        }
        return status;
    }

    public void setContentStream(InputStream in) {
        _content = in;
    }

    public InputStream getContentInputStream() {
        return _content;
    }

}
