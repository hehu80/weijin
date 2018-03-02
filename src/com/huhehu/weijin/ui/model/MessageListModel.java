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

package com.huhehu.weijin.ui.model;

import com.huhehu.weijin.wechat.WeChatSession;
import com.huhehu.weijin.wechat.contacts.WeChatContact;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;

import javax.swing.*;

public class MessageListModel extends AbstractListModel<WeChatMessage> implements WeChatSession.MessageListener {
    private WeChatSession session;
    private WeChatContact contact;

    public MessageListModel(WeChatSession session) {
        this.session = session;
        session.addMessageListener(this);
    }

    public void setContact(WeChatContact contact) {
        if (this.contact != contact) {
            if (this.contact != null && getSize() > 0)
                this.fireIntervalRemoved(contact, 0, getSize() - 1);
            this.contact = contact;
            if (this.contact != null && getSize() > 0)
                this.fireIntervalAdded(contact, 0, getSize() - 1);
        }
    }

    @Override
    public int getSize() {
        return contact == null ? 0 : session.getChat(contact).size();
    }

    @Override
    public WeChatMessage getElementAt(int index) {
        return contact == null ? null : session.getChat(contact).get(index);
    }

    @Override
    public void onMessageReceived(WeChatContact contact, WeChatMessage message) {
        System.out.println("Message received");
        if (contact.equals(this.contact)) {
            fireIntervalAdded(message, session.getChat(contact).size() - 1, session.getChat(contact).size() - 1);
        }
    }
}
