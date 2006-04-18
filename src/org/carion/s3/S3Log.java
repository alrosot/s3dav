package org.carion.s3;

public interface S3Log {
    void log(String message);
    void log(String message, Throwable ex);
    S3Log getLogger(String prefix);

    // returns a time stamp
    String ts();

    void eol();

    String getRawLogs();
}
