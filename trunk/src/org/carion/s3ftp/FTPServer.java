package org.carion.s3ftp;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.carion.s3.S3Log;
import org.carion.s3.S3Repository;
import org.carion.s3.util.S3LogImpl;

public class FTPServer extends Thread {
    private final S3Log _log;

    private final String _userName;

    private final String _password;

    private final int _port;

    private final List _connections = new ArrayList();

    private final S3Repository _repository;

    private ServerSocket _serverSocket;

    private boolean _running;

    public FTPServer(String userName, String password, int port,
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
        FTPConnection newConnection;

        _running = true;

        _log.log("Port: " + _serverSocket.getLocalPort());

        while (_running) {
            try {
                Socket incoming = _serverSocket.accept();
                newConnection = new FTPConnection(this, incoming,
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
    public void signalConnectionTerminated(FTPConnection connection) {
        _connections.remove(connection);
    }

    boolean isValidUser(String userName) {
        return _userName.equals(userName);
    }

    boolean isValidPassword(String password) {
        return _password.equals(password);
    }
}
