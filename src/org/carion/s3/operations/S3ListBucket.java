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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class S3ListBucket {
    private String _name;

    private String _prefix;

    private String _marker;

    private int _maxKeys;

    private boolean _isTruncated;

    private final List _contents = new ArrayList();

    void addContent(String key, Date lastModified, int size) {
        _contents.add(new Content(key, lastModified, size));
    }

    List getContents() {
        return _contents;
    }

    Content getLastContent() {
        return (Content) _contents.get(_contents.size() - 1);
    }

    public class Content {
        private final String _key;

        private final Date _lastModified;

        private final int _size;

        Content(String key, Date lastModified, int size) {
            _key = key;
            _lastModified = lastModified;
            _size = size;
        }

        public String getKey() {
            return _key;
        }

        public Date getLastModified() {
            return _lastModified;
        }

        public int getSize() {
            return _size;
        }
    }

    public boolean isTruncated() {
        return _isTruncated;
    }

    public void isTruncated(boolean truncated) {
        _isTruncated = truncated;
    }

    public String getMarker() {
        return _marker;
    }

    public void setMarker(String marker) {
        _marker = marker;
    }

    public int getMaxKeys() {
        return _maxKeys;
    }

    public void setMaxKeys(int keys) {
        _maxKeys = keys;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public String getPrefix() {
        return _prefix;
    }

    public void setPrefix(String prefix) {
        _prefix = prefix;
    }
}
