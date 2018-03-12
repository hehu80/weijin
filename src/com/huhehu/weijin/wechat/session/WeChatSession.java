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
import com.huhehu.weijin.wechat.contacts.WeChatContact;
import static com.huhehu.weijin.wechat.contacts.WeChatUser.USER_FILE_HELPER;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import com.huhehu.weijin.wechat.session.event.WeChatMultiEventHandler;
import com.huhehu.weijin.wechat.session.event.WeChatSingleEventHandler;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;

public class WeChatSession implements Serializable {

    private transient WeChatConnection connection;
    private transient WeChatMediaCache mediaCache;
    private List<WeChatContact> contacts = new ArrayList<>();
    private Map<WeChatContact, List<WeChatMessage>> chats = new HashMap<>();
    private WeChatContact loginUser;
    private WeChatContact logoutUser;
    private WeChatContact selectedChat;
    private transient WeChatMultiEventHandler<WeChatContact> onContactUpdated;
    private transient WeChatMultiEventHandler<WeChatMessage> onMessageReceived;
    private transient WeChatMultiEventHandler<WeChatMessage> onMessageUpdated;
    private transient WeChatMultiEventHandler<Exception> onSessionError;
    private transient WeChatSingleEventHandler<Image> onSessionQRCodeReceived;
    private transient WeChatSingleEventHandler<WeChatContact> onSessionConnect;
    private transient WeChatSingleEventHandler<WeChatContact> onSessionDisconnect;
    private transient WeChatSingleEventHandler<WeChatContact> onSessionChatSelected;

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public void connect() {
        if (connection == null) {
            connection = new WeChatConnection(this);

            mediaCache = new WeChatMediaCache(this);
            mediaCache.loadAll();
        }
    }

    public void disconnect() {
        if (connection != null) {
            connection.shutdownNow();
            connection = null;
        }

        if (mediaCache != null) {
            mediaCache.shutdownNow();
            mediaCache = null;
        }
    }

    public synchronized void sendMessage(WeChatMessage message) throws WeChatException {
        if (!isConnected()) {
            throw new WeChatException("please connect first before send a new message");
        }

        connection.sendMessage(message);
    }

    public synchronized void selectChat(WeChatContact contact) {
        if (selectedChat == null || !selectedChat.equals(contact)) {
            onChatSelected(contact);
        }
    }

    protected synchronized void onError(Exception e) {
        fireEvents(onSessionError, e);
    }

    protected synchronized void onMessageReceived(WeChatMessage... messages) {
        for (WeChatMessage message : messages) {
            if (loginUser.equals(message.getToUserName())) {
                message.setReceived(true);
            } else {
                message.setReceived(false);
            }

            WeChatContact contact = new WeChatContact(message.isReceived() ? message.getFromUserName() : message.getToUserName());

            if (!chats.containsKey(contact)) {
                chats.put(contact, new ArrayList<>());
            }
            chats.get(contact).add(message);

            if (mediaCache.isMediaMessage(message)) {
                mediaCache.downloadMedia(message, true, () -> fireEvents(onMessageUpdated, message));
            }
        }

        fireEvents(onMessageReceived, messages);
    }

    protected synchronized void onContactUpdated(WeChatContact... contacts) {
        for (WeChatContact contact : contacts) {
            int index = this.contacts.indexOf(contact);
            boolean newAvatar = false;
            if (index >= 0) {
                newAvatar = contact.getImageUrl() != null && !contact.getImageUrl().equals(this.contacts.get(index).getImageUrl());
                this.contacts.set(index, contact);
            } else {
                this.contacts.add(contact);
            }

            mediaCache.downloadMedia(contact, newAvatar, () -> fireEvents(onContactUpdated, contact));
        }

        fireEvents(onContactUpdated, contacts);
    }

    protected synchronized void onChatSelected(WeChatContact contact) {
        if (selectedChat == null || !selectedChat.equals(contact)) {
            selectedChat = getContact(contact);
            fireEvents(onSessionChatSelected, selectedChat);
        }
    }

    protected synchronized void onConnect(WeChatContact user) {
        loginUser = getContact(user);
        fireEvents(onSessionConnect, loginUser);
    }

    protected synchronized void onDisconnect() {
        logoutUser = getContact(loginUser);
        fireEvents(onSessionDisconnect, logoutUser);
    }

    protected synchronized void onQRCodeReceived(String url) {
        mediaCache.downloadMedia("qrCode", true, false, url, () -> {
            if (!connection.isConnected()) {
                fireEvents(onSessionQRCodeReceived, mediaCache.getMedia("qrCode"));
            }
        });
    }

    public synchronized WeChatContact getLoginUser() {
        return getContact(loginUser);
    }

    public synchronized WeChatContact getSelectedChat() {
        return getContact(selectedChat);
    }

    public synchronized WeChatContact getFileHelper() {
        return getContact(USER_FILE_HELPER);
    }

    public synchronized WeChatContact getContact(String userName) {
        return getContact(new WeChatContact(userName));
    }

    public synchronized WeChatContact getContact(WeChatContact contact) {
        if (contact == null) {
            return null;
        } else {
            int index = contacts.indexOf(contact);
            if (index >= 0) {
                return contacts.get(index);
            } else {
                return contact;
            }
        }
    }

    public synchronized List<WeChatContact> getContacts() {
        return new ArrayList<>(contacts);
    }

    public synchronized List<WeChatMessage> getMessages(WeChatContact contact) {
        if (chats.containsKey(contact)) {
            return new ArrayList<>(chats.get(contact));
        } else {
            return new ArrayList<>();
        }
    }
    
    public WeChatConnection getConnection() {
        return connection;
    }

    public WeChatMediaCache getMediaCache() {
        return mediaCache;
    }

    private void fireEvents(EventListener eventHandler, Object... events) {
        if (eventHandler != null) {
            if (eventHandler instanceof WeChatSingleEventHandler) {
                ((WeChatSingleEventHandler) eventHandler).onWeChatEvent(events[0]);
            } else if (eventHandler instanceof WeChatMultiEventHandler) {
                ((WeChatMultiEventHandler) eventHandler).onWeChatEvent(events);
            }
        }
    }

    public WeChatSession setOnContactUpdated(WeChatMultiEventHandler<WeChatContact> onContactUpdatedHandler) {
        this.onContactUpdated = onContactUpdatedHandler;
        return this;
    }

    public WeChatSession setOnMessageReceived(WeChatMultiEventHandler<WeChatMessage> onMessageReceivedHandler) {
        this.onMessageReceived = onMessageReceivedHandler;
        return this;
    }

    public WeChatSession setOnMessageUpdated(WeChatMultiEventHandler<WeChatMessage> onMessageUpdatedHandler) {
        this.onMessageUpdated = onMessageUpdatedHandler;
        return this;
    }

    public WeChatSession setOnSessionError(WeChatMultiEventHandler<Exception> onSessionErrorHandler) {
        this.onSessionError = onSessionErrorHandler;
        return this;
    }

    public WeChatSession setOnSessionQRCodeReceived(WeChatSingleEventHandler<Image> onSessionQRCodeReceivedHandler) {
        this.onSessionQRCodeReceived = onSessionQRCodeReceivedHandler;
        return this;
    }

    public WeChatSession setOnSessionConnect(WeChatSingleEventHandler<WeChatContact> onSessionConnectHandler) {
        this.onSessionConnect = onSessionConnectHandler;
        return this;
    }

    public WeChatSession setOnSessionDisconnect(WeChatSingleEventHandler<WeChatContact> onSessionDisconnectHandler) {
        this.onSessionDisconnect = onSessionDisconnectHandler;
        return this;
    }

    public WeChatSession setOnSessionChatSelected(WeChatSingleEventHandler<WeChatContact> onSessionChatSelectedHandler) {
        this.onSessionChatSelected = onSessionChatSelectedHandler;
        return this;
    }

}
