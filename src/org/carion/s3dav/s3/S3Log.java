package org.carion.s3dav.s3;

public interface S3Log {
    void log(String message);
    void log(String message, Throwable ex);
}
