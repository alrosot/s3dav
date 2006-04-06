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
package org.carion.s3dav.webdav;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Logs the webdav requests.
 * The logs have the "combined log format" as defined by
 * the Apache web server access logs.
 * @see http://httpd.apache.org/docs/1.3/logs.html#accesslog
 */
public class AccessLogger {
    private static final DateFormat DF = new SimpleDateFormat(
            "dd/MMM/yyyy:HH:mm:ss Z");

    void log(WebdavRequest request, WebdavResponse response) {
        StringBuffer sb = new StringBuffer();
        sb.append(nonull(request.getClient().getHostAddress()));
        sb.append(" - -");
        sb.append(" [");
        sb.append(DF.format(new Date()));
        sb.append("] ");
        sb.append("\"");
        sb.append(nonull(request.getStartLine()));
        sb.append("\" ");
        sb.append(response.getResponseStatus());
        sb.append(" ");
        sb.append(response.getHeader("Content-Length"));
        System.out.println(sb.toString());
    }

    private String nonull(String input) {
        return input == null ? "-" : input;
    }

}
