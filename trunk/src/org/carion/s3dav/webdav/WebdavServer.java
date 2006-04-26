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

import java.util.HashMap;
import java.util.Map;

import org.carion.s3.S3Log;
import org.carion.s3.S3Repository;
import org.carion.s3.http.HttpProcessing;
import org.carion.s3.http.HttpRequest;
import org.carion.s3.http.HttpServer;

/**
 * Implements a (partial) webdav compliant server. This is not supposed to be a
 * fully compliant webdav server, we just need a webdav server which could be
 * used in conjunction with the file explorer of Windows to manage a "s3" file
 * system
 * 
 * This class is highly/freely inspired from:
 * <ul>
 * <li> the webdav servler which is shipped with Tomcat</li>
 * <li> the rewrite of the previous servlet done by Robert Erler</li>
 * </ul>
 * 
 * @see http://www.webdav.org/specs/rfc2518.html
 * @see http://webdav-servlet.sourceforge.net/
 * @see http://tomcat.apache.org/
 */
public class WebdavServer extends HttpServer {
    private final Map _handlers = new HashMap();

    public WebdavServer(int port, S3Repository repository, S3Log log) {
        super(port, repository, log);
    }

    protected void init(S3Repository repository) {
        if (_repository.isAvailable()) {
            _handlers.put("PROPFIND", new HandlerPropfind(_repository));
            _handlers.put("PROPPATCH", new HandlerUnsupported(_repository));
            _handlers.put("MKCOL", new HandlerMkcol(_repository));
            _handlers.put("GET", new HandlerGet(_repository));
            _handlers.put("HEAD", new HandlerHead(_repository));
            _handlers.put("POST", new HandlerPost(_repository));
            _handlers.put("DELETE", new HandlerDelete(_repository));
            _handlers.put("PUT", new HandlerPut(_repository));
            _handlers.put("COPY", new HandlerCopy(_repository));
            _handlers.put("MOVE", new HandlerMove(_repository));
            _handlers.put("LOCK", new HandlerUnsupported(_repository));
            _handlers.put("UNLOCK", new HandlerUnsupported(_repository));
            _handlers.put("OPTIONS", new HandlerOptions(_repository));
        } else {
            _handlers.put("GET", new HandlerGet(_repository));
            _handlers.put("POST", new HandlerPost(_repository));
            _handlers.put("HEAD", new HandlerForbidden(_repository));
            _handlers.put("PROPFIND", new HandlerForbidden(_repository));
            _handlers.put("PROPPATCH", new HandlerForbidden(_repository));
            _handlers.put("MKCOL", new HandlerForbidden(_repository));
            _handlers.put("DELETE", new HandlerForbidden(_repository));
            _handlers.put("PUT", new HandlerForbidden(_repository));
            _handlers.put("COPY", new HandlerForbidden(_repository));
            _handlers.put("MOVE", new HandlerForbidden(_repository));
            _handlers.put("LOCK", new HandlerForbidden(_repository));
            _handlers.put("UNLOCK", new HandlerForbidden(_repository));
            _handlers.put("OPTIONS", new HandlerForbidden(_repository));
        }
    }

    protected HttpProcessing getProcessing(HttpRequest request) {
        String litmus = request.getHttpHeader("X-Litmus");
        if (litmus != null) {
            _log.log("**********");
            _log.log("Litmus test:" + litmus);
            _log.log("**********");
        }
        // let's find a handler to process this request
        HandlerBase handler = (HandlerBase) _handlers.get(request.getMethod());
        return handler;
    }
}
