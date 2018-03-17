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

import static com.huhehu.weijin.ui.ChatWindow.MESSAGE_TIME_FORMAT;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import com.huhehu.weijin.wechat.session.WeChatSession;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class MessageViewCell extends ListCell<WeChatMessage> {

    private final BorderPane pane;
    private final BorderPane contentPane;
    private final Label contentLabel;
    private final Label contentTime;
    private final ImageView contentMedia;
    private final WeChatSession session;

    public MessageViewCell(WeChatSession session) {
        this.session = session;

        contentLabel = new Label();
        contentLabel.setStyle("-fx-text-fill:white;");

        contentTime = new Label();
        contentTime.setStyle("-fx-text-fill:lightgray;-fx-font-size: 9px;");

        contentMedia = new ImageView();

        contentPane = new BorderPane();
        contentPane.setCenter(contentLabel);
        contentPane.setBottom(contentTime);
        contentPane.setPadding(new Insets(5.0d));
        contentPane.setStyle("-fx-background-color:green;");

        pane = new BorderPane();
    }

    @Override
    public void updateItem(WeChatMessage message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setGraphic(null);
            setText(null);
            setContextMenu(null);
        } else {
            if (message.isReceived()) {
                contentLabel.setTextAlignment(TextAlignment.LEFT);
                pane.setRight(null);
                pane.setLeft(contentPane);
            } else {
                contentLabel.setTextAlignment(TextAlignment.RIGHT);
                pane.setLeft(null);
                pane.setRight(contentPane);
            }

            Image media = session.getMedia(message);
            if (media != null) {
                contentMedia.setImage(media);
                contentPane.setLeft(null);
                contentPane.setLeft(contentMedia);
            } else {
                contentPane.setLeft(null);
            }

            if (message.getTime() != null) {
                contentTime.setText(MESSAGE_TIME_FORMAT.format(message.getTime()));
                contentPane.setBottom(contentTime);
            } else {
                contentPane.setBottom(null);
            }
            contentLabel.setText(message.getContent());
            setGraphic(pane);
        }
    }
}
