package org.carion.s3dav.s3.naming.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.carion.s3dav.s3.naming.S3UrlName;

/**
 *
 * From webDAV server
 * a c
 * >dav> 13/Apr/2006:14:57:24 -0700- Request:MOVE /pierre/New%20Folder HTTP/1.1
 * >dav> Destination header: (http://127.0.0.1:8070/pierre/a%20c)
 *
 * a+c
 * >dav> 13/Apr/2006:14:59:08 -0700- Request:MOVE /pierre/New%20Folder HTTP/1.1
 * >dav> Destination header: (http://127.0.0.1:8070/pierre/a%2Bc)
 *
 * a&c
 * >dav> 13/Apr/2006:15:00:31 -0700- Request:MOVE /pierre/New%20Folder HTTP/1.1
 * >dav> Destination header: (http://127.0.0.1:8070/pierre/a%26c)
 *
 * a%c
 * >dav> 13/Apr/2006:15:01:30 -0700- Request:MOVE /pierre/New%20Folder HTTP/1.1
 * >dav> Destination header: (http://127.0.0.1:8070/pierre/a%25c)
 *
 *
 * @author pcarion
 *
 */
public class WebdavResourceName extends BaseResourceName implements S3UrlName {
    private final String _uri;

    private final String _bucket;

    private final String _name;

    private final boolean _isRoot;

    private final boolean _isBucket;

    private final List _parts;

    public WebdavResourceName(String uri, boolean decode) {
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

    private WebdavResourceName(List parts) {
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
        return new WebdavResourceName(_parts.subList(0, _parts.size() - 1));
    }

    public S3UrlName getChild(String name) {
        List parts = new ArrayList();
        parts.addAll(_parts);
        parts.add(name);
        return new WebdavResourceName(parts);
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

}
