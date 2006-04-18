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
package org.carion.s3dav.webdav.htmlPages;

import java.io.IOException;
import java.util.StringTokenizer;

import org.carion.s3dav.Version;
import org.carion.s3dav.repository.WebdavFolder;
import org.carion.s3dav.repository.WebdavObject;
import org.carion.s3dav.repository.WebdavRepository;
import org.carion.s3dav.repository.WebdavResource;
import org.carion.s3dav.s3.naming.S3UrlName;
import org.carion.s3dav.util.Util;
import org.carion.s3dav.webdav.WebdavRequest;

public class BrowserPage {
    private final HtmlWriter _w = new HtmlWriter();

    public String getFolderHtmlPage(WebdavFolder folder, WebdavRequest request,
            WebdavRepository repository) throws IOException {
        String theUri = folder.getUrl().getUri();
        _w.header("Directory:" + theUri);
        _w.h1("s3DAV version:" + Version.VERSION);
        _w.div("breadcrumb");
        _w.line("<a href=\"/\">[Root]</a>/");
        StringTokenizer st = new StringTokenizer(theUri, "/");
        StringBuffer link = new StringBuffer();
        while (st.hasMoreTokens()) {
            String elt = st.nextToken();
            link.append("/");
            link.append(elt);
            _w.line("<a href=\"" + link.toString() + "\">"
                    + Util.urlDecode(elt) + "</a>/");
        }
        _w.div_end();
        _w.h2("the s3DAV Browsing Page");

        _w.div("page");
        _w.div("menu");

        _w.menu("/index.html", "Admin Page");
        _w.menu("index.html?page=logs", "Logs Page");

        _w.div_end(); // menu

        _w.div("content");

        _w.out("<table>");
        _w.out("<tr>");
        _w.th("File name");
        _w.th("size");
        _w.th("type");
        _w.out("</tr>");

        S3UrlName[] files = folder.getChildrenUris();

        int lineno = 0;
        String className;

        // pass #1: the directories
        for (int i = 0; i < files.length; i++) {
            S3UrlName uri = files[i];
            if (repository.isFolder(uri)) {
                className = ((lineno % 2) == 0) ? "cell_0" : "cell_1";
                WebdavFolder res = repository.getFolder(uri);
                _w.out("<tr>");
                _w.out("<td class=\"" + className + "\"><a href=\""
                        + mkUrl(res, request) + "\">"
                        + Util.urlDecode(res.getName())
                        + "</a></td><td class=\"" + className
                        + "\">&nbsp;</td><td class=\"" + className
                        + "\" style=\"text-align:center\">Folder</td>");
                _w.out("</tr>");
                lineno++;
            }
        }
        for (int i = 0; i < files.length; i++) {
            S3UrlName uri = files[i];
            if (repository.isResource(uri)) {
                className = ((lineno % 2) == 0) ? "cell_0" : "cell_1";
                WebdavResource res = repository.getResource(uri);
                _w.out("<tr><td class=\"" + className + "\"><a href=\""
                        + mkUrl(res, request) + "\">"
                        + Util.urlDecode(res.getName())
                        + "</a></td><td class=\"" + className
                        + "\" align=\"right\">" + res.getLength()
                        + "</td><td class=\"" + className
                        + "\" align=\"right\">" + res.getContentType()
                        + "</td></tr>");
                lineno++;
            }
        }
        _w.out("</table>");

        _w.div_end(); // content
        _w.div_end(); // page
        _w.footer();

        return _w.toString();
    }

    private String mkUrl(WebdavObject res, WebdavRequest request) {
        return "http://" + request.getHost() + res.getUrl().getUri();
    }

}
