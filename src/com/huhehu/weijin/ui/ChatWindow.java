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

package com.huhehu.weijin.ui;

import com.huhehu.weijin.ui.model.ContactListModel;
import com.huhehu.weijin.ui.model.MessageListModel;
import com.huhehu.weijin.wechat.WeChatNotConnectedException;
import com.huhehu.weijin.wechat.WeChatSession;
import com.huhehu.weijin.wechat.WeChatUtil;
import com.huhehu.weijin.wechat.contacts.WeChatContact;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ChatWindow extends JFrame {
    private ContactList contactList;
    private ContactListModel contactListModel;
    private MessageList messageList;
    private MessageListModel messageListModel;
    private WeChatSession session;

    public ChatWindow(WeChatSession session) {
        this.session = session;

        GridLayout layout = new GridLayout();
        setLayout(layout);

        contactListModel = new ContactListModel(session);
        contactList = new ContactList(contactListModel);
        getContentPane().add(new JScrollPane(contactList));

        messageListModel = new MessageListModel(session);
        messageList = new MessageList(messageListModel);
        getContentPane().add(new JScrollPane(messageList));

        contactList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent event) {
                WeChatContact contact = contactList.getModel().getElementAt(event.getLastIndex());
                System.out.println("Select User " + contact.getNickName());
                ((MessageListModel) messageList.getModel()).setContact(contact);
            }
        });

        Timer updateTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                try {
                    System.out.println("update ...");
                    session.update();
                    saveSession(session);
                } catch (IOException ignore) {
                }
            }
        });
        updateTimer.setRepeats(true);
        updateTimer.start();
    }

    public static void main(String[] args) {
        WeChatSession session = loadSession();

        try {
            session.loadContacts(true);
            saveSession(session);
        } catch (WeChatNotConnectedException e) {
            try {
                System.out.println("not connected, download another QR-Code ...");
                String qrCode = "qrcode" + System.currentTimeMillis() + ".jpeg";
                WeChatUtil.downloadImageFromUrl(session.loadQRCode(), qrCode);
                session.connect();
                Files.delete(Paths.get(qrCode));
                saveSession(session);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ChatWindow chatWindow = new ChatWindow(session);
        chatWindow.setSize(800, 500);
        chatWindow.setVisible(true);
    }

    public static void saveSession(WeChatSession session) throws IOException {
        try (FileOutputStream connectionFile = new FileOutputStream("session")) {
            try (ObjectOutputStream output = new ObjectOutputStream(connectionFile)) {
                output.writeObject(session);
            }
        }
    }

    public static WeChatSession loadSession() {
        try (FileInputStream connectionFile = new FileInputStream("session")) {
            try (ObjectInputStream input = new ObjectInputStream(connectionFile)) {
                return (WeChatSession) input.readObject();
            }
        } catch (Exception e) {
            return new WeChatSession();
        }
    }
}
