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

import java.io.IOException;

public interface S3Repository {
    boolean isAvailable();
    
    S3UploadManager getUploadManager();

    S3Log getLog();

    void deleteObject(S3UrlName resource) throws IOException;

    boolean objectExists(S3UrlName resource) throws IOException;

    boolean isFolder(S3UrlName resource) throws IOException;

    boolean isResource(S3UrlName resource) throws IOException;

    S3Folder createFolder(S3UrlName resource) throws IOException;

    S3Resource createResource(S3UrlName resource) throws IOException;

    S3Folder getFolder(S3UrlName resource) throws IOException;

    S3Resource getResource(S3UrlName resource) throws IOException;

    void copy(S3UrlName source, S3UrlName destination) throws IOException;
}