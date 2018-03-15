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
import com.huhehu.weijin.wechat.session.WeChatSession;
import javafx.application.Platform;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class ContactListModel extends ObservableListModel<WeChatContact> {

    private WeChatSession session;

    /**
     *
     * @param session
     */
    public ContactListModel(WeChatSession session) {
        super(session.getContactsActive());
        this.session = session;

        this.session.setOnContactUpdated((contacts) -> {
            Platform.runLater(() -> {
                beginChange();
                for (WeChatContact contact : contacts) {
                    int index = indexOf(contact);
                    if (index >= 0) {
                        if (session.isContactActive(contact)) {
                            set(index, contact);
                        } else {
                            remove(index);
                        }
                    } else if (session.isContactActive(contact)) {
                        add(contact);
                    }
                }
                endChange();
            });
        });

//        this.session.addOnContactRemoved((contacts) -> {
//            Platform.runLater(() -> {
//                beginChange();
//                System.out.println("remove " + contacts);
//                removeAll(contacts);
//                endChange();
//            });
//        });
    }
}
