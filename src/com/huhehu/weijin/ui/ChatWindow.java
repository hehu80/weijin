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
import com.huhehu.weijin.wechat.contacts.WeChatGroup;
import com.huhehu.weijin.wechat.contacts.WeChatUser;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import com.huhehu.weijin.wechat.session.WeChatSession;
import static com.huhehu.weijin.wechat.session.WeChatSession.loadSession;
import static com.huhehu.weijin.wechat.session.WeChatSession.saveSession;
import com.huhehu.weijin.wechat.session.event.WeChatSingleEventHandler;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import static com.huhehu.weijin.wechat.WeChatUtil.prettyJson;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class ChatWindow extends Application {

    public static final Image ICON_WECHAT = new Image("com/huhehu/weijin/wechat.png");
    public static final Image ICON_AVATAR = new Image("com/huhehu/weijin/avatar.png");
    public static final String SESSION_FILE = "session";
    public static final DateTimeFormatter MESSAGE_TIME_FORMAT = DateTimeFormatter.ofPattern("MM/dd HH:mm").withZone(ZoneOffset.systemDefault());

    private ListView<WeChatContact> contactsViewSaved;
    private ListView<WeChatContact> contactsViewActive;
    private Button contactsSavedSelectButton;
    private Button contactsActiveSelectButton;
    private BorderPane contactsPane;
    private ListView<WeChatMessage> messageView;
    private Label messageViewHeader;
    private TextField messageField;
    private BorderPane messagePane;
    private WeChatSession session;
    private Stage mainStage;
    private Stage qrCodeStage;
    private ImageView qrCodeView;

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     *
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {
        session = createSession();
        mainStage = primaryStage;

        createMessagePane();
        createContactsPane();

        BorderPane rootPane = new BorderPane();
        rootPane.setLeft(contactsPane);
        rootPane.setCenter(messagePane); 
        
        mainStage.setOnCloseRequest((event) -> doClose());
        mainStage.setTitle("WeiJin");
        mainStage.getIcons().add(ICON_WECHAT);
        mainStage.setScene(new Scene(rootPane, 800, 500));
        mainStage.show();

        qrCodeView = new ImageView();
        qrCodeStage = createStage("Please scan QR-Code to login", new BorderPane(qrCodeView));
        qrCodeStage.setOnCloseRequest((event) -> doClose());

        doUserActive(session.getUserActive());

        session.setOnSessionQRCodeReceived(onSessionQRCodeReceived);
        session.setOnSessionConnect(onSesssionConnect);
        session.setOnSessionUserActive(onSessionUserActive);
        session.connect();
    }

    private void createMessagePane() {
        messageViewHeader = new Label("");
        messageViewHeader.setStyle("-fx-background-color:lightgray;-fx-text-fill:black;");
        messageViewHeader.setPadding(new Insets(5.0d));
        messageViewHeader.setAlignment(Pos.CENTER);
        messageViewHeader.setPrefWidth(Double.MAX_VALUE);

        messageField = new TextField();
        messageField.setOnAction((event) -> doSendMessage());

        messageView = new ListView(new MessageListModel(session));
        messageView.setCellFactory((list) -> new MessageViewCell(session));
        messageView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        messageView.setContextMenu(new ContextMenu(
                createMenuItem("Show JSON", (event) -> doShowMessageJSON(messageView.getSelectionModel().getSelectedItem()))
        ));

        messagePane = new BorderPane();
        messagePane.setTop(messageViewHeader);
        messagePane.setCenter(messageView);
        messagePane.setBottom(messageField);
    }

    private void createContactsPane() {
        contactsViewSaved = new ListView(new ContactListModel(false, true, session));
        contactsViewSaved.setCellFactory((list) -> new ContactViewCell(session));
        contactsViewSaved.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        contactsViewSaved.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> doUserActive(newSelection));
        contactsViewSaved.setContextMenu(new ContextMenu(
                createMenuItem("Contact Details", (event) -> doShowContactDetails(contactsViewSaved.getSelectionModel().getSelectedItem())),
                createMenuItem("Show JSON", (event) -> doShowContactJSON(contactsViewSaved.getSelectionModel().getSelectedItem()))
        ));

        contactsViewActive = new ListView(new ContactListModel(true, false, session));
        contactsViewActive.setCellFactory((list) -> new ContactViewCell(session));
        contactsViewActive.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        contactsViewActive.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> doUserActive(newSelection));
        contactsViewActive.setContextMenu(new ContextMenu(
                createMenuItem("Contact Details", (event) -> doShowContactDetails(contactsViewActive.getSelectionModel().getSelectedItem())),
                createMenuItem("Show JSON", (event) -> doShowContactJSON(contactsViewActive.getSelectionModel().getSelectedItem()))
        ));

        contactsSavedSelectButton = new Button("Saved");
        contactsSavedSelectButton.setOnAction((event) -> contactsPane.setCenter(contactsViewSaved));
        contactsActiveSelectButton = new Button("Active");
        contactsActiveSelectButton.setOnAction((event) -> contactsPane.setCenter(contactsViewActive));

        HBox contactsSelectionButtons = new HBox(contactsSavedSelectButton, contactsActiveSelectButton);
        contactsSelectionButtons.setPadding(new Insets(5.0d));
        contactsSelectionButtons.setSpacing(10.0d);
        contactsSelectionButtons.setStyle("-fx-background-color:gray;");

        contactsPane = new BorderPane();
        contactsPane.setTop(contactsSelectionButtons);
        contactsPane.setCenter(contactsViewActive);
    }

    private WeChatSession createSession() {
        try {
            return loadSession(Paths.get(SESSION_FILE));
        } catch (IOException | ClassNotFoundException ignore) {
            return new WeChatSession();
        }
    }

    private MenuItem createMenuItem(String text, EventHandler<ActionEvent> eventHandler) {
        MenuItem menuItem = new MenuItem(text);
        if (eventHandler != null) {
            menuItem.setOnAction(eventHandler);
        }
        return menuItem;
    }

    private Stage createStage(String title, Parent node) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);
        stage.getIcons().add(ICON_WECHAT);
        stage.setScene(new Scene(node));
        return stage;
    }

    private final WeChatSingleEventHandler<Image> onSessionQRCodeReceived = (qrCode) -> {
        doLater(() -> {
            qrCodeView.setImage(qrCode);
            qrCodeStage.show();
        });
    };

    private final WeChatSingleEventHandler<WeChatContact> onSesssionConnect = (user) -> {
        doLater(() -> {
            qrCodeStage.hide();
        });
    };

    private final WeChatSingleEventHandler<WeChatContact> onSessionUserActive = (user) -> {
        doLater(() -> {
            doUserActive(user);
        });
    };

    private void doClose() {
        qrCodeStage.hide();
        mainStage.hide();
        session.disconnect();

        try {
            saveSession(session, Paths.get(SESSION_FILE));
        } catch (IOException ignore) {
        }
    }

    private void doSendMessage() {
        try {
            session.sendMessage(new WeChatMessage().setContent(messageField.getText()));
            messageField.setText("");
        } catch (WeChatException ignore) {
        }
    }

    private void doUserActive(WeChatContact contact) {
        if (contact != null) {
            session.setUserActive(contact);
            messageView.setItems(new MessageListModel(session));
            messageViewHeader.setText(contact.getNickName());
            if (!contact.equals(contactsViewSaved.getSelectionModel().getSelectedItem())) {
                contactsViewSaved.getSelectionModel().select(contact);
                contactsViewSaved.scrollTo(contact);
            }
            if (!contact.equals(contactsViewActive.getSelectionModel().getSelectedItem())) {
                contactsViewActive.getSelectionModel().select(contact);
                contactsViewActive.scrollTo(contact);
            }
        }
    }

    private void doShowMessageJSON(WeChatMessage message) {
        if (message != null) {
            String json = message.getContent() == null
                    ? "not available" : prettyJson(message.getContent());
            createStage("JSON " + message.getContent(),
                    new TextArea(json)).show();
        }
    }

    private void doShowContactJSON(WeChatContact contact) {
        if (contact != null) {
            String json = contact.getJson() == null
                    ? "not available" : prettyJson(contact.getJson());
            createStage("JSON " + contact.getNickName(),
                    new TextArea(json)).show();
        }
    }

    private void doShowContactDetails(WeChatContact contact) {
        if (contact != null) {
            Image avatar = session.getMedia(contact);

            GridPane rootPane = new GridPane();
            rootPane.setPadding(new Insets(10.0d));
            rootPane.setHgap(10);
            rootPane.setVgap(10);
            if (contact instanceof WeChatGroup) {
                WeChatGroup group = (WeChatGroup) contact;
                rootPane.add(new ImageView(avatar == null ? ICON_AVATAR : avatar), 0, 0, 1, 3);
                rootPane.add(new Label("Nickname:"), 1, 0);
                rootPane.add(new Label(contact.getNickName()), 2, 0);
                rootPane.add(new Label("Alias:"), 1, 1);
                rootPane.add(new Label(group.getRemarkName()), 2, 1);
                rootPane.add(new Label("Description:"), 1, 2);
                rootPane.add(new Label(group.getSignature()), 2, 2);
            } else {
                WeChatUser user = (WeChatUser) contact;
                rootPane.add(new ImageView(avatar == null ? ICON_AVATAR : avatar), 0, 0, 1, 6);
                rootPane.add(new Label("Nickname:"), 1, 0);
                rootPane.add(new Label(user.getNickName()), 2, 0);
                rootPane.add(new Label("Alias:"), 1, 1);
                rootPane.add(new Label(user.getRemarkName()), 2, 1);
                rootPane.add(new Label("Sex:"), 1, 2);
                rootPane.add(new Label(user.getSex() == 1 ? "male" : user.getSex() == 2 ? "female" : ""), 2, 2);
                rootPane.add(new Label("City:"), 1, 3);
                rootPane.add(new Label(user.getCity()), 2, 3);
                rootPane.add(new Label("Province:"), 1, 4);
                rootPane.add(new Label(user.getProvince()), 2, 4);
                rootPane.add(new Label("Description:"), 1, 5);
                rootPane.add(new Label(user.getSignature()), 2, 5);
            }

            createStage("Details of " + contact.getNickName(), rootPane).show();
        }
    }
    
    protected void doLater(Runnable later){
        Platform.runLater(later);
    }
}
