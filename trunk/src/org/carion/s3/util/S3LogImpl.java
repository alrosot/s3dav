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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.carion.s3.S3Log;

public class S3LogImpl implements S3Log {
    private static final DateFormat DF = new SimpleDateFormat(
            "dd/MMM/yyyy:HH:mm:ss Z");

    private final LogWriter _writer;

    private final String _prefix;

    public S3LogImpl(LogWriter writer) {
        this(writer, "");
    }

    public S3LogImpl(LogWriter writer, String prefix) {
        _writer = writer;
        _prefix = prefix;
    }

    public void log(String message) {
        _writer.log(_prefix + " " + message);
    }

    public void log(String message, Throwable ex) {
        _writer.log(_prefix + " " + message);

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        String stacktrace = sw.toString();

        _writer.log(stacktrace);
    }

    public S3Log getLogger(String prefix) {
        return new S3LogImpl(_writer, _prefix + prefix);
    }

    public String ts() {
        return (DF.format(new Date()));
    }

    public void eol() {
        _writer.log("");
    }
}
