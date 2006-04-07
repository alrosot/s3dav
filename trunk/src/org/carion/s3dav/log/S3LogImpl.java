package org.carion.s3dav.log;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class S3LogImpl implements S3Log {
    private static final DateFormat DF = new SimpleDateFormat(
            "dd/MMM/yyyy:HH:mm:ss Z");

    private final PrintStream _out;

    private final String _prefix;

    public S3LogImpl(PrintStream out) {
        this(out, "");
    }

    public S3LogImpl(PrintStream out, String prefix) {
        _out = out;
        _prefix = prefix;
    }

    public void log(String message) {
        _out.println(_prefix + " " + message);
    }

    public void log(String message, Throwable ex) {
        _out.println(_prefix + " " + message);
        ex.printStackTrace(_out);
    }

    public S3Log getLogger(String prefix) {
        return new S3LogImpl(_out, _prefix + prefix);
    }

    public String ts() {
        return (DF.format(new Date()));
    }

    public void eol() {
        _out.println("");
    }

}
