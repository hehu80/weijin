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
import com.huhehu.weijin.wechat.conversation.WeChatMessage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WeChatSession {
    private WeChatConnection connection;
    private List<WeChatContact> contacts = new ArrayList<>();
    private Map<WeChatContact, Image> contactAvatars = new HashMap<>();
    private Map<WeChatContact, List<WeChatMessage>> chats = new HashMap<>();
    private Image qrCode;
    private WeChatContact loginUser;
    private WeChatContact activeUser;
    private ExecutorService mediaDownloader;
    private WeChatContactHandler contactHandler;
    private WeChatMessageHandler messageHandler;
    private WeChatSessionHandler sessionHandler;

    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    public void connect() throws WeChatException {
        if (connection != null) {
            throw new WeChatException("please disconnect first before open a new connection");
        }

        connection = new WeChatConnection(this);
        connection.start();

        mediaDownloader = Executors.newFixedThreadPool(5);
    }

    public void disconnect() throws WeChatException {
        if (connection == null) {
            throw new WeChatException("not connected");
        }

        connection.kill();
        connection = null;

        mediaDownloader.shutdown();
    }

    public synchronized void sendMessage(WeChatMessage message) throws WeChatException {
        if (!isConnected()) {
            throw new WeChatException("please connect first before send a new message");
        }

        connection.sendMessage(message);
    }

    protected synchronized void onError(Exception e) {
        if (sessionHandler != null) {
            sessionHandler.onError(e);
        }
    }

    protected synchronized void onQRCodeReceived(String url) {
        mediaDownloader.submit(() -> {
            if (!connection.isConnected()) {
                try (InputStream mediaStream = connection.downloadMedia(url)) {
                    synchronized (WeChatSession.this) {
                        WeChatSession.this.qrCode = ImageIO.read(mediaStream);
                    }

                    if (sessionHandler != null) {
                        sessionHandler.onQRCodeReceived(WeChatSession.this.qrCode);
                    }
                } catch (IOException ignore) {
                }
            }
        });

    }

    protected synchronized void onMessageReceived(WeChatMessage... messages) {
        for (WeChatMessage message : messages) {
            WeChatContact contact = new WeChatContact(loginUser.equals(message.getToUserName()) ? message.getFromUserName() : message.getToUserName());

            if (!chats.containsKey(contact)) {
                chats.put(contact, new ArrayList<>());
            }
            chats.get(contact).add(message);
        }

        if (messageHandler != null) {
            messageHandler.onMessageReceived(messages);
        }
    }

    protected synchronized void onContactUpdated(WeChatContact... contacts) {
        for (WeChatContact contact : contacts) {
            int index = this.contacts.indexOf(contact);
            if (index >= 0) {
                this.contacts.set(index, contact);
            } else {
                this.contacts.add(contact);
            }

            if (contact.equals(loginUser)) {
                loginUser = contact;
            }

            mediaDownloader.submit(() -> {
                if (connection.isConnected()) {
                    try (InputStream mediaStream = connection.downloadMedia(contact.getImageUrl())) {
                        synchronized (WeChatSession.this) {
                            contactAvatars.put(contact, ImageIO.read(mediaStream).getScaledInstance(64, 64, 0));
                        }
                        if (contactHandler != null) {
                            contactHandler.onContactUpdated(contact);
                        }
                    } catch (IOException ignore) {
                    }
                }
            });
        }

        if (contactHandler != null) {
            contactHandler.onContactUpdated(contacts);
        }
    }

    protected synchronized void onChatSelected(WeChatContact contact) {
        activeUser = getContact(contact);
        if (messageHandler != null) {
            messageHandler.onChatSelected(activeUser);
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

    public synchronized WeChatContact getUser() {
        return loginUser;
    }

    public synchronized WeChatContact getFileHelper() {
        return getContact("filehelper");
    }

    public synchronized WeChatContact getContact(String userName) {
        return getContact(new WeChatContact(userName));
    }

    public synchronized WeChatContact getContact(WeChatContact contact) {
        int index = contacts.indexOf(contact);
        if (index >= 0) {
            return contacts.get(index);
        } else {
            return contact;
        }
    }

    public synchronized Image getContactAvatar(WeChatContact contact) {
        return contactAvatars.get(contact);
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

    public void setContactHandler(WeChatContactHandler contactHandler) {
        this.contactHandler = contactHandler;
    }

    public void setMessageHandler(WeChatMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void setSessionHandler(WeChatSessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }
}
