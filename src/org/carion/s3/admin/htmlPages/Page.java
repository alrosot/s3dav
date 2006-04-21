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

import org.carion.s3.S3Log;
import org.carion.s3.S3UploadManager;
import org.carion.s3.impl.S3RepositoryImpl;
import org.carion.s3.util.LogWriter;

abstract class Page {
    private final String _pageTitle;

    private final String _pageName;

    protected HtmlWriter _w;

    private AdminPage _adminPage;

    protected S3RepositoryImpl _repository;

    protected S3Log _log;

    protected LogWriter _logWriter;

    protected S3UploadManager _uploadManager;

    Page(String pageTitle, String pageName) {
        _pageTitle = pageTitle;
        _pageName = pageName;
    }

    void setContext(HtmlWriter w, AdminPage adminPage,
            S3RepositoryImpl repository, LogWriter logWriter,
            S3UploadManager uploadManager) {
        _w = w;
        _adminPage = adminPage;
        _repository = repository;
        _log = repository.getLog();
        _logWriter = logWriter;
        _uploadManager = uploadManager;
    }

    public String getPageTitle() {
        return _pageTitle;
    }

    public String getPageName() {
        return _pageName;
    }

    protected String getParam(String name) {
        return _adminPage.getParam(name);
    }

    abstract void page();

    abstract boolean needsRepository();

    boolean isVisible() {
        return true;
    }

    void action() {
    }
}
