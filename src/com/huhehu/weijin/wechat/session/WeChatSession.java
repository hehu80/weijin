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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.image.Image;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatSession implements Serializable {

    private transient WeChatConnection connection;
    private transient WeChatMediaCache mediaCache = new WeChatMediaCache(this);
    private List<WeChatContact> contacts = new ArrayList<>();
    private Map<WeChatContact, List<WeChatMessage>> chats = new HashMap<>();
    private WeChatContact loginUser;
    private WeChatContact logoutUser;
    private WeChatContact activeUser;
    private transient WeChatMultiEventHandler<WeChatContact> onContactUpdated;
    private transient WeChatMultiEventHandler<WeChatMessage> onMessageReceived;
    private transient WeChatMultiEventHandler<WeChatMessage> onMessageUpdated;
    private transient WeChatMultiEventHandler<Exception> onSessionError;
    private transient WeChatSingleEventHandler<Image> onSessionQRCodeReceived;
    private transient WeChatSingleEventHandler<WeChatContact> onSessionConnect;
    private transient WeChatSingleEventHandler<WeChatContact> onSessionDisconnect;
    private transient WeChatSingleEventHandler<WeChatContact> onSessionChatSelected;

    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();

        mediaCache = new WeChatMediaCache(this);
        mediaCache.loadAll(); // just start loading all files directly again
    }

    /**
     *
     * @return
     */
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    /**
     *
     */
    public void connect() {
        if (connection == null) {
            connection = new WeChatConnection(this);
        }
    }

    /**
     *
     */
    public void disconnect() {
        if (connection != null) {
            connection.shutdownNow();
            connection = null;
        }

        mediaCache.shutdownNow();
    }

    /**
     *
     * @param message
     * @throws WeChatException
     */
    public synchronized void sendMessage(WeChatMessage message) throws WeChatException {
        if (!isConnected()) {
            throw new WeChatException("please connect first before send a new message");
        }

        message.setTime(Instant.now());
        connection.sendMessage(message);
    }

    /**
     *
     * @param contact
     */
    public synchronized void selectChat(WeChatContact contact) {
        if (activeUser == null || !activeUser.equals(contact)) {
            onChatSelected(contact);
        }
    }

    /**
     *
     * @param e
     */
    protected synchronized void onError(Exception e) {
        fireEvents(onSessionError, e);
    }

    /**
     *
     * @param newMessages
     */
    protected synchronized void onMessageReceived(WeChatMessage... newMessages) {
        for (WeChatMessage message : newMessages) {
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

        fireEvents(onMessageReceived, newMessages);
    }

    private void refreshUserNames(Map<String, String> oldUserNames, Collection objects) {
        for (Object object : objects) {
            if (object instanceof WeChatContact) {
                WeChatContact contact = (WeChatContact) object;
                String newUserName = oldUserNames.get(contact.getUserName());
                if (newUserName != null) {
                    contact.setUserName(newUserName);
                }
            } else if (object instanceof WeChatMessage) {
                WeChatMessage message = (WeChatMessage) object;
                String newFromUserName = oldUserNames.get(message.getFromUserName());
                if (newFromUserName != null) {
                    message.setFromUserName(newFromUserName);
                }
                String newToUserName = oldUserNames.get(message.getToUserName());
                if (newToUserName != null) {
                    message.setToUserName(newToUserName);
                }
            }
        }
    }

    /**
     *
     * @param newContacts
     */
    protected synchronized void onContactReceived(WeChatContact... newContacts) {
        // CAUTION: each session we have to update the contact names
        // since WeChat is changeing them all after each login
        List<WeChatContact> removedContacts = new ArrayList<>();

        // first we collect all new user names
        Map<String, String> newUserNames = new HashMap<>();
        for (WeChatContact contact : newContacts) {
            if (contact.getSeq() != null) {
                newUserNames.put(contact.getSeq(), contact.getUserName());
            }
        }

        // TODO check UIN too
        // than we collect all old user names
        Map<String, String> oldUserNames = new HashMap<>();
        for (WeChatContact oldContact : contacts) {
            if (oldContact.getSeq() != null) {
                String newUserName = newUserNames.get(oldContact.getSeq());
                if (newUserName != null) {
                    oldUserNames.put(oldContact.getUserName(), newUserName);
                } else {
                    removedContacts.add(oldContact);
                }
            }
        }

        // at last, we replace all old user names with new user names
        refreshUserNames(oldUserNames, contacts);
        refreshUserNames(oldUserNames, chats.keySet());
        refreshUserNames(oldUserNames, Arrays.asList(loginUser, logoutUser, activeUser));
        for (List<WeChatMessage> chat : chats.values()) {
            refreshUserNames(oldUserNames, chat);
        }

        // TODO remove not longer existing contacts
        onContactUpdated(newContacts);
    }

    /**
     *
     * @param newContacts
     */
    protected synchronized void onContactUpdated(WeChatContact... newContacts) {
        for (WeChatContact contact : newContacts) {
            int index = contacts.indexOf(contact);
            if (index >= 0) {
                contacts.set(index, contact);
                // load avatar from cache but don't force refresh (download again)
                mediaCache.downloadMedia(contact, false, () -> fireEvents(onContactUpdated, contact));
            } else {
                contacts.add(contact);
                // always download avatar again for new contacts
                mediaCache.downloadMedia(contact, true, () -> fireEvents(onContactUpdated, contact));
            }
        }

        fireEvents(onContactUpdated, newContacts);
    }

    /**
     *
     * @param contact
     */
    protected synchronized void onChatSelected(WeChatContact contact) {
        if (activeUser == null || !activeUser.equals(contact)) {
            activeUser = getContact(contact);
            fireEvents(onSessionChatSelected, activeUser);
        }
    }

    /**
     *
     * @param user
     */
    protected synchronized void onConnect(WeChatContact user) {
        loginUser = getContact(user);
        if (!loginUser.equals(logoutUser)) {
            mediaCache.clearAll(true); // reset cache if user changed
        }

        logoutUser = loginUser;
        fireEvents(onSessionConnect, loginUser);
    }

    /**
     *
     */
    protected synchronized void onDisconnect() {
        fireEvents(onSessionDisconnect, logoutUser);
    }

    /**
     *
     * @param url
     */
    protected synchronized void onQRCodeReceived(String url) {
        mediaCache.downloadMedia("qrCode", true, false, url, () -> {
            if (!connection.isConnected()) {
                fireEvents(onSessionQRCodeReceived, mediaCache.getMedia("qrCode"));
            }
        });
    }

    /**
     *
     * @return
     */
    public synchronized WeChatContact getLoginUser() {
        return getContact(loginUser);
    }

    /**
     *
     * @return
     */
    public synchronized WeChatContact getSelectedChat() {
        return getContact(activeUser);
    }

    /**
     *
     * @return
     */
    public synchronized WeChatContact getFileHelper() {
        return getContact(USER_FILE_HELPER);
    }

    /**
     *
     * @param userName
     * @return
     */
    public synchronized WeChatContact getContact(String userName) {
        return getContact(new WeChatContact(userName));
    }

    /**
     *
     * @param contact
     * @return
     */
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

    /**
     *
     * @return
     */
    public synchronized List<WeChatContact> getContacts() {
        return new ArrayList<>(contacts);
    }

    /**
     *
     * @param contact
     * @return
     */
    public synchronized List<WeChatMessage> getMessages(WeChatContact contact) {
        if (chats.containsKey(contact)) {
            return new ArrayList<>(chats.get(contact));
        } else {
            return new ArrayList<>();
        }
    }

    /**
     *
     * @param contact
     * @return
     */
    public Image getMedia(WeChatContact contact) {
        return mediaCache == null ? null : mediaCache.getMedia(contact);
    }

    /**
     *
     * @param message
     * @return
     */
    public Image getMedia(WeChatMessage message) {
        return mediaCache == null ? null : mediaCache.getMedia(message);
    }

    /**
     *
     * @return
     */
    public WeChatConnection getConnection() {
        return connection;
    }

    /**
     *
     * @return
     */
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

    /**
     *
     * @param onContactUpdatedHandler
     * @return
     */
    public WeChatSession setOnContactUpdated(WeChatMultiEventHandler<WeChatContact> onContactUpdatedHandler) {
        this.onContactUpdated = onContactUpdatedHandler;
        return this;
    }

    /**
     *
     * @param onMessageReceivedHandler
     * @return
     */
    public WeChatSession setOnMessageReceived(WeChatMultiEventHandler<WeChatMessage> onMessageReceivedHandler) {
        this.onMessageReceived = onMessageReceivedHandler;
        return this;
    }

    /**
     *
     * @param onMessageUpdatedHandler
     * @return
     */
    public WeChatSession setOnMessageUpdated(WeChatMultiEventHandler<WeChatMessage> onMessageUpdatedHandler) {
        this.onMessageUpdated = onMessageUpdatedHandler;
        return this;
    }

    /**
     *
     * @param onSessionErrorHandler
     * @return
     */
    public WeChatSession setOnSessionError(WeChatMultiEventHandler<Exception> onSessionErrorHandler) {
        this.onSessionError = onSessionErrorHandler;
        return this;
    }

    /**
     *
     * @param onSessionQRCodeReceivedHandler
     * @return
     */
    public WeChatSession setOnSessionQRCodeReceived(WeChatSingleEventHandler<Image> onSessionQRCodeReceivedHandler) {
        this.onSessionQRCodeReceived = onSessionQRCodeReceivedHandler;
        return this;
    }

    /**
     *
     * @param onSessionConnectHandler
     * @return
     */
    public WeChatSession setOnSessionConnect(WeChatSingleEventHandler<WeChatContact> onSessionConnectHandler) {
        this.onSessionConnect = onSessionConnectHandler;
        return this;
    }

    /**
     *
     * @param onSessionDisconnectHandler
     * @return
     */
    public WeChatSession setOnSessionDisconnect(WeChatSingleEventHandler<WeChatContact> onSessionDisconnectHandler) {
        this.onSessionDisconnect = onSessionDisconnectHandler;
        return this;
    }

    /**
     *
     * @param onSessionChatSelectedHandler
     * @return
     */
    public WeChatSession setOnSessionChatSelected(WeChatSingleEventHandler<WeChatContact> onSessionChatSelectedHandler) {
        this.onSessionChatSelected = onSessionChatSelectedHandler;
        return this;
    }

    /**
     *
     * @param session
     * @param path
     * @return
     * @throws IOException
     */
    public static WeChatSession saveSession(WeChatSession session, Path path) throws IOException {
        try (FileOutputStream connectionFile = new FileOutputStream(path.toFile())) {
            try (ObjectOutputStream output = new ObjectOutputStream(connectionFile)) {
                output.writeObject(session);
            }
        }
        return session;
    }

    /**
     *
     * @param path
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static WeChatSession loadSession(Path path) throws IOException, ClassNotFoundException {
        try (FileInputStream connectionFile = new FileInputStream(path.toFile())) {
            try (ObjectInputStream input = new ObjectInputStream(connectionFile)) {
                return (WeChatSession) input.readObject();
            }
        }
    }
}
