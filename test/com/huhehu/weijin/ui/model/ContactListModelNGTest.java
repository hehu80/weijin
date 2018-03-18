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
import com.huhehu.weijin.wechat.session.event.WeChatMultiEventHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class ContactListModelNGTest {

    public ContactListModelNGTest() {
    }

    @Test
    public void test_constructor_withActive() {
        SessionImpl session = new SessionImpl();
        session.setContactsSaved(
                new WeChatContact("test1"),
                new WeChatContact("test2"),
                new WeChatContact("test3"));
        session.setContactsActive(
                new WeChatContact("test4"),
                new WeChatContact("test5"),
                new WeChatContact("test6"));

        ContactListModel instance1 = new ContactListModelImpl(true, false, session);
        assertEquals(instance1.size(), 3);
        assertEquals(instance1.get(0).getUserId(), "test4");
        assertEquals(instance1.get(1).getUserId(), "test5");
        assertEquals(instance1.get(2).getUserId(), "test6");
    }

    @Test
    public void test_constructor_withSaved() {
        SessionImpl session = new SessionImpl();
        session.setContactsSaved(
                new WeChatContact("test1"),
                new WeChatContact("test2"),
                new WeChatContact("test3"));
        session.setContactsActive(
                new WeChatContact("test4"),
                new WeChatContact("test5"),
                new WeChatContact("test6"));

        ContactListModel instance1 = new ContactListModelImpl(false, true, session);
        assertEquals(instance1.size(), 3);
        assertEquals(instance1.get(0).getUserId(), "test1");
        assertEquals(instance1.get(1).getUserId(), "test2");
        assertEquals(instance1.get(2).getUserId(), "test3");
    }

    @Test
    public void test_constructor_withSavedAndActive() {
        SessionImpl session = new SessionImpl();
        session.setContactsSaved(
                new WeChatContact("test1"),
                new WeChatContact("test2"),
                new WeChatContact("test3"));
        session.setContactsActive(
                new WeChatContact("test4"),
                new WeChatContact("test5"),
                new WeChatContact("test6"));

        ContactListModel instance1 = new ContactListModelImpl(true, true, session);
        assertEquals(instance1.size(), 6);
        assertTrue(instance1.contains(new WeChatContact("test1")));
        assertTrue(instance1.contains(new WeChatContact("test2")));
        assertTrue(instance1.contains(new WeChatContact("test3")));
        assertTrue(instance1.contains(new WeChatContact("test4")));
        assertTrue(instance1.contains(new WeChatContact("test5")));
        assertTrue(instance1.contains(new WeChatContact("test6")));
    }

    @Test
    public void test_saved_contact_update_withSaved() {
        SessionImpl session = new SessionImpl();
        session.setContactsSaved(
                new WeChatContact("test1"),
                new WeChatContact("test2"),
                new WeChatContact("test3"));

        ContactListModel instance1 = new ContactListModelImpl(false, true, session);
        session.fireOnContactUpdated(new WeChatContact("test1").setNickName("test4"));
        session.fireOnContactUpdated(new WeChatContact("test2").setNickName("test5"));
        session.fireOnContactUpdated(new WeChatContact("test3").setNickName("test6"));

        assertEquals(instance1.size(), 3);
        assertEquals(instance1.get(0).getNickName(), "test4");
        assertEquals(instance1.get(1).getNickName(), "test5");
        assertEquals(instance1.get(2).getNickName(), "test6");
    }

    @Test
    public void test_active_contact_update_withSaved() {
        SessionImpl session = new SessionImpl();
        session.setContactsSaved(
                new WeChatContact("test1").setNickName("test1"),
                new WeChatContact("test2").setNickName("test2"),
                new WeChatContact("test3").setNickName("test3"));
        session.setContactsActive(
                new WeChatContact("test4").setNickName("test4"),
                new WeChatContact("test5").setNickName("test5"),
                new WeChatContact("test6").setNickName("test6"));

        ContactListModel instance1 = new ContactListModelImpl(false, true, session);
        session.fireOnContactUpdated(new WeChatContact("test4").setNickName("test8"));
        session.fireOnContactUpdated(new WeChatContact("test5").setNickName("test9"));
        session.fireOnContactUpdated(new WeChatContact("test6").setNickName("test10"));

        assertEquals(instance1.size(), 3);
        assertEquals(instance1.get(0).getNickName(), "test1");
        assertEquals(instance1.get(1).getNickName(), "test2");
        assertEquals(instance1.get(2).getNickName(), "test3");
    }

    @Test
    public void test_saved_contact_update_withActive() {
        SessionImpl session = new SessionImpl();
        session.setContactsActive(
                new WeChatContact("test1").setNickName("test1"),
                new WeChatContact("test2").setNickName("test2"),
                new WeChatContact("test3").setNickName("test3"));
        session.setContactsSaved(
                new WeChatContact("test4").setNickName("test4"),
                new WeChatContact("test5").setNickName("test5"),
                new WeChatContact("test6").setNickName("test6"));

        ContactListModel instance1 = new ContactListModelImpl(true, false, session);
        session.fireOnContactUpdated(new WeChatContact("test4").setNickName("test8"));
        session.fireOnContactUpdated(new WeChatContact("test5").setNickName("test9"));
        session.fireOnContactUpdated(new WeChatContact("test6").setNickName("test10"));

        assertEquals(instance1.size(), 3);
        assertEquals(instance1.get(0).getNickName(), "test1");
        assertEquals(instance1.get(1).getNickName(), "test2");
        assertEquals(instance1.get(2).getNickName(), "test3");
    }

    @Test
    public void test_active_contact_update_withActive() {
        SessionImpl session = new SessionImpl();
        session.setContactsActive(
                new WeChatContact("test1"),
                new WeChatContact("test2"),
                new WeChatContact("test3"));

        ContactListModel instance1 = new ContactListModelImpl(true, false, session);
        session.fireOnContactUpdated(new WeChatContact("test1").setNickName("test4"));
        session.fireOnContactUpdated(new WeChatContact("test2").setNickName("test5"));
        session.fireOnContactUpdated(new WeChatContact("test3").setNickName("test6"));

        assertEquals(instance1.size(), 3);
        assertEquals(instance1.get(0).getNickName(), "test4");
        assertEquals(instance1.get(1).getNickName(), "test5");
        assertEquals(instance1.get(2).getNickName(), "test6");
    }

    @Test
    public void test_saved_contact_added_withSaved() {
        SessionImpl session = new SessionImpl();

        ContactListModel instance1 = new ContactListModelImpl(false, true, session);
        session.setContactsSaved(new WeChatContact("test1"));
        session.fireOnContactUpdated(new WeChatContact("test1"));
        assertEquals(instance1.size(), 1);
        assertEquals(instance1.get(0).getUserId(), "test1");
    }

    @Test
    public void test_active_contact_added_withSaved() {
        SessionImpl session = new SessionImpl();

        ContactListModel instance1 = new ContactListModelImpl(false, true, session);
        session.setContactsActive(new WeChatContact("test1"));
        session.fireOnContactUpdated(new WeChatContact("test1"));
        assertEquals(instance1.size(), 0);
    }

    @Test
    public void test_saved_contact_added_withActive() {
        SessionImpl session = new SessionImpl();

        ContactListModel instance1 = new ContactListModelImpl(true, false, session);
        session.setContactsSaved(new WeChatContact("test1"));
        session.fireOnContactUpdated(new WeChatContact("test1"));
        assertEquals(instance1.size(), 0);
    }

    @Test
    public void test_active_contact_added_withActive() {
        SessionImpl session = new SessionImpl();

        ContactListModel instance1 = new ContactListModelImpl(true, false, session);
        session.setContactsActive(new WeChatContact("test1"));
        session.fireOnContactUpdated(new WeChatContact("test1"));
        assertEquals(instance1.size(), 1);
        assertEquals(instance1.get(0).getUserId(), "test1");
    }

    public static class SessionImpl extends WeChatSession {

        private List<WeChatContact> contactsSaved = new ArrayList<>();
        private List<WeChatContact> contactsActive = new ArrayList<>();
        private WeChatMultiEventHandler<WeChatContact> onContactUpdated;

        public void setContactsSaved(WeChatContact... contacts) {
            contactsSaved = Arrays.asList(contacts);
        }

        @Override
        public synchronized List<WeChatContact> getContactsSaved() {
            return contactsSaved;
        }

        @Override
        public synchronized boolean isContactSaved(WeChatContact contact) {
            return contactsSaved.contains(contact);
        }

        public void setContactsActive(WeChatContact... contacts) {
            contactsActive = Arrays.asList(contacts);
        }

        @Override
        public synchronized List<WeChatContact> getContactsActive() {
            return contactsActive;
        }

        @Override
        public synchronized boolean isContactActive(WeChatContact contact) {
            return contactsActive.contains(contact);
        }

        @Override
        public WeChatSession setOnContactUpdated(WeChatMultiEventHandler<WeChatContact> onContactUpdated) {
            this.onContactUpdated = onContactUpdated;
            return this;
        }

        public void fireOnContactUpdated(WeChatContact... contacts) {
            onContactUpdated.onWeChatEvent(contacts);
        }
    }

    public static class ContactListModelImpl extends ContactListModel {

        public ContactListModelImpl(boolean active, boolean saved, WeChatSession session) {
            super(active, saved, session);
        }

        @Override
        protected void doLater(Runnable later) {
            later.run();
        }
    }
}
