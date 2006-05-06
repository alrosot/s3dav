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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface S3UploadManager {
    void upload(S3UrlName name, InputStream content, String contentType,
            long length) throws IOException;

    List getCurrentUploads();

    void shutdown();
    
    List getUploadsInDirectory(S3UrlName name);

    /**
     * Describe an upload
     */
    public interface Upload {
        public final static int STATE_NOT_STARTED = 0;

        public final static int STATE_STARTED = 1;

        public final static int STATE_FINISHED = 2;

        public final static int STATE_ERROR = -1;

        S3UrlName getName();

        File getStorageFile();

        long getSize();

        long getUploaded();

        int getPercentage();

        int getState();

        void abort();
    }
}
