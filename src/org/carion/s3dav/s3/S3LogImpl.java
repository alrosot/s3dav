package org.carion.s3dav.s3;

import java.io.PrintStream;

public class S3LogImpl implements S3Log {
    private final PrintStream _out;

    S3LogImpl(PrintStream out) {
        _out = out;
    }

    public void log(String message) {
        _out.println(">S3> " + message);
    }

    public void log(String message, Throwable ex) {
        _out.println(">S3> " + message);
        ex.printStackTrace(_out);
    }

}
