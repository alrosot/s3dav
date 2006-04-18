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
package org.carion.s3.operations;

import java.io.InputStream;

public interface S3Processing {
    void amzError(int responseCode, S3Error error, String amzRequestId,
            String amzId2);

    void amzOk(int responseCode, String amzRequestId, String amzId2);

    void amzException(Exception ex);

    void amzHeader(String name, String value);

    void amzMeta(String name, String value);

    void amzInputStream(InputStream in);
}
