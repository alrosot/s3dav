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
package org.carion.s3.operations;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/*
 * Here are a few links to sources which helped me debug
 * this implememtation
 * http://dev.w3.org/cvsweb/java/classes/org/w3c/tools/crypt/Md5.java?rev=1.5&content-type=text/x-cvsweb-markup
 * http://www.koders.com/java/fidEE625DA8C326622AFFD0294AB053CDD220542596.aspx
 * http://dll.nu/duper/MD5.java
 *
 * See also:
 * http://pajhome.org.uk/crypt/md5/md5src.html
 * http://www.zvon.org/tmRFC/RFC2202/Output/chapter7.html
 * http://www.jonh.net/~jonh/md5/MD5.java

 */
public class Md5 {
    private static final int S11 = 7;

    private static final int S12 = 12;

    private static final int S13 = 17;

    private static final int S14 = 22;

    private static final int S21 = 5;

    private static final int S22 = 9;

    private static final int S23 = 14;

    private static final int S24 = 20;

    private static final int S31 = 4;

    private static final int S32 = 11;

    private static final int S33 = 16;

    private static final int S34 = 23;

    private static final int S41 = 6;

    private static final int S42 = 10;

    private static final int S43 = 15;

    private static final int S44 = 21;

    private static byte padding[] = { (byte) 0x80, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
            (byte) 0 };

    private final ByteBuffer input;

    private final int state[];

    private long count = 0;

    private byte buffer[] = null;

    //private byte digest[] = null;

    private final int F(int x, int y, int z) {
        return ((x & y) | ((~x) & z));
    }

    private final int G(int x, int y, int z) {
        return ((x & z) | (y & (~z)));
    }

    private final int H(int x, int y, int z) {
        return (x ^ y ^ z);
    }

    private final int I(int x, int y, int z) {
        return (y ^ (x | (~z)));
    }

    private final int rotate_left(int x, int n) {
        return ((x << n) | (x >>> (32 - n)));
    }

    private final int FF(int a, int b, int c, int d, int x, int s, int ac) {
        a += (F(b, c, d) + x + ac);
        a = rotate_left(a, s);
        a += b;
        return a;
    }

    private final int GG(int a, int b, int c, int d, int x, int s, int ac) {
        a += (G(b, c, d) + x + ac);
        a = rotate_left(a, s);
        a += b;
        return a;
    }

    private final int HH(int a, int b, int c, int d, int x, int s, int ac) {
        a += (H(b, c, d) + x + ac);
        a = rotate_left(a, s);
        a += b;
        return a;
    }

    private final int II(int a, int b, int c, int d, int x, int s, int ac) {
        a += (I(b, c, d) + x + ac);
        a = rotate_left(a, s);
        a += b;
        return a;
    }

    private final void decode(int output[], ByteBuffer inputBuffer, int off,
            int len) {
        byte[] input = new byte[len];
        for (int i = 0; i < len; i++) {
            input[i] = inputBuffer.get(off + i);
        }

        int i = 0;
        int j = 0;
        for (; j < len; i++, j += 4) {
            output[i] = (((int) (input[j] & 0xff))
                    | (((int) (input[j + 1] & 0xff)) << 8)
                    | (((int) (input[j + 2] & 0xff)) << 16) | (((int) (input[j + 3] & 0xff)) << 24));
        }
    }

    private final void transform(byte block[], int offset) {
        transform(ByteBuffer.wrap(block), offset);
    }

    //    private final void transform2(byte block[], int offset) {
    private final void transform(ByteBuffer block, int offset) {
        int a = state[0];
        int b = state[1];
        int c = state[2];
        int d = state[3];
        int x[] = new int[16];

        decode(x, block, offset, 64);

        /* Round 1 */
        a = FF(a, b, c, d, x[0], S11, 0xd76aa478); /* 1 */
        d = FF(d, a, b, c, x[1], S12, 0xe8c7b756); /* 2 */
        c = FF(c, d, a, b, x[2], S13, 0x242070db); /* 3 */
        b = FF(b, c, d, a, x[3], S14, 0xc1bdceee); /* 4 */
        a = FF(a, b, c, d, x[4], S11, 0xf57c0faf); /* 5 */
        d = FF(d, a, b, c, x[5], S12, 0x4787c62a); /* 6 */
        c = FF(c, d, a, b, x[6], S13, 0xa8304613); /* 7 */
        b = FF(b, c, d, a, x[7], S14, 0xfd469501); /* 8 */
        a = FF(a, b, c, d, x[8], S11, 0x698098d8); /* 9 */
        d = FF(d, a, b, c, x[9], S12, 0x8b44f7af); /* 10 */
        c = FF(c, d, a, b, x[10], S13, 0xffff5bb1); /* 11 */
        b = FF(b, c, d, a, x[11], S14, 0x895cd7be); /* 12 */
        a = FF(a, b, c, d, x[12], S11, 0x6b901122); /* 13 */
        d = FF(d, a, b, c, x[13], S12, 0xfd987193); /* 14 */
        c = FF(c, d, a, b, x[14], S13, 0xa679438e); /* 15 */
        b = FF(b, c, d, a, x[15], S14, 0x49b40821); /* 16 */
        /* Round 2 */
        a = GG(a, b, c, d, x[1], S21, 0xf61e2562); /* 17 */
        d = GG(d, a, b, c, x[6], S22, 0xc040b340); /* 18 */
        c = GG(c, d, a, b, x[11], S23, 0x265e5a51); /* 19 */
        b = GG(b, c, d, a, x[0], S24, 0xe9b6c7aa); /* 20 */
        a = GG(a, b, c, d, x[5], S21, 0xd62f105d); /* 21 */
        d = GG(d, a, b, c, x[10], S22, 0x2441453); /* 22 */
        c = GG(c, d, a, b, x[15], S23, 0xd8a1e681); /* 23 */
        b = GG(b, c, d, a, x[4], S24, 0xe7d3fbc8); /* 24 */
        a = GG(a, b, c, d, x[9], S21, 0x21e1cde6); /* 25 */
        d = GG(d, a, b, c, x[14], S22, 0xc33707d6); /* 26 */
        c = GG(c, d, a, b, x[3], S23, 0xf4d50d87); /* 27 */
        b = GG(b, c, d, a, x[8], S24, 0x455a14ed); /* 28 */
        a = GG(a, b, c, d, x[13], S21, 0xa9e3e905); /* 29 */
        d = GG(d, a, b, c, x[2], S22, 0xfcefa3f8); /* 30 */
        c = GG(c, d, a, b, x[7], S23, 0x676f02d9); /* 31 */
        b = GG(b, c, d, a, x[12], S24, 0x8d2a4c8a); /* 32 */

        /* Round 3 */
        a = HH(a, b, c, d, x[5], S31, 0xfffa3942); /* 33 */
        d = HH(d, a, b, c, x[8], S32, 0x8771f681); /* 34 */
        c = HH(c, d, a, b, x[11], S33, 0x6d9d6122); /* 35 */
        b = HH(b, c, d, a, x[14], S34, 0xfde5380c); /* 36 */
        a = HH(a, b, c, d, x[1], S31, 0xa4beea44); /* 37 */
        d = HH(d, a, b, c, x[4], S32, 0x4bdecfa9); /* 38 */
        c = HH(c, d, a, b, x[7], S33, 0xf6bb4b60); /* 39 */
        b = HH(b, c, d, a, x[10], S34, 0xbebfbc70); /* 40 */
        a = HH(a, b, c, d, x[13], S31, 0x289b7ec6); /* 41 */
        d = HH(d, a, b, c, x[0], S32, 0xeaa127fa); /* 42 */
        c = HH(c, d, a, b, x[3], S33, 0xd4ef3085); /* 43 */
        b = HH(b, c, d, a, x[6], S34, 0x4881d05); /* 44 */
        a = HH(a, b, c, d, x[9], S31, 0xd9d4d039); /* 45 */
        d = HH(d, a, b, c, x[12], S32, 0xe6db99e5); /* 46 */
        c = HH(c, d, a, b, x[15], S33, 0x1fa27cf8); /* 47 */
        b = HH(b, c, d, a, x[2], S34, 0xc4ac5665); /* 48 */

        /* Round 4 */
        a = II(a, b, c, d, x[0], S41, 0xf4292244); /* 49 */
        d = II(d, a, b, c, x[7], S42, 0x432aff97); /* 50 */
        c = II(c, d, a, b, x[14], S43, 0xab9423a7); /* 51 */
        b = II(b, c, d, a, x[5], S44, 0xfc93a039); /* 52 */
        a = II(a, b, c, d, x[12], S41, 0x655b59c3); /* 53 */
        d = II(d, a, b, c, x[3], S42, 0x8f0ccc92); /* 54 */
        c = II(c, d, a, b, x[10], S43, 0xffeff47d); /* 55 */
        b = II(b, c, d, a, x[1], S44, 0x85845dd1); /* 56 */
        a = II(a, b, c, d, x[8], S41, 0x6fa87e4f); /* 57 */
        d = II(d, a, b, c, x[15], S42, 0xfe2ce6e0); /* 58 */
        c = II(c, d, a, b, x[6], S43, 0xa3014314); /* 59 */
        b = II(b, c, d, a, x[13], S44, 0x4e0811a1); /* 60 */
        a = II(a, b, c, d, x[4], S41, 0xf7537e82); /* 61 */
        d = II(d, a, b, c, x[11], S42, 0xbd3af235); /* 62 */
        c = II(c, d, a, b, x[2], S43, 0x2ad7d2bb); /* 63 */
        b = II(b, c, d, a, x[9], S44, 0xeb86d391); /* 64 */

        state[0] += a;
        state[1] += b;
        state[2] += c;
        state[3] += d;
    }

    private final void update(byte input[], int len) {
        update(ByteBuffer.wrap(input, 0, len));
    }

    //    private final void update2(byte input[], int len) {
    private final void update(ByteBuffer input) {
        int len = input.remaining();
        int index = ((int) (count >> 3)) & 0x3f;
        count += (len << 3);
        int partLen = 64 - index;
        int i = 0;
        if (len >= partLen) {
            for (int i2 = 0; i2 < partLen; i2++) {
                buffer[i2 + index] = input.get(i2);
            }
            transform(buffer, 0);
            for (i = partLen; i + 63 < len; i += 64) {
                transform(input, i);
            }
            index = 0;
        } else {
            i = 0;
        }

        // remaining input
        for (int i2 = 0; i2 < len - i; i2++) {
            buffer[index + i2] = input.get(i + i2);
        }
    }

    private byte[] end() {
        byte bits[] = new byte[8];
        for (int i = 0; i < 8; i++) {
            bits[i] = (byte) ((count >>> (i * 8)) & 0xff);
        }
        int index = ((int) (count >> 3)) & 0x3f;
        int padlen = (index < 56) ? (56 - index) : (120 - index);
        update(padding, padlen);
        update(bits, 8);
        return encode(state, 16);
    }

    // Encode the content.state array into 16 bytes array
    private byte[] encode(int input[], int len) {
        byte output[] = new byte[len];
        int i = 0;
        int j = 0;
        for (; j < len; i++, j += 4) {
            output[j] = (byte) ((input[i]) & 0xff);
            output[j + 1] = (byte) ((input[i] >> 8) & 0xff);
            output[j + 2] = (byte) ((input[i] >> 16) & 0xff);
            output[j + 3] = (byte) ((input[i] >> 24) & 0xff);
        }
        return output;
    }

    /**
     * Get the digest for our input stream. This method constructs the input
     * stream digest, and return it, as a a String, following the MD5 (rfc1321)
     * algorithm,
     *
     * @return An instance of String, giving the message digest.
     * @exception IOException
     *                Thrown if the digestifier was unable to read the input
     *                stream.
     */
    public byte[] getDigest() throws IOException {
        this.count = 0;
        state[0] = 0x67452301;
        state[1] = 0xefcdab89;
        state[2] = 0x98badcfe;
        state[3] = 0x10325476;
        update(input);
        return end();
    }

    /**
     * Construct a digestifier for the given string.
     *
     * @param input
     *            The string to be digestified.
     * @param encoding
     *            the encoding name used (such as UTF8)
     */
    public Md5(ByteBuffer input) {
        this.input = input;
        this.state = new int[4];
        this.buffer = new byte[64];
        this.count = 0;
    }

    public static String stringify(byte buf[]) {
        StringBuffer sb = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            int h = (buf[i] & 0xf0) >> 4;
            int l = (buf[i] & 0x0f);
            sb.append(new Character((char) ((h > 9) ? 'a' + h - 10 : '0' + h)));
            sb.append(new Character((char) ((l > 9) ? 'a' + l - 10 : '0' + l)));
        }
        return sb.toString();
    }

    public static byte[] getDigest(File file) throws IOException {
        // Create a read-only memory-mapped file
        FileChannel roChannel = new RandomAccessFile(file, "r").getChannel();
        ByteBuffer roBuf = roChannel.map(FileChannel.MapMode.READ_ONLY, 0,
                (int) roChannel.size());

        Md5 md5 = new Md5(roBuf);
        return md5.getDigest();
    }

    public static byte[] getDigest(ByteBuffer roBuf) throws IOException {
        Md5 md5 = new Md5(roBuf);
        return md5.getDigest();
    }
}