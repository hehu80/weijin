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

import com.huhehu.weijin.wechat.WeChatException;
import com.huhehu.weijin.wechat.WeChatNotConnectedException;
import static com.huhehu.weijin.wechat.WeChatUtil.getStringFromInputStream;
import static com.huhehu.weijin.wechat.WeChatUtil.getTimestamp;
import static com.huhehu.weijin.wechat.WeChatUtil.getValueFromJavaScript;
import com.huhehu.weijin.wechat.contacts.WeChatContact;
import com.huhehu.weijin.wechat.contacts.WeChatUser;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import static com.huhehu.weijin.wechat.conversation.WeChatMessage.TYPE_CHAT_CHANGE;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatConnection {

    protected static final String APP_ID = "wx782c26e4c19acffb";

    protected static final String URL_QR_CODE_DOWNLOAD = "https://login.weixin.qq.com/qrcode/%s";
    protected static final String URL_QR_CODE_REQUEST = "https://login.web2.wechat.com/jslogin?appid=%s&redirect_uri=https://web2.wechat.com/cgi-bin/mmwebwx-bin/webwxnewloginpage&fun=new&lang=de_&_=1518272529736";
    protected static final String URL_LOGIN_1 = "https://login.web2.wechat.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid=%s&tip=1&r=2145092312&_=1518273329652";
    protected static final String URL_LOGIN_2 = "https://login.web.wechat.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid=%s&tip=0&r=2145092312&_=1518273329652";
    protected static final String URL_INIT = "https://web.wechat.com/cgi-bin/mmwebwx-bin/webwxinit?r=1922173868&lang=de_&pass_ticket=%s";
    protected static final String URL_SYNCHRONIZE = "https://web.wechat.com/cgi-bin/mmwebwx-bin/webwxsync?sid=%s&skey=%s&pass_ticket=%s";
    protected static final String URL_SEND_MESSAGE = "https://web.wechat.com/cgi-bin/mmwebwx-bin/webwxsendmsg?lang=de_&pass_ticket=%s";
    protected static final String URL_SYNCHRONIZE_CHECK = "https://webpush.web.wechat.com/cgi-bin/mmwebwx-bin/synccheck?r=1518496288126&skey=%s&sid=%s&uin=%s&deviceid=%s&synckey=%s&_=1518496248732";
    protected static final String URL_CONTACT_LIST = "https://web.wechat.com/cgi-bin/mmwebwx-bin/webwxgetcontact?lang=de_&pass_ticket=%s&r=1518281986728&seq=0&skey=%s";

    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    private int readTimeout = 0;
    private int connectTimeout = 0;
    private String wxskey;
    private String wxsid;
    private String wxuin;
    private String wxuuid;
    private String wxpassticket;
    private List<String> wxcookies;
    private Map<Long, Long> wxsynckey;
    private WeChatContact user;
    private WeChatSession session;
    private List<WeChatMessage> outbox = new ArrayList<>();
    private ExecutorService eventExecutor;
    private ExecutorService updateExecutor;
    private boolean stop;

    /**
     *
     * @param session
     */
    protected WeChatConnection(WeChatSession session) {
        this.session = session;

        eventExecutor = createEventExecutor();
        updateExecutor = createUpdateExecutor();
        updateExecutor.submit(new UpdateRunnable());
    }

    /**
     *
     * @return
     */
    public WeChatSession getSession() {
        return session;
    }

    /**
     *
     * @return
     */
    protected ExecutorService createEventExecutor() {
        return Executors.newSingleThreadExecutor(new WeChatSessionThreadFactory("WeChat-Event"));
    }

    /**
     *
     * @return
     */
    protected ExecutorService createUpdateExecutor() {
        return Executors.newSingleThreadExecutor(new WeChatSessionThreadFactory("WeChat-Update"));
    }

    /**
     *
     * @param readTimeout
     * @return
     */
    public WeChatConnection setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    /**
     *
     * @param connectTimeout
     * @return
     */
    public WeChatConnection setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    /**
     *
     * @return
     */
    protected String getSessionKey() {
        return wxskey;
    }

    /**
     *
     * @return
     */
    public synchronized boolean isConnected() {
        return wxuin != null && user != null && wxuuid != null;
    }

    private synchronized void disconnect() {
        wxuin = null;
        wxuuid = null;
        wxcookies = null;
        wxsynckey = null;
        outbox.clear();
    }

    /**
     *
     */
    protected synchronized void shutdownNow() {
        stop = true;
        updateExecutor.shutdownNow();
        eventExecutor.shutdownNow();
    }

    /**
     *
     * @param message
     * @return
     */
    protected synchronized WeChatConnection sendMessage(WeChatMessage message) {
        outbox.add(message);
        return this;
    }

    /**
     * 
     * @param url
     * @return
     * @throws IOException 
     */
    protected synchronized HttpsURLConnection createUrlConnection(String url) throws IOException {
        return (HttpsURLConnection) new URL(url).openConnection();
    }
    
    /**
     *
     * @param url
     * @return
     * @throws IOException
     */
    protected final synchronized HttpsURLConnection openConnection(String url) throws IOException {
        HttpsURLConnection connection = createUrlConnection(url);

        if (readTimeout > 0) {
            connection.setReadTimeout(readTimeout);
        }
        if (connectTimeout > 0) {
            connection.setConnectTimeout(connectTimeout);
        }

        if (isConnected()) {
            for (String s : wxcookies) {
                connection.addRequestProperty("Cookie", s.substring(0, s.indexOf("; Domain")));
            }
            connection.addRequestProperty("Cookie", "last_wxuin: " + wxuin);
            connection.addRequestProperty("Cookie", "login_frequency: 1");
        }
        return connection;
    }
       
    private JSONObject sendJSONRequest(HttpsURLConnection connection) throws IOException {
        return sendJSONRequest(connection, new JSONObject());
    }

    private JSONObject sendJSONRequest(HttpsURLConnection connection, String key, Object value) throws IOException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        return sendJSONRequest(connection, json);
    }

    private JSONObject sendJSONRequest(HttpsURLConnection connection, JSONObject json) throws IOException {
        String device = "e" + System.currentTimeMillis() + "91";

        JSONObject baseRequest = new JSONObject();
        baseRequest.put("Uin", wxuin);
        baseRequest.put("Sid", wxsid);
        baseRequest.put("Skey", wxskey);
        baseRequest.put("DeviceID", device);
        json.put("BaseRequest", baseRequest);

        byte[] request = json.toString().getBytes("UTF-8");

        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setFixedLengthStreamingMode(request.length);
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestMethod("POST");
        try (OutputStream output = connection.getOutputStream()) {
            output.write(request);
        }

        return json;
    }

    private JSONObject parseJSONResponse(HttpsURLConnection connection) throws IOException {
        JSONObject json = new JSONObject(getStringFromInputStream(connection, "UTF-8"));

        if (isConnected()) {
            String s = json.toString();
            if (s.contains("@")) {
                System.out.println(s);
            }
        }

        if (json.has("BaseResponse")) {
            int result = json.getJSONObject("BaseResponse").getInt("Ret");
            if (1101 == result) {
                disconnect();
                throw new WeChatNotConnectedException();
            }
        }

        if (json.has("SyncKey")) {
            if (wxsynckey == null) {
                wxsynckey = new HashMap<>();
            }
            for (Object key : json.getJSONObject("SyncKey").getJSONArray("List")) {
                wxsynckey.put(((JSONObject) key).getLong("Key"), ((JSONObject) key).getLong("Val"));
            }
        }

        if (!isConnected() && json.has("User")) {
            user = WeChatContact.fromJson((JSONObject) json.get("User"));
            if (isConnected()) {
                eventExecutor.submit(() -> session.onConnect(user)); // TODO refresh user
            }
        }

        if (isConnected() && json.has("ContactList")) {
            List<WeChatContact> contacts = new ArrayList<WeChatContact>();
            for (Object member : json.getJSONArray("ContactList")) {
                contacts.add(WeChatContact.fromJson((JSONObject) member));
            }
            eventExecutor.submit(() -> session.onContactActiveUpdated(contacts.toArray(new WeChatContact[contacts.size()])));
        }

        // DEL CONTACT LIST
        // MOD CONTACT LIST
        List<WeChatMessage> messages = new ArrayList<>();
        if (isConnected() && json.has("AddMsgList")) {
            for (int i = 0; i < json.getJSONArray("AddMsgList").length(); i++) {
                WeChatMessage message = WeChatMessage.fromJson(json.getJSONArray("AddMsgList").getJSONObject(i));
                if (message.getMessageType() == TYPE_CHAT_CHANGE) {
                    WeChatContact contact = user.equals(message.getToUser()) ? message.getFromUser() : message.getToUser();
                    eventExecutor.submit(() -> session.onUserActivate(contact));
                } else {
                    messages.add(message);
                }
            }
        }

        if (isConnected() && json.has("MPSubscribeMsgList") && json.getJSONArray("MPSubscribeMsgList").length() > 0) {
            JSONObject articleList = json.getJSONArray("MPSubscribeMsgList").getJSONObject(0);
            WeChatUser articleListOwner = new WeChatUser(articleList.getString("UserName"));
            for (int i = 0; i < articleList.getJSONArray("MPArticleList").length(); i++) {
                // TODO move to external
                WeChatMessage article = new WeChatMessage();
                article.setFromUser(articleListOwner);
                article.setToUser(user);
                article.setContent(articleList.getJSONArray("MPArticleList").getJSONObject(i).getString("Title"));
                article.setJson(articleList.getJSONArray("MPArticleList").getJSONObject(i).toString());
                messages.add(article);
                // TODO maybe old message
            }
        }
        if (!messages.isEmpty()) {
            eventExecutor.submit(() -> session.onMessageReceived(messages.toArray(new WeChatMessage[messages.size()])));
        }

        return json;
    }

    private void retrieveUpdates() throws IOException {
        HttpsURLConnection connection = openConnection(String.format(URL_SYNCHRONIZE, wxsid, wxskey, wxpassticket));

        JSONArray jsonSyncArray = new JSONArray();
        for (Map.Entry<Long, Long> syncKey : wxsynckey.entrySet()) {
            JSONObject jsonSyncKey = new JSONObject();
            jsonSyncKey.put("Key", syncKey.getKey());
            jsonSyncKey.put("Val", syncKey.getValue());
            jsonSyncArray.put(jsonSyncKey);
        }

        JSONObject json = new JSONObject();
        json.put("Count", jsonSyncArray.length());
        json.put("List", jsonSyncArray);

        sendJSONRequest(connection, "SyncKey", json);

        json = parseJSONResponse(connection);
    }

    private void retreiveContacts() throws IOException {
        HttpsURLConnection connection = openConnection(String.format(URL_CONTACT_LIST, wxpassticket, wxskey));

        List<WeChatContact> contacts = new ArrayList<WeChatContact>();
        for (Object member : parseJSONResponse(connection).getJSONArray("MemberList")) {
            contacts.add(WeChatContact.fromJson((JSONObject) member));
        }

        eventExecutor.submit(() -> session.onContactSavedUpdated(contacts.toArray(new WeChatContact[contacts.size()])));
    }

    private boolean retrieveUserId() throws IOException {
        HttpsURLConnection connection = openConnection(String.format(URL_QR_CODE_REQUEST, APP_ID));
        wxuuid = getValueFromJavaScript(connection, "window.QRLogin.uuid", "UTF-8");
        return true;
    }

    private boolean retreiveUser() throws IOException {
        HttpsURLConnection connection = openConnection(String.format(URL_LOGIN_1, wxuuid));
        String code = getValueFromJavaScript(connection, "window.code", "UTF-8");

        if ("201".equals(code)) {
            connection = openConnection(String.format(URL_LOGIN_2, wxuuid));
            String redirect = getValueFromJavaScript(connection, "window.redirect_uri", "UTF-8");

            connection = openConnection(redirect + "&fun=new&version=v2&lang=de_");
            String response = getStringFromInputStream(connection, "UTF-8");

            user = null;
            try {
                wxskey = response.substring(response.indexOf("<skey>") + 6, response.indexOf("</skey>"));
                wxsid = response.substring(response.indexOf("<wxsid>") + 7, response.indexOf("</wxsid>"));
                wxuin = response.substring(response.indexOf("<wxuin>") + 7, response.indexOf("</wxuin>"));
                wxpassticket = response.substring(response.indexOf("<pass_ticket>") + 13, response.indexOf("</pass_ticket>"));
                wxcookies = connection.getHeaderFields().get("Set-Cookie");
            } catch (Exception e) {
                throw new WeChatException("failed to receive session paramter after login");
            }

            if (wxcookies == null || wxcookies.size() < 1) {
                throw new WeChatException("failed to get session cookies after login");
            }

            connection = openConnection(String.format(URL_INIT, wxpassticket));
            sendJSONRequest(connection);

            parseJSONResponse(connection);
            return isConnected();
        } else if (!"408".equals(code)) {
            throw new WeChatException("Connection refused");
        }

        return false;
    }

    private void deliverOutbox() throws IOException {
        while (true) {
            WeChatMessage message;
            synchronized (outbox) {
                if (!outbox.isEmpty()) {
                    message = outbox.get(0);
                } else {
                    break;
                }
            }

            HttpsURLConnection connection = openConnection(String.format(URL_SEND_MESSAGE, wxpassticket));

            message.setMessageId("" + System.currentTimeMillis());
            JSONObject json = new JSONObject();
            json.put("Type", message.getMessageType() == 0 ? 1 : message.getMessageType());
            json.put("Content", message.getContent());
            json.put("FromUserName", user.getUserId());
            json.put("ToUserName", message.getToUser().getUserId());
            json.put("LocalID", message.getMessageId());
            json.put("ClientMsgId", message.getMessageId());
            json.put("CreateTime", getTimestamp(message.getTime()));

            sendJSONRequest(connection, "Msg", json);
            parseJSONResponse(connection);

            synchronized (outbox) {
                outbox.remove(0);
            }

            eventExecutor.submit(() -> session.onMessageReceived(message));
        }
    }

    private class UpdateRunnable implements Runnable {

        public void run() {
            while (!stop) {
                if (!isConnected()) {
                    if (wxuuid == null) {
                        try {
                            if (retrieveUserId()) {
                                session.onQRCodeReceived(String.format(URL_QR_CODE_DOWNLOAD, wxuuid));
                            } else {
                                session.onError(new WeChatException("failed to get QR code, try again later ..."));
                            }
                        } catch (IOException e) {
                            disconnect();
                            eventExecutor.submit(() -> session.onError(e));
                        }
                    }
                    if (wxuuid != null) {
                        try {
                            if (retreiveUser()) {
                                retreiveContacts();
                            } else {
                                session.onError(new WeChatException("failed to get user, try again later ..."));
                            }
                        } catch (IOException e) {
                            disconnect();
                            eventExecutor.submit(() -> session.onError(e));
                        }
                    }
                } else {
                    try {
                        deliverOutbox();
                        retrieveUpdates();
                    } catch (WeChatNotConnectedException e) {
                        disconnect();
                        eventExecutor.submit(session::onDisconnect);
                    } catch (IOException e) {
                        eventExecutor.submit(() -> session.onError(e));
                    }
                }
            }
        }
    }
}
