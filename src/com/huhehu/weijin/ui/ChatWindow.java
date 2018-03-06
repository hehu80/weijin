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

public class ChatWindow extends JFrame implements WeChatSessionHandler, ListSelectionListener, ActionListener, WindowListener {
    private ContactList contactList;
    private ContactListModel contactListModel;
    private MessageList messageList;
    private MessageListModel messageListModel;
    private JTextField messageField;
    private JFrame qrCodeFrame;
    private WeChatSession session;

    public ChatWindow() throws WeChatException {
        session = new WeChatSession();

        BorderLayout layout = new BorderLayout();
        setLayout(layout);
        setTitle("WeiJin");

        addWindowListener(this);

        contactListModel = new ContactListModel(session);
        contactList = new ContactList(contactListModel);
        contactList.addListSelectionListener(this);
        getContentPane().add(new JScrollPane(contactList), BorderLayout.LINE_START);

        messageListModel = new MessageListModel(session);
        messageList = new MessageList(messageListModel);
        getContentPane().add(new JScrollPane(messageList), BorderLayout.CENTER);

        messageField = new JTextField();
        messageField.addActionListener(this);
        getContentPane().add(messageField, BorderLayout.PAGE_END);

        qrCodeFrame = new JFrame();

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
        JLabel qrCodeLabel = new JLabel();
        qrCodeLabel.setIcon(new ImageIcon(qrCode));
        qrCodeFrame.setTitle("Scan QR-Code to login");
        qrCodeFrame.getContentPane().add(qrCodeLabel);
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
    public void valueChanged(ListSelectionEvent event) {
        messageListModel.setContact(contactListModel.getElementAt(contactList.getSelectedIndex()));
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        WeChatMessage message = new WeChatMessage();
        message.setToUserName(messageListModel.getContact());
        message.setFromUserName(session.getUser());
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
        qrCodeFrame.setVisible(false);
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
