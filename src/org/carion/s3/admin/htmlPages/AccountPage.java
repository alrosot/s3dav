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

class AccountPage extends Page {
    AccountPage(String pageName) {
        super("Account Page", pageName);
    }

    boolean needsRepository() {
        return false;
    }

    void action() {
        String key = getParam("key");
        String secret = getParam("secret");
        String delete = getParam("delete");

        if (delete != null) {
            _repository.deleteCredential();
        } else if ((key != null) && (secret != null)
                && (key.trim().length() > 0) && (secret.trim().length() > 0)) {
            _repository.newCredentialInformation(key.trim(), secret.trim());
        }
    }

    void page() {
        _w.article("Account information");
        _w.p("The following form allows you to enter your S3 account information");

        _w.out("<form action=\"index.html?page=" + getPageName()
                + "\" method=\"post\">");
        _w.out("<div>");
        _w.out("<label class=\"flabel\" for=\"key\">Access Key ID:</label>");
        _w.out("<input type=\"text\" class=\"textbox\" id=\"key\" name=\"key\" size=\"50\" value=\""
                + _repository.getAccessKey() + "\"/></div>");
        _w.out("<div>");
        _w.out("<label class=\"flabel\" for=\"secret\">Secret Access Key ID (*):</label>");
        _w.out("<input type=\"text\" class=\"textbox\" id=\"secret\" name=\"secret\" size=\"50\"/></div>");
        _w.out("<div>");
        _w.p("(*) : For security reasons, the value of the Secret Access Key is not displayed on this screen.");
        _w.out("</div>");
        _w.out("<div><input type=\"submit\" value=\"AWS Account Information\" name=\"submit\" class=\"button\" /></div>");
        _w.out("</form>");

        _w.article_end();

        _w.article("Delete Account information");
        _w.p("If you want to delete your account information, click on the following link:<a href=\"index.html?page="
                + getPageName() + "&delete=1\">delete account information</a>");
        _w.article_end();
    }
}
