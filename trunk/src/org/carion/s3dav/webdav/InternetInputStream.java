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

import java.io.PushbackInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * InputStrean wrapper to read the InputStream associated
 * to the (HTTP) socket input stream
 *
 * @author pcarion
 */
public class InternetInputStream extends PushbackInputStream {

    public InternetInputStream(InputStream in, int size) {
        super(in, size);
    }

    public InternetInputStream(InputStream in) {
        super(in, 4096);
    }

    public String readline() throws IOException {
        StringBuffer buf = readBuffer();
        if (buf == null)
            return null;
        return buf.toString();
    }

    public StringBuffer readBuffer() throws IOException {
        StringBuffer buffer = null;

        int ch = -1;
        while ((ch = read()) >= 0) {
            if (buffer == null) {
                buffer = new StringBuffer();
            }
            if (ch == '\r') {
                ch = read();
                if (ch > 0 && ch != '\n') {
                    unread(ch);
                }
                break;
            } else if (ch == '\n') {
                break;
            }
            buffer.append((char) ch);
        }
        return buffer;
    }

    public String read(int length) throws IOException {
        byte[] data = new byte[length];

        if (read(data, 0, length) < 0) {
            return null;
        }
        return new String(data);
    }
}
