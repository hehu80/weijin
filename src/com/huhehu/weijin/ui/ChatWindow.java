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
import com.huhehu.weijin.wechat.WeChatException;
import com.huhehu.weijin.wechat.contacts.WeChatContact;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import com.huhehu.weijin.wechat.session.WeChatSession;
import com.huhehu.weijin.wechat.session.WeChatSessionHandler;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ChatWindow extends JFrame implements WeChatSessionHandler, ListSelectionListener, ActionListener, WindowListener {

    private ContactList contactList;
    private MessageList messageList;
    private JTextField messageField;
    private JFrame qrCodeFrame;
    private JLabel qrCodeLabel;
    private WeChatSession session;

    public ChatWindow() throws WeChatException {
        session = new WeChatSession();

        BorderLayout layout = new BorderLayout();
        setLayout(layout);
        setTitle("WeiJin");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        addWindowListener(this);

        contactList = new ContactList(new ContactListModel(session));
        contactList.addListSelectionListener(this);
        getContentPane().add(new JScrollPane(contactList), BorderLayout.LINE_START);

        messageList = new MessageList(new MessageListModel(session));
        getContentPane().add(new JScrollPane(messageList), BorderLayout.CENTER);

        messageField = new JTextField();
        messageField.addActionListener(this);
        getContentPane().add(messageField, BorderLayout.PAGE_END);

        qrCodeFrame = new JFrame();
        qrCodeLabel = new JLabel();
        qrCodeFrame.getContentPane().add(qrCodeLabel);
        qrCodeFrame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        qrCodeFrame.setTitle("Scan QR-Code to login");

        session.setSessionHandler(this);
        session.connect(); // TODO not in constructor
    }

    public static void main(String[] args) throws WeChatException {
        ChatWindow chatWindow = new ChatWindow();
        chatWindow.setSize(800, 500);
        chatWindow.setVisible(true);
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onQRCodeReceived(Image qrCode) {
        qrCodeLabel.setIcon(new ImageIcon(qrCode));
        qrCodeFrame.setSize(qrCodeLabel.getMinimumSize().width, qrCodeLabel.getMinimumSize().height);
        qrCodeFrame.setVisible(true);
    }

    @Override
    public void onConnect(WeChatContact user) {
        qrCodeFrame.setVisible(false);
    }

    @Override
    public void onDisconnect() {
    }

    @Override
    public void onChatSelected(WeChatContact contact) {
        SwingUtilities.invokeLater(() -> contactList.setSelectedValue(contact, true));
    }

    @Override
    public void valueChanged(ListSelectionEvent event) {
        try {
            session.selectChat(contactList.getSelectedValue());
            messageList.setModel(new MessageListModel(session));
        } catch (WeChatException ignore) {
        }
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        WeChatMessage message = new WeChatMessage();
        message.setToUserName(contactList.getSelectedValue());
        message.setFromUserName(session.getLoginUser());
        message.setContent(messageField.getText());

        try {
            session.sendMessage(message);
            messageField.setText("");
        } catch (Exception ignore) {
        }
    }

    @Override
    public void windowOpened(WindowEvent windowEvent) {
    }

    @Override
    public void windowClosing(WindowEvent windowEvent) {
        try {
            session.disconnect();
        } catch (Exception ignore) {
        }
        qrCodeFrame.dispose();
    }

    @Override
    public void windowClosed(WindowEvent windowEvent) {
    }

    @Override
    public void windowIconified(WindowEvent windowEvent) {
    }

    @Override
    public void windowDeiconified(WindowEvent windowEvent) {
    }

    @Override
    public void windowActivated(WindowEvent windowEvent) {
    }

    @Override
    public void windowDeactivated(WindowEvent windowEvent) {
    }
}
