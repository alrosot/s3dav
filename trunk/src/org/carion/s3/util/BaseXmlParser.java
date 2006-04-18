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
package org.carion.s3.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class BaseXmlParser extends DefaultHandler {
    private final List _elements = new ArrayList();

    private final StringBuffer _data = new StringBuffer();

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (_data.length() > 0) {
            pushData();
        }

        _elements.add(localName);

        processStartElement(localName, getFullName(), attributes);

    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        _data.append(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (_data.length() > 0) {
            pushData();
        }

        processEndElement(localName, getFullName());
        _elements.remove(_elements.size() - 1);
    }

    public void endDocument() throws SAXException {
        if (_data.length() > 0) {
            pushData();
        }

        processEndDocument();
    }

    private String getFullName() {
        StringBuffer sb = new StringBuffer();
        for (Iterator iter = _elements.iterator(); iter.hasNext();) {
            String element = (String) iter.next();
            if (sb.length() > 0) {
                sb.append('.');
            }
            sb.append(element);
        }
        return sb.toString();
    }

    private void pushData() throws SAXException {
        processData((String) _elements.get(_elements.size() - 1),
                getFullName(), _data.toString().trim());
        _data.setLength(0);
    }

    protected void processStartElement(String elementName, String fullName,
            Attributes attributes) {
    }

    protected void processData(String elementName, String fullName, String data) {
    }

    protected void processEndElement(String elementName, String fullName) {
    }

    protected void processEndDocument() {
    }

}
