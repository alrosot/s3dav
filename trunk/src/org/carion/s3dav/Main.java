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
package org.carion.s3dav;

import org.carion.s3dav.log.S3LogImpl;
import org.carion.s3dav.repository.S3Log;
import org.carion.s3dav.repository.WebdavRepository;
import org.carion.s3dav.s3.Credential;
import org.carion.s3dav.s3.CredentialFactory;
import org.carion.s3dav.s3.WebdavRepositoryImpl;
import org.carion.s3dav.webdav.WebdavServer;

public class Main {
    private final static int WEBDAVSERVER_PORT = 8070;

    public static void main(String[] args) {
        try {
            S3Log log = new S3LogImpl(System.out);
            log.log("s3DAV - version:" + Version.VERSION);
            Credential credential = CredentialFactory.getCredential();
            WebdavRepository repository = new WebdavRepositoryImpl(credential,
                    log.getLogger(">s3>"));
            WebdavServer X = new WebdavServer(WEBDAVSERVER_PORT, repository,
                    log.getLogger(">dav>"));
            X.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
