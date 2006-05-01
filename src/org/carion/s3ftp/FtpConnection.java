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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.carion.s3.S3Log;
import org.carion.s3dav.Version;

/**
 * Ftp protocol described here: http://www.faqs.org/rfcs/rfc959.html
 * 
 * Interesting reading about PASSIVE vs ACTIVE ftp :
 * http://slacksite.com/other/ftp.html
 * 
 * X commands described here http://www.wu-ftpd.org/rfc/rfc775.html
 * 
 * @author pcarion
 * 
 */
public class FtpConnection implements Runnable {
    private final S3Log _log;

    private final FtpServer _ftpServer;

    private final Socket _socket;

    private final FtpDirectory _directory;

    private boolean _running = false;

    private boolean _inBinaryMode = false;

    private BufferedReader _input;

    private PrintWriter _output;

    private boolean _isLoggedIn;

    private String renameFrom;

    private Socket clientSocket;

    private String clientAddress;

    private int clientPort;

    private int resumePosition;

    // this date is used to decide which date format to use for file date
    // in list files command
    private final Date _sixMonthsAgo;

    private final static long SIX_MONTHS_DELTA = (long) (6L * 30L * 24L * 3600L * 1000L);

    public FtpConnection(FtpServer ftpServer, Socket socket,
            FtpDirectory directory, S3Log log) {
        _log = log;
        _isLoggedIn = false;
        _ftpServer = ftpServer;

        _socket = socket;
        clientPort = 0;

        _directory = directory;

        resumePosition = 0;

        Date now = new Date();
        long time = now.getTime();
        _sixMonthsAgo = new Date(time - SIX_MONTHS_DELTA);
    }

    public void start() {
        _running = true;
        try {
            _output = new PrintWriter(new OutputStreamWriter(_socket
                    .getOutputStream()), true);
            _input = new BufferedReader(new InputStreamReader(_socket
                    .getInputStream()));
        } catch (Exception e) {
            _log.log("Error creating flows", e);
            return;
        }
        new Thread(this).start();
    }

    public void stop() {
        _running = false;
        _ftpServer.signalConnectionTerminated(this);
    }

    public void run() {
        String incomingString = null;

        output("220 " + Version.USER_AGENT);

        while (_running) {
            try {
                incomingString = _input.readLine();
                if (incomingString == null) {
                    throw new IOException(
                            "Client inflow interrupted during standard processing");
                } else {
                    parse(incomingString);
                }
            } catch (Exception e) {
                _log.log("Error reading command", e);
                stop();
            }
        }
        _log.log("Client terminated connection normally");
        stop();
    }

    private void parse(String command) {
        StringTokenizer st = new StringTokenizer(command);
        String ftpCommand = null;

        ftpCommand = st.nextToken();

        _log.log("parse> " + command);

        try {

            if (ftpCommand.equalsIgnoreCase("USER")) {
                user(st.nextToken());
            } else if (ftpCommand.equalsIgnoreCase("PASS")) {
                pass(st.nextToken());
            } else {

                if (!_isLoggedIn) {
                    output("530 Login incorrect");
                } else {
                    if (ftpCommand.equalsIgnoreCase("PWD")
                            || ftpCommand.equalsIgnoreCase("XPWD")) {
                        pwd();
                    } else if (ftpCommand.equalsIgnoreCase("SYST")) {
                        syst();
                    } else if (ftpCommand.equalsIgnoreCase("QUIT")) {
                        quit();
                    } else if (ftpCommand.equalsIgnoreCase("LIST")) {
                        list(false);
                    } else if (ftpCommand.equalsIgnoreCase("NLST")) {
                        list(true);
                    } else if (ftpCommand.equalsIgnoreCase("CDUP")) {
                        cdup();
                    } else if (ftpCommand.equalsIgnoreCase("CWD")) {
                        cwd(allRemainingTokens(st).trim());
                    } else if (ftpCommand.equalsIgnoreCase("RETR")) {
                        retr(allRemainingTokens(st).trim());
                    } else if (ftpCommand.equalsIgnoreCase("TYPE")) {
                        type(allRemainingTokens(st).trim());
                    } else if (ftpCommand.equalsIgnoreCase("STRU")) {
                        stru(allRemainingTokens(st).trim());
                    } else if (ftpCommand.equalsIgnoreCase("MODE")) {
                        mode(allRemainingTokens(st).trim());
                    } else if (ftpCommand.equalsIgnoreCase("STOR")) {
                        stor(allRemainingTokens(st).trim());
                        // } else if (ftpCommand.equalsIgnoreCase("APPE")) {
                        // stor(allRemainingTokens(st).trim(), true);
                    } else if (ftpCommand.equalsIgnoreCase("PORT")) {
                        port(allRemainingTokens(st).trim());
                    } else if (ftpCommand.equalsIgnoreCase("DELE")) {
                        dele(allRemainingTokens(st).trim());
                    } else if (ftpCommand.equalsIgnoreCase("NOOP")) {
                        noop();
                    } else if (ftpCommand.equalsIgnoreCase("REST")) {
                        rest(allRemainingTokens(st).trim());
                    } else if (ftpCommand.equalsIgnoreCase("RNFR")) {
                        rnfr(allRemainingTokens(st).trim());
                        // } else if (ftpCommand.equalsIgnoreCase("RNTO")) {
                        // rnto(allRemainingTokens(st).trim());
                    } else if (ftpCommand.equalsIgnoreCase("MKD")) {
                        mkd(allRemainingTokens(st).trim());
                    } else if (ftpCommand.equalsIgnoreCase("RMD")
                            || ftpCommand.equalsIgnoreCase("XRMD")) {
                        rmd(allRemainingTokens(st).trim());
                    } else {
                        output("502 " + ftpCommand + " command not supported");
                    }
                }
            }
        } catch (Exception ex) {
            _log.log("Error processing:" + ftpCommand, ex);
            output("550 " + ftpCommand + " command failed");
        }
    }

    /**
     * Creates a single string that contains all the tokens that have not yet
     * been parsed out of a given StringTokenizer.
     */
    private String allRemainingTokens(StringTokenizer st) {
        String r = new String();

        while (st.hasMoreTokens()) {
            r = r + st.nextToken() + " ";
        }

        return r;
    }

    public void user(String userName) {
        if (_ftpServer.isValidUser(userName)) {
            output("331 Password required for " + userName);
        } else {
            output("530 Login incorrect");
        }
    }

    public void pass(String password) {
        _isLoggedIn = _ftpServer.isValidPassword(password);
        if (_isLoggedIn) {
            output("230 user logged in");
        } else {
            output("530 Login incorrect");
        }
    }

    public void pwd() {
        output("257 \"" + _directory.getName() + "\" is current directory");
    }

    public void syst() {
        output("215 " + Version.USER_AGENT);
    }

    public void quit() {
        output("221 Goodbye!");
        endSession();
    }

    public void cwd(String directory) throws IOException {
        if (_directory.setDirectory(directory)) {
            output("250 CWD command successfull");
        } else {
            output("550 CWD command failed");
        }
    }

    public void cdup() {
        if (_directory.cdup()) {
            output("250 CDUP command successfull");
        } else {
            output("550 CDUP command failed");
        }
    }

    /**
     * This outputs a list of filenames to the data connection. It will send
     * details if the shortForm parameter is false, otherwise it sends just a
     * stripped down list of file names.
     */
    public void list(boolean shortForm) throws IOException {
        Socket dataSocket = getDataConnection();
        output("150 Opening ASCII mode data connection for LIST command");

        List children = _directory.getChildren();

        try {
            OutputStream out = null;

            out = dataSocket.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));

            if (children.size() > 0) {
                for (Iterator iter = children.iterator(); iter.hasNext();) {
                    FtpDirectory.Child child = (FtpDirectory.Child) iter.next();

                    if (shortForm) {
                        writer.println(child.getName());
                    } else {
                        if (child.isDirectory()) {
                            writer.println("dr-xr-xr-x\t1\towner group\t0\t"
                                    + child.getFtpDate(_sixMonthsAgo) + "\t"
                                    + child.getName());
                        } else {
                            writer.println("-r-xr-xr-x\t1\towner\tgroup\t"
                                    + child.getSize() + "\t"
                                    + child.getFtpDate(_sixMonthsAgo) + "\t"
                                    + child.getName());
                        }
                    }
                }
            }
            writer.close();
            out.close();
            dataSocket.close();
            output("226 ASCII transfer complete");
        } catch (IOException ex) {
            _log.log("Can't list content of:" + _directory.getName(), ex);
            output("550 CDUP command failed");
        }
    }

    public void type(String commandArgs) {
        if (commandArgs.equalsIgnoreCase("I")) {
            _inBinaryMode = true;
            output("200 Type set to I");
        } else {
            _inBinaryMode = false;
            output("200 Type set to A");
        }
    }

    public void retr(String fileName) throws IOException {
        if (_inBinaryMode) {
            retrI(fileName);
        } else {
            retrA(fileName);
        }
    }

    private void retrA(String fileName) throws IOException {
        PrintWriter writer = null;
        BufferedReader dataStream = _directory.getReader(fileName);
        String line = null;

        if (dataStream == null) {
            System.out
                    .println("Error sending file in ASCII format for RETR, file not found, or object not in child list");
            return;
        }

        Socket dataSocket = getDataConnection();
        output("150 Opening ASCII mode data connection for " + fileName);

        try {
            if (resumePosition > 0) {
                dataStream.skip(resumePosition);
            }
            writer = new PrintWriter(dataSocket.getOutputStream());

            line = dataStream.readLine();
            while (line != null) {
                if (line != null) {
                    writer.println(line);
                    writer.flush();
                }
                line = dataStream.readLine();
            }

            writer.close();
            dataSocket.close();
            dataStream.close();
        } catch (Exception e) {
            System.out.println("Error sending file in ASCII format for RETR");
            System.out.println(e);
            return;
        }
        output("226 ASCII transfer complete");
    }

    private void retrI(String fileName) throws IOException {
        BufferedOutputStream writer = null;
        BufferedInputStream dataStream = _directory.getInputStream(fileName);
        int byt = -1;

        if (dataStream == null) {
            System.out
                    .println("Error sending file in BINARY format for RETR, file not found, or object not in child list");
            return;
        }

        Socket listSocket = getDataConnection();
        output("150 Opening BINARY mode data connection for " + fileName);

        try {
            if (resumePosition > 0) {
                dataStream.skip(resumePosition);
            }
            writer = new BufferedOutputStream(listSocket.getOutputStream(), 1);

            byt = dataStream.read();
            while (byt >= 0) {
                if (byt >= 0) {
                    writer.write(byt);
                    writer.flush();
                }
                byt = dataStream.read();
            }

            writer.close();
            listSocket.close();
            dataStream.close();
        } catch (Exception e) {
            System.out.println("Error sending file in BINARY format for RETR");
            System.out.println(e);
            return;
        }

        output("226 BINARY transfer complete");
    }

    public void stru(String commandArgs) {
        if (commandArgs.equalsIgnoreCase("F")) {
            output("200 Structure set to F");
        } else if (commandArgs.equalsIgnoreCase("R")) {
            output("504 Structure cannot be set to R");
        } else if (commandArgs.equalsIgnoreCase("P")) {
            output("504 Structure cannot be set to P");
        } else {
            output("504 Structure cannot be set to " + commandArgs);
        }
    }

    public void mode(String commandArgs) {
        if (commandArgs.equalsIgnoreCase("S")) {
            output("200 Mode set to S (Stream)");
        } else if (commandArgs.equalsIgnoreCase("B")) {
            output("504 Mode cannot be set to B (Block)");
        } else if (commandArgs.equalsIgnoreCase("C")) {
            output("504 Mode cannot be set to C (Compressed)");
        } else {
            output("504 Mode cannot be set to " + commandArgs);
        }
    }

    public void noop() {
        output("200 NOOP succeeded");
    }

    /**
     * This will STORe a file on the server. If the append argument is true this
     * method will APPEnd to the existing file.
     */
    public void stor(String fileName) throws IOException {
        Socket dataSocket = getDataConnection();
        output("150 Opening BINARY mode data connection to receive " + fileName);
        BufferedInputStream incomingData = new BufferedInputStream(dataSocket
                .getInputStream());
        _directory.upload(fileName, incomingData);
        incomingData.close();

        if (_inBinaryMode) {
            output("226 BINARY transfer complete");

        } else {
            output("226 ASCII transfer complete");
        }
    }

    public void port(String commandArgs) {
        StringTokenizer st = new StringTokenizer(commandArgs, ",");
        int portHi, portLo;

        clientAddress = new String(st.nextToken() + "." + st.nextToken() + "."
                + st.nextToken() + "." + st.nextToken());

        try {
            portHi = new Integer(st.nextToken()).intValue();
            portLo = new Integer(st.nextToken()).intValue();
            clientPort = (portHi << 8) | portLo;
        } catch (Exception e) {
            _log.log("PORT command failed, "
                    + "could not interperate port value", e);
        }
        output("200 PORT command succeded");
    }

    /**
     * This method deletes the file named by the commandArgs string. The file
     * must be in the current directory (i.e. a child of the current object).
     */
    public void dele(String fileName) throws IOException {
        _directory.delete(fileName);
        output("250 DELE command succeded, " + fileName + "deleted.");
    }

    /**
     * This sets the offset from the beginning of the file at which transfers
     * will begin.
     */
    public void rest(String commandArgs) {
        int position;

        try {
            position = Integer.parseInt(commandArgs);
        } catch (Exception e) {

            output("504 Resume position cannot be set to " + commandArgs);
            return;
        }

        /**
         * Sets the position in a file at which transfers begin.
         */
        resumePosition = position;
        output("350 REST command succeded, transfers will begin at " + position);
    }

    /**
     * Set the filename from which we will be renaming. This is the first half
     * of the complete rename command.
     */
    public void rnfr(String fileName) throws IOException {
        if (_directory.childExists(fileName)) {
            /**
             * Set the source name for a rename command
             */
            renameFrom = fileName;
            output("350 RNFR command succeded, send RNTO command to proceed");
        } else {
            output("550 File specified by RNFR does not exist");
        }
    }

    /**
     * Set the new name of the file we are about to rename, then actually rename
     * it. This is the second half of the rename command.
     */
    public void rnto(String fileName) throws IOException {
        _directory.renameChild(renameFrom, fileName);
        renameFrom = "";
        output("250 RNTO command succeded");
    }

    /**
     * Create a directory below the currently active directory.
     */
    public void mkd(String fileName) throws IOException {
        _directory.makeDirectory(fileName);
        output("257 Directory created");
    }

    /**
     * Delete a directory below the currently active directory. This method will
     * trim the path to its last element before passing it to the delete method
     * of the object.
     */
    public void rmd(String fileName) throws IOException {
        String fixedName = fileName;

        if (fileName.indexOf('\\') > 0) {
            fixedName = fileName.substring(fileName.lastIndexOf('\\') + 1);
        } else if (fileName.indexOf('/') > 0) {
            fixedName = fileName.substring(fileName.lastIndexOf('/') + 1);
        }

        _directory.deleteDirectory(fixedName);
        output("250 Directory '" + fixedName + "' removed");
    }

    private void output(String out) {
        _log.log("--> (" + out + ")");
        try {
            _output.println(out);
        } catch (Exception e) {
            _log.log("Error sending response (" + out + ")", e);
        }
    }

    public void endSession() {
        try {
            clientSocket.close();
        } catch (Exception e) {
        }
        try {
            _input.close();
        } catch (Exception e) {
        }
        try {
            _output.close();
        } catch (Exception e) {
        }
        try {
            _socket.close();
        } catch (Exception e) {
        }

        stop();
    }

    private Socket getDataConnection() {
        if (clientPort > 0) {
            try {
                clientSocket = new Socket(clientAddress, clientPort);
            } catch (Exception e) {
                return null;
            }
            return clientSocket;
        }
        return null;
    }

    /**
     * Returns a usefull string representation of this FTPConnection object.
     */
    public String toString() {
        return "Connection in " + _directory.getName();
    }
}
