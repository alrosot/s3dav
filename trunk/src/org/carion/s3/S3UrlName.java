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
    
    boolean isSameUri(S3UrlName name);
    
    boolean equals(Object o);
}
