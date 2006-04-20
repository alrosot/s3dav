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

import java.io.File;

import org.carion.s3.Credential;
import org.carion.s3.S3Log;
import org.carion.s3.S3Repository;
import org.carion.s3.admin.AdminServer;
import org.carion.s3.impl.CredentialFactory;
import org.carion.s3.impl.S3RepositoryImpl;
import org.carion.s3.util.LogWriter;
import org.carion.s3.util.S3LogImpl;
import org.carion.s3dav.webdav.WebdavServer;
import org.carion.s3ftp.FtpServer;

public class Main {
    private final static int ADMINSERVER_PORT = 8060;

    private final static int WEBDAVSERVER_PORT = 8070;

    private final static int FTPSERVER_PORT = 21;

    public static void main(String[] args) {
        try {
            String userHome = System.getProperty("user.home");
            File s3DavDir = new File(userHome, "s3dav");
            if (!s3DavDir.isDirectory() && !s3DavDir.mkdirs()) {
                throw new RuntimeException("Can't create directory:" + s3DavDir);
            }

            // 1) Initialize log mechanism
            File logDir = new File(s3DavDir, "log");
            if (!logDir.isDirectory() && !logDir.mkdirs()) {
                throw new RuntimeException("Can't create directory:" + logDir);
            }
            LogWriter logWriter = new LogWriter(true, logDir);
            S3Log log = new S3LogImpl(logWriter);
            log.log("s3DAV - version:" + Version.VERSION);

            // 2) Initialize s3 repository access
            Credential credential = CredentialFactory.getCredential();
            S3Repository repository = new S3RepositoryImpl(credential, log
                    .getLogger(">s3>"));

            // 3) Initialize admin server
            AdminServer adminServer = new AdminServer(ADMINSERVER_PORT,
                    repository, log.getLogger(">admin>"));
            adminServer.start();

            // 4) Initialize s3DAV server
            WebdavServer webdavServer = new WebdavServer(WEBDAVSERVER_PORT,
                    repository, log.getLogger(">dav>"));
            webdavServer.start();

            // 5) Initialize ftp server
            FtpServer ftpServer = new FtpServer("s3dav", "s3dav",
                    FTPSERVER_PORT, repository, log.getLogger(">ftp>"));
            ftpServer.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
