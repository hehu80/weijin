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
import com.huhehu.weijin.wechat.session.WeChatContactHandler;
import com.huhehu.weijin.wechat.session.WeChatSession;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ContactListModel extends AbstractListModel<WeChatContact> implements WeChatContactHandler {
    private WeChatSession session;
    private List<WeChatContact> contacts;

    public ContactListModel(WeChatSession session) {
        this.session = session;
        this.session.setContactHandler(this);
        this.contacts = new ArrayList<>(session.getContacts());
    }
    
    public WeChatSession getSession(){
        return session;
    }

    @Override
    public int getSize() {
        return contacts.size();
    }

    @Override
    public WeChatContact getElementAt(int i) {
        return contacts.get(i);
    }

    @Override
    public void onContactUpdated(WeChatContact... contacts) {
        SwingUtilities.invokeLater(() -> {
            for (WeChatContact contact : contacts) {
                int index = this.contacts.indexOf(contact);
                if (index >= 0) {
                    this.contacts.set(index, contact);
                    fireContentsChanged(contact, index, index); 
                } else {
                    this.contacts.add(contact);
                    fireIntervalAdded(contact, this.contacts.size() - 1, this.contacts.size() - 1);
                }
            }
        });
    }
}
