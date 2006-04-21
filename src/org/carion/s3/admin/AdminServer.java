package org.carion.s3.admin;

import java.io.IOException;

import org.carion.s3.S3Folder;
import org.carion.s3.S3Log;
import org.carion.s3.S3Object;
import org.carion.s3.S3Repository;
import org.carion.s3.S3Resource;
import org.carion.s3.S3UploadManager;
import org.carion.s3.S3UrlName;
import org.carion.s3.admin.htmlPages.AdminPage;
import org.carion.s3.admin.htmlPages.BrowserPage;
import org.carion.s3.http.HttpProcessing;
import org.carion.s3.http.HttpRequest;
import org.carion.s3.http.HttpResponse;
import org.carion.s3.http.HttpServer;
import org.carion.s3.util.LogWriter;
import org.carion.s3.util.Util;

public class AdminServer extends HttpServer implements HttpProcessing {
    private final LogWriter _logWriter;

    private final S3UploadManager _uploadManager;

    public AdminServer(int port, S3Repository repository, LogWriter logWriter,
            S3UploadManager uploadmanager, S3Log log) {
        super(port, repository, log);
        _logWriter = logWriter;
        _uploadManager = uploadmanager;
    }

    protected void init(S3Repository repository) {
    }

    protected HttpProcessing getProcessing(HttpRequest request) {
        return this;
    }

    public void process(HttpRequest request, HttpResponse response)
            throws IOException {
        S3UrlName href = request.getUrl();

        // catch request to admin pages
        if (href.getUri().startsWith("/index.html")) {
            AdminPage page = new AdminPage(request, _repository,
                    _uploadManager, _logWriter);
            String body = page.getHtmlPage();

            response.setResponseHeader("last-modified", Util.getHttpDate());

            response.setResponseBody(body, "text/html");
        } else if (_repository.isAvailable()) {
            boolean exists = _repository.objectExists(href);
            if (exists) {
                boolean isDirectory = _repository.isFolder(href);

                if (isDirectory) {
                    S3Folder folder = _repository.getFolder(href);
                    writeFolder(folder, response, request);
                } else {
                    S3Resource resource = _repository.getResource(href);
                    setHeaders(resource, response);
                    response.setContentStream(resource.getContent());
                }
            } else {
                response.setResponseStatus(HttpResponse.SC_NOT_FOUND);
            }
        } else {
            response.setResponseStatus(HttpResponse.SC_FORBIDDEN);
        }

    }

    protected void setHeaders(S3Resource resource, HttpResponse response)
            throws IOException {
        // set HTTP headers
        response.setResponseHeader("last-modified", Util.getHttpDate(resource
                .getLastModified()));

        response.setResponseHeader("Content-Length", String.valueOf(resource
                .getLength()));

        response.setContentType(resource.getContentType());
    }

    private void writeFolder(S3Folder folder, HttpResponse response,
            HttpRequest request) throws IOException {
        BrowserPage page = new BrowserPage();
        String body = page.getFolderHtmlPage(folder, request, _repository);
        // String body = getFolderHtmlPage(folder, request);

        response.setResponseHeader("last-modified", Util.getHttpDate());

        response.setResponseBody(body, "text/html");
    }

    /*
     * If you need an extra proof, that java generated HTML code is ugly, here
     * it is ...
     */
    protected String getFolderHtmlPage(S3Folder folder, HttpRequest request)
            throws IOException {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><header><title>List of files in:"
                + folder.getUrl().getUri() + "</title>");
        sb.append("<style type=\"text/css\">");
        sb.append("body {");
        sb.append("    background-color: #FFFFFF;");
        sb.append("    font-size: small;");
        sb.append("    color: #000000;");
        sb.append("}");
        sb.append(".cell_0 {");
        sb.append("    background-color: #FFFFFF;");
        sb.append("    color: #000000;");
        sb.append("    font-size: small;");
        sb.append("    padding: 1px;");
        sb.append("    border: 1px;");
        sb.append("    border-style: solid;");
        sb.append("    border-color: #000000;");
        sb.append("    text-decoration: none;");
        sb.append("}");
        sb.append(".cell_1 {");
        sb.append("    background-color: #CCCCCC;");
        sb.append("    color: #000000;");
        sb.append("    font-size: small;");
        sb.append("    padding: 1px;");
        sb.append("    border: 1px;");
        sb.append("    border-style: solid;");
        sb.append("    border-color: #000000;");
        sb.append("    text-decoration: none;");
        sb.append("}");
        sb.append("</style>");
        sb
                .append("<meta http-equiv=\"cache-control\" content=\"no-cache\" forua=\"true\"/>");

        sb.append("</header>");
        sb.append("<body><table>");
        sb.append("<tr><th>File name</th><th>size</th><th>type</th></tr>");
        S3UrlName[] files = folder.getChildrenUris();

        int lineno = 0;
        String className;

        // pass #1: the directories
        for (int i = 0; i < files.length; i++) {
            S3UrlName uri = files[i];
            if (_repository.isFolder(uri)) {
                className = ((lineno % 2) == 0) ? "cell_0" : "cell_1";
                S3Folder res = _repository.getFolder(uri);
                sb.append("<tr><td class=\"" + className + "\"><a href=\""
                        + mkUrl(res, request) + "\">" + res.getName()
                        + "</a></td><td class=\"" + className
                        + "\">&nbsp;</td><td class=\"" + className
                        + "\" align=\"right\">Folder</td></tr>");
                lineno++;
            }
        }
        for (int i = 0; i < files.length; i++) {
            S3UrlName uri = files[i];
            if (_repository.isResource(uri)) {
                className = ((lineno % 2) == 0) ? "cell_0" : "cell_1";
                S3Resource res = _repository.getResource(uri);
                sb.append("<tr><td class=\"" + className + "\"><a href=\""
                        + mkUrl(res, request) + "\">" + res.getName()
                        + "</a></td><td class=\"" + className
                        + "\" align=\"right\">" + res.getLength()
                        + "</td><td class=\"" + className
                        + "\" align=\"right\">" + res.getContentType()
                        + "</td></tr>");
                lineno++;
            }
        }
        sb.append("</table></body></html>");

        return sb.toString();
    }

    private String mkUrl(S3Object res, HttpRequest request) {
        return "http://" + request.getHost() + res.getUrl().getUri();
    }

}
