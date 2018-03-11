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
import com.huhehu.weijin.wechat.contacts.WeChatContact;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import com.huhehu.weijin.wechat.session.WeChatSession;
import com.huhehu.weijin.wechat.session.WeChatSessionHandler;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/**
 *
 * @author henning
 */
public class ChatWindow extends Application implements WeChatSessionHandler {

    private ListView<WeChatContact> contactsView;
    private ListView<WeChatMessage> messageView;
    private TextField messageField;
    private WeChatSession session = new WeChatSession();
    private Stage qrCodeStage;
    private Stage mainStage;

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        session.setSessionHandler(this);

        messageField = new TextField();
        messageField.setOnAction(messageFieldActionHandler);

        messageView = new ListView(new MessageListModel(session));
        messageView.setCellFactory(messageCellFactory);
        messageView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        contactsView = new ListView(new ContactListModel(session));
        contactsView.setCellFactory(contactCellFactory);
        contactsView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        contactsView.getSelectionModel().selectedItemProperty().addListener(contactListSelectionHandler);

        BorderPane chatPane = new BorderPane();
        chatPane.setCenter(messageView);
        chatPane.setBottom(messageField);

        BorderPane rootPane = new BorderPane();
        rootPane.setLeft(contactsView);
        rootPane.setCenter(chatPane);

        mainStage.setOnCloseRequest(stageCloseHandler);
        mainStage.setTitle("WeiJin");
        mainStage.setScene(new Scene(rootPane, 800, 500));
        mainStage.show();

        qrCodeStage = new Stage();
        qrCodeStage.setOnCloseRequest(stageCloseHandler);
        qrCodeStage.initModality(Modality.APPLICATION_MODAL);
        qrCodeStage.setTitle("Please scan QR-Code to login");

        session.connect();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void onError(Exception e) {
    }

    @Override
    public void onQRCodeReceived(Image qrCode) {
        Platform.runLater(() -> {
            VBox rootPane = new VBox(new ImageView(qrCode));
            rootPane.setAlignment(Pos.CENTER);
            rootPane.setPadding(new Insets(0));

            qrCodeStage.setScene(new Scene(rootPane));
            qrCodeStage.show();
        });
    }

    @Override
    public void onConnect(WeChatContact user) {
        Platform.runLater(() -> {
            qrCodeStage.hide();
        });
    }

    @Override
    public void onDisconnect() {
    }

    @Override
    public void onChatSelected(WeChatContact contact) {
        Platform.runLater(() -> {
            if (contact != contactsView.getSelectionModel().getSelectedItem()) {
                contactsView.getSelectionModel().select(contact);
                contactsView.scrollTo(contact);
            }
        });
    }

    private final EventHandler<WindowEvent> stageCloseHandler = (event) -> {
        qrCodeStage.hide();
        mainStage.hide();
        session.disconnect();
    };

    private final EventHandler<ActionEvent> messageFieldActionHandler = (event) -> {
        WeChatMessage message = new WeChatMessage();
        message.setToUserName(contactsView.getSelectionModel().getSelectedItem());
        message.setFromUserName(session.getLoginUser());
        message.setContent(messageField.getText());

        try {
            session.sendMessage(message);
            messageField.setText("");
        } catch (Exception ignore) {
        }
    };

    private final ChangeListener<WeChatContact> contactListSelectionHandler = (observable, oldSelection, newSelection) -> {
        session.selectChat(newSelection);
        messageView.setItems(new MessageListModel(session));
    };

    private final Callback<ListView<WeChatContact>, ListCell<WeChatContact>> contactCellFactory = (list) -> {
        return new ListCell<WeChatContact>() {
            @Override
            public void updateItem(WeChatContact contact, boolean empty) {
                super.updateItem(contact, empty);
                if (contact != null) {
                    setText(contact.getNickName());
                    Image avatar = session.getMediaCache().getMedia(contact);
                    if (avatar != null) {
                        ImageView avatarView = new ImageView(avatar);
                        avatarView.setFitHeight(30.0d);
                        avatarView.setFitWidth(30.0d);
                        setGraphic(avatarView);
                    }
                }
            }
        };
    };

    private final Callback<ListView<WeChatMessage>, ListCell<WeChatMessage>> messageCellFactory = (list) -> {
        return new ListCell<WeChatMessage>() {
            @Override
            public void updateItem(WeChatMessage message, boolean empty) {
                super.updateItem(message, empty);
                if (message != null) {
                    setText(message.getContent());
                    Image media = session.getMediaCache().getMedia(message);
                    if (media != null) {
                        ImageView mediaView = new ImageView(media);
                        setGraphic(mediaView);
                    }
                }
            }
        };
    };
}
