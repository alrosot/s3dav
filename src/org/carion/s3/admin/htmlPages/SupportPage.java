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

class SupportPage extends Page {
    SupportPage(String pageName) {
        super("Support Page", pageName);
    }

    boolean needsRepository() {
        return false;
    }

    void page() {
        _w.article("Support");
        _w.p("You can get information about s3DAV here : <a href=\"http://www.carion.org/s3dav/index.html\">s3DAV official site</a>.");
        _w.p("Any question about s3DAV can be mailed to: pcarion@gmail.com");
        _w.article_end();
    }
}
