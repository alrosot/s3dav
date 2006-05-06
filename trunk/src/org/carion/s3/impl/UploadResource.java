package org.carion.s3.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.carion.s3.S3Resource;
import org.carion.s3.S3UrlName;
import org.carion.s3.util.MimeTypes;

public class UploadResource implements S3Resource {
    private final S3UrlName _href;

    public UploadResource(S3UrlName href) {
        _href = href;
    }

    public InputStream getContent() throws IOException {
        throw new IOException("Invalid call");
    }

    public String getContentType() {
        return MimeTypes.ext2mimeType(_href.getExt());
    }

    public Date getCreationDate() throws IOException {
        return new Date();
    }

    public Date getLastModified() throws IOException {
        return new Date();
    }

    public long getLength() throws IOException {
        return 1;
    }

    public String getName() {
        return _href.getName();
    }

    public S3UrlName getUrl() {
        return _href;
    }

    public void remove() throws IOException {
        // TODO Auto-generated method stub

    }

    public void setResourceContent(InputStream content, String contentType,
            long length) throws IOException {
        throw new IOException("Invalid call");
    }
}
