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

import static com.huhehu.weijin.wechat.WeChatUtil.getStringFromInputStream;
import static com.huhehu.weijin.wechat.WeChatUtil.getTimestamp;
import static com.huhehu.weijin.wechat.WeChatUtil.getValueFromJavaScript;
import static com.huhehu.weijin.wechat.WeChatUtil.prettyJson;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.json.JSONObject;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatUtilNGTest {

    public WeChatUtilNGTest() {
    }

    @Test
    public void testSomeMethod() {
    }

    @Test
    public void test_prettyJson_String() {
        JSONObject json = new JSONObject();
        json.put("test1", 1);
        json.put("test2", 2);
        json.put("test3", 3);

        String testResult = prettyJson(json.toString());
        assertEquals(new JSONObject(testResult).toString(), json.toString());
        assertTrue(testResult.contains("\n"));
    }

    @Test
    public void test_prettyJson_Json() {
        JSONObject json = new JSONObject();
        json.put("test1", 1);
        json.put("test2", 2);
        json.put("test3", 3);

        String testResult = prettyJson(json);
        assertEquals(new JSONObject(testResult).toString(), json.toString());
        assertTrue(testResult.contains("\n"));
    }

    @Test
    public void test_getTimestamp_Instant() {
        Instant time = Instant.ofEpochMilli(123456);
        long testResult = getTimestamp(time);
        assertEquals(testResult, 123000);
    }

    @Test
    public void test_getTimestamp_long() {
        Instant testResult = getTimestamp(123456);
        assertEquals(testResult.toEpochMilli(), 123000);
    }

    @Test
    public void test_getStringFromInputStream_URLConnection() throws IOException {
        URLConnection connection = new DummyUrlConnection("test");
        assertEquals("test", getStringFromInputStream(connection, "UTF-8"));
    }

    @Test
    public void test_getStringFromInputStream_String() throws IOException {
        InputStream inputStream = new DummyUrlConnection("test").getInputStream();
        assertEquals("test", getStringFromInputStream(inputStream, "UTF-8"));
    }

    @Test
    public void test_getValueFromJavaScript_String_notFound() throws IOException {
        String javaScript = "a=1;b=2;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "d"), null);

        javaScript = "a=1;b=2;c   =   3  ";
        assertEquals(getValueFromJavaScript(javaScript, "d"), null);

        javaScript = "a=1;   b=    2   ;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "d"), null);

        javaScript = "a=1;b=2;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "d"), null);

        javaScript = "a   =1   ;b=2;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "d"), null);

        javaScript = "a=1;b=2;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "d"), null);

        javaScript = "";
        assertEquals(getValueFromJavaScript(javaScript, "d"), null);

        javaScript = "=";
        assertEquals(getValueFromJavaScript(javaScript, "d"), null);

        javaScript = null;
        assertEquals(getValueFromJavaScript(javaScript, "d"), null);
    }

    @Test
    public void test_getValueFromJavaScript_URLConnection_notFound() throws IOException {
        URLConnection javaScript = new DummyUrlConnection("a=1;b=2;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "d", "UTF-8"), null);

        javaScript = new DummyUrlConnection("a=1;b=2;c   =   3  ");
        assertEquals(getValueFromJavaScript(javaScript, "d", "UTF-8"), null);

        javaScript = new DummyUrlConnection("a=1;   b=    2   ;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "d", "UTF-8"), null);

        javaScript = new DummyUrlConnection("a=1;b=2;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "d", "UTF-8"), null);

        javaScript = new DummyUrlConnection("a   =1   ;b=2;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "d", "UTF-8"), null);

        javaScript = new DummyUrlConnection("a=1;b=2;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "d", "UTF-8"), null);

        javaScript = new DummyUrlConnection("");
        assertEquals(getValueFromJavaScript(javaScript, "d", "UTF-8"), null);

        javaScript = new DummyUrlConnection("=");
        assertEquals(getValueFromJavaScript(javaScript, "d", "UTF-8"), null);
    }

    @Test
    public void test_getValueFromJavaScript_String_Found() throws IOException {
        String javaScript = "a=1;b=2;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "c"), "3");

        javaScript = "a=1;b=2;c   =   3  ";
        assertEquals(getValueFromJavaScript(javaScript, "c"), "3");

        javaScript = "a=1;   b=    2   ;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "b"), "2");

        javaScript = "a=1;b=2;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "b"), "2");

        javaScript = "a   =1   ;b=2;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "a"), "1");

        javaScript = "a   =\"1\"   ;b=2;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "a"), "1");

        javaScript = "a   =\"1   \";b=2;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "a"), "1   ");

        javaScript = "a=1;b=2;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "a"), "1");

        javaScript = "a=\"1\";b=2;c=3";
        assertEquals(getValueFromJavaScript(javaScript, "a"), "1");
    }

    @Test
    public void test_getValueFromJavaScript_URLConnection_Found() throws IOException {
        URLConnection javaScript = new DummyUrlConnection("a=1;b=2;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "c", "UTF-8"), "3");

        javaScript = new DummyUrlConnection("a=1;b=2;c   =   3  ");
        assertEquals(getValueFromJavaScript(javaScript, "c", "UTF-8"), "3");

        javaScript = new DummyUrlConnection("a=1;   b=    2   ;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "b", "UTF-8"), "2");

        javaScript = new DummyUrlConnection("a=1;b=2;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "b", "UTF-8"), "2");

        javaScript = new DummyUrlConnection("a   =1   ;b=2;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "a", "UTF-8"), "1");

        javaScript = new DummyUrlConnection("a   =\"1\"   ;b=2;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "a", "UTF-8"), "1");

        javaScript = new DummyUrlConnection("a   =\"1   \";b=2;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "a", "UTF-8"), "1   ");

        javaScript = new DummyUrlConnection("a=1;b=2;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "a", "UTF-8"), "1");

        javaScript = new DummyUrlConnection("a=\"1\";b=2;c=3");
        assertEquals(getValueFromJavaScript(javaScript, "a", "UTF-8"), "1");
    }

    public static class DummyUrlConnection extends URLConnection {

        private InputStream inputStream;

        public DummyUrlConnection(String content) {
            super(null);
            inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public void connect() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return inputStream;
        }
    }

}
