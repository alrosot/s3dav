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
package org.carion.s3dav.webdav;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.carion.s3dav.repository.WebdavFolder;
import org.carion.s3dav.repository.WebdavObject;
import org.carion.s3dav.repository.WebdavRepository;
import org.carion.s3dav.repository.WebdavResource;
import org.carion.s3dav.util.BaseXmlParser;
import org.carion.s3dav.util.Util;
import org.carion.s3dav.util.XMLWriter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class HandlerPropfind extends HandlerBase {
    private final int MODE_NAMED_PROPERTIES = 1;

    private final int MODE_ALL_PROPERTIES = 2;

    private final int MODE_ALL_PROPERTY_NAMES = 3;

    private int _mode;

    private List _properties = null;

    private final static String STATUS_OK = "HTTP/1.1 200 OK";

    private final static String STATUS_NOT_OK = "HTTP/1.1 404 Not Found";

    private final static String[] RESOURCE_PROPERTIES = { "creationdate",
            "displayName", "getlastmodified", "getcontentlength",
            "resourcetype", "getcontenttype" };

    private final static String[] FOLDER_PROPERTIES = { "creationdate",
            "displayName", "getlastmodified", "resourcetype" };

    HandlerPropfind(WebdavRepository repository) {
        super(repository);
    }

    void process(WebdavRequest request, WebdavResponse response)
            throws IOException {
        String body = request.getBodyAsString();

        if ((body != null) && (body.length() > 0)) {
            try {
                parsePropfind(body);
            } catch (SAXException ex) {
                ex.printStackTrace();
                throw new IOException(ex.getMessage());
            }
        } else {
            _mode = MODE_ALL_PROPERTIES;
        }
        String url = request.getUrl();
        if (!_repository.objectExists(url)) {
            response.setResponseStatus(WebdavResponse.SC_NOT_FOUND);
        } else {
            XMLWriter writer = response.getXMLWriter("multistatus");
            process(writer, request.getDepth(), url);
            response.setResponseStatus(WebdavResponse.SC_MULTI_STATUS);
        }
    }

    private void process(XMLWriter writer, int depth, String href)
            throws IOException {
        href = normalize(href);
        boolean isFolder = _repository.isFolder(href);
        WebdavFolder folder = null;

        writer.opening("response");
        writer.property("href", href);

        if (isFolder) {
            folder = _repository.getFolder(href);
            writeResourceProperties(writer, folder);
        } else {
            WebdavResource resource = _repository.getResource(href);
            writeResourceProperties(writer, resource);
        }
        writer.closing("response");

        if (isFolder && (depth > 0)) {
            String[] uris = folder.getChildrenUris();
            for (int i = 0; i < uris.length; i++) {
                String uri = uris[i];
                process(writer, depth - 1, uri);
            }
        }
    }

    private void writeResourceProperties(XMLWriter writer, WebdavObject object)
            throws IOException {
        WebdavResource resource = null;
        WebdavFolder folder = null;
        String[] objectProperties;

        if (object instanceof WebdavResource) {
            resource = (WebdavResource) object;
            objectProperties = RESOURCE_PROPERTIES;
        } else if (object instanceof WebdavFolder) {
            folder = (WebdavFolder) object;
            objectProperties = FOLDER_PROPERTIES;
        } else {
            throw new IOException("internal error");
        }

        switch (_mode) {
        case MODE_NAMED_PROPERTIES:
            writer.opening("propstat");
            writer.opening("prop");
            List properties404 = new ArrayList();
            for (Iterator iter = _properties.iterator(); iter.hasNext();) {
                String property = (String) iter.next();
                if (resource != null) {
                    if (!writeResourceProperty(writer, property, resource)) {
                        properties404.add(property);
                    }
                } else {
                    if (!writeResourceProperty(writer, property, folder)) {
                        properties404.add(property);
                    }
                }
            }
            writer.closing("prop");
            writer.property("status", STATUS_OK);
            writer.closing("propstat");

            // Unknown properties now
            writer.opening("propstat");
            writer.opening("prop");
            for (Iterator iter = properties404.iterator(); iter.hasNext();) {
                String property = (String) iter.next();
                writer.empty(property);
            }
            writer.closing("prop");
            writer.property("status", STATUS_NOT_OK);
            writer.closing("propstat");
            break;

        case MODE_ALL_PROPERTIES:
            writer.opening("propstat");
            writer.opening("prop");
            for (int i = 0; i < objectProperties.length; i++) {
                String property = objectProperties[i];
                if (resource != null) {
                    writeResourceProperty(writer, property, resource);
                } else {
                    writeResourceProperty(writer, property, folder);
                }
            }
            writer.closing("prop");
            writer.property("status", STATUS_OK);
            writer.closing("propstat");
            break;

        case MODE_ALL_PROPERTY_NAMES:
            writer.opening("propstat");
            writer.opening("prop");
            for (int i = 0; i < objectProperties.length; i++) {
                String property = objectProperties[i];
                writer.empty(property);
            }
            writer.closing("prop");
            writer.property("status", STATUS_OK);
            writer.closing("propstat");
        }
    }

    private boolean writeResourceProperty(XMLWriter writer,
            String propertyName, WebdavResource resource) throws IOException {
        String property = null;
        boolean useCdata = false;
        if ("creationdate".equals(propertyName)) {
            property = Util.getIsoDate(resource.getCreationDate());
        } else if ("displayName".equals(propertyName)) {
            useCdata = true;
            property = resource.getName();
        } else if ("getlastmodified".equals(propertyName)) {
            property = Util.getHttpDate(resource.getLastModified());
        } else if ("getcontentlength".equals(propertyName)) {
            property = String.valueOf(resource.getLength());
        } else if ("resourcetype".equals(propertyName)) {
            writer.empty(propertyName);
            return true;
        } else if ("getcontenttype".equals(propertyName)) {
            property = resource.getContentType();
        }

        if (property == null) {
            return false;
        } else {
            if (useCdata) {
                writer.opening(propertyName);
                writer.writeData(property);
                writer.closing(propertyName);
            } else {
                writer.property(propertyName, property);
            }
            return true;
        }
    }

    private boolean writeResourceProperty(XMLWriter writer,
            String propertyName, WebdavFolder folder) throws IOException {
        String property = null;
        boolean useCdata = false;
        if ("creationdate".equals(propertyName)) {
            property = Util.getIsoDate(folder.getCreationDate());
        } else if ("displayName".equals(propertyName)) {
            useCdata = true;
            property = folder.getName();
        } else if ("getlastmodified".equals(propertyName)) {
            property = Util.getHttpDate(folder.getLastModified());
        } else if ("resourcetype".equals(propertyName)) {
            writer.opening(propertyName);
            writer.empty("collection");
            writer.closing(propertyName);
            return true;
        }

        if (property == null) {
            return false;
        } else {
            if (useCdata) {
                writer.opening(propertyName);
                writer.writeData(property);
                writer.closing(propertyName);
            } else {
                writer.property(propertyName, property);
            }
            return true;
        }
    }

    private void parsePropfind(String body) throws SAXException, IOException {
        XMLReader xr = Util.createXMLReader();
        PropfindHandler handler = new PropfindHandler();
        xr.setContentHandler(handler);
        xr.setErrorHandler(handler);
        xr.parse(new InputSource(new StringReader(body)));
    }

    private class PropfindHandler extends BaseXmlParser {
        PropfindHandler() {
        }

        protected void processStartElement(String elementName, String fullName,
                Attributes attributes) {
            if (fullName.equals("propfind.prop")) {
                _properties = new ArrayList();
                _mode = MODE_NAMED_PROPERTIES;
            } else if (fullName.equals("propfind.allprop")) {
                _mode = MODE_ALL_PROPERTIES;
            } else if (fullName.equals("propfind.propname")) {
                _mode = MODE_ALL_PROPERTY_NAMES;
            } else if (fullName.startsWith("propfind.prop.")) {
                _properties.add(elementName);
            }
        }
    }

    public String normalize(String href) throws IOException {
        href = Util.urlDecode(href.trim());
        if (_repository.isFolder(href)) {
            if (!href.endsWith("/")) {
                href = href + "/";
            }
        }
        return href;
    }

}
