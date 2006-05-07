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

class WelcomePage extends Page {
    WelcomePage(String pageName) {
        super("Welcome Page", pageName);
    }

    boolean needsRepository() {
        return false;
    }

    void page() {
        _w.article("Welcome");
        _w.p("This Web interface allows you to "
                + "perform some configuration and administrative"
                + " tasks on your Amazon/S3 account.");
        _w.p("The menu on the left of this page "
                + "gives you access to the different " + "tasks available.");
        _w.article_end();
        _w.article("Available Task");
        _w.out("<p><ul>");
        _w.out("<li><b>Welcome Page</b>: This present page.</li>");
        _w.out("<li><b>Account Page</b>: " + "This page allows you to enter "
                + "your S3 account login information. "
                + "You need to enter those information "
                + "before being able to use the s3DAV server.</li>");
        _w.out("<li><b>Buckets Page</b>: " + "This page lists the buckets "
                + "that you have created. "
                + "This page allows you to create a new bucket</li>");
        _w.out("<li><b>Uploads Page</b>: "
                + "This page lists your current uploads</li>");
        _w.out("<li><b>Support Page</b>: " + "Nothing works for you ? "
                + "you're lost ? this is the page you should read.</li>");
        _w.out("<li><b>Logs Page</b>: "
                + "This page gives you access to the logs of s3DAV.</li>");
        _w.out("<li><b>Browse content</b>: "
                + "This page allows you to browse the content of " + ""
                + "your s3 account.</li>");
        _w.out("<li><b>Credits Page</b>: "
                + "This page gives credit to where credit is due</li>");
        _w.out("</ul></p>");
        _w.article_end();
    }
}
