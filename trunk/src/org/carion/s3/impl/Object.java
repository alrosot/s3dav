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

import java.util.Date;

public class Object {
    private final String _key;

    private final Date _lastModified;

    private final int _size;

    public Object(String key, Date lastModified, int size) {
        _key = key;
        _lastModified = lastModified;
        _size = size;
    }

    public String getKey() {
        return _key;
    }

    // that's the name without the prefix
    public String getName() {
        int pos = _key.lastIndexOf("/");
        if (pos < 0) {
            throw new RuntimeException("bad key name for object:"+_key);
        }
        return _key.substring(pos+1);
    }

    public Date getLastModified() {
        return _lastModified;
    }

    public int getSize() {
        return _size;
    }
}
