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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.carion.s3.S3Folder;
import org.carion.s3.S3Repository;
import org.carion.s3.S3Resource;
import org.carion.s3.S3UrlName;
import org.carion.s3.impl.S3UrlNameImpl;
import org.carion.s3.impl.UploadResource;
import org.carion.s3.util.MimeTypes;

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
        S3UrlName s3Name = mkResourceName(directory);
        if (_repository.isFolder(s3Name)) {
            _name = s3Name.getUri();
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

        List uploads = _repository.getUploadManager().getUploadsInDirectory(
                folder.getUrl());
        for (Iterator iter = uploads.iterator(); iter.hasNext();) {
            S3UrlName uri = (S3UrlName) iter.next();
            S3Resource res = new UploadResource(uri);
            result.add(new Child(res.getName(), res.getLastModified(), res
                    .getLength(), false));
        }

        for (int i = 0; i < files.length; i++) {
            S3UrlName uri = files[i];
            Child child;
            if (_repository.isFolder(uri)) {
                S3Folder f = _repository.getFolder(uri);
                child = new Child(f.getName(), f.getLastModified(), 0, true);
            } else {
                S3Resource r = _repository.getResource(uri);
                boolean found = false;
                for (Iterator iter = uploads.iterator(); iter.hasNext();) {
                    if (uri.isSameUri((S3UrlName) iter.next())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    child = new Child(r.getName(), r.getLastModified(), r
                            .getLength(), false);

                } else {
                    child = null;
                }

            }
            if (child != null) {
                result.add(child);
            }
        }
        return result;
    }

    BufferedReader getReader(String name) throws IOException {
        S3UrlName s3Name = mkResourceName(name);
        if (!_repository.isResource(s3Name)) {
            throw new IOException("Invalid file name:" + s3Name.getUri());
        }
        S3Resource res = _repository.getResource(s3Name);
        InputStream in = res.getContent();
        return new BufferedReader(new InputStreamReader(in));
    }

    BufferedInputStream getInputStream(String name) throws IOException {
        S3UrlName s3Name = mkResourceName(name);
        if (!_repository.isResource(s3Name)) {
            throw new IOException("Invalid file name:" + s3Name.getUri());
        }
        S3Resource res = _repository.getResource(s3Name);
        InputStream in = res.getContent();
        return new BufferedInputStream(in);
    }

    void upload(String fileName, InputStream in) throws IOException {
        S3UrlName s3Name = mkResourceName(fileName);
        String contentType = MimeTypes.ext2mimeType(s3Name.getExt());
        S3Resource resource = _repository.getResource(s3Name);
        resource.setResourceContent(in, contentType, -1);
    }

    boolean delete(String name) throws IOException {
        S3UrlName s3Name = mkResourceName(name);
        if (!_repository.objectExists(s3Name)) {
            return false;
        }
        _repository.deleteObject(s3Name);
        return true;
    }

    boolean childExists(String name) throws IOException {
        S3UrlName s3Name = mkResourceName(name);
        return _repository.isResource(s3Name);
    }

    boolean renameChild(String from, String to) throws IOException {
        throw new IOException("rename child is not implemented");
    }

    void makeDirectory(String fileName) throws IOException {
        S3UrlName s3Name = mkResourceName(fileName);
        _repository.createFolder(s3Name);
    }

    boolean deleteDirectory(String fileName) throws IOException {
        S3UrlName s3Name = mkResourceName(fileName);
        if (_repository.isFolder(s3Name)) {
            S3Folder folder = _repository.getFolder(s3Name);
            folder.remove();
            return true;
        } else {
            return false;
        }
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
        if ((sb.length() > 1) && (sb.charAt(sb.length() - 1) == '/')) {
            return sb.substring(0, sb.length() - 1);
        } else {
            return sb.toString();
        }
    }

    private S3UrlName mkResourceName(String name) {
        String fName = cleanupName(name);
        String result;
        if (fName.startsWith("/")) {
            result = fName;
        } else {
            if (_name.equals("/")) {
                result = "/" + fName;
            } else {
                result = _name + "/" + fName;
            }
        }
        return new S3UrlNameImpl(result, false);
    }
}
