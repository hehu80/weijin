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

import com.huhehu.weijin.wechat.contacts.WeChatContact;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.huhehu.weijin.wechat.WeChatUtil.getStringFromInputStream;
import static com.huhehu.weijin.wechat.WeChatUtil.getValueFromJavaScript;

public class WeChatSession implements Serializable {
    private static final String APP_ID = "wx782c26e4c19acffb";
    private static final String URL_QR_CODE_REQUEST = "https://login.web2.wechat.com/jslogin?appid=%s&redirect_uri=https://web2.wechat.com/cgi-bin/mmwebwx-bin/webwxnewloginpage&fun=new&lang=de_&_=%s";
    private static final String URL_QR_CODE_DOWNLOAD = "https://login.weixin.qq.com/qrcode/%s";
    private static final String URL_LOGIN_1 = "https://login.web2.wechat.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid=%s&tip=1&r=2145092312&_=1518273329652";
    private static final String URL_LOGIN_2 = "https://login.web.wechat.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid=%s&tip=0&r=2145092312&_=1518273329652";
    private static final String URL_INIT = "https://web.wechat.com/cgi-bin/mmwebwx-bin/webwxinit?r=1922173868&lang=de_&pass_ticket=%s";
    private static final String URL_SYNCHRONIZE = "https://web.wechat.com/cgi-bin/mmwebwx-bin/webwxsync?sid=%s&skey=%s&pass_ticket=%s";
    private static final String URL_SEND_MESSAGE = "https://web.wechat.com/cgi-bin/mmwebwx-bin/webwxsendmsg?lang=de_&pass_ticket=%s";
    private static final String URL_SYNCHRONIZE_CHECK = "https://webpush.web.wechat.com/cgi-bin/mmwebwx-bin/synccheck?r=1518496288126&skey=%s&sid=%s&uin=%s&deviceid=%s&synckey=%s&_=1518496248732";

    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    private String wxskey;
    private String wxsid;
    private String wxuin;
    private String wxuuid;
    private String wxpassticket;
    private List<String> wxcookies;
    private Map<Long, Long> wxsynckey;
    private List<WeChatContact> contacts;
    private Map<WeChatContact, List<WeChatMessage>> chats;
    private WeChatContact user;
    private int connectTimeout;
    private int readTimeout;
    private boolean connected;
    private transient List<SessionListener> sessionListener = new ArrayList<>();
    private transient List<ContactListener> contactListener = new ArrayList<>();
    private transient List<MessageListener> messageListener = new ArrayList<>();

    public WeChatSession setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public WeChatSession setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    protected HttpsURLConnection openConnection(String url) throws IOException {
        return openConnection(new URL(url));
    }

    protected HttpsURLConnection openConnection(URL url) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        if (connectTimeout > 0)
            connection.setConnectTimeout(connectTimeout);
        if (readTimeout > 0)
            connection.setReadTimeout(readTimeout);
        if (connected) {
            for (String s : wxcookies) {
                connection.addRequestProperty("Cookie", s.substring(0, s.indexOf("; Domain")));
            }
            connection.addRequestProperty("Cookie", "last_wxuin: " + wxuin);
            connection.addRequestProperty("Cookie", "login_frequency: 1");
        }
        return connection;
    }

    public List<WeChatMessage> getChat(String userName) throws IOException {
        return getChat(getContact(userName));
    }

    public List<WeChatMessage> getChat(WeChatContact contact) {
        if (chats == null) {
            chats = new HashMap<>();
        }

        if (!chats.containsKey(contact)) {
            chats.put(contact, new ArrayList<>());
        }
        return chats.get(contact);
    }

    public WeChatContact getContact(String userName) throws IOException {
        return getContact(new WeChatContact(userName));
    }

    public WeChatContact getContact(WeChatContact contact) throws IOException {
        int index = loadContacts().indexOf(contact);
        return index >= 0 ? loadContacts().get(index) : null;
    }

    public List<WeChatContact> loadContacts() throws IOException {
        return loadContacts(false);
    }

    public synchronized List<WeChatContact> loadContacts(boolean refresh) throws IOException {
        if (contacts == null) {
            contacts = new ArrayList<>();
        }
        if (!refresh && !contacts.isEmpty()) {
            return contacts;
        }
        if (!connected) {
            throw new WeChatNotConnectedException();
        }

        HttpsURLConnection connection = openConnection("https://web.wechat.com/cgi-bin/mmwebwx-bin/webwxgetcontact?lang=de_&pass_ticket=" + wxpassticket + "&r=1518281986728&seq=0&skey=" + wxskey);

        contacts.clear();
        for (Object member : parseJSONResponse(connection).getJSONArray("MemberList")) {
            WeChatContact contact = WeChatContact.fromJson((JSONObject) member);
            contacts.add(contact);
            if (contactListener != null)
                contactListener.forEach(listener -> listener.onContactUpdate(contact));
        }

        return contacts;
    }

    public synchronized void handshake() throws IOException {
        if (!connected) {
            throw new WeChatNotConnectedException();
        }

        String syncKeys = "";
        for (Map.Entry<Long, Long> entry : wxsynckey.entrySet()) {
            syncKeys += entry.getKey() + "_" + entry.getValue() + "|";
        }
        syncKeys = syncKeys.substring(0, syncKeys.length() - 1);
        String device = "e" + System.currentTimeMillis() + "91";

        HttpsURLConnection connection = openConnection(String.format(URL_SYNCHRONIZE_CHECK, wxskey, wxsid, wxuin, device, syncKeys));

        getValueFromJavaScript(connection, "window.synccheck", "UTF-8");
    }

    public synchronized void update() throws IOException {
        if (!connected) {
            throw new WeChatNotConnectedException();
        }

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

        if (json.has("AddMsgList")) {
            for (int i = 0; i < json.getJSONArray("AddMsgList").length(); i++) {
                WeChatMessage message = WeChatMessage.fromJson(json.getJSONArray("AddMsgList").getJSONObject(i));
                WeChatContact contact = user.equals(message.getFromUserName()) ?
                        getContact(message.getToUserName()) : getContact(message.getFromUserName());

                getChat(contact).add(message);
                if (messageListener != null)
                    messageListener.forEach(listener -> listener.onMessageReceived(contact, message));
            }
        }
    }

    public synchronized void sendMessage(WeChatMessage message) throws IOException {
        if (!connected) {
            throw new WeChatNotConnectedException();
        }

        HttpsURLConnection connection = openConnection(String.format(URL_SEND_MESSAGE, wxpassticket));

        message.setId("" + System.currentTimeMillis());
        JSONObject json = new JSONObject();
        json.put("Type", 1);
        json.put("Content", message.getContent());
        json.put("FromUserName", user.getUserName());
        json.put("ToUserName", message.getToUserName());
        json.put("LocalID", message.getId());
        json.put("ClientMsgId", message.getId());

        sendJSONRequest(connection, "Msg", json);
        parseJSONResponse(connection);
    }

    public String loadQRCode() throws IOException {
        if (connected) {
            throw new WeChatException("cannot get new QR code if already connected");
        }

        try {
            HttpsURLConnection connection = openConnection(String.format(URL_QR_CODE_REQUEST, APP_ID, "1518272529736"));
            wxuuid = getValueFromJavaScript(connection, "window.QRLogin.uuid", "UTF-8");
            return String.format(URL_QR_CODE_DOWNLOAD, wxuuid);
        } catch (IOException e) {
            throw e;
        }
    }

    public boolean isConnected() {
        return connected;
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

        if (json.has("BaseResponse")) {
            int result = json.getJSONObject("BaseResponse").getInt("Ret");
            if (1101 == result) {
                connected = false;
                throw new WeChatNotConnectedException();
            }
        }

        if (json.has("SyncKey")) {
            if (wxsynckey == null) wxsynckey = new HashMap<>();
            for (Object key : json.getJSONObject("SyncKey").getJSONArray("List")) {
                wxsynckey.put(((JSONObject) key).getLong("Key"), ((JSONObject) key).getLong("Val"));
            }
        }

        return json;
    }

    public boolean connect() throws IOException {
        return connect(10);
    }

    public synchronized boolean connect(int attempts) throws IOException {
        if (connected) {
            throw new WeChatException("please disconnect first before open a new connection");
        }
        if (wxuuid == null) {
            throw new WeChatException("please download QR code first before open a new connection");
        }

        try {
            for (int attempt = 0; attempt < attempts; attempt++) {
                wxcookies = null;
                wxsynckey = null;

                HttpsURLConnection connection = openConnection(String.format(URL_LOGIN_1, wxuuid));
                String code = getValueFromJavaScript(connection, "window.code", "UTF-8");

                if ("201".equals(code)) {
                    initAfterLogin();
                    return connected = true;
                } else if (!"408".equals(code)) {
                    break;
                }
            }
        } catch (IOException e) {
            wxuuid = null;
            throw e;
        }

        wxuuid = null;
        return false;
    }

    private void initAfterLogin() throws IOException {
        HttpsURLConnection connection = openConnection(String.format(URL_LOGIN_2, wxuuid));
        String redirect = getValueFromJavaScript(connection, "window.redirect_uri", "UTF-8");

        connection = openConnection(redirect + "&fun=new&version=v2&lang=de_");
        String response = getStringFromInputStream(connection, "UTF-8");

        wxskey = response.substring(response.indexOf("<skey>") + 6, response.indexOf("</skey>"));
        wxsid = response.substring(response.indexOf("<wxsid>") + 7, response.indexOf("</wxsid>"));
        wxuin = response.substring(response.indexOf("<wxuin>") + 7, response.indexOf("</wxuin>"));
        wxpassticket = response.substring(response.indexOf("<pass_ticket>") + 13, response.indexOf("</pass_ticket>"));
        wxcookies = connection.getHeaderFields().get("Set-Cookie");

        if (wxcookies == null || wxcookies.size() < 1) {
            throw new WeChatException("failed to get session cookies after login");
        }

        connection = openConnection(String.format(URL_INIT, wxpassticket));
        sendJSONRequest(connection);

        JSONObject json = parseJSONResponse(connection);
        if (json.has("User")) {
            user = WeChatContact.fromJson((JSONObject) json.get("User"));
        }
    }

    public WeChatSession addSessionListener(SessionListener listener) {
        if (sessionListener == null) {
            sessionListener = new ArrayList<>();
        }
        sessionListener.add(listener);
        return this;
    }

    public WeChatSession removeSessionListener(SessionListener listener) {
        if (sessionListener == null) {
            sessionListener = new ArrayList<>();
        }
        sessionListener.remove(listener);
        return this;
    }

    public WeChatSession addMessageListener(MessageListener listener) {
        if (messageListener == null) {
            messageListener = new ArrayList<>();
        }
        messageListener.add(listener);
        return this;
    }

    public WeChatSession removeMessageListener(MessageListener listener) {
        if (messageListener == null) {
            messageListener = new ArrayList<>();
        }
        messageListener.remove(listener);
        return this;
    }

    public WeChatSession addContactListener(ContactListener listener) {
        if (contactListener == null) {
            contactListener = new ArrayList<>();
        }
        contactListener.add(listener);
        return this;
    }

    public WeChatSession removeContactListener(ContactListener listener) {
        if (contactListener == null) {
            contactListener = new ArrayList<>();
        }
        contactListener.remove(listener);
        return this;
    }

    public interface SessionListener {
    }

    public interface MessageListener {
        void onMessageReceived(WeChatContact contact, WeChatMessage message);
    }

    public interface ContactListener {
        void onContactUpdate(WeChatContact contact);
    }
}
