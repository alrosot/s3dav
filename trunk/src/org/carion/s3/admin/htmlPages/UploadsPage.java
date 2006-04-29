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

import java.util.Iterator;
import java.util.List;

import org.carion.s3.S3UploadManager;

class UploadsPage extends Page {
    UploadsPage(String pageName) {
        super("Uploads Page", pageName);
    }

    boolean needsRepository() {
        return true;
    }

    void page() {
        _w.article("Pending Uploads");
        List uploads = _uploadManager.getCurrentUploads();

        if (uploads.size() == 0) {
            _w.p("You have no pending upload");
        } else {
            _w.out("<p><table cellpadding=\"10\">");
            _w.out("<thead>");
            _w.out("<tr>");
            _w.th("upload name");
            _w.th("total size");
            _w.th("% upload");
            _w.th("uploaded");
            _w.th("temporary file");
            _w.th("state");
            _w.out("</tr>");
            _w.out("</thead>");
            _w.out("<tbody>");
            for (Iterator iter = uploads.iterator(); iter.hasNext();) {
                S3UploadManager.Upload upload = (S3UploadManager.Upload) iter
                        .next();
                _w.out("<tr>");
                _w.td(upload.getName().getUri());
                _w.td(String.valueOf(upload.getSize()));
                _w.td(upload.getPercentage() + "%");
                _w.td(String.valueOf(upload.getUploaded()));
                _w.td(upload.getStorageFile().getAbsolutePath());
                _w.td(String.valueOf(upload.getState()));
                _w.out("</tr>");
            }
            _w.out("</tbody>");
            _w.out("</table></p>");
        }
        _w.article_end();
    }
}
