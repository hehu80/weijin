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

import static com.huhehu.weijin.ui.ChatWindow.ICON_AVATAR;
import com.huhehu.weijin.wechat.contacts.WeChatContact;
import com.huhehu.weijin.wechat.session.WeChatSession;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class ContactViewCell extends ListCell<WeChatContact> {

    private WeChatSession session;

    public ContactViewCell(WeChatSession session) {
        this.session = session;
    }

    @Override
    public void updateItem(WeChatContact contact, boolean empty) {
        super.updateItem(contact, empty);
        if (empty || contact == null) {
            setGraphic(null);
            setText(null);
            setContextMenu(null);
        } else {
            Image avatar = session.getMedia(contact);
            ImageView avatarView = new ImageView(avatar == null ? ICON_AVATAR : avatar);
            avatarView.setFitHeight(30.0d);
            avatarView.setFitWidth(30.0d);

            setGraphic(avatarView);
            setText(contact.getNickName());
        }
    }
}
