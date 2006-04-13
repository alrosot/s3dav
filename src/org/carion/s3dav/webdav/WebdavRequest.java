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
package org.carion.s3dav.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.carion.s3dav.util.Util;

/**
 * This class contains all the information describing the
 * incoming webdav request
 *
 * @author pcarion
 */
public class WebdavRequest {
    private final String _startLine;

    private final String _method;

    private final String _url;

    private final String _protocol;

    private final InetAddress _client;

    private InputStream _inputStream;

    private Map _httpHeaders = new LinkedHashMap();

    WebdavRequest(String startLine, InetAddress client) {
        _startLine = startLine;
        _client = client;

        StringTokenizer tokenizer = new StringTokenizer(_startLine);
        _method = tokenizer.nextToken();
        _url = Util.urlDecode(tokenizer.nextToken());
        _protocol = tokenizer.nextToken();
    }

    void setInputStream(InputStream inputStream) {
        _inputStream = inputStream;
    }

    public String getStartLine() {
        return _startLine;
    }

    public String getUrl() {
        return _url;
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
        if ("Keep-Alive".equals(keepAliveHeader)) {
            return true;
        } else {
            return false;
        }
    }

    public String getBodyAsString() throws IOException {
        return Util.readInputStreamAsString(_inputStream);
    }

    public InputStream getInputStream() {
        return _inputStream;
    }

    int getDepth() {
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
        return (String) _httpHeaders.get(key);
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

    public String getDestination() {
        String destination = getHeader("Destination");

        if (destination == null) {
            return null;
        }
        // Remove url encoding from destination
        destination = Util.urlDecode(destination);

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

        return destination;
    }

    public void parseParameters(HashMap parameters) throws IOException {
        int queryIndex = _url.indexOf('?');
        if (queryIndex > 0) {
            if ((queryIndex + 1) < _url.length()) {
                String query = _url.substring(queryIndex + 1);
                addParameters(parameters, query);
            }
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
