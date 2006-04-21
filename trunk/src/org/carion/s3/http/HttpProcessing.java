package org.carion.s3.http;

import java.io.IOException;

public interface HttpProcessing {
    void process(HttpRequest request, HttpResponse response) throws IOException;
}
