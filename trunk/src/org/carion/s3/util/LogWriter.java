package org.carion.s3.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

public class LogWriter {
    private final static long MAX_FILE_LOG_SIZE = 100000;

    private final boolean _toSystemOut;

    private final File _file0;

    private final File _file1;

    private long _currentSize;

    private PrintStream _outFile;

    public LogWriter(boolean toSystemOut, File logDir) {
        _toSystemOut = toSystemOut;
        logDir.mkdirs();

        _file0 = new File(logDir, "log0.txt");
        _file1 = new File(logDir, "log1.txt");
        reOpenLog();
    }

    synchronized void log(String message) {
        if (_toSystemOut) {
            System.out.println(message);
        }

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

    private void reOpenLog() {
        try {
            _outFile = new PrintStream(new FileOutputStream(_file0, true));
            _currentSize = _file0.length();
        } catch (IOException ex) {
            System.out.println("Can't open (" + _file0 + ")");
        }
    }

}
