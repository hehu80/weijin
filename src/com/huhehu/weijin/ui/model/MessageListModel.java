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

import com.huhehu.weijin.wechat.contacts.WeChatContact;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import com.huhehu.weijin.wechat.session.WeChatMessageHandler;
import com.huhehu.weijin.wechat.session.WeChatSession;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class MessageListModel extends AbstractListModel<WeChatMessage> implements WeChatMessageHandler {

    private WeChatSession session;
    private WeChatContact contact;
    private List<WeChatMessage> messages;

    public MessageListModel(WeChatSession session) {
        this.session = session;
        this.contact = session.getSelectedChat();
        this.session.setMessageHandler(this);
        this.messages = new ArrayList<>(session.getMessages(contact));
    }

    public WeChatSession getSession() {
        return session;
    }

    @Override
    public int getSize() {
        return messages.size();
    }

    @Override
    public WeChatMessage getElementAt(int index) {
        return messages.get(index);
    }

    @Override
    public void onMessageReceived(WeChatMessage... messages) {
        SwingUtilities.invokeLater(() -> {
            int count = 0;
            int size = 0;

            if (contact != null) {
                for (WeChatMessage message : messages) {
                    if (contact.equals(message.getToUserName()) || contact.equals(message.getFromUserName())) {
                        this.messages.add(message);
                        count++;
                    }
                }
            }

            if (count > 0) {
                size = this.messages.size();
                fireIntervalAdded(this.messages, size - count, size);
            }
        });
    }

    @Override
    public void onMessageUpdated(WeChatMessage... messages) {
        SwingUtilities.invokeLater(() -> {
            for (WeChatMessage message : messages) {
                int index = this.messages.indexOf(message);

                if (index >= 0) {
                    this.messages.set(index, message);
                    fireContentsChanged(message, index, index);
                }
            }
        });
    }
}
