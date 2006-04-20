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

import org.carion.s3.impl.Bucket;

class BucketsPage extends Page {
    private String _errorMessage;

    BucketsPage(String pageName) {
        super("Buckets Page", pageName);
    }

    boolean needsRepository() {
        return true;
    }

    void action() {
        _errorMessage = null;
        String bucket = getParam("bucketname");
        if (bucket != null) {
            try {
                _repository.createBucket(bucket);
            } catch (IOException ex) {
                _errorMessage = ex.getMessage();
            }
        }
    }

    void page() {
        if (_errorMessage != null) {
            _w.error(_errorMessage);
        }

        _w.article("Buckets associated to your account");
        try {
            List buckets = _repository.getBuckets();

            if (buckets.size() == 0) {
                _w.p("You have no bucket associated to your account."
                        + " Check menu on the left to create a bucket");
            } else {
                _w.out("<p><table cellpadding=\"10\">");
                _w.out("<thead>");
                _w.out("<tr>");
                _w.th("bucket name");
                _w.th("creation date");
                _w.th("raw listing");
                _w.out("</tr>");
                _w.out("</thead>");
                _w.out("<tbody>");
                for (Iterator iter = buckets.iterator(); iter.hasNext();) {
                    Bucket bucket = (Bucket) iter.next();
                    _w.out("<tr>");
                    _w.td(bucket.getName());
                    _w.td(bucket.getCreationDate().toString());
                    _w.td(null, null, "raw listing",
                            "index.html?page=rawlisting&bucket="
                                    + bucket.getName());
                    _w.out("</tr>");
                }
                _w.out("</tbody>");
                _w.out("</table></p>");
            }

        } catch (IOException ex) {
            _repository.getLog().log("Error retrieving buxkets", ex);
            _w.error("An error occured while trying to "
                    + "retrieve the list of your buckets "
                    + "- Please double check your AWS identification");
        }
        _w.article_end();

        _w.article("Bucket creation");
        _w.p("The following form allows you to create a bucket");
        _w.out("<form action=\"index.html?page=" + getPageName()
                + "\" method=\"post\">");
        _w.out("<div>");
        _w.out("<label for=\"bucketname\">Name:</label>");
        _w.out("<input type=\"text\"" + " class=\"textbox\" id=\"bucketname\" "
                + "name=\"bucketname\" /></div>");
        _w.out("<div><input type=\"submit\" " + "value=\"Create bucket\" "
                + "name=\"submit\" class=\"button\" /></div>");
        _w.out("</form>");
        _w.article_end();
    }
}
