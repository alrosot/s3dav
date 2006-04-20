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

public class HtmlWriter {
    private final StringBuffer _buf = new StringBuffer();

    void h1(String text) {
        _buf.append("<h1>" + text + "</h1>");
    }

    void h2(String text) {
        _buf.append("<h2>" + text + "</h2>");
    }

    void div(String id) {
        _buf.append("<div id=\"" + id + "\">");
    }

    void div_end() {
        _buf.append("</div>");
    }

    void header(String title) {
        _buf.append("<!DOCTYPE html PUBLIC "
                + "\"-//W3C//DTD XHTML 1.0 Strict//EN\"");
        _buf.append("     "
                + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
        _buf.append("<html xmlns=\"http://www.w3.org/1999/xhtml\""
                + " xml:lang=\"en\" lang=\"en\">");
        _buf.append("<head><title>" + title + "</title>");
        _buf.append("<style type=\"text/css\">");
        setCSS();
        _buf.append("</style>");
        _buf.append("<meta http-equiv=\"cache-control\""
                + " content=\"no-cache\" forua=\"true\"/>");
        _buf.append("</head>");
        _buf.append("<body>");
    }

    void footer() {
        _buf.append("</body>");
        _buf.append("</html>");
    }

    void line(String text) {
        _buf.append(text);
    }

    void error(String text) {
        _buf.append("<p class=\"error\">" + text + "</p>");
    }

    void menu(String url, String text) {
        _buf.append("<div><a href=\"" + url + "\">" + text + "</a></div>");
    }

    void article(String title) {
        _buf.append("<div class=\"article\">");
        _buf.append("<div class=\"article-title\">" + title + "</div>");
    }

    void p(String text) {
        _buf.append("<p>" + text + "</p>");
    }

    void article_end() {
        _buf.append("</div>");
    }

    void out(String text) {
        _buf.append(text);
    }

    void th(String text) {
        _buf.append("<th>" + text + "</th>");
    }

    void td(String value) {
        td(null, null, value);
    }

    void td(String className, String value) {
        td(className, null, value);
    }

    void td(String className, String style, String value) {
        td(className, style, value, null);
    }

    void td(String className, String style, String value, String link) {
        _buf.append("<td");
        if (className != null) {
            _buf.append(" class=\"" + className + "\"");
        }
        if (style != null) {
            _buf.append(" style=\"" + style + "\"");
        }
        _buf.append(">");
        if (link != null) {
            _buf.append("<a href=\"" + link + "\">");
        }
        _buf.append(value);
        if (link != null) {
            _buf.append("</a>");
        }
        _buf.append("</td>");
    }

    private void setCSS() {
        _buf.append("body {");
        _buf.append("  background-color: #F0FFF0;");
        _buf.append("  color: Black;");
        _buf.append("  border: 0px;");
        _buf.append("  margin: 0px;");
        _buf.append("  font-family: Verdana, Arial, Helvetica, sans-serif;");
        _buf.append("}");

        _buf.append("h1 { font-family: Georgia, serif;");
        _buf.append("  font-size: x-large;");
        _buf.append("  font-style: italic;");
        _buf.append("  color: #556B2F;");
        _buf.append("  background-color: #FFFFF0;");
        _buf.append("  border: 1px dashed #556B2F;");
        _buf.append("  padding: 10px 15px 10px 15px;");
        _buf.append("  margin: 15px;");
        _buf.append("  white-space: nowrap;");
        _buf.append("}");

        _buf.append("h2 {");
        _buf.append("  font-family: Georgia, serif;");
        _buf.append("  font-size: large;");
        _buf.append("  color: #2F6A44;");
        _buf.append("  background-color: transparent;");
        _buf.append("  margin-right: 25px;");
        _buf.append("  text-align: right;");
        _buf.append("  white-space: nowrap;");
        _buf.append("}");

        _buf.append("#breadcrumb {");
        _buf.append("  font-family: Arial, Helvetica, sans-serif;");
        _buf.append("  font-size: small;");
        _buf.append("  border-bottom: 1px solid green;");
        _buf.append("  border-top:1px solid green;");
        _buf.append("  padding-top: 10px;");
        _buf.append("  padding-bottom: 10px;");
        _buf.append("  padding-left: 50px;");
        _buf.append("  background-color: #FAFAD2;");
        _buf.append("  color: #2F4F4F;");
        _buf.append("  white-space: nowrap;");
        _buf.append("}");

        _buf.append("#breadcrumb a{");
        _buf.append("color: Maroon;");
        _buf.append("text-decoration: none;");
        _buf.append("background-color: transparent;");
        _buf.append("}");

        _buf.append("#breadcrumb a:hover {");
        _buf.append("  text-decoration: underline;");
        _buf.append("}");

        _buf.append("#page {");
        _buf.append("  margin: 10px;");
        _buf.append("  padding: 10px;");
        _buf.append("}");

        _buf.append("#menu {");
        _buf.append("  position: absolute;");
        _buf.append("  width: 150px;");
        _buf.append("  top: 180px;");
        _buf.append("  text-align: left;");
        _buf.append("  font-size: small;");
        _buf.append("}");

        _buf.append("#menu a{");
        _buf.append("  text-decoration: none;");
        _buf.append("  background-color: #F0FFF0;");
        _buf.append("  font-weight: bold; ");
        _buf.append("  display: block; ");
        _buf.append("  padding: 5px; ");
        _buf.append("  width: 120px; ");
        _buf.append("  margin-bottom: 5px; ");
        _buf.append("  border: 1px solid #8FBC8F;");
        _buf.append("  color: #556B2F;");
        _buf.append("}");

        _buf.append("#menu a:hover {");
        _buf.append("  background-color: #6B8E23;");
        _buf.append("  color: White;");
        _buf.append("  border-top: 1px solid #8FBC8F;");
        _buf.append("  border-left: 1px solid #8FBC8F;");
        _buf.append("  border-right: 1px solid #8FBC8F;");
        _buf.append("  border-bottom: 1px solid #2F4F4F;");
        _buf.append("}");

        _buf.append("#options a{");
        _buf.append("  background-color: #6B8E23;");
        _buf.append("  color: White;");
        _buf.append("  border-top: 1px solid #8FBC8F;");
        _buf.append("  border-left: 1px solid #8FBC8F;");
        _buf.append("  border-right: 1px solid #8FBC8F;");
        _buf.append("  border-bottom: 1px solid #2F4F4F;");
        _buf.append("}");

        _buf.append("#content {");
        _buf.append("  position: absolute;");
        _buf.append("  border-left: 1px solid green;");
        _buf.append("  padding-left: 20px;");
        _buf.append("  left: 170px;");
        _buf.append("  top: 180px;");
        _buf.append("}");

        _buf.append("#content p {");
        _buf.append("  padding-left: 30px;");
        _buf.append("}");

        _buf.append("#footer {");
        _buf.append("  border: 1px dotted black;");
        _buf.append("  color: #556B2F;");
        _buf.append("  background-color: #FAFAD2;");
        _buf.append("  margin-top: 20px;");
        _buf.append("  margin-bottom: 20px;");
        _buf.append("}");

        _buf.append("a { color: Maroon; ");
        _buf.append("  text-decoration: none;");
        _buf.append("  background-color: transparent;");
        _buf.append("}");

        _buf.append("a:hover {");
        _buf.append("  text-decoration: underline;");
        _buf.append("}");

        _buf.append(".article-footer {");
        _buf.append("  text-align: right;");
        _buf.append("  font-size: xx-small;");
        _buf.append("  color: Gray;");
        _buf.append("  padding-bottom: 15px;");
        _buf.append("}");

        _buf.append(".author, .datetime {");
        _buf.append("  font-weight: bold;");
        _buf.append("  color: #4F4F4F;");
        _buf.append("}");

        _buf.append(".article-title {");
        _buf.append("  font-size: large;");
        _buf.append("  font-weight: bold;");
        _buf.append("  font-family: Georgia, Sans-serif;");
        _buf.append("  font-style: italic;");
        _buf.append("  padding: 20px 15px 10px 20px;");
        _buf.append("  border-bottom: 1px solid gray;");
        _buf.append("}");

        _buf.append("#content form {");
        _buf.append("  padding-left: 30px;");
        _buf.append("  font-size: small;");
        _buf.append("}");

        _buf.append("#content form p{");
        _buf.append("}");

        _buf.append(".textbox {");
        _buf.append("  border: 1px solid gray;");
        _buf.append("  margin: 5px;");
        _buf.append("  vertical-align: right;");
        _buf.append("}");

        _buf.append(".flabel {");
        _buf.append("  margin: 5px;");
        _buf.append("  width: 100px;");
        _buf.append("}");

        _buf.append(".button { ");
        _buf.append("  border: medium outset;");
        _buf.append("  background-color: #F5F5F5;");
        _buf.append("  margin-left: 150px;");
        _buf.append("  color: black;");
        _buf.append("  margin-top: 10px;");
        _buf.append("}");

        _buf.append("fieldset {");
        _buf.append("  margin-top: 5px;");
        _buf.append("  width: 200px;");
        _buf.append("}");

        _buf.append("form {");
        _buf.append("  border: thick double #90EE90;");
        _buf.append("  padding: 10px;");
        _buf.append("  margin: 10px;");
        _buf.append("  margin-left: 30px;");
        _buf.append("  margin-top: 10px;");
        _buf.append("}");

        _buf.append(".error {");
        _buf.append("  border: thick double #FF0000;");
        _buf.append("  padding: 10px;");
        _buf.append("  margin: 10px;");
        _buf.append("  width: 280px;");
        _buf.append("  margin-left: 30px;");
        _buf.append("  margin-top: 10px;");
        _buf.append("}");

        _buf.append("caption {");
        _buf.append("  background-color:#8080ff;");
        _buf.append("  color:white;");
        _buf.append("  border-style:solid;");
        _buf.append("  border-width:2px;");
        _buf.append("  border-color:black;");
        _buf.append("}");

        _buf.append("table {");
        _buf.append("  font-family:arial;");
        _buf.append("  font-size:10pt;");
        _buf.append("  background-color:#808080;");
        _buf.append("  border-style:solid;");
        _buf.append("  border-color:black;");
        _buf.append("  border-width:2px;");
        _buf.append("}");

        _buf.append("th {");
        _buf.append("  font-size:10pt;");
        _buf.append("  color:white;");
        _buf.append("}");

        _buf.append("tr {");
        _buf.append("  spacing:5px;");
        _buf.append("}");

        _buf.append("td {");
        _buf.append("  font-size:10pt;");
        _buf.append("  background-color:#99FFCC;");
        _buf.append("  color:#000099;");
        _buf.append("  border-style:solid;");
        _buf.append("  border-width:1px;");
        _buf.append("  text-align:left;");
        _buf.append("  padding:5px;");
        _buf.append("}");

        _buf.append("td.cell_0 {");
        _buf.append("  background-color:#FF9999;");
        _buf.append("}");

        _buf.append("td.cell_1 {");
        _buf.append("  background-color:#CCCCFF;");
        _buf.append("}");
    }

    public String toString() {
        return _buf.toString();
    }
}
