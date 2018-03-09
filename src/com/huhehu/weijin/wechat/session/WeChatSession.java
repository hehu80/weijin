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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeChatSession implements Serializable {

    private transient WeChatConnection connection;
    private transient WeChatMediaCache mediaCache;
    private List<WeChatContact> contacts = new ArrayList<>();
    private Map<WeChatContact, List<WeChatMessage>> chats = new HashMap<>();
    private WeChatContact loginUser;
    private WeChatContact selectedChat;
    private transient WeChatContactHandler contactHandler;
    private transient WeChatMessageHandler messageHandler;
    private transient WeChatSessionHandler sessionHandler;

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public void connect() throws WeChatException {
        if (connection != null) {
            throw new WeChatException("please disconnect first before open a new connection");
        }

        connection = new WeChatConnection(this);

        mediaCache = new WeChatMediaCache(this);
    }

    public void disconnect() throws WeChatException {
        if (connection == null) {
            throw new WeChatException("not connected");
        }

        connection.shutdownNow();
        connection = null;

        mediaCache.shutdownNow();
        mediaCache = null;
    }

    public synchronized void sendMessage(WeChatMessage message) throws WeChatException {
        if (!isConnected()) {
            throw new WeChatException("please connect first before send a new message");
        }

        connection.sendMessage(message);
    }

    public synchronized void selectChat(WeChatContact contact) throws WeChatException {
        if (selectedChat == null || !selectedChat.equals(contact)) {
            onChatSelected(contact);
        }
    }

    protected synchronized void onError(Exception e) {
        if (sessionHandler != null) {
            sessionHandler.onError(e);
        }
    }

    protected synchronized void onMessageReceived(WeChatMessage... messages) {
        for (WeChatMessage message : messages) {
            WeChatContact contact = new WeChatContact(loginUser.equals(message.getToUserName()) ? message.getFromUserName() : message.getToUserName());

            if (!chats.containsKey(contact)) {
                chats.put(contact, new ArrayList<>());
            }
            chats.get(contact).add(message);

            if (mediaCache.isMediaMessage(message)) {
                mediaCache.downloadMedia(message, true, () -> {
                    if (messageHandler != null) {
                        messageHandler.onMessageUpdated(message);
                    }
                });
            }
        }

        if (messageHandler != null) {
            messageHandler.onMessageReceived(messages);
        }
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

            mediaCache.downloadMedia(contact, newAvatar, () -> {
                if (contactHandler != null) {
                    contactHandler.onContactUpdated(contact);
                }
            });
        }

        if (contactHandler != null) {
            contactHandler.onContactUpdated(contacts);
        }
    }

    protected synchronized void onChatSelected(WeChatContact contact) {
        if (selectedChat == null || !selectedChat.equals(contact)) {
            selectedChat = getContact(contact);
            if (sessionHandler != null) {
                sessionHandler.onChatSelected(selectedChat);
            }
        }
    }

    protected synchronized void onConnect(WeChatContact user) {
        loginUser = getContact(user);
        if (sessionHandler != null) {
            sessionHandler.onConnect(loginUser);
        }
    }

    protected synchronized void onDisconnect() {
        if (sessionHandler != null) {
            sessionHandler.onDisconnect();
        }
    }

    protected synchronized void onQRCodeReceived(String url) {
        mediaCache.downloadMedia("qrCode", true, false, url, () -> {
            if (!connection.isConnected()) {
                if (sessionHandler != null) {
                    sessionHandler.onQRCodeReceived(mediaCache.getMedia("qrCode"));
                }
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

    public WeChatSession setContactHandler(WeChatContactHandler contactHandler) {
        this.contactHandler = contactHandler;
        return this;
    }

    public WeChatSession setMessageHandler(WeChatMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }

    public WeChatSession setSessionHandler(WeChatSessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
        return this;
    }
}
