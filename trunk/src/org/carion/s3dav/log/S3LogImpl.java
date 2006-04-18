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
package org.carion.s3dav.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.carion.s3.S3Log;

public class S3LogImpl implements S3Log {
    private static final DateFormat DF = new SimpleDateFormat(
            "dd/MMM/yyyy:HH:mm:ss Z");

    private static final String USER_HOME = System.getProperty("user.home");

    private final long MAX_FILE_LOG_SIZE = 5000;

    private long _currentSize;

    private final PrintStream _out;

    private PrintStream _outFile;

    private final String _prefix;

    private final File _file0;

    private final File _file1;

    public S3LogImpl(PrintStream out) {
        this(out, "");
    }

    public S3LogImpl(PrintStream out, String prefix) {
        _out = out;
        _prefix = prefix;
        File logDir = new File(USER_HOME, "s3dav/log");
        logDir.mkdirs();

        _file0 = new File(logDir, "log0.txt");
        _file1 = new File(logDir, "log1.txt");
        reOpenLog();
    }

    public void log(String message) {
        _out.println(_prefix + " " + message);
        fPrintln(_prefix + " " + message);
    }

    public void log(String message, Throwable ex) {
        _out.println(_prefix + " " + message);
        fPrintln(_prefix + " " + message);

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stacktrace = sw.toString();

        _out.println(stacktrace);
        fPrintln(stacktrace);
    }

    public S3Log getLogger(String prefix) {
        return new S3LogImpl(_out, _prefix + prefix);
    }

    public String ts() {
        return (DF.format(new Date()));
    }

    public void eol() {
        _out.println("");
        fPrintln("");
    }

    private synchronized void fPrintln(String message) {
        if (_outFile != null) {
            _outFile.println(message);
            _currentSize += message.length();
            if (_currentSize > MAX_FILE_LOG_SIZE) {
                _outFile.close();
                _file1.delete();
                _file0.renameTo(_file1);
                _file0.delete();
                reOpenLog();
            }
        }
    }

    private void reOpenLog() {
        try {
            _outFile = new PrintStream(new FileOutputStream(_file0, true));
            _currentSize = _file0.length();
        } catch (IOException ex) {
            _out.println("Can't open (" + _file0 + ")");
        }
    }

    public synchronized String getRawLogs() {
        _outFile.close();
        StringBuffer sb = new StringBuffer();
        try {
            getRawLogs(_file1, sb);
        } catch (IOException ex) {
            ex.printStackTrace();
            sb.append("!! error reading:" + _file1);
        }
        try {
            getRawLogs(_file0, sb);
        } catch (IOException ex) {
            ex.printStackTrace();
            sb.append("!! error reading:" + _file0);
        }
        reOpenLog();
        return sb.toString();
    }

    private void getRawLogs(File f, StringBuffer sb) throws IOException {
        if (!f.exists()) {
            return;
        }

        BufferedReader in = new BufferedReader(new FileReader(f));
        String str;
        while ((str = in.readLine()) != null) {
            sb.append(str);
            sb.append("\n");
        }
        in.close();
    }
}
