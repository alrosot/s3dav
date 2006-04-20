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
package org.carion.s3.admin.htmlPages;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.carion.s3.impl.Object;
import org.carion.s3.util.Util;

public class RawListingPage extends Page {
    RawListingPage(String pageName) {
        super("Raw Listing Page", pageName);
    }

    boolean needsRepository() {
        return true;
    }

    boolean isVisible() {
        return false;
    }

    void page() {
        String bucket = getParam("bucket");
        _w.article("Raw listing for bucket:" + bucket);
        _w.out("<br/>");
        try {
            List objects = _repository.getRawListing(bucket);
            if (objects.size() == 0) {
                _w.p("there is no objects stored in this bucket");
            } else {
                _w.out("<table>");
                _w.out("<tr>");
                _w.th("key");
                _w.th("size");
                _w.th("last modified");
                _w.th("action");
                _w.out("</tr>");
                int lineno = 0;
                String className;
                for (Iterator iter = objects.iterator(); iter.hasNext();) {
                    Object obj = (Object) iter.next();
                    className = ((lineno % 2) == 0) ? "cell_0" : "cell_1";
                    _w.out("<tr>");
                    _w.td(className, obj.getKey());
                    _w.td(className, "text-align:right", String.valueOf(obj
                            .getSize()));
                    _w.td(className, obj.getLastModified().toString());
                    _w.td(className, null, "delete object",
                            "index.html?page=deleteobject&bucket="
                                    + Util.urlEncode(bucket) + "&key="
                                    + Util.urlEncode(obj.getKey()));

                    _w.out("</tr>");
                    lineno++;
                }
                _w.out("</table>");
            }

        } catch (IOException ex) {
            _log.log("Error retrieving content of bucket:" + bucket, ex);
            _w.error("Error retrieving content of bucket:" + bucket);
        }

        _w.article_end();
    }
}
