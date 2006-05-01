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
import org.carion.s3.admin.AdminServer;
import org.carion.s3.impl.CredentialFactory;
import org.carion.s3.impl.S3RepositoryImpl;
import org.carion.s3.util.LogWriter;
import org.carion.s3.util.S3LogImpl;
import org.carion.s3dav.webdav.WebdavServer;
import org.carion.s3ftp.FtpServer;

public class Main {

    public static void main(String[] args) {
        String userHome = System.getProperty("user.home");
        File s3DavDir = new File(userHome, "s3dav");
        if (!s3DavDir.isDirectory() && !s3DavDir.mkdirs()) {
            throw new RuntimeException("Can't create directory:" + s3DavDir);
        }

        // check if user wants to create credentials
        if ((args.length == 3) && ("-x".equals(args[0].trim()))) {
            createCredentials(s3DavDir, args[1], args[2]);
            System.exit(0);
        }

        int adminServerPort = getPortValue(args, 0, 8060);
        int webdavServerPort = getPortValue(args, 1, 8070);
        int ftpServerPort = getPortValue(args, 2, 21);

        try {
            // 1) Initialize log mechanism
            File logDir = new File(s3DavDir, "log");
            if (!logDir.isDirectory() && !logDir.mkdirs()) {
                throw new RuntimeException("Can't create directory:" + logDir);
            }
            LogWriter logWriter = new LogWriter(true, logDir);
            S3Log log = new S3LogImpl(logWriter);
            log.log("s3DAV - version:" + Version.VERSION);

            // 2) Initialize s3 repository access
            Credential credential = CredentialFactory.getCredential(s3DavDir);

            File uploadDir = new File(s3DavDir, "upload");
            if (!uploadDir.isDirectory() && !uploadDir.mkdirs()) {
                throw new RuntimeException("Can't create directory:"
                        + uploadDir);
            }

            S3RepositoryImpl repository = new S3RepositoryImpl(credential,
                    s3DavDir, uploadDir, log.getLogger(">s3>"));

            // 3) Initialize admin server
            if (adminServerPort > 0) {
                AdminServer adminServer = new AdminServer(adminServerPort,
                        repository, logWriter, repository.getUploadManager(),
                        log.getLogger(">admin>"));
                adminServer.start();
            }

            // 4) Initialize s3DAV server
            if (webdavServerPort > 0) {
                WebdavServer webdavServer = new WebdavServer(webdavServerPort,
                        repository, log.getLogger(">dav>"));
                webdavServer.start();
            }

            // 5) Initialize ftp server
            if (ftpServerPort > 0) {
                FtpServer ftpServer = new FtpServer("s3dav", "s3dav",
                        ftpServerPort, repository, log.getLogger(">ftp>"));
                ftpServer.start();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static int getPortValue(String[] args, int numParam,
            int defaultValue) {
        int result = 0;

        if (args.length >= (numParam + 1)) {
            try {
                result = Integer.parseInt(args[numParam]);
            } catch (NumberFormatException ex) {
                System.out.println("Invalid value for port number:("
                        + args[numParam] + ")");
                System.exit(1);
            }
        } else {
            result = defaultValue;
        }
        return result;
    }

    private static void createCredentials(File s3DavDir, String accessKey,
            String secretKey) {
        System.out.println("Credential creation ...");
        CredentialFactory.newCredential(s3DavDir, accessKey, secretKey);
        System.out.println("Credential created");
    }
}
