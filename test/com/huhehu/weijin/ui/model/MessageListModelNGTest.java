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
import com.huhehu.weijin.wechat.session.event.WeChatMultiEventHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class MessageListModelNGTest {

    public MessageListModelNGTest() {
    }

    @Test
    public void test_constructor_withActive() {
        SessionImpl session = new SessionImpl();
        session.setUserActive(new WeChatContact("test1"));
        session.setMessages(new WeChatContact("test1"),
                new WeChatMessage("test1"),
                new WeChatMessage("test2"),
                new WeChatMessage("test3"));

        MessageListModel instance1 = new MessageListModelImpl(session);
        assertEquals(instance1.size(), 3);
        assertEquals(instance1.get(0).getContent(), "test1");
        assertEquals(instance1.get(1).getContent(), "test2");
        assertEquals(instance1.get(2).getContent(), "test3");
    }

    @Test
    public void test_constructor_withoutActive() {
        SessionImpl session = new SessionImpl();
        session.setUserActive(new WeChatContact("test2"));
        session.setMessages(new WeChatContact("test1"),
                new WeChatMessage("test1"),
                new WeChatMessage("test2"),
                new WeChatMessage("test3"));

        MessageListModel instance1 = new MessageListModelImpl(session);
        assertEquals(instance1.size(), 0);
    }

    @Test
    public void test_message_received_forActiveUser() {
        SessionImpl session = new SessionImpl();
        session.setUserActive(new WeChatContact("test1"));
        session.setMessages(new WeChatContact("test1"),
                new WeChatMessage("test1"),
                new WeChatMessage("test2"),
                new WeChatMessage("test3"));

        MessageListModel instance1 = new MessageListModelImpl(session);
        session.fireOnMessageReceived(new WeChatMessage("test4").setFromUser(session.getUserActive()));
        session.fireOnMessageReceived(new WeChatMessage("test5").setToUser(session.getUserActive()));
        session.fireOnMessageReceived(new WeChatMessage("test6").setFromUser(session.getUserActive()));

        assertEquals(instance1.size(), 6);
        assertEquals(instance1.get(0).getContent(), "test1");
        assertEquals(instance1.get(1).getContent(), "test2");
        assertEquals(instance1.get(2).getContent(), "test3");
        assertEquals(instance1.get(3).getContent(), "test4");
        assertEquals(instance1.get(4).getContent(), "test5");
        assertEquals(instance1.get(5).getContent(), "test6");
    }

    @Test
    public void test_message_received_noActiveUser() {
        SessionImpl session = new SessionImpl();
        session.setMessages(new WeChatContact("test1"),
                new WeChatMessage("test1"),
                new WeChatMessage("test2"),
                new WeChatMessage("test3"));

        MessageListModel instance1 = new MessageListModelImpl(session);
        session.fireOnMessageReceived(new WeChatMessage("test4"));
        session.fireOnMessageReceived(new WeChatMessage("test5"));
        session.fireOnMessageReceived(new WeChatMessage("test6"));

        assertEquals(instance1.size(), 0);
    }

    @Test
    public void test_message_received_notForActiveUser() {
        SessionImpl session = new SessionImpl();
        session.setUserActive(new WeChatContact("test1"));
        session.setMessages(new WeChatContact("test1"),
                new WeChatMessage("test1"),
                new WeChatMessage("test2"),
                new WeChatMessage("test3"));

        MessageListModel instance1 = new MessageListModelImpl(session);
        session.fireOnMessageReceived(new WeChatMessage("test4").setFromUser(new WeChatContact("test2")));
        session.fireOnMessageReceived(new WeChatMessage("test5").setToUser(new WeChatContact("test2")));
        session.fireOnMessageReceived(new WeChatMessage("test6").setFromUser(new WeChatContact("test2")));

        assertEquals(instance1.size(), 3);
        assertEquals(instance1.get(0).getContent(), "test1");
        assertEquals(instance1.get(1).getContent(), "test2");
        assertEquals(instance1.get(2).getContent(), "test3");
    }

    @Test
    public void test_message_updated_forActiveUser() {
        SessionImpl session = new SessionImpl();
        session.setUserActive(new WeChatContact("test1"));
        session.setMessages(new WeChatContact("test1"),
                new WeChatMessage("test1").setMessageId("test1"),
                new WeChatMessage("test2").setMessageId("test2"),
                new WeChatMessage("test3").setMessageId("test3"));

        MessageListModel instance1 = new MessageListModelImpl(session);
        session.fireOnMessageReceived(new WeChatMessage("test4").setMessageId("test1").setFromUser(session.getUserActive()));
        session.fireOnMessageReceived(new WeChatMessage("test5").setMessageId("test2").setToUser(session.getUserActive()));
        session.fireOnMessageReceived(new WeChatMessage("test6").setMessageId("test3").setFromUser(session.getUserActive()));

        assertEquals(instance1.size(), 3);
        assertEquals(instance1.get(0).getContent(), "test4");
        assertEquals(instance1.get(1).getContent(), "test5");
        assertEquals(instance1.get(2).getContent(), "test6");
    }

    @Test
    public void test_message_updated_notForActiveUser() {
        SessionImpl session = new SessionImpl();
        session.setUserActive(new WeChatContact("test1"));
        session.setMessages(new WeChatContact("test1"),
                new WeChatMessage("test1").setMessageId("test1"),
                new WeChatMessage("test2").setMessageId("test2"),
                new WeChatMessage("test3").setMessageId("test3"));

        MessageListModel instance1 = new MessageListModelImpl(session);
        session.fireOnMessageReceived(new WeChatMessage("test4").setMessageId("test1").setFromUser(new WeChatContact("test2")));
        session.fireOnMessageReceived(new WeChatMessage("test5").setMessageId("test2").setToUser(new WeChatContact("test2")));
        session.fireOnMessageReceived(new WeChatMessage("test6").setMessageId("test3").setFromUser(new WeChatContact("test2")));

        assertEquals(instance1.size(), 3);
        assertEquals(instance1.get(0).getContent(), "test1");
        assertEquals(instance1.get(1).getContent(), "test2");
        assertEquals(instance1.get(2).getContent(), "test3");
    }

    public static class SessionImpl extends WeChatSession {

        private Map<WeChatContact, List<WeChatMessage>> messages = new HashMap<>();
        private WeChatContact activeUser;
        private WeChatMultiEventHandler<WeChatMessage> onMessageReceived;

        public void setMessages(WeChatContact contact, WeChatMessage... messages) {
            this.messages.put(contact, Arrays.asList(messages));
        }

        @Override
        public synchronized List<WeChatMessage> getMessages(WeChatContact contact) {
            if (this.messages.containsKey(contact)) {
                return messages.get(contact);
            } else {
                return new ArrayList<>();
            }
        }

        public void setUserActive(WeChatContact contact) {
            activeUser = contact;
        }

        @Override
        public synchronized WeChatContact getUserActive() {
            return activeUser;
        }

        @Override
        public WeChatSession setOnMessageReceived(WeChatMultiEventHandler<WeChatMessage> onMessageReceived) {
            this.onMessageReceived = onMessageReceived;
            return this;
        }

        public void fireOnMessageReceived(WeChatMessage... messages) {
            onMessageReceived.onWeChatEvent(messages);
        }
    }

    public static class MessageListModelImpl extends MessageListModel {

        public MessageListModelImpl(WeChatSession session) {
            super(session);
        }

        @Override
        protected void doLater(Runnable later) {
            later.run();
        }
    }
}
