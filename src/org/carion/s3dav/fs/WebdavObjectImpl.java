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
package org.carion.s3dav.fs;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.carion.s3dav.repository.WebdavObject;

public abstract class WebdavObjectImpl implements WebdavObject {
    protected final File _file;

    protected final String _uri;

    private String _name;

    WebdavObjectImpl(File file, String uri) {
        _file = file;
        _uri = uri;
    }

    public String getURI() {
        return _uri;
    }

    public String getName() {
        if (_name == null) {
            int slash = _uri.lastIndexOf('/');
            if (slash >= 0) {
                _name = _uri.substring(slash + 1);
            } else {
                _name = _uri;
            }
        }
        return _name;
    }

    public Date getCreationDate() throws IOException {
        // No easy way (?) to get the creation date in java
        return new Date(_file.lastModified());
    }

    public Date getLastModified() throws IOException {
        return new Date(_file.lastModified());
    }

}
