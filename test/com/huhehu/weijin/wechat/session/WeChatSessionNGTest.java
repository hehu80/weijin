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
package com.huhehu.weijin.wechat.session;

import com.huhehu.weijin.wechat.contacts.WeChatContact;
import com.huhehu.weijin.wechat.session.WeChatSession.Contact;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatSessionNGTest {

    public WeChatSessionNGTest() {
    }

    @Test
    public void test_contact_contstructor() {
        WeChatContact contact = new WeChatContact("").setUin(123).setUserId("userId").setSeq("seq");
        Contact instance1 = new Contact(contact);
        assertEquals(instance1.getActualContact(), contact);
        assertEquals(instance1.getUin(), 123);
        assertEquals(instance1.getSeq(), "seq");
        assertEquals(instance1.getUserId(), "userId");
    }

    @Test
    public void test_contact_hashCode() {
        WeChatContact contact = new WeChatContact("").setUin(123).setUserId("userId").setSeq("seq");
        Contact instance1 = new Contact(contact);
        assertEquals(instance1.hashCode(), contact.hashCode());
    }

    @Test
    public void test_contact_toString() {
        WeChatContact contact = new WeChatContact("").setUin(123).setUserId("userId").setSeq("seq");
        Contact instance1 = new Contact(contact);
        assertEquals(instance1.toString(), contact.toString());
    }
}
