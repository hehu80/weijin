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
import static com.huhehu.weijin.wechat.session.WeChatSession.loadSession;
import static com.huhehu.weijin.wechat.session.WeChatSession.saveSession;
import com.huhehu.weijin.wechat.session.event.WeChatSingleEventHandler;
import java.io.IOException;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/**
 *
 * @author henning
 */
public class ChatWindow extends Application {

    private ListView<WeChatContact> contactsView;
    private ListView<WeChatMessage> messageView;
    private TextField messageField;
    private WeChatSession session;
    private Stage qrCodeStage;
    private Stage mainStage;
    public static final Image ICON_WECHAT = new Image("file:wechat.png"); // TODO resource
    public static final Image ICON_AVATAR = new Image("file:avatar.png"); // TODO resource
    public static final String SESSION_FILE = "session";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            session = loadSession(Paths.get(SESSION_FILE));
        } catch (IOException | ClassNotFoundException ignore) {
            session = new WeChatSession();
        }

        mainStage = primaryStage;

        messageField = new TextField();
        messageField.setOnAction(messageFieldActionHandler);

        messageView = new ListView(new MessageListModel(session));
        messageView.setCellFactory(messageCellFactory);
        messageView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        contactsView = new ListView(new ContactListModel(session));
        contactsView.setCellFactory(contactCellFactory);
        contactsView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        contactsView.getSelectionModel().selectedItemProperty().addListener(contactListSelectionHandler);
        
        if(session.getSelectedChat() != null){
            contactsView.getSelectionModel().select(session.getSelectedChat());
            contactsView.scrollTo(session.getSelectedChat());
        }

        BorderPane chatPane = new BorderPane();
        chatPane.setCenter(messageView);
        chatPane.setBottom(messageField);

        BorderPane rootPane = new BorderPane();
        rootPane.setLeft(contactsView);
        rootPane.setCenter(chatPane);

        mainStage.setOnCloseRequest(stageCloseHandler);
        mainStage.setTitle("WeiJin");
        mainStage.getIcons().add(ICON_WECHAT);
        mainStage.setScene(new Scene(rootPane, 800, 500));
        mainStage.show();

        qrCodeStage = new Stage();
        qrCodeStage.setOnCloseRequest(stageCloseHandler);
        qrCodeStage.initModality(Modality.APPLICATION_MODAL);
        qrCodeStage.setTitle("Please scan QR-Code to login");
        qrCodeStage.getIcons().add(ICON_WECHAT);

        session.setOnSessionQRCodeReceived(onQRCodeReceivedHandler);
        session.setOnSessionConnect(onSesssionConnectHandler);
        session.setOnSessionChatSelected(onSessionChatSelectedHandler);
        session.connect();
    }

    private final WeChatSingleEventHandler<Image> onQRCodeReceivedHandler = (qrCode) -> {
        Platform.runLater(() -> {
            VBox rootPane = new VBox(new ImageView(qrCode));
            rootPane.setAlignment(Pos.CENTER);
            rootPane.setPadding(new Insets(0));

            qrCodeStage.setScene(new Scene(rootPane));
            qrCodeStage.show();
        });
    };

    private final WeChatSingleEventHandler<WeChatContact> onSesssionConnectHandler = (user) -> {
        Platform.runLater(() -> {
            qrCodeStage.hide();
        });
    };

    private final WeChatSingleEventHandler<WeChatContact> onSessionChatSelectedHandler = (user) -> {
        Platform.runLater(() -> {
            if (user != contactsView.getSelectionModel().getSelectedItem()) {
                contactsView.getSelectionModel().select(user);
                contactsView.scrollTo(user);
            }
        });
    };

    private final EventHandler<WindowEvent> stageCloseHandler = (event) -> {
        qrCodeStage.hide();
        mainStage.hide();
        session.disconnect();
        
        try {
            saveSession(session, Paths.get(SESSION_FILE));
        } catch (IOException ignore) {
        }
    };

    private final EventHandler<ActionEvent> messageFieldActionHandler = (event) -> {
        WeChatMessage message = new WeChatMessage();
        message.setToUserName(contactsView.getSelectionModel().getSelectedItem());
        message.setFromUserName(session.getLoginUser());
        message.setContent(messageField.getText());

        try {
            session.sendMessage(message);
            messageField.setText("");
        } catch (WeChatException ignore) {
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
                    Image avatar = session.getMedia(contact);
                    if (avatar == null) {
                        avatar = ICON_AVATAR;
                    }
                    ImageView avatarView = new ImageView(avatar);
                    avatarView.setFitHeight(30.0d);
                    avatarView.setFitWidth(30.0d);
                    setGraphic(avatarView);
                }
            }
        };
    };

    private final Callback<ListView<WeChatMessage>, ListCell<WeChatMessage>> messageCellFactory = (list) -> {
        return new ListCell<WeChatMessage>() {
            private BorderPane pane;
            private BorderPane contentPane;
            private Label contentLabel;
            private ImageView contentMedia;

            @Override
            public void updateItem(WeChatMessage message, boolean empty) {
                super.updateItem(message, empty);

                if (empty) {
                    setGraphic(null);
                } else {
                    if (pane == null) {
                        contentLabel = new Label();
                        contentLabel.setStyle("-fx-text-fill:white;");

                        contentMedia = new ImageView();

                        contentPane = new BorderPane();
                        contentPane.setCenter(contentLabel);
                        contentPane.setPadding(new Insets(10.0d));
                        contentPane.setStyle("-fx-background-color:green;");

                        pane = new BorderPane();
                    }

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

                    contentLabel.setText(message.getContent());
                    setGraphic(pane);
                }
            }
        };
    };
}
