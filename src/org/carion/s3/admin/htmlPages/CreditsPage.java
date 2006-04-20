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

class CreditsPage extends Page {
    CreditsPage(String pageName) {
        super("Credits Page", pageName);
    }

    boolean needsRepository() {
        return false;
    }

    void page() {
        _w.article("Web design");
        _w.p("The design of this page is called <i>Spring Shades</i> by <i>nmyers</i> and can be found at <a href=\"http://www.oswd.org/design/preview/id/1171\">the Open Source Web Design (OSWD)</a>.");
        _w.p("The CSS for the tables have been generated using the <a href=\"http://www.spectrum-research.com/V2/generators/tableframe.asp\">Spectrum Research Table CSS generator</a>");
        _w.article_end();
    }
}
