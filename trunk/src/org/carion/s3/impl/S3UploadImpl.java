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
package org.carion.s3.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.carion.s3.S3Log;
import org.carion.s3.S3UploadManager;
import org.carion.s3.S3UrlName;
import org.carion.s3.operations.ObjectPUT;
import org.carion.s3.operations.UploadNotification;

public class S3UploadImpl implements S3UploadManager.Upload {
    private final S3UrlName _name;

    private final File _file;

    private FileChannel _roChannel;

    private final S3UploadManagerImpl _manager;

    private final Cache _cache;

    private long _size;

    private UploadTask _task;

    S3UploadImpl(S3UrlName name, File baseDirectory, Cache cache,
            S3UploadManagerImpl manager) throws IOException {
        _name = name;
        _file = File.createTempFile("s3dav", ".mem", baseDirectory);
        _manager = manager;
        _cache = cache;
    }

    public S3UrlName getName() {
        return _name;
    }

    public long getSize() {
        return _size;
    }

    public long getUploaded() {
        if (_task == null) {
            return 0;
        } else {
            return _task.getUploaded();
        }
    }

    public int getPercentage() {
        return (int) ((getUploaded() * 100) / _size);
    }

    public File getStorageFile() {
        return _file;
    }

    public void abort() {
        if (_task != null) {
            _task.abort();
            _task = null;
        }
        close();
    }

    public int getState() {
        if (_task == null) {
            return STATE_NOT_STARTED;
        } else {
            return _task.getUploadState();
        }
    }

    public void close() {
        System.out.println("@@@ close:" + _file);
        try {
            _roChannel.close();
        } catch (Exception ex) {
        }
        if (!_file.delete()) {
            System.out.println("@@@ can't close:" + _file);
        }
    }

    void loadContent(InputStream in, long contentLength) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(_file);
            byte[] data = new byte[1024];
            if (contentLength > 0) {
                long tobeRead = contentLength;

                while (tobeRead > 0) {
                    int len = in.read(data, 0, Math.min((int) tobeRead,
                            data.length));
                    fos.write(data, 0, len);
                    tobeRead -= len;
                }
            } else {
                int len = 0;
                while ((len = in.read(data)) >= 0) {
                    fos.write(data, 0, len);
                }
            }
            fos.flush();
            fos.close();
            fos = null;
        } finally {
            _size = _file.length();
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    ByteBuffer getByteBuffer() throws IOException {
        _roChannel = new RandomAccessFile(_file, "r").getChannel();
        ByteBuffer roBuf = _roChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                (int) _roChannel.size());
        return roBuf;
    }

    public void asynchronousUpload(ObjectPUT ope, String contentType, S3Log log) {
        _task = new UploadTask(this, ope, contentType, log);
        Thread t = new Thread(_task);
        t.start();
    }

    private class UploadTask extends Thread implements UploadNotification {
        private final S3UploadImpl _upload;

        private final ObjectPUT _ope;

        private final String _contentType;

        private final S3Log _log;

        private int _state = 0;

        private long _uploaded = 0;

        private boolean _abort = false;

        UploadTask(S3UploadImpl upload, ObjectPUT ope, String contentType,
                S3Log log) {
            _upload = upload;
            _ope = ope;
            _contentType = contentType;
            _log = log;
        }

        public void run() {
            try {
                _state = STATE_STARTED;
                if (!_ope.execute(getByteBuffer(), _contentType, this)) {
                    throw new IOException("Can't PUT:" + _name.getResourceKey());
                }
                _state = STATE_FINISHED;
            } catch (IOException ex) {
                _log.log("Can't upload content for:" + _name.getUri(), ex);
                _state = STATE_ERROR;
            } finally {
                _upload.close();
                _manager.uploadDone(_upload, _state);
            }
        }

        public boolean ntfUploaded(int count) {
            _uploaded += count;
            _cache.delete(_name.getResourceKey());
            return !_abort;
        }

        public void abort() {
            _abort = true;
        }

        int getUploadState() {
            return _state;
        }

        long getUploaded() {
            return _uploaded;
        }
    }
}
