package org.carion.s3.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.carion.s3.S3Log;
import org.carion.s3.S3UrlName;
import org.carion.s3.impl.S3UrlNameImpl;
import org.carion.s3.util.Util;

public class HttpRequest {
    private final String _startLine;

    private final String _method;

    private final S3UrlNameImpl _resourceName;

    private final String _queryParameters;

    private final String _protocol;

    private final InetAddress _client;

    private final S3Log _log;

    private InputStream _inputStream;

    private Map _httpHeaders = new LinkedHashMap();

    HttpRequest(String startLine, InetAddress client, S3Log log) {
        _startLine = startLine;
        _client = client;
        _log = log;
        StringTokenizer tokenizer = new StringTokenizer(_startLine);
        _method = tokenizer.nextToken();
        String url = tokenizer.nextToken();
        int queryIndex = url.indexOf('?');
        if (queryIndex > 0) {
            if ((queryIndex + 1) < url.length()) {
                _queryParameters = url.substring(queryIndex + 1);
                url = url.substring(0, queryIndex);
            } else {
                _queryParameters = null;
            }
        } else {
            _queryParameters = null;
        }
        _resourceName = new S3UrlNameImpl(url, true);
        _protocol = tokenizer.nextToken();
    }

    public S3UrlName getUrl() {
        return _resourceName;
    }

    void setInputStream(InputStream inputStream) {
        _inputStream = inputStream;
    }

    public String getStartLine() {
        return _startLine;
    }

    public S3UrlNameImpl getResourceName() {
        return _resourceName;
    }

    public String getMethod() {
        return _method;
    }

    public String getProtocol() {
        return _protocol;
    }

    public InetAddress getClient() {
        return _client;
    }

    String getHeader(String header) {
        return (String) _httpHeaders.get(header);

    }

    boolean getKeepAlive() {
        String keepAliveHeader = getHttpHeader("Connection");
        if (keepAliveHeader.indexOf("Keep-Alive") >= 0) {
            return true;
        } else {
            return false;
        }
    }

    public String getBodyAsString() throws IOException {
        return Util.readInputStreamAsString(_inputStream);
    }

    public boolean hasBody() {
        return getContentLength() > 0;
        /*
        try {
            String body = getBodyAsString();
            if ((body != null) && body.length() > 0) {
                return true;
            }
            return false;
        } catch (IOException ex) {
            return false;
        }
        */
    }

    public InputStream getInputStream() {
        return _inputStream;
    }

    public int getDepth() {
        String depthStr = getHeader("Depth");
        int depth = Integer.MAX_VALUE;
        if (depthStr != null) {
            if (depthStr.equals("0")) {
                depth = 0;
            } else if (depthStr.equals("1")) {
                depth = 1;
            } else if (depthStr.equals("infinity")) {
                depth = Integer.MAX_VALUE;
            }
        }
        return depth;
    }

    public String getHttpHeader(String key) {
        for (Iterator iter = _httpHeaders.keySet().iterator(); iter.hasNext();) {
            String hkey = (String) iter.next();
            if (hkey.equalsIgnoreCase(key)) {
                return (String) _httpHeaders.get(hkey);
            }
        }
        return null;
    }

    public void setHttpHeader(String key, String value) {
        _httpHeaders.put(key, value);
    }

    public int getContentLength() {
        String cl = getHttpHeader("Content-Length");
        if (cl == null) {
            return -1;
        } else {
            return Integer.parseInt(cl);
        }
    }

    public String getContentType() {
        return getHttpHeader("Content-Type");
    }

    public String getHost() {
        return getHttpHeader("Host");
    }

    public boolean getOverwrite() {
        String h = getHttpHeader("Overwrite");
        boolean overwrite;
        if ((h == null) || h.equalsIgnoreCase("T")) {
            overwrite = true;
        } else {
            overwrite = false;
        }
        return overwrite;
    }

    /**
     * Destination is something like this: http://127.0.0.1:8070/pierre/a%26c
     * 
     * @return
     */
    public S3UrlNameImpl getDestination() {
        String destination = getHeader("Destination");

        _log.log("Destination header: (" + destination + ")");
        if (destination == null) {
            return null;
        }

        // let's look for the first single '/' , not preceded by another '/'
        int position = -1;
        for (int i = 0; i < destination.length(); i++) {
            char c = destination.charAt(i);
            if (c == '/') {
                char next = ((i + 1) < destination.length()) ? destination
                        .charAt(i + 1) : ' ';
                if (next != '/') {
                    position = i;
                    break;
                } else {
                    i++;
                }
            }
        }

        if (position < 0) {
            return null;
        }

        destination = destination.substring(position);

        return new S3UrlNameImpl(destination, true);
    }

    public void parseParameters(HashMap parameters) throws IOException {
        if (_queryParameters != null) {
            addParameters(parameters, _queryParameters);
        }

        String contentType = getHttpHeader("Content-Type");
        if ("application/x-www-form-urlencoded".equals(contentType)) {
            addParameters(parameters, getBodyAsString());
        }
    }

    private void addParameters(HashMap parameters, String query) {
        if (query != null) {
            query = query.replace('+', ' ');
            StringTokenizer st = new StringTokenizer(query, "&");
            try {
                while (st.hasMoreTokens()) {
                    String field = st.nextToken();
                    int index = field.indexOf('=');
                    if (index < 0) {
                        addParameter(parameters, URLDecoder.decode(field,
                                "UTF-8"), "");
                    } else {
                        addParameter(parameters, URLDecoder.decode(field
                                .substring(0, index), "UTF-8"), URLDecoder
                                .decode(field.substring(index + 1), "UTF-8"));
                    }
                }
            } catch (UnsupportedEncodingException e) {
            }
        }
    }

    private void addParameter(HashMap parameters, String key, String value) {
        List params = (List) parameters.get(key);
        if (params == null) {
            params = new ArrayList();
        }
        params.add(value);
        parameters.put(key, params);
    }
}
