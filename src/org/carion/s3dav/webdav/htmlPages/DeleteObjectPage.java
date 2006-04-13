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


public class DeleteObjectPage extends Page {
    DeleteObjectPage(String pageName) {
        super("Delete Object Page", pageName);
    }

    boolean needsRepository() {
        return true;
    }

    boolean isVisible() {
        return false;
    }

    void page() {
        String bucket = getParam("bucket");
        String key = getParam("key");
        _w.article("Delete Object");
        _w.out("<br/>");
        _w.out("Bucket:" + bucket);
        _w.out("Key:" + key);
        /*        try {
         } catch (IOException ex) {
         _log.log("Error retrieving content of bucket:" + bucket, ex);
         _w.error("Error retrieving content of bucket:" + bucket);
         }
         */
        _w.article_end();
    }
}
