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

import java.io.IOException;
import java.io.StringReader;

import org.carion.s3.util.BaseXmlParser;
import org.carion.s3.util.Util;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class S3ResponseParser {
    private final InputSource _inputSource;

    S3ResponseParser(String xml) {
        _inputSource = new InputSource(new StringReader(xml));
    }

    S3Error parseError() throws IOException, SAXException {
        S3ErrorImpl error = new S3ErrorImpl();
        XMLReader xr = Util.createXMLReader();
        ErrorHandler handler = new ErrorHandler(error);
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        xr.parse(_inputSource);
        return error;
    }

    void parse(BaseXmlParser handler) throws IOException, SAXException {
        XMLReader xr = Util.createXMLReader();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        xr.parse(_inputSource);
    }

    private class ErrorHandler extends BaseXmlParser {
        private final S3ErrorImpl _error;

        ErrorHandler(S3ErrorImpl error) {
            _error = error;
        }

        protected void processData(String elementName, String fullName,
                String data) {
            if (elementName.equals("Code")) {
                _error.setCode(data);
            } else if (elementName.equals("Message")) {
                _error.setMessage(data);
            } else if (elementName.equals("Resource")) {
                _error.setResource(data);
            } else if (elementName.equals("RequestId")) {
                _error.setRequestId(data);
            }
        }
    }

}
