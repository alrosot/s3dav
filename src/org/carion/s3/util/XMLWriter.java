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

public class XMLWriter {
    protected StringBuffer buffer = new StringBuffer();

    private final String _root;

    public XMLWriter(String root, String xmlns) {
        _root = root;
        buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
        buffer.append("<");
        buffer.append(root);
        buffer.append(" xmlns=\"");
        buffer.append(xmlns);
        buffer.append("\">");
    }

    public String finish() {
        closing(_root);
        return _root;
    }

    public String getData() {
        return buffer.toString();
    }

    public void property(String name, String value) {
        opening(name);
        buffer.append(value);
        closing(name);
    }

    public void property(String name) {
        empty(name);
    }

    public void opening(String name) {
        buffer.append("<");
        buffer.append(name);
        buffer.append(">");
    }

    public void closing(String name) {
        buffer.append("</");
        buffer.append(name);
        buffer.append(">");
    }

    public void empty(String name) {
        buffer.append("<");
        buffer.append(name);
        buffer.append("/>");
    }

    public void writeData(String data) {
        buffer.append("<![CDATA[" + data + "]]>");
    }
}
