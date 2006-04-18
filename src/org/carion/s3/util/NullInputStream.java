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