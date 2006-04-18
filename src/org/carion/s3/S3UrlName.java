package org.carion.s3;

/**
 * Represents the naming of a resource
 * coming from a web client, such as a webDAV client
 * Those names does not rely on the naming used in
 * the s3DAV file system.
 *
 * @author pcarion
 *
 */
public interface S3UrlName {
    boolean isRoot();

    boolean isBucket();

    String getName();

    String getExt();

    String getBucket();

    String getUri();

    String getUrlEncodedUri();

    S3UrlName getParent();

    S3UrlName getChild(String name);

    String getResourceKey();

    String getPrefixKey();
}
