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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.carion.s3dav.repository.S3Log;
import org.carion.s3dav.repository.WebdavRepository;
import org.carion.s3dav.util.Util;

/**
 * Implements a (partial) webdav compliant server.
 * This is not supposed to be a fully compliant webdav server,
 * we just need a webdav server which could be used in conjunction
 * with the file explorer of Windows to manage a "s3" file system
 *
 * This class is highly/freely inspired from: <ul>
 * <li> the webdav servler which is shipped with Tomcat</li>
 * <li> the rewrite of the previous servlet done by Robert Erler</li>
 * </ul>
 *
 * @see http://www.webdav.org/specs/rfc2518.html
 * @see http://webdav-servlet.sourceforge.net/
 * @see http://tomcat.apache.org/
 */
public class WebdavServer extends Thread {
    private final String SERVER_NAME = "s3dav/0.9";

    private final int _port;

    private final WebdavRepository _repository;

    private final Map _handlers = new HashMap();

    private final S3Log _log;

    private boolean _repositoryAvailable;

    public WebdavServer(int port, WebdavRepository repository, S3Log log) {
        _log = log;
        _port = port;
        _repository = repository;
        initHandlers();
    }

    void initHandlers() {
        _repositoryAvailable = _repository.isAvailable();
        if (_repositoryAvailable) {
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

    public void run() {
        ServerSocket serversocket = null;

        try {
            _log.log("Listening on port:" + _port);
            serversocket = new ServerSocket(_port);
        } catch (Exception e) {
            _log.log("Can't listen on socket", e);
            return;
        }

        while (true) {
            try {
                Socket socket = serversocket.accept();
                // check if the repository availability has changed
                if (_repositoryAvailable ^ _repository.isAvailable()) {
                    initHandlers();
                }
                InetAddress client = socket.getInetAddress();
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();
                WebdavHandler handler = new WebdavHandler(client, input, output);
                handler.start();
            } catch (Exception e) {
                _log.log("Can't accept connections", e);
            }
        }
    }

    private class WebdavHandler extends Thread {
        private final InetAddress _client;

        private final InputStream _input;

        private final OutputStream _output;

        private InternetInputStream _stream;

        public WebdavHandler(InetAddress client, InputStream input,
                OutputStream output) {
            _client = client;
            _input = input;
            _output = output;
        }

        public void run() {
            WebdavRequest request = null;
            try {
                _stream = new InternetInputStream(_input);
                boolean keepAlive = true;

                while (keepAlive) {
                    // 1) Read start line
                    String startLine = null;
                    do {
                        startLine = _stream.readline();
                        if (startLine == null) {
                            throw new EOFException();
                        }
                    } while (startLine.trim().length() == 0);

                    _log.log(_log.ts() + "- Request:" + startLine);

                    request = new WebdavRequest(startLine, _client, _log);

                    // 2) read HTTP headers
                    String currentKey = null;
                    while (true) {
                        String line = _stream.readline();
                        if ((line == null) || (line.length() == 0)) {
                            break;
                        }

                        if (!Character.isSpaceChar(line.charAt(0))) {
                            int index = line.indexOf(':');
                            if (index >= 0) {
                                currentKey = line.substring(0, index).trim();
                                String value = line.substring(index + 1).trim();
                                request.setHttpHeader(currentKey, value);
                            }
                        } else if (currentKey != null) {
                            String value = request.getHttpHeader(currentKey);
                            request.setHttpHeader(currentKey, value + "\n\t"
                                    + line.trim());
                        }
                    }
                    keepAlive = request.getKeepAlive();
                    int contentLength = request.getContentLength();

                    // let's find a handler to process this request
                    HandlerBase handler = (HandlerBase) _handlers.get(request
                            .getMethod());

                    _log.log("@@ content-length is:"
                            + request.getContentLength() + ","
                            + request.getHttpHeader("Content-Length") + ","
                            + "Keep-Alive:" + keepAlive);

                    InputStream wrappedInputStream = Util.wrap(_stream,
                            keepAlive, contentLength);
                    // IMPORTANT:
                    // we don't want to rely on the handlers
                    // do read the content because we have to be
                    // absolutely sure that the content has been
                    // read in order to be able to read subsequent
                    // request using the same socket. (Keep-Alive)
                    // the problem is that the content-length is not always
                    // set and we don't know if we must read the body or not
                    request.setInputStream(wrappedInputStream);

                    // prepare response
                    WebdavResponse response = new WebdavResponse();

                    // check if we have a handler to process this request
                    if (handler == null) {
                        response
                                .setResponseStatus(WebdavResponse.SC_BAD_REQUEST);
                    } else {
                        // process the response now ...
                        handler.process(request, response);
                    }

                    // finalize the response
                    response.finish();
                    // send response back to client
                    sendResponse(response, request);

                    // this input stream won't be actually closed
                    // if the keep-alive set to true
                    wrappedInputStream.close();

                    // check if the socket connection
                    // should be closed or not
                    // TODO: timeout to free this connection if no
                    // request comes in
                    keepAlive = request.getKeepAlive();
                }
            } catch (Exception ex) {
                _log.log("Error processign request", ex);
                WebdavResponse response = new WebdavResponse();
                response.setResponseStatus(WebdavResponse.SC_INTERNAL_ERROR);
                sendResponse(response, request);
            } finally {
                try {
                    _output.flush();
                } catch (Exception ex) {
                }
                try {
                    _output.close();
                } catch (Exception ex) {
                }
                try {
                    _input.close();
                } catch (Exception ex) {
                }
            }
        }

        public void sendResponse(WebdavResponse response, WebdavRequest request) {
            PrintWriter pw = new PrintWriter(_output);
            pw.println("HTTP/1.1 " + response.getResponseStatus() + " "
                    + response.getStatusMessage());
            pw.println("Server: " + SERVER_NAME);
            for (Iterator iter = response.getHeaders(); iter.hasNext();) {
                String key = (String) iter.next();
                pw.println(key + ": " + response.getHeader(key));
            }
            pw.println("Date: " + Util.getHttpDate());
            pw.println();
            pw.flush();

            if (response.hasContent()) {
                InputStream in = response.getContentInputStream();
                try {
                    if (in != null) {
                        try {
                            int read = -1;
                            byte[] copyBuffer = new byte[1024 * 5];

                            while ((read = in.read(copyBuffer, 0,
                                    copyBuffer.length)) != -1) {
                                _output.write(copyBuffer, 0, read);
                            }
                        } finally {
                            // IMPORTANT: don't close the output stream
                            // we may have a Keep-Alive connection
                            in.close();
                            _output.flush();
                        }
                    }
                } catch (IOException ex) {
                    // that's bad to have an exception here ... as it's too late
                    // to send back an error to the client. Sniff.

                    // for now ... let's just print the exception
                    // TODO: proper error management required here
                    _log.log("Unexpected error", ex);
                }
            }

            // Log the response
            _log.log(_log.ts() + ": {" + response.getResponseStatus() + ","
                    + response.getHeader("Content-Length") + "} for "
                    + request.getStartLine());
        }
    }
}
