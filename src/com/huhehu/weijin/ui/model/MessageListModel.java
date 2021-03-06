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
import com.huhehu.weijin.wechat.session.WeChatSession;
import javafx.application.Platform;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class MessageListModel extends ObservableListModel<WeChatMessage> {

    private WeChatContact contact;

    /**
     *
     * @param session
     */
    public MessageListModel(final WeChatSession session) {
        super(session.getMessages(session.getUserActive()));
        contact = session.getUserActive();

        session.setOnMessageReceived((messages) -> {
            doLater(() -> {
                beginChange();
                if (contact != null) {
                    for (WeChatMessage message : messages) {
                        if (contact.equals(message.getToUser()) || contact.equals(message.getFromUser())) {
                            int index = indexOf(message);
                            if (index >= 0) {
                                set(index, message);
                            } else {
                                add(message);
                            }
                        }
                    }
                }
                endChange();
            });
        });
    }
}
