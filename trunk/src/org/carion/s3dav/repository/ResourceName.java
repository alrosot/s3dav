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
package org.carion.s3dav.repository;

import java.util.StringTokenizer;

import org.carion.s3dav.util.Util;

public class ResourceName {
    private final String _uri;

    private final String _bucket;

    private final String _name;

    private final String _parentName;

    private final boolean _isRoot;

    public ResourceName(String uri) {
        uri = uri.trim();

        if (uri.equals("/")) {
            _isRoot = true;
            _bucket = null;
            _name = null;
            _parentName = null;
            _uri = "/";
        } else {
            _isRoot = false;
            StringTokenizer st = new StringTokenizer(uri.trim(), "/");
            StringBuffer sb = new StringBuffer();
            StringBuffer sb2 = new StringBuffer();
            String previous = null;
            String part = null;
            String first = null;
            while (st.hasMoreElements()) {
                part = encode(st.nextToken().trim());
                sb.append("/");
                sb.append(part);
                if (previous != null) {
                    sb2.append("/");
                    sb2.append(previous);
                }
                if (first == null) {
                    first = part;
                }
                previous = part;
            }
            _uri = sb.toString();
            _parentName = sb2.toString().equals("") ? "/" : sb2.toString();
            _name = part;
            _bucket = first;
        }

    }

    public boolean isRoot() {
        return _isRoot;
    }

    public String getBucket() {
        return _bucket;
    }

    public String getName() {
        return _name;
    }

    public String getParentUri() {
        return _parentName;
    }

    public String getUri() {
        return _uri;
    }

    public String getExt() {
        String ext = null;
        int dot = getName().lastIndexOf('.');
        if (dot >= 0) {
            ext = getName().substring(dot + 1);
        }
        return ext;

    }

    private String encode(String name) {
        return Util.urlEncode(name);
    }
}
