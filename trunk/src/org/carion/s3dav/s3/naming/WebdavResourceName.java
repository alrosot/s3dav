package org.carion.s3dav.s3.naming;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
public class WebdavResourceName extends BaseResourceName {
    private final String _uri;

    private final String _bucket;

    private final String _name;

    private final boolean _isRoot;

    private final boolean _isBucket;

    private final List _parts = new ArrayList();

    public WebdavResourceName(String uri) {
        uri = uri.trim();

        if (uri.equals("/")) {
            _isRoot = true;
            _isBucket = false;
            _bucket = null;
            _name = null;
            _uri = "/";
        } else {
            _isRoot = false;
            StringTokenizer st = new StringTokenizer(uri, "/", true);
            int countParts = 0;
            String first = null;
            String last = null;
            StringBuffer sb = new StringBuffer();
            while (st.hasMoreElements()) {
                String tk = st.nextToken();
                if ("/".equals(tk)) {
                    _parts.add(tk);
                    sb.append(tk);
                } else {
                    String part = decode(tk);
                    if (countParts == 0) {
                        first = part;
                    }
                    last = part;
                    _parts.add(part);
                    sb.append(part);
                    countParts++;
                }
            }
            _bucket = first;
            _name = last;
            _isBucket = (countParts == 1);
            _uri = sb.toString();
        }
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
}
