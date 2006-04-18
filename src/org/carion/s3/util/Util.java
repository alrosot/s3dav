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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class Util {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private static final SimpleDateFormat httpDateFormat = new SimpleDateFormat(
            "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);

    public static InputStream wrap(InputStream in, boolean keepAlive,
            long contentLength) throws IOException {
        return wrap(in, keepAlive, contentLength, null);
    }

    public static InputStream wrap(InputStream in, boolean keepAlive,
            long contentLength, InputStreamObserver observer)
            throws IOException {
        if (contentLength >= 0) {
            return new ContentLengthInputStream(in, contentLength, keepAlive,
                    observer);
        } else if (!keepAlive) {
            return new WrappedInputStream(in, keepAlive, observer);
        } else {
            return new NullInputStream(keepAlive, contentLength);
        }
    }

    public static String readInputStreamAsString(InputStream in)
            throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        copyStream(in, bos);
        return new String(bos.toByteArray());
    }

    public static int copyStream(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }

        //        while ((n = input.read()) != -1) {
        //            output.write((byte) n);
        //            count++;
        //        }
        return count;
    }

    public static MemoryMappedFile mkMemoryMapFile(InputStream in,
            long contentLength) throws IOException {
        if (contentLength == 0) {
            return null;
        } else {
            MemoryMappedFile mf = MemoryMappedFile.mk();
            mf.copy(in, (int) contentLength);
            return mf;
        }
    }

    public static String getHttpDate() {
        return getHttpDate(new Date());
    }

    public static String getHttpDate(Date date) {
        return httpDateFormat.format(date);
    }

    public static Date parseHttpdate(String str) {
        try {
            return httpDateFormat.parse(str);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return new Date();
        }
    }

    public static XMLReader createXMLReader() {
        try {
            return XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            // oops, lets try doing this (needed in 1.4)
            System.setProperty("org.xml.sax.driver",
                    "org.apache.crimson.parser.XMLReaderImpl");
        }
        try {
            // try once more
            return XMLReaderFactory.createXMLReader();
        } catch (SAXException e) {
            throw new RuntimeException(
                    "Couldn't initialize a sax driver for the XMLReader");
        }
    }

    public static String getIsoDate(Date date) {
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.setTime(date);
        StringBuffer buffer = new StringBuffer();
        buffer.append(calendar.get(Calendar.YEAR));
        buffer.append("-");
        buffer.append(twoDigit(calendar.get(Calendar.MONTH) + 1));
        buffer.append("-");
        buffer.append(twoDigit(calendar.get(Calendar.DAY_OF_MONTH)));
        buffer.append("T");
        buffer.append(twoDigit(calendar.get(Calendar.HOUR_OF_DAY)));
        buffer.append(":");
        buffer.append(twoDigit(calendar.get(Calendar.MINUTE)));
        buffer.append(":");
        buffer.append(twoDigit(calendar.get(Calendar.SECOND)));
        buffer.append(".");
        buffer.append(twoDigit(calendar.get(Calendar.MILLISECOND) / 10));
        buffer.append("Z");
        return buffer.toString();
    }

    public static Date parseIsoDate(String isodate) {
        Calendar calendar = getCalendar(isodate);
        return calendar.getTime();
    }

    private static Calendar getCalendar(String isodate) {
        // YYYY-MM-DDThh:mm:ss.sTZD
        StringTokenizer st = new StringTokenizer(isodate, "-T:.+Z", true);

        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        try {
            // Year
            if (st.hasMoreTokens()) {
                int year = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.YEAR, year);
            } else {
                return calendar;
            }
            // Month
            if (check(st, "-") && (st.hasMoreTokens())) {
                int month = Integer.parseInt(st.nextToken()) - 1;
                calendar.set(Calendar.MONTH, month);
            } else {
                return calendar;
            }
            // Day
            if (check(st, "-") && (st.hasMoreTokens())) {
                int day = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.DAY_OF_MONTH, day);
            } else {
                return calendar;
            }
            // Hour
            if (check(st, "T") && (st.hasMoreTokens())) {
                int hour = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            }
            // Minutes
            if (check(st, ":") && (st.hasMoreTokens())) {
                int minutes = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.MINUTE, minutes);
            } else {
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            }

            //
            // Not mandatory now
            //

            // Secondes
            if (!st.hasMoreTokens()) {
                return calendar;
            }
            String tok = st.nextToken();
            if (tok.equals(":")) { // secondes
                if (st.hasMoreTokens()) {
                    int secondes = Integer.parseInt(st.nextToken());
                    calendar.set(Calendar.SECOND, secondes);
                    if (!st.hasMoreTokens()) {
                        return calendar;
                    }
                    // frac sec
                    tok = st.nextToken();
                    if (tok.equals(".")) {
                        // bug fixed, thx to Martin Bottcher
                        String nt = st.nextToken();
                        while (nt.length() < 3) {
                            nt += "0";
                        }
                        nt = nt.substring(0, 3); // Cut trailing chars..
                        int millisec = Integer.parseInt(nt);
                        // int millisec = Integer.parseInt(st.nextToken()) * 10;
                        calendar.set(Calendar.MILLISECOND, millisec);
                        if (!st.hasMoreTokens()) {
                            return calendar;
                        }
                        tok = st.nextToken();
                    } else {
                        calendar.set(Calendar.MILLISECOND, 0);
                    }
                } else {
                    throw new RuntimeException("No secondes specified");
                }
            } else {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            }
            // Timezone
            if (!tok.equals("Z")) { // UTC
                if (!(tok.equals("+") || tok.equals("-"))) {
                    throw new RuntimeException("only Z, + or - allowed");
                }
                boolean plus = tok.equals("+");
                if (!st.hasMoreTokens()) {
                    throw new RuntimeException("Missing hour field");
                }
                int tzhour = Integer.parseInt(st.nextToken());
                int tzmin = 0;
                if (check(st, ":") && (st.hasMoreTokens())) {
                    tzmin = Integer.parseInt(st.nextToken());
                } else {
                    throw new RuntimeException("Missing minute field");
                }
                if (plus) {
                    calendar.add(Calendar.HOUR, -tzhour);
                    calendar.add(Calendar.MINUTE, -tzmin);
                } else {
                    calendar.add(Calendar.HOUR, tzhour);
                    calendar.add(Calendar.MINUTE, tzmin);
                }
            }
        } catch (NumberFormatException ex) {
            throw new RuntimeException("[" + ex.getMessage()
                    + "] is not an integer");
        }
        return calendar;
    }

    private static boolean check(StringTokenizer st, String token) {
        try {
            if (st.nextToken().equals(token)) {
                return true;
            } else {
                throw new RuntimeException("Missing [" + token + "]");
            }
        } catch (NoSuchElementException ex) {
            return false;
        }
    }

    private static String twoDigit(int i) {
        if (i >= 0 && i < 10) {
            return "0" + String.valueOf(i);
        }
        return String.valueOf(i);
    }

    public static String getIsoDate() {
        return getIsoDate(new Date());
    }

    public static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return URLDecoder.decode(str);
        }
    }

    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return URLDecoder.decode(str);
        }
    }
}
