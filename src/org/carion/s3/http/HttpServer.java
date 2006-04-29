package org.carion.s3.http;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Iterator;

import org.carion.s3.S3Log;
import org.carion.s3.S3Repository;
import org.carion.s3.util.Util;
import org.carion.s3dav.Version;
import org.carion.s3dav.webdav.InternetInputStream;

public abstract class HttpServer extends Thread {
    private final static String BIND_ADDRESS = "127.0.0.1";

    private final int _port;

    protected final S3Repository _repository;

    protected final S3Log _log;

    protected boolean _repositoryAvailable;

    public HttpServer(int port, S3Repository repository, S3Log log) {
        _log = log;
        _port = port;
        _repository = repository;
    }

    abstract protected void init(S3Repository repository);

    abstract protected HttpProcessing getProcessing(HttpRequest request);

    public void run() {
        init(_repository);
        ServerSocket serversocket = null;

        try {
            _log.log("Listening on port:" + _port);
            // serversocket = new ServerSocket(_port);
            serversocket = new ServerSocket();
            SocketAddress sa = new InetSocketAddress(BIND_ADDRESS, _port);
            serversocket.bind(sa);
        } catch (Exception e) {
            _log.log("Can't listen on socket", e);
            return;
        }

        while (true) {
            try {
                Socket socket = serversocket.accept();
                // check if the repository availability has changed
                if (_repositoryAvailable ^ _repository.isAvailable()) {
                    init(_repository);
                }
                InetAddress client = socket.getInetAddress();
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();
                HttpHandler handler = new HttpHandler(client, input, output);
                handler.start();
            } catch (Exception e) {
                _log.log("Can't accept connections", e);
            }
        }
    }

    private class HttpHandler extends Thread {
        private final InetAddress _client;

        private final InputStream _input;

        private final OutputStream _output;

        private InternetInputStream _stream;

        public HttpHandler(InetAddress client, InputStream input,
                OutputStream output) {
            _client = client;
            _input = input;
            _output = output;
        }

        public void run() {
            HttpRequest request = null;
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

                    request = new HttpRequest(startLine, _client, _log);

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
                    HttpProcessing processing = getProcessing(request);

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
                    HttpResponse response = new HttpResponse();

                    // check if we have a handler to process this request
                    if (processing == null) {
                        response.setResponseStatus(HttpResponse.SC_BAD_REQUEST);
                    } else {
                        // process the response now ...
                        processing.process(request, response);
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
                HttpResponse response = new HttpResponse();
                response.setResponseStatus(HttpResponse.SC_INTERNAL_ERROR);
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

        public void sendResponse(HttpResponse response, HttpRequest request) {
            PrintWriter pw = new PrintWriter(_output);
            pw.println("HTTP/1.1 " + response.getResponseStatus() + " "
                    + response.getStatusMessage());
            pw.println("Server: " + Version.USER_AGENT);
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
