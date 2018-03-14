/* MIT License

   Copyright (c) 2018, Henning Voss <henning@huhehu.com>

   Permission is hereby granted, free of charge, to any person obtaining a copy
   of this software and associated documentation files (the "Software"), to deal
   in the Software without restriction, including without limitation the rights
   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the Software is
   furnished to do so, subject to the following conditions:

   The above copyright notice and this permission notice shall be included in all
   copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
 */
package com.huhehu.weijin.wechat;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URLConnection;
import java.time.Instant;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public final class WeChatUtil {

    private WeChatUtil() {
    }

    /**
     *
     * @param time
     * @return
     */
    public static long getTimestamp(Instant time) {
        return time.getEpochSecond() / (10l * 10l * 10l);
    }

    /**
     *
     * @param time
     * @return
     */
    public static Instant getTimestamp(long time) {
        return Instant.ofEpochMilli(time * (10l * 10l * 10l));
    }

    /**
     *
     * @param connection
     * @param charsetName
     * @return
     * @throws IOException
     */
    public static String getStringFromInputStream(URLConnection connection, String charsetName) throws IOException {
        String string;
        try (InputStream input = connection.getInputStream()) {
            string = getStringFromInputStream(input, charsetName);
        }
        return string;
    }

    /**
     *
     * @param input
     * @param charsetName
     * @return
     * @throws IOException
     */
    public static String getStringFromInputStream(InputStream input, String charsetName) throws IOException {
        int n;
        char[] buffer = new char[1024];
        InputStreamReader reader = new InputStreamReader(input, charsetName);
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer))) {
            writer.write(buffer, 0, n);
        }
        return writer.toString();
    }

    /**
     *
     * @param connection
     * @param key
     * @param charsetName
     * @return
     * @throws IOException
     */
    public static String getValueFromJavaScript(URLConnection connection, String key, String charsetName) throws IOException {
        return getValueFromJavaScript(getStringFromInputStream(connection, charsetName), key);
    }

    /**
     *
     * @param javaScript
     * @param key
     * @return
     */
    public static String getValueFromJavaScript(String javaScript, String key) {
        try {
            int firstKeyEnd = javaScript.indexOf("=");
            String firstKey = javaScript.substring(0, firstKeyEnd).trim();
            javaScript = javaScript.substring(firstKeyEnd + 1).trim();

            if (javaScript.isEmpty()) {
                return null;
            } else {
                int firstValueEnd = javaScript.indexOf(";");
                String firstValue = javaScript.substring(0, firstValueEnd).trim();

                if (!firstValue.isEmpty() && firstValue.charAt(0) == '"') {
                    firstValue = firstValue.substring(1, firstValue.length());
                    if (!firstValue.isEmpty() && firstValue.charAt(firstValue.length() - 1) == '"') {
                        firstValue = firstValue.substring(0, firstValue.length() - 1);
                    }
                }

                if (key.equals(firstKey)) {
                    return firstValue;
                } else {
                    return getValueFromJavaScript(javaScript.substring(firstValueEnd + 1).trim(), key);
                }
            }
        } catch (Exception ignore) {
            return null;
        }
    }
}
