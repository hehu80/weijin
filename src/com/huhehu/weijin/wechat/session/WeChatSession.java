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

import com.huhehu.weijin.wechat.WeChatNotConnectedException;
import com.huhehu.weijin.wechat.contacts.WeChatContact;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javafx.scene.image.Image;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatSession implements Serializable {

    private transient WeChatConnection connection;
    private transient WeChatMediaCache mediaCache;
    private List<Contact> contactsSession = new ArrayList<>(); // all the contacts
    private List<Contact> contactsSaved = new ArrayList<>(); // contacts stored by user
    private List<Contact> contactsActive = new ArrayList<>(); // contacts with active chats
    private Map<Contact, List<WeChatMessage>> chats = new HashMap<>();
    private Contact loginUser;
    private Contact logoutUser;
    private Contact activeUser;
    private transient WeChatMultiEventHandler<WeChatContact> onContactSavedRemoved;
    private transient WeChatMultiEventHandler<WeChatContact> onContactActiveRemoved;
    private transient WeChatMultiEventHandler<WeChatContact> onContactUpdated;
    private transient WeChatMultiEventHandler<WeChatMessage> onMessageReceived;
    private transient WeChatMultiEventHandler<Exception> onSessionError;
    private transient WeChatSingleEventHandler<Image> onSessionQRCodeReceived;
    private transient WeChatSingleEventHandler<WeChatContact> onSessionConnect;
    private transient WeChatSingleEventHandler<WeChatContact> onSessionDisconnect;
    private transient WeChatSingleEventHandler<WeChatContact> onSessionUserActive;

    public WeChatSession(){
        mediaCache = createMediaCache();
        mediaCache.loadAll(); // just start loading all files directly again
    }
    
    private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        inputStream.defaultReadObject();

        mediaCache = createMediaCache();
        mediaCache.loadAll(); // just start loading all files directly again
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

    /**
     * 
     * @return 
     */
    protected WeChatMediaCache createMediaCache(){
        return new WeChatMediaCache(this);
    }
    
    /**
     *
     * @return
     */
    public WeChatMediaCache getMediaCache() {
        return mediaCache;
    }

    /**
     *
     * @param contact
     * @return
     */
    public Image getMedia(WeChatContact contact) {
        return contact == null || mediaCache == null ? null : mediaCache.getMedia(contact);
    }

    /**
     *
     * @param message
     * @return
     */
    public Image getMedia(WeChatMessage message) {
        return message == null || mediaCache == null ? null : mediaCache.getMedia(message);
    }
    
    /**
     * 
     * @return 
     */
    protected WeChatConnection createConnection(){
        return new WeChatConnection(this);
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
    public boolean isConnected() {
        return connection != null && connection.isConnected() && loginUser != null;
    }

    /**
     *
     */
    public void connect() {
        if (connection == null) {
            connection = createConnection();
            connection.startSynchronize();
        }
    }

    /**
     *
     */
    public void disconnect() {
        if (connection != null) {
            connection.stopSynchronize();
            connection = null;
        }

        mediaCache.shutdownNow();
    }

    /**
     *
     * @return
     */
    public synchronized WeChatContact getUserLogin() {
        return toActualContact(loginUser);
    }

    /**
     *
     * @return
     */
    public synchronized WeChatContact getUserActive() {
        return toActualContact(activeUser);
    }

    /**
     *
     * @param contact
     */
    public synchronized void setUserActive(WeChatContact contact) {
        if (activeUser == null || !activeUser.equals(contact)) {
            activeUser = createOrGetSessionContact(contact);
            fireEvents(onSessionUserActive, toActualContact(activeUser));

            if (!isContactActive(contact)) {
                contactsActive.add(activeUser);
                fireEvents(onContactUpdated, toActualContact(activeUser));
            }

            // TODO send to server
        }
    }

    /**
     *
     * @param contact
     * @return
     */
    public synchronized boolean isContactActive(WeChatContact contact) {
        return contact == null ? false : contactsActive.contains(new Contact(contact));
    }

    /**
     *
     * @return
     */
    public synchronized List<WeChatContact> getContactsActive() {
        return toActualContacts(new ArrayList(contactsActive));
    }

    /**
     *
     * @param contact
     * @return
     */
    public synchronized boolean isContactSaved(WeChatContact contact) {
        return contact == null ? false : contactsSaved.contains(new Contact(contact));
    }

    /**
     *
     * @return
     */
    public synchronized List<WeChatContact> getContactsSaved() {
        return toActualContacts(new ArrayList(contactsSaved));
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
            int index = contactsSession.indexOf(new Contact(contact));
            if (index >= 0) {
                return toActualContact(contactsSession.get(index));
            } else {
                return null;
            }
        }
    }

    /**
     *
     * @param contact
     * @return
     */
    public synchronized List<WeChatMessage> getMessages(WeChatContact contact) {
        List<WeChatMessage> messages = contact == null ? null : chats.get(new Contact(contact));
        if (messages != null) {
            return new ArrayList<>(messages);
        } else {
            return new ArrayList<>();
        }
    }

    /**
     *
     * @param message
     * @throws WeChatNotConnectedException
     */
    public synchronized void sendMessage(WeChatMessage message) throws WeChatNotConnectedException {
        if (!isConnected()) {
            throw new WeChatNotConnectedException();
        }

        if (message.getToUser() == null) {
            message.setToUser(activeUser);
        }

        message.setFromUser(loginUser);
        message.setTime(Instant.now());
        connection.sendMessage(message);
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
     * @param contact
     */
    protected synchronized void onConnect(WeChatContact contact) {
        loginUser = createOrGetSessionContact(contact);

        if (!loginUser.equals(logoutUser)) {
            mediaCache.clearAll(true); // reset cache if user changed
        }

        logoutUser = loginUser;
        fireEvents(onSessionConnect, toActualContact(loginUser));
    }

    /**
     *
     */
    protected synchronized void onDisconnect() {
        loginUser = null;
        fireEvents(onSessionDisconnect, toActualContact(logoutUser));
    }

    /**
     *
     * @param newMessages
     */
    protected synchronized void onMessageReceived(WeChatMessage... newMessages) {
        for (WeChatMessage newMessage : newMessages) {
            newMessage.setToUser(createOrGetSessionContact(newMessage.getToUser()));
            newMessage.setFromUser(createOrGetSessionContact(newMessage.getFromUser()));
            newMessage.setReceived(loginUser.equals(newMessage.getToUser()));

            if (newMessage.getTime() == null) {
                newMessage.setTime(Instant.now());
            }

            Contact sessionContact = (Contact) (newMessage.isReceived() ? 
                    newMessage.getFromUser() : newMessage.getToUser());
            if (!chats.containsKey(sessionContact)) {
                chats.put(sessionContact, new ArrayList<>());
            }
            chats.get(sessionContact).add(newMessage);

            if (mediaCache.isMediaMessage(newMessage)) {
                mediaCache.downloadMedia(newMessage, true, () -> fireEvents(onMessageReceived, newMessage));
            }

            if (!isContactActive(sessionContact)) {
                contactsActive.add(sessionContact);
                fireEvents(onContactUpdated, toActualContact(sessionContact));
            }
        }

        fireEvents(onMessageReceived, newMessages);
    }

    /**
     *
     * @param removedContacts
     */
    protected synchronized void onContactRemoved(WeChatContact... removedContacts) {
        // TODO remove contacts implementation
        // fireEvents(onContactRemoved, removedContacts);
    }

    /**
     *
     * @param newContacts
     */
    protected synchronized void onContactSavedUpdated(WeChatContact... newContacts) {
        // TODO remove unused at first call
        onContactUpdated(contactsSaved, newContacts);
    }

    /**
     *
     * @param newContacts
     */
    protected synchronized void onContactActiveUpdated(WeChatContact... newContacts) {
        // TODO remove unused at first call
        onContactUpdated(contactsActive, newContacts);
    }

    private synchronized void onContactUpdated(List<Contact> contacts, WeChatContact... newContacts) {
        // TODO remove unused at first call
        for (WeChatContact newContact : newContacts) {
            Contact sessionContact;
            if (contactsSession.contains(new Contact(newContact))) {
                sessionContact = createOrGetSessionContact(newContact).setActualContact(newContact, false);
                // load avatar from cache but don't force refresh (download again)
                mediaCache.downloadMedia(newContact, false, () -> fireEvents(onContactUpdated, newContact));
            } else {
                sessionContact = createOrGetSessionContact(newContact);
                // always download avatar again for new contactsSession
                mediaCache.downloadMedia(newContact, true, () -> fireEvents(onContactUpdated, newContact));
            }

            if (!contacts.contains(sessionContact)) {
                contacts.add(sessionContact);
            }
        }

        fireEvents(onContactUpdated, toActualContacts(newContacts));
    }

    /**
     *
     * @param contact
     */
    protected synchronized void onUserActivate(WeChatContact contact) {
        setUserActive(contact);
    }

    /**
     *
     * @param contact
     * @return
     */
    protected Contact createOrGetSessionContact(WeChatContact contact) {
        if (contact == null) {
            return null;
        } else {
            Contact sessionContact = contact instanceof Contact ? (Contact) contact : new Contact(contact);
            int index = contactsSession.indexOf(sessionContact);
            if (index >= 0) {
                sessionContact = contactsSession.get(index).setActualContact(contact, true);
            } else {
                System.out.println("XXX new session contact " + sessionContact);
                contactsSession.add(sessionContact);
            }
            return sessionContact;
        }
    }

    /**
     *
     * @param contact
     * @return
     */
    protected static WeChatContact toActualContact(WeChatContact contact) {
        if (contact == null) {
            return null;
        } else if (contact instanceof Contact) {
            return ((Contact) contact).getActualContact();
        } else {
            return contact;
        }
    }

    /**
     *
     * @param <T>
     * @param contacts
     * @return
     */
    protected static <T extends WeChatContact> List<T> toActualContacts(List<T> contacts) {
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i) instanceof Contact) {
                contacts.set(i, (T) ((Contact) contacts.get(i)).getActualContact());
            }
        }
        return contacts;
    }

    /**
     *
     * @param <T>
     * @param contacts
     * @return
     */
    protected static <T extends WeChatContact> T[] toActualContacts(T... contacts) {
        for (int i = 0; i < contacts.length; i++) {
            if (contacts[i] instanceof Contact) {
                contacts[i] = (T) ((Contact) contacts[i]).getActualContact();
            }
        }
        return contacts;
    }

    protected static class Contact extends WeChatContact {

        private WeChatContact actualContact;

        public Contact(WeChatContact actualContact) {
            this.actualContact = actualContact;
            setUin(actualContact.getUin());
            setSeq(actualContact.getSeq());
            setUserId(actualContact.getUserId());
        }

        public WeChatContact getActualContact() {
            return actualContact;
        }

        public Contact setActualContact(WeChatContact actualContact, boolean onlySession) {
            if (actualContact instanceof Contact) {
                setActualContact(((Contact) actualContact).actualContact, onlySession);
            } else {
                if (actualContact.getUin() != 0) {
                    setUin(actualContact.getUin());
                }
                if (actualContact.getSeq() != null) {
                    setSeq(actualContact.getSeq());
                }
                if (actualContact.getUserId() != null) {
                    setUserId(actualContact.getUserId());
                }
                if (!onlySession || this.actualContact == null) {
                    this.actualContact = actualContact;
                }
                this.actualContact.setUin(getUin());
                this.actualContact.setSeq(getSeq());
                this.actualContact.setUserId(getUserId());
            }
            return this;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (!(obj instanceof Contact)) {
                return actualContact.equals(obj);
            } else if (getUin() != 0 && getUin() == ((Contact) obj).getUin()) {
                return true;
            } else if (Objects.equals(getUserId(), ((Contact) obj).getUserId())) {
                return true;
            } else if (Objects.equals(getSeq(), ((Contact) obj).getSeq())) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return actualContact.toString();
        }

        @Override
        public int hashCode() {
            return actualContact.hashCode();
        }
    }

    private <T> void fireEvents(WeChatSingleEventHandler<T> eventHandler, T event) {
        try {
            if (eventHandler != null) {
                eventHandler.onWeChatEvent(event);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> void fireEvents(WeChatMultiEventHandler<T> eventHandler, T... events) {
        try {
            if (eventHandler != null) {
                eventHandler.onWeChatEvent(events);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param onContactSavedRemoved
     * @return
     */
    public WeChatSession setOnContactSavedRemoved(WeChatMultiEventHandler<WeChatContact> onContactSavedRemoved) {
        this.onContactSavedRemoved = onContactSavedRemoved;
        return this;
    }

    /**
     *
     * @param onContactActiveRemoved
     * @return
     */
    public WeChatSession setOnContactActiveRemoved(WeChatMultiEventHandler<WeChatContact> onContactActiveRemoved) {
        this.onContactActiveRemoved = onContactActiveRemoved;
        return this;
    }

    /**
     *
     * @param onContactUpdated
     * @return
     */
    public WeChatSession setOnContactUpdated(WeChatMultiEventHandler<WeChatContact> onContactUpdated) {
        this.onContactUpdated = onContactUpdated;
        return this;
    }

    /**
     *
     * @param onMessageReceived
     * @return
     */
    public WeChatSession setOnMessageReceived(WeChatMultiEventHandler<WeChatMessage> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
        return this;
    }

    /**
     *
     * @param onSessionError
     * @return
     */
    public WeChatSession setOnSessionError(WeChatMultiEventHandler<Exception> onSessionError) {
        this.onSessionError = onSessionError;
        return this;
    }

    /**
     *
     * @param onSessionQRCodeReceived
     * @return
     */
    public WeChatSession setOnSessionQRCodeReceived(WeChatSingleEventHandler<Image> onSessionQRCodeReceived) {
        this.onSessionQRCodeReceived = onSessionQRCodeReceived;
        return this;
    }

    /**
     *
     * @param onSessionConnect
     * @return
     */
    public WeChatSession setOnSessionConnect(WeChatSingleEventHandler<WeChatContact> onSessionConnect) {
        this.onSessionConnect = onSessionConnect;
        return this;
    }

    /**
     *
     * @param onSessionDisconnect
     * @return
     */
    public WeChatSession setOnSessionDisconnect(WeChatSingleEventHandler<WeChatContact> onSessionDisconnect) {
        this.onSessionDisconnect = onSessionDisconnect;
        return this;
    }

    /**
     *
     * @param onSessionUserActive
     * @return
     */
    public WeChatSession setOnSessionUserActive(WeChatSingleEventHandler<WeChatContact> onSessionUserActive) {
        this.onSessionUserActive = onSessionUserActive;
        return this;
    }

}
