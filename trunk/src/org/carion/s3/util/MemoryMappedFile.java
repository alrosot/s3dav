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
package org.carion.s3.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class MemoryMappedFile {

    private final File _file;

    public static MemoryMappedFile mk() throws IOException {
        File memFile = File.createTempFile("s3dav", ".mem");
        return new MemoryMappedFile(memFile);
    }

    private MemoryMappedFile(File file) {
        _file = file;
    }

    public void copy(InputStream in, int length) throws IOException{
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(_file);
            byte[] data = new byte[1024];
            if (length > 0) {
                int tobeRead = length;

                while (tobeRead > 0) {
                    int len = in.read(data, 0, Math.min(tobeRead, data.length));
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
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public ByteBuffer getByteBuffer() throws IOException {
        FileChannel roChannel = new RandomAccessFile(_file, "r").getChannel();
        ByteBuffer roBuf = roChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                (int) roChannel.size());
        return roBuf;
    }

    public void delete() {
        _file.delete();
    }

}
