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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.carion.s3.S3Folder;
import org.carion.s3.S3Repository;
import org.carion.s3.S3Resource;
import org.carion.s3.S3UrlName;
import org.carion.s3.impl.S3UrlNameImpl;

public class FtpDirectory {
    private String _name;

    private final S3Repository _repository;

    private final static SimpleDateFormat _formatterRecent = new SimpleDateFormat(
            "MMM dd HH:mm");

    private final static SimpleDateFormat _formatterOld = new SimpleDateFormat(
            "MMM dd yyyy");

    FtpDirectory(S3Repository repository) {
        _repository = repository;
        _name = "/";
    }

    public String getName() {
        return _name;
    }

    boolean setDirectory(String directory) throws IOException {
        String newDirectory = cleanupName(directory);
        if (!newDirectory.startsWith("/")) {
            if (_name.equals("/")) {
                newDirectory = "/" + newDirectory;
            } else {
                newDirectory = _name + "/" + newDirectory;
            }
        }
        S3UrlName name = new S3UrlNameImpl(newDirectory, false);
        if (_repository.isFolder(name)) {
            _name = newDirectory;
            return true;
        } else {
            return false;
        }
    }

    boolean cdup() {
        S3UrlName name = new S3UrlNameImpl(_name, false);
        S3UrlName parent = name.getParent();
        if (parent == null) {
            return false;
        } else {
            _name = parent.getUri();
            return true;
        }
    }

    List getChildren() throws IOException {
        S3UrlName name = new S3UrlNameImpl(_name, false);
        S3Folder folder = _repository.getFolder(name);
        S3UrlName[] files = folder.getChildrenUris();
        List result = new ArrayList();
        // pass #1: the directories
        for (int i = 0; i < files.length; i++) {
            S3UrlName uri = files[i];
            Child child;
            if (_repository.isFolder(uri)) {
                S3Folder f = _repository.getFolder(uri);
                child = new Child(f.getName(), f.getLastModified(), 0, true);
            } else {
                S3Resource r = _repository.getResource(uri);
                child = new Child(r.getName(), r.getLastModified(), r
                        .getLength(), false);
            }
            result.add(child);
        }
        return result;
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

        public String getFtpDate(Date sixMonthsAgo) {
            if (_date.before(sixMonthsAgo)) {
                return _formatterOld.format(_date);
            } else {
                return _formatterRecent.format(_date);
            }
        }
    }

    private String cleanupName(String name) {
        name = name.trim();
        StringBuffer sb = new StringBuffer();
        boolean prevIsSlash = false;

        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ((c == '/') || (c == '\\')) {
                if (!prevIsSlash) {
                    sb.append('/');
                }
                prevIsSlash = true;
            } else {
                prevIsSlash = false;
                sb.append(c);
            }
        }
        if (sb.charAt(sb.length() - 1) == '/') {
            return sb.substring(0, sb.length() - 1);
        } else {
            return sb.toString();
        }
    }
}
