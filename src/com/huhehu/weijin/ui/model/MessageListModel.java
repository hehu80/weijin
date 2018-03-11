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
 * @author henning
 */
public class MessageListModel extends ObservableListModel<WeChatMessage> {

    private WeChatSession session;
    private WeChatContact contact;

    public MessageListModel(WeChatSession session) {
        super(session.getMessages(session.getSelectedChat()));
        this.session = session;
        this.contact = session.getSelectedChat();

        this.session.setOnMessageReceived((messages) -> {
            Platform.runLater(() -> {
                beginChange();
                if (contact != null) {
                    for (WeChatMessage message : messages) {
                        if (contact.equals(message.getToUserName()) || contact.equals(message.getFromUserName())) {
                            add(message);
                        }
                    }
                }
                endChange();
            });
        });

        this.session.setOnMessageUpdated((messages) -> {
            Platform.runLater(() -> {
                for (WeChatMessage message : messages) {
                    int index = indexOf(message);

                    if (index >= 0) {
                        beginChange();
                        set(index, message);
                        endChange();
                    }
                }
            });
        });
    }
}
