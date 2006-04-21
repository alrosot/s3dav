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

public class ContentLengthInputStream extends InputStream {

    private static int BUFFER_SIZE = 2048;

    /**
     * The maximum number of bytes that can be read from the stream. Subsequent
     * read operations will return -1.
     */
    private final long _contentLength;

    /** The current position */
    private long _pos = 0;

    /** True if the stream is closed. */
    private boolean _closed = false;

    private final boolean _keepAlive;

    /**
     * Wrapped input stream that all calls are delegated to.
     */
    private final InputStream _in;

    private final InputStreamObserver _observer;

    /**
     * Creates a new length limited stream
     *
     * @param in The stream to wrap
     * @param contentLength The maximum number of bytes that can be read from
     * the stream. Subsequent read operations will return -1.
     *
     * @since 3.0
     */
    ContentLengthInputStream(InputStream in, long contentLength,
            boolean keepAlive, InputStreamObserver observer) {
        if (in == null) {
            throw new IllegalArgumentException("Input stream may not be null");
        }
        if (contentLength < 0) {
            throw new IllegalArgumentException(
                    "Content length may not be negative");
        }
        _keepAlive = keepAlive;
        _observer = observer;
        _in = in;
        _contentLength = contentLength;
    }

    /**
     * <p>Reads until the end of the known length of content.</p>
     *
     * <p>Does not close the underlying socket input, but instead leaves it
     * primed to parse the next response.</p>
     * @throws IOException If an IO problem occurs.
     */
    public void close() throws IOException {
        if (!_closed) {
            try {
                byte buffer[] = new byte[BUFFER_SIZE];
                while (read(buffer) >= 0) {
                }
            } finally {
                // close after above so that we don't throw an exception trying
                // to read after closed!
                _closed = true;
                if (!_keepAlive) {
                    _in.close();
                }
                if (_observer != null) {
                    _observer.closeConnection();
                }
            }
        }
    }

    /**
     * Read the next byte from the stream
     * @return The next byte or -1 if the end of stream has been reached.
     * @throws IOException If an IO problem occurs
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException {
        if (_closed) {
            throw new IOException("Attempted read from closed stream.");
        }

        if (_pos >= _contentLength) {
            return -1;
        }
        _pos++;
        return _in.read();
    }

    /**
     * Does standard {@link InputStream#read(byte[], int, int)} behavior, but
     * also notifies the watcher when the contents have been consumed.
     *
     * @param b The byte array to fill.
     * @param off Start filling at this position.
     * @param len The number of bytes to attempt to read.
     * @return The number of bytes read, or -1 if the end of content has been
     * reached.
     *
     * @throws java.io.IOException Should an error occur on the wrapped stream.
     */
    public int read(byte[] b, int off, int len) throws java.io.IOException {
        if (_closed) {
            throw new IOException("Attempted read from closed stream.");
        }

        if (_pos >= _contentLength) {
            return -1;
        }

        if (_pos + len > _contentLength) {
            len = (int) (_contentLength - _pos);
        }
        int count = _in.read(b, off, len);
        _pos += count;
        return count;
    }

    /**
     * Read more bytes from the stream.
     * @param b The byte array to put the new data in.
     * @return The number of bytes read into the buffer.
     * @throws IOException If an IO problem occurs
     * @see java.io.InputStream#read(byte[])
     */
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    /**
     * Skips and discards a number of bytes from the input stream.
     * @param n The number of bytes to skip.
     * @return The actual number of bytes skipped. <= 0 if no bytes
     * are skipped.
     * @throws IOException If an error occurs while skipping bytes.
     * @see InputStream#skip(long)
     */
    public long skip(long n) throws IOException {
        if (n <= 0) {
            return 0;
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        // make sure we don't skip more bytes than are
        // still available
        long remaining = Math.min(n, _contentLength - _pos);
        // skip and keep track of the bytes actually skipped
        long count = 0;
        while (remaining > 0) {
            int l = read(buffer, 0, (int) Math.min(BUFFER_SIZE, remaining));
            if (l == -1) {
                break;
            }
            count += l;
            remaining -= l;
        }
        _pos += count;
        return count;
    }
}