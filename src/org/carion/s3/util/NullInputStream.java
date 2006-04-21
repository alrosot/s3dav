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

import java.io.IOException;
import java.io.InputStream;

public class NullInputStream extends InputStream {
    private final boolean _keepAlive;

    private final long _contentLength;

    NullInputStream(boolean keepAlive, long contentLength) {
        _keepAlive = keepAlive;
        _contentLength = contentLength;
    }

    public int read() throws IOException {
        throw new IOException("Can't wrap InputStream: keepAlive:" + _keepAlive
                + " contentLength:" + _contentLength);
    }

    /// Returns the number of bytes that can be read without blocking.
    // @return the number of available bytes.
    public int available() throws IOException {
        return 0;
    }

}