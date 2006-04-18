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
package org.carion.s3.util;

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {
    private final static String DEFAULT_MIME_TYPE = "application/octet-stream";

    private final static Map _mimeTypes = new HashMap();

    static {
        _mimeTypes.put("jpg", "image/jpeg");
        _mimeTypes.put("jpeg", "image/jpeg");
        _mimeTypes.put("png", "image/png");
        _mimeTypes.put("gif", "image/gif");
        _mimeTypes.put("html", "text/html");
        _mimeTypes.put("htm", "text/html");
        _mimeTypes.put("xml", "text/xml");
        _mimeTypes.put("txt", "text/plain");
        _mimeTypes.put("text", "text/plain");
        _mimeTypes.put("asc", "text/plain");
        _mimeTypes.put("sql", "text/plain");
    }

    public static String ext2mimeType(String ext) {
        String mt = DEFAULT_MIME_TYPE;
        if (ext != null) {
            mt = (String) _mimeTypes.get(ext);
            if (mt == null) {
                mt = DEFAULT_MIME_TYPE;
            }
        }
        return mt;
    }
}
