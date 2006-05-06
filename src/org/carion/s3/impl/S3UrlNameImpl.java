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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.carion.s3.S3UrlName;

/**
 * 
 * From webDAV server a c >dav> 13/Apr/2006:14:57:24 -0700- Request:MOVE
 * /pierre/New%20Folder HTTP/1.1 >dav> Destination header:
 * (http://127.0.0.1:8070/pierre/a%20c)
 * 
 * a+c >dav> 13/Apr/2006:14:59:08 -0700- Request:MOVE /pierre/New%20Folder
 * HTTP/1.1 >dav> Destination header: (http://127.0.0.1:8070/pierre/a%2Bc)
 * 
 * a&c >dav> 13/Apr/2006:15:00:31 -0700- Request:MOVE /pierre/New%20Folder
 * HTTP/1.1 >dav> Destination header: (http://127.0.0.1:8070/pierre/a%26c)
 * 
 * a%c >dav> 13/Apr/2006:15:01:30 -0700- Request:MOVE /pierre/New%20Folder
 * HTTP/1.1 >dav> Destination header: (http://127.0.0.1:8070/pierre/a%25c)
 * 
 * 
 * @author pcarion
 * 
 */
public class S3UrlNameImpl implements S3UrlName {
    private final static String ENCODING = "UTF-8";

    private final static char[] HEXADECIMAL = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    private final String _uri;

    private final String _bucket;

    private final String _name;

    private final boolean _isRoot;

    private final boolean _isBucket;

    private final List _parts;

    public S3UrlNameImpl(String uri, boolean decode) {
        _parts = new ArrayList();
        uri = uri.trim();

        if (uri.equals("/")) {
            _isRoot = true;
            _isBucket = false;
            _bucket = null;
            _name = null;
            _uri = "/";
        } else {
            _isRoot = false;
            StringTokenizer st = new StringTokenizer(uri, "/");
            int countParts = 0;
            String first = null;
            String last = null;
            StringBuffer sb = new StringBuffer();
            while (st.hasMoreElements()) {
                String tk = st.nextToken();
                String part;

                if (decode) {
                    part = decode(tk);
                } else {
                    part = tk;
                }
                if (countParts == 0) {
                    first = part;
                }
                last = part;
                _parts.add(part);
                sb.append("/");
                sb.append(part);
                countParts++;
            }
            _bucket = first;
            _name = last;
            _isBucket = (countParts == 1);
            _uri = sb.toString();
        }
    }

    private S3UrlNameImpl(List parts) {
        _parts = parts;

        if (_parts.size() == 0) {
            _isRoot = true;
            _isBucket = false;
            _bucket = null;
            _name = null;
        } else {
            _isRoot = false;
            if (_parts.size() == 1) {
                _isBucket = true;
            } else {
                _isBucket = false;
            }
            _bucket = (String) parts.get(0);
            _name = (String) parts.get(parts.size() - 1);
        }
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = _parts.iterator(); iter.hasNext();) {
            String tk = (String) iter.next();
            sb.append("/");
            sb.append(tk);
        }
        _uri = sb.toString();
    }

    public boolean isRoot() {
        return _isRoot;
    }

    public boolean isBucket() {
        return _isBucket;
    }

    public String getName() {
        return _name;
    }

    public String getBucket() {
        return _bucket;
    }

    public String getUri() {
        return _uri;
    }

    public String getUrlEncodedUri() {
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = _parts.iterator(); iter.hasNext();) {
            String part = (String) iter.next();
            sb.append("/");
            sb.append(encode(part, false));
        }
        return sb.toString();
    }

    public S3UrlName getParent() {
        if (_isRoot) {
            return null;
        }
        return new S3UrlNameImpl(_parts.subList(0, _parts.size() - 1));
    }

    public S3UrlName getChild(String name) {
        List parts = new ArrayList();
        parts.addAll(_parts);
        parts.add(name);
        return new S3UrlNameImpl(parts);
    }

    public String getExt() {
        return getExt(getName());
    }

    public String getPrefixKey() {
        if (_isRoot) {
            throw new IllegalArgumentException();
        }
        if (_isBucket) {
            return "/";
        }
        StringBuffer sb = new StringBuffer();
        int count = 0;
        for (Iterator iter = _parts.iterator(); iter.hasNext(); count++) {
            String part = (String) iter.next();
            if (count > 0) {
                if (count > 1) {
                    sb.append("/");
                }
                sb.append(encode(part, true));
            }
        }
        sb.append("//");
        return sb.toString();
    }

    public String getResourceKey() {
        if (_isRoot || _isBucket) {
            throw new IllegalArgumentException();
        }
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = _parts.iterator(); iter.hasNext();) {
            String part = (String) iter.next();
            if (iter.hasNext()) {
                sb.append("/");
                sb.append(encode(part, true));
            } else {
                sb.append("//");
                sb.append(encode(part, true));
            }
        }
        return sb.toString();
    }

    String decode(String s) {
        if (s.length() == 0) {
            return "";
        }
        byte[] bytes = null;

        int len = s.length();
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                buf.append(c);
            } else if (c == '+') {
                // + is always %xx from the webDAV server
                // so it's safe to assume that + should always
                // be considered as a ' '
                buf.append(' ');
            } else if (c == '%') {
                // we try to process all the %xx which could be there
                try {
                    if (bytes == null) {
                        // highly sufficient to store the decoded bytes
                        bytes = new byte[len];
                    }
                    int nbBytes = 0;

                    while (((i + 2) < len) && (c == '%')) {
                        bytes[nbBytes++] = (byte) Integer.parseInt(s.substring(
                                i + 1, i + 3), 16);
                        // let's go beyond the latest char in the %xx
                        i += 3;
                        if (i < len) {
                            c = s.charAt(i);
                        }
                    }
                    if ((i < len) && (c == '%')) {
                        throw new IllegalArgumentException(
                                "URLDecoder: Incomplete trailing escape (%) pattern");
                    }
                    buf.append(new String(bytes, 0, nbBytes, ENCODING));
                    i--; // to restart the outer loop at the right position
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "URLDecoder: Illegal hex characters in escape (%) pattern - "
                                    + e.getMessage());
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException("Unsupported encoding ("
                            + ENCODING + ")");
                }
            } else {
                // we consider that any other character is safe
                buf.append(c);
            }
        }
        return buf.toString();
    }

    String encode(String s, boolean plusForSpace) {
        if (s.length() == 0) {
            return "";
        }
        int len = s.length();
        StringBuffer buf = new StringBuffer();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(bos, ENCODING);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding (" + ENCODING
                    + ")");
        }

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                buf.append(c);
            } else if (c == ' ') {
                if (plusForSpace) {
                    buf.append('+');
                } else {
                    buf.append("%20");
                }
            } else {
                // convert to external encoding before hex conversion
                try {
                    writer.write((char) c);
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                byte[] ba = bos.toByteArray();
                for (int j = 0; j < ba.length; j++) {
                    // Converting each byte in the buffer
                    byte toEncode = ba[j];
                    buf.append('%');
                    int low = (int) (toEncode & 0x0f);
                    int high = (int) ((toEncode & 0xf0) >> 4);
                    buf.append(HEXADECIMAL[high]);
                    buf.append(HEXADECIMAL[low]);
                }
                bos.reset();
            }
        }
        return buf.toString();
    }

    protected String getExt(String name) {
        String ext = null;
        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            ext = name.substring(dot + 1);
        }
        return ext;
    }

    public boolean equals(Object obj) {
        if (obj instanceof S3UrlName) {
            S3UrlName name2 = (S3UrlName) obj;
            return getUri().equals(name2.getUri());
        } else {
            return false;
        }
    }
    
    public boolean isSameUri(S3UrlName name) {
        if (name == null) {
            return false;
        }
        return getUri().equals(name.getUri());
    }

}
