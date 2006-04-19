package org.carion.s3ftp;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.util.Date;
import java.util.List;

import org.carion.s3.S3Repository;

public class FtpDirectory {
    private String _name;

    private final S3Repository _repository;

    FtpDirectory(S3Repository repository) {
        _repository = repository;
        _name = "/";
    }

    public String getName() {
        return _name;
    }

    boolean setDirectory(String directory) {
        return true;
    }

    boolean cdup() {
        return true;
    }

    List getChildren() {
        return null;
    }

    BufferedReader getReader(String name) {
        return null;
    }

    BufferedInputStream getInputStream(String name) {
        return null;
    }

    File getTempFile(String name) {
        return null;
    }

    void sendFile(File file, String name) {
    }

    boolean delete(String name) {
        return true;
    }

    boolean childExists(String name) {
        return true;
    }

    boolean renameChild(String from, String to) {
        return true;
    }

    boolean makeDirectory(String fileName) {
        return true;
    }

    boolean deleteDirectory(String fileName) {
        return true;
    }

    public class Child {
        private final String _name;

        private final Date _date;

        private final long _size;

        private final boolean _isDirectory;

        Child(String name, Date date, long size, boolean isDirectory) {
            _name = name;
            _date = date;
            _size = size;
            _isDirectory = isDirectory;
        }

        public Date getDate() {
            return _date;
        }

        public String getName() {
            return _name;
        }

        public long getSize() {
            return _size;
        }

        public boolean isDirectory() {
            return _isDirectory;
        }
    }
}
