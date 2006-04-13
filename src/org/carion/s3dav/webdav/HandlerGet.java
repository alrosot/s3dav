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

import org.carion.s3dav.repository.WebdavFolder;
import org.carion.s3dav.repository.WebdavObject;
import org.carion.s3dav.repository.WebdavRepository;
import org.carion.s3dav.repository.WebdavResource;
import org.carion.s3dav.util.Util;
import org.carion.s3dav.webdav.htmlPages.AdminPage;
import org.carion.s3dav.webdav.htmlPages.BrowserPage;

/**
 * Handles 'GET' request.
 * rfc2518: <i>8.4 : The semantics of GET are unchanged when
 * applied to a collection, since GET is defined as,
 * "retrieve whatever information (in the form of an entity)
 * is identified by the Request-URI" [RFC2068].
 * GET when applied to a collection may return the
 * contents of an "index.html" resource, a human-readable
 * view of the contents of the collection, or something
 * else altogether.
 * Hence it is possible that the result of a GET on a
 * collection will bear no correlation to the membership
 * of the collection.</i>
 *
 * @author pcarion
 *
 */
public class HandlerGet extends HandlerBase {
    HandlerGet(WebdavRepository repository) {
        super(repository);
    }

    void process(WebdavRequest request, WebdavResponse response)
            throws IOException {
        String href = request.getUrl();

        // catch request to admin pages
        if (href.startsWith("/index.html")) {
            AdminPage page = new AdminPage(request, _repository);
            String body = page.getHtmlPage();

            response.setResponseHeader("last-modified", Util.getHttpDate());

            response.setResponseBody(body, "text/html");
        } else if (_repository.isAvailable()) {
            boolean exists = _repository.objectExists(href);
            if (exists) {
                boolean isDirectory = _repository.isFolder(href);

                if (isDirectory) {
                    WebdavFolder folder = _repository.getFolder(href);
                    writeFolder(folder, response, request);
                } else {
                    WebdavResource resource = _repository.getResource(href);
                    setHeaders(resource, response);
                    response.setContentStream(resource.getContent());
                }
            } else {
                response.setResponseStatus(WebdavResponse.SC_NOT_FOUND);
            }
        } else {
            response.setResponseStatus(WebdavResponse.SC_FORBIDDEN);
        }
    }

    /**
     * Set the headers associated to the GET on the resource
     * FYI: this method will be reused in the HEAD request,
     * that's why we have a separate method to set the headers
     * @param resource resource to process
     * @param response response to set
     * @throws IOException
     */
    protected void setHeaders(WebdavResource resource, WebdavResponse response)
            throws IOException {
        // set HTTP headers
        response.setResponseHeader("last-modified", Util.getHttpDate(resource
                .getLastModified()));

        response.setResponseHeader("Content-Length", String.valueOf(resource
                .getLength()));

        response.setContentType(resource.getContentType());
    }

    private void writeFolder(WebdavFolder folder, WebdavResponse response,
            WebdavRequest request) throws IOException {
        BrowserPage page = new BrowserPage();
        String body = page.getFolderHtmlPage(folder, request, _repository);
        //        String body = getFolderHtmlPage(folder, request);

        response.setResponseHeader("last-modified", Util.getHttpDate());

        response.setResponseBody(body, "text/html");
    }

    /*
     * If you need an extra proof, that java generated
     * HTML code is ugly, here it is ...
     */
    protected String getFolderHtmlPage(WebdavFolder folder,
            WebdavRequest request) throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><header><title>List of files in:" + folder.getURI()
                + "</title>");
        sb.append("<style type=\"text/css\">");
        sb.append("body {");
        sb.append("    background-color: #FFFFFF;");
        sb.append("    font-size: small;");
        sb.append("    color: #000000;");
        sb.append("}");
        sb.append(".cell_0 {");
        sb.append("    background-color: #FFFFFF;");
        sb.append("    color: #000000;");
        sb.append("    font-size: small;");
        sb.append("    padding: 1px;");
        sb.append("    border: 1px;");
        sb.append("    border-style: solid;");
        sb.append("    border-color: #000000;");
        sb.append("    text-decoration: none;");
        sb.append("}");
        sb.append(".cell_1 {");
        sb.append("    background-color: #CCCCCC;");
        sb.append("    color: #000000;");
        sb.append("    font-size: small;");
        sb.append("    padding: 1px;");
        sb.append("    border: 1px;");
        sb.append("    border-style: solid;");
        sb.append("    border-color: #000000;");
        sb.append("    text-decoration: none;");
        sb.append("}");
        sb.append("</style>");
        sb
                .append("<meta http-equiv=\"cache-control\" content=\"no-cache\" forua=\"true\"/>");

        sb.append("</header>");
        sb.append("<body><table>");
        sb.append("<tr><th>File name</th><th>size</th><th>type</th></tr>");
        String[] files = folder.getChildrenUris();

        int lineno = 0;
        String className;

        // pass #1: the directories
        for (int i = 0; i < files.length; i++) {
            String uri = files[i];
            if (_repository.isFolder(uri)) {
                className = ((lineno % 2) == 0) ? "cell_0" : "cell_1";
                WebdavFolder res = _repository.getFolder(uri);
                sb.append("<tr><td class=\"" + className + "\"><a href=\""
                        + mkUrl(res, request) + "\">" + res.getName()
                        + "</a></td><td class=\"" + className
                        + "\">&nbsp;</td><td class=\"" + className
                        + "\" align=\"right\">Folder</td></tr>");
                lineno++;
            }
        }
        for (int i = 0; i < files.length; i++) {
            String uri = files[i];
            if (_repository.isResource(uri)) {
                className = ((lineno % 2) == 0) ? "cell_0" : "cell_1";
                WebdavResource res = _repository.getResource(uri);
                sb.append("<tr><td class=\"" + className + "\"><a href=\""
                        + mkUrl(res, request) + "\">" + res.getName()
                        + "</a></td><td class=\"" + className
                        + "\" align=\"right\">" + res.getLength()
                        + "</td><td class=\"" + className
                        + "\" align=\"right\">" + res.getContentType()
                        + "</td></tr>");
                lineno++;
            }
        }
        sb.append("</table></body></html>");

        return sb.toString();
    }

    private String mkUrl(WebdavObject res, WebdavRequest request) {
        return "http://" + request.getHost() + res.getURI();
    }
}
