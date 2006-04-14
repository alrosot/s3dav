package org.carion.s3dav.s3.naming;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class BaseResourceName {
    private final static String ENCODING = "UTF-8";

    private final static char[] HEXADECIMAL = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };


    String decode(String s) {
        if (s.length() == 0) {
            return "";
        }
        byte[] bytes = null;

        int len = s.length();
        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                buf.append(c);
            } else if (c == '+') {
                // + is always %xx from the webDAV server
                // so it's safe to assume that + should always
                // be considered as a ' '
                buf.append(' ');
            } else if (c == '%') {
                // we try to process all the %xx which could be there
                try {
                    if (bytes == null) {
                        // highly sufficient to store the decoded bytes
                        bytes = new byte[len];
                    }
                    int nbBytes = 0;

                    while (((i + 2) < len) && (c == '%')) {
                        bytes[nbBytes++] = (byte) Integer.parseInt(s.substring(
                                i + 1, i + 3), 16);
                        // let's go beyond the latest char in the %xx
                        i += 3;
                        if (i < len) {
                            c = s.charAt(i);
                        }
                    }
                    if ((i < len) && (c == '%')) {
                        throw new IllegalArgumentException(
                                "URLDecoder: Incomplete trailing escape (%) pattern");
                    }
                    buf.append(new String(bytes, 0, nbBytes, ENCODING));
                    i--; // to restart the outer loop at the right position
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "URLDecoder: Illegal hex characters in escape (%) pattern - "
                                    + e.getMessage());
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException("Unsupported encoding ("
                            + ENCODING + ")");
                }
            } else {
                // we consider that any other character is safe
                buf.append(c);
            }
        }
        return buf.toString();
    }

    String encode(String s, boolean plusForSpace) {
        if (s.length() == 0) {
            return "";
        }
        int len = s.length();
        StringBuffer buf = new StringBuffer();

        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(bos, ENCODING);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Unsupported encoding (" + ENCODING
                    + ")");
        }

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                buf.append(c);
            } else if (c == ' ') {
                if (plusForSpace) {
                    buf.append('+');
                } else {
                    buf.append("%20");
                }
            } else {
                // convert to external encoding before hex conversion
                try {
                    writer.write((char) c);
                    writer.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                byte[] ba = bos.toByteArray();
                for (int j = 0; j < ba.length; j++) {
                    // Converting each byte in the buffer
                    byte toEncode = ba[j];
                    buf.append('%');
                    int low = (int) (toEncode & 0x0f);
                    int high = (int) ((toEncode & 0xf0) >> 4);
                    buf.append(HEXADECIMAL[high]);
                    buf.append(HEXADECIMAL[low]);
                }
                bos.reset();
            }
        }
        return buf.toString();
    }

}
