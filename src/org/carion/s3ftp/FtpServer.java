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
package org.carion.s3ftp;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.carion.s3.S3Log;
import org.carion.s3.S3Repository;

public class FtpServer extends Thread {
    private final S3Log _log;

    private final String _userName;

    private final String _password;

    private final int _port;

    private final List _connections = new ArrayList();

    private final S3Repository _repository;

    private ServerSocket _serverSocket;

    private boolean _running;

    public FtpServer(String userName, String password, int port,
            S3Repository repository, S3Log log) {
        _log = log;
        _repository = repository;
        _userName = userName;
        _password = password;
        _port = port;

        try {
            _serverSocket = new ServerSocket(_port);
        } catch (Exception e) {
            _log.log("Start failed", e);
        }
    }

    public void stopServer() {
        _running = false;
    }

    /**
     * Waits for and accepts client connections.
     */
    public void run() {
        FtpConnection newConnection;

        _running = true;

        _log.log("Listening on port:" + _serverSocket.getLocalPort());

        while (_running) {
            try {
                Socket incoming = _serverSocket.accept();
                newConnection = new FtpConnection(this, incoming,
                        new FtpDirectory(_repository), _log);
                newConnection.start();
                _connections.add(newConnection);
            } catch (Exception e) {
                _log.log("Accept failed", e);
            }
        }
    }

    /**
     * Called by an active FTPConnection object just as it becomes inactive,
     * normally, or due to an error.
     */
    public void signalConnectionTerminated(FtpConnection connection) {
        _connections.remove(connection);
    }

    boolean isValidUser(String userName) {
        return _userName.equals(userName);
    }

    boolean isValidPassword(String password) {
        return _password.equals(password);
    }
}
