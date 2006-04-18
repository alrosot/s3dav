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

class S3ErrorImpl implements S3Error {
    private String _code;

    private String _message;

    private String _requestId;

    private String _resource;

    public String getCode() {
        return _code;
    }

    public String getMessage() {
        return _message;
    }

    public String getResource() {
        return _resource;
    }

    public String getRequestId() {
        return _requestId;
    }

    public S3ErrorImpl setCode(String code) {
        _code = code;
        return this;
    }

    public S3ErrorImpl setMessage(String message) {
        _message = message;
        return this;
    }

    public S3ErrorImpl setRequestId(String requestId) {
        _requestId = requestId;
        return this;
    }

    public S3ErrorImpl setResource(String resource) {
        _resource = resource;
        return this;
    }

}