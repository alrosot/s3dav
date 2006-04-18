package org.carion.s3dav.util;

import java.io.IOException;
import java.io.InputStream;

public class WrappedInputStream extends InputStream {

    /** True if the stream is closed. */
    private boolean _closed = false;

    private final boolean _keepAlive;

    /**
     * Wrapped input stream that all calls are delegated to.
     */
    private final InputStream _in;

    private final InputStreamObserver _observer;

    WrappedInputStream(InputStream in, boolean keepAlive,
            InputStreamObserver observer) {
        if (in == null) {
            throw new IllegalArgumentException("Input stream may not be null");
        }
        _keepAlive = keepAlive;
        _observer = observer;
        _in = in;
    }

    public void close() throws IOException {
        if (_keepAlive) {
            return;
        }
        if (!_closed) {
            try {
                _in.close();
            } finally {
                // close after above so that we don't throw an exception trying
                // to read after closed!
                _closed = true;
                if (_observer != null) {
                    _observer.closeConnection();
                }
            }
        }
    }

    public int read() throws IOException {
        if (_closed) {
            throw new IOException("Attempted read from closed stream.");
        }
        int ret = _in.read();
        System.out.print("<" + (char) ret + ">");
        return ret;
    }

    public int read(byte[] b, int off, int len) throws java.io.IOException {
        if (_closed) {
            throw new IOException("Attempted read from closed stream.");
        }
        return _in.read(b, off, len);
    }

    public int read(byte[] b) throws IOException {
        if (_closed) {
            throw new IOException("Attempted read from closed stream.");
        }
        return _in.read(b);
    }

    public long skip(long n) throws IOException {
        return _in.skip(n);
    }
}