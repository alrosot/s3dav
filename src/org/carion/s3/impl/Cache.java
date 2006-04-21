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

import java.util.LinkedHashMap;
import java.util.Map;

import org.carion.s3.operations.ObjectHEAD;

/**
 * A LRU cache using a LinkedHashMap see:
 * http://www.source-code.biz/snippets/java/6.htm or
 * http://javaalmanac.com/egs/java.util/coll_Cache.html
 */
public class Cache {
    private final int _size;

    private final LinkedHashMap _map;

    Cache(int size) {
        _size = size;
        float hashTableLoadFactor = 0.75f;
        int hashTableCapacity = (int) Math.ceil(_size / hashTableLoadFactor) + 1;

        _map = new LinkedHashMap(hashTableCapacity, hashTableLoadFactor, true) {
            // This method is called just after a new entry has been added
            public boolean removeEldestEntry(Map.Entry eldest) {
                return size() > _size;
            }
        };
    }

    synchronized ObjectHEAD get(String uri) {
        return (ObjectHEAD) _map.get(uri);
    }

    synchronized void put(String uri, ObjectHEAD head) {
        _map.put(uri, head);
    }

    synchronized void delete(String uri) {
        _map.remove(uri);
    }
}
