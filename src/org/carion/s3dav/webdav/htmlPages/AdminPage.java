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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.carion.s3dav.Version;
import org.carion.s3dav.repository.WebdavRepository;
import org.carion.s3dav.s3.WebdavRepositoryImpl;
import org.carion.s3dav.webdav.WebdavRequest;

/**
 * Spring shades
 * http://www.oswd.org/design/preview/id/1171
 * http://www.spectrum-research.com/V2/generators/tableframe.asp
 * @author pcarion
 *
 */
public class AdminPage {
    private final WebdavRepositoryImpl _repository;

    private final WebdavRequest _request;

    private final HashMap _parameters = new HashMap();

    private final HtmlWriter _w = new HtmlWriter();

    private final static List _pages = new ArrayList();

    static {
        _pages.add(new WelcomePage("welcome"));
        _pages.add(new AccountPage("account"));
        _pages.add(new BucketsPage("buckets"));
        _pages.add(new SupportPage("support"));
        _pages.add(new CreditsPage("credits"));
    }

    public AdminPage(WebdavRequest request, WebdavRepository repository) {
        _repository = (WebdavRepositoryImpl) repository;
        _request = request;
        _request.parseParameters(_parameters);
    }

    private Page getPage() {
        String pageName = getParam("page");
        Page thePage = null;

        if (pageName != null) {
            for (Iterator iter = _pages.iterator(); iter.hasNext();) {
                Page page = (Page) iter.next();
                if (pageName.equals(page.getPageName())) {
                    thePage = page;
                }
            }
        }

        if (thePage == null) {
            thePage = new WelcomePage("");
        }
        thePage.setContext(_w, this, _repository);
        return thePage;
    }

    public String getParam(String name) {
        List params = (List) _parameters.get(name);
        if (params == null) {
            return null;
        } else {
            return (String) params.get(0);
        }
    }

    public String getHtmlPage() {
        return getHtmlPage(getPage());
    }

    private String getHtmlPage(Page page) {
        page.action();

        _w.header("s3DAV Admin Page:" + page.getPageTitle());
        _w.h1("s3DAV version:" + Version.VERSION);
        _w.div("breadcrumb");
        _w.line("<a href=\"index.html\">" + "Home Page</a> > "
                + page.getPageTitle());

        _w.div_end();
        _w.h2("The s3DAV Admin Page");

        _w.div("page");
        _w.div("menu");

        for (Iterator iter = _pages.iterator(); iter.hasNext();) {
            Page aPage = (Page) iter.next();
            // we don't display pages which needs the repository if
            // the repository is not available !
            if (_repository.isAvailable() || !aPage.needsRepository()) {
                _w.menu("index.html?page=" + aPage.getPageName(), aPage
                        .getPageTitle());
            }
        }
        _w.menu("/", "Browse Content");
        _w.div_end(); // menu

        _w.div("content");

        if (!_repository.isAvailable()) {
            _w.error("Your S3 account information are not available");
        }

        page.page();

        _w.div_end(); // content
        _w.div_end(); // page
        _w.footer();
        return _w.toString();
    }

}
