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
package com.huhehu.weijin.wechat.session;

import com.huhehu.weijin.wechat.contacts.WeChatContact;
import static com.huhehu.weijin.wechat.session.WeChatConnection.APP_ID;
import static com.huhehu.weijin.wechat.session.WeChatConnection.URL_CONTACT_LIST;
import static com.huhehu.weijin.wechat.session.WeChatConnection.URL_INIT;
import static com.huhehu.weijin.wechat.session.WeChatConnection.URL_LOGIN_1;
import static com.huhehu.weijin.wechat.session.WeChatConnection.URL_LOGIN_2;
import static com.huhehu.weijin.wechat.session.WeChatConnection.URL_QR_CODE_REQUEST;
import static com.huhehu.weijin.wechat.session.WeChatConnection.URL_SYNCHRONIZE;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatConnectionNGTest {

    public WeChatConnectionNGTest() {
    }

    @Test
    public void test_connect_sucessfull() throws MalformedURLException {
        WeChatSessionImpl testSession = new WeChatSessionImpl();
        testSession.addConnection().request(String.format(URL_QR_CODE_REQUEST, APP_ID))
                .response("window.QRLogin.uuid=test");
        testSession.addConnection().request(String.format(URL_LOGIN_1, "test"))
                .response("window.code=201");
        testSession.addConnection().request(String.format(URL_LOGIN_2, "test"))
                .response("window.redirect_uri=http://localhost");
        testSession.addConnection().request(String.format("http://localhost&fun=new&version=v2&lang=de_"))
                .response("<skey>testskey</skey><wxsid>testwxsid</wxsid><wxuin>testwxuin</wxuin><pass_ticket>testpassticket</pass_ticket>")
                .requestCookie("test", "test");
        testSession.addConnection().request(String.format(URL_INIT, "testpassticket"))
                .requestJson("\\{\"BaseRequest\":\\{\"DeviceID\":\"e\\d+\",\"Uin\":\"testwxuin\",\"Skey\":\"testskey\",\"Sid\":\"testwxsid\"\\}\\}")
                .response("{User: {HeadImgUrl:\"\", Sex:1, Alias:\"alias\", Uin:123, UserName:\"testuser\", NickName:\"\", ContactFlag:1, RemarkName:\"\", Signature:\"\", VerifyFlag:2, PYInitial:\"\", PYQuanPin:\"\"}}");
        testSession.addConnection().request(String.format(URL_CONTACT_LIST, "testpassticket", "testskey"))
                .response("{SyncKey: {List: []}, MemberList: []}")
                .responseCookie("last_wxuin", "testwxuin")
                .responseCookie("login_frequency", "1")
                .responseCookie("test", "test");
        testSession.addConnection().request(String.format(URL_SYNCHRONIZE, "testwxsid", "testskey", "testpassticket"))
                .requestJson("\\{\"BaseRequest\":\\{\"DeviceID\":\"e\\d+\",\"Uin\":\"testwxuin\",\"Skey\":\"testskey\",\"Sid\":\"testwxsid\"\\},\"SyncKey\":\\{\"List\":\\[\\],\"Count\":0\\}\\}")
                .response("{SyncKey: {List: []}, MemberList: []}")
                .responseCookie("last_wxuin", "testwxuin")
                .responseCookie("login_frequency", "1")
                .responseCookie("test", "test");

        testSession.connect();
        assertTrue(testSession.getConnections().isEmpty());
        assertTrue(testSession.isConnected());
        assertTrue(testSession.getConnection().isConnected());
        assertTrue(testSession.getContactsActive().isEmpty());
        assertTrue(testSession.getContactsSaved().isEmpty());
        assertEquals(testSession.getUserLogin(), new WeChatContact("testuser"));
    }

    public static class WeChatSessionImpl extends WeChatSession {

        private List<MockedUrlConnection> connections = new ArrayList<>();

        public List<MockedUrlConnection> getConnections() {
            return connections;
        }

        public MockedUrlConnection addConnection() throws MalformedURLException {
            MockedUrlConnection connection = new MockedUrlConnection();
            connections.add(connection);
            return connection;
        }

        @Override
        protected WeChatConnection createConnection() {
            return new WeChatConnectionImpl(this);
        }

        @Override
        protected WeChatMediaCache createMediaCache() {
            return new DummyMediaCache(this);
        }

    }

    public static class WeChatConnectionImpl extends WeChatConnection {       
        
        public WeChatConnectionImpl(WeChatSessionImpl session) {
            super(session);
        }

        private List<MockedUrlConnection> getSessionConnections() {
            return ((WeChatSessionImpl) getSession()).getConnections();
        }

        @Override
        protected synchronized HttpsURLConnection createUrlConnection(String url) throws IOException {
            if (getSessionConnections().isEmpty()) {
                fail("no further request expected, but found " + url);
            }

            MockedUrlConnection connection = getSessionConnections().remove(0);

            if (!url.equals(connection.getRequest()) && !url.matches(connection.getRequest())) {
                fail("url doesn't match: [" + connection.getRequest() + "] expected but was [" + url + "]");
            }

            if (getSessionConnections().isEmpty()) {
                shutdownNow(); // CAUTION: stop connection after last URL request (otherwise it will never stop)
            }

            return connection;
        }

        @Override
        protected ExecutorService createUpdateExecutor() {
            return new MockedExecutorService(true);
        }

        @Override
        protected ExecutorService createEventExecutor() {
            return new MockedExecutorService(true);
        }

    }

    public static class MockedUrlConnection extends HttpsURLConnection {

        private String request = "";
        private String response = "";
        private List<String> requestCookies = new ArrayList<>();
        private Map<String, String> responseCookies = new HashMap<>();
        private String json = null;

        public MockedUrlConnection() throws MalformedURLException {
            super(new URL("http://localhost"));
        }

        public MockedUrlConnection request(String request) {
            this.request += request;
            return this;
        }

        public MockedUrlConnection response(String response) {
            this.response += response;
            return this;
        }

        public MockedUrlConnection requestCookie(String key, String value) {
            requestCookies.add(key + "=" + value + "; Domain");
            return this;
        }

        public MockedUrlConnection responseCookie(String key, String value) {
            responseCookies.put(key, value);
            return this;
        }

        public MockedUrlConnection requestJson(String json) {
            this.json = json;
            return this;
        }

        public String getRequest() {
            return request;
        }

        public String getResponse() {
            return response;
        }

        @Override
        public String getCipherSuite() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Certificate[] getLocalCertificates() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Certificate[] getServerCertificates() throws SSLPeerUnverifiedException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void disconnect() {
        }

        @Override
        public boolean usingProxy() {
            return false;
        }

        @Override
        public void connect() throws IOException {
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            return new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    String input = new String(toByteArray());
                    if (json == null) {
                        fail("no JSON request expected but found [" + input + "]");
                    } else if (!input.equals(json) && !input.matches(json)) {
                        fail("JSON request doesn't match: [" + json + "] expected but was [" + input + "]");
                    }
                }
            };
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if (!responseCookies.isEmpty() && getRequestProperties().containsKey("Cookie")) {
                List<String> cookies = getRequestProperties().get("Cookie");
                for (String cookie : cookies) {
                    String key = cookie.substring(0, cookie.indexOf(":") >= 0 ? cookie.indexOf(":") : cookie.indexOf("=")).trim();
                    String value = cookie.substring(cookie.indexOf(":") >= 0 ? cookie.indexOf(":") + 1 : cookie.indexOf("=") + 1, cookie.length()).trim();
                    if (responseCookies.containsKey(key)) {
                        if (responseCookies.get(key).equals(value)) {
                            responseCookies.remove(key);
                        }
                    }
                }
            }
            if (!responseCookies.isEmpty()) {
                fail("some cookies not send " + responseCookies.keySet() + "");
            }
            return new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public Map<String, List<String>> getHeaderFields() {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("Set-Cookie", requestCookies);
            return headers;
        }

    }

    public static class MockedExecutorService implements ExecutorService {

        private boolean shutdown;
        private boolean execute;

        public MockedExecutorService(boolean execute) {
            this.execute = execute;
        }

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true;
            return new ArrayList<>();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return true;
        }

        @Override
        public <T> Future<T> submit(Callable<T> task) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> Future<T> submit(Runnable task, T result) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Future<?> submit(Runnable task) {
            if (execute) {
                task.run();
            }
            return null;
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void execute(Runnable command) {
            if (execute) {
                command.run();
            }
        }

    }

    public static class DummyMediaCache extends WeChatMediaCache {

        public DummyMediaCache(WeChatSession session) {
            super(session);
        }

        @Override
        protected ExecutorService createMediaDownloader() {
            return new MockedExecutorService(false);
        }

    }
}
