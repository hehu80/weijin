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
package com.huhehu.weijin.wechat.contacts;

import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatContactNGTest {

    public WeChatContactNGTest() {
    }

    @Test
    public void test_constructor_userId() {
        assertEquals("test", new WeChatContact("test").getUserId());
    }

    @Test
    public void test_hashCode_same() {
        WeChatContact instance1 = new WeChatContact().setUserId("test1");
        WeChatContact instance2 = new WeChatContact().setUserId("test1");
        assertEquals(instance1.hashCode(), instance2.hashCode());
    }

    @Test
    public void test_hashCode_notSame() {
        WeChatContact instance1 = new WeChatContact().setUserId("test1");
        WeChatContact instance2 = new WeChatContact().setUserId("test2");
        assertNotEquals(instance1.hashCode(), instance2.hashCode());
    }

    @Test
    public void test_equals_null() {
        WeChatContact instance1 = new WeChatContact("test1");
        WeChatContact instance2 = null;
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void test_equals_differentClass() {
        WeChatContact instance1 = new WeChatContact("test1");
        WeChatMessage instance2 = new WeChatMessage().setMessageId("test1");
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void test_equals_differentId() {
        WeChatContact instance1 = new WeChatContact("test1");
        WeChatContact instance2 = new WeChatContact("test2");
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void test_equals_sameId() {
        WeChatContact instance1 = new WeChatContact("test1");
        WeChatContact instance2 = new WeChatContact("test1");
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
    }

    @Test
    public void test_equals_nullId() {
        WeChatContact instance1 = new WeChatContact(null);
        WeChatContact instance2 = new WeChatContact("test2");
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void test_toString_nickName() {
        WeChatContact instance1 = new WeChatContact()
                .setNickName("nickName").setUserId("userId");
        assertEquals(instance1.toString(), "nickName");
    }

    @Test
    public void test_toString_noNickName() {
        WeChatContact instance1 = new WeChatContact()
                .setNickName(null).setUserId("userId");
        assertEquals(instance1.toString(), "userId");
    }

    @Test
    public void test_SetterGetter() {
        WeChatContact instance1;

        instance1 = new WeChatContact();
        assertEquals(instance1.setUin(123).getUin(), 123);
        instance1 = new WeChatContact();
        assertEquals(instance1.setUserId("userId").getUserId(), "userId");
        instance1 = new WeChatContact();
        assertEquals(instance1.setNickName("nickName").getNickName(), "nickName");
        instance1 = new WeChatContact();
        assertEquals(instance1.setRemarkName("remarkName").getRemarkName(), "remarkName");
        instance1 = new WeChatContact();
        assertEquals(instance1.setSignature("signature").getSignature(), "signature");
        instance1 = new WeChatContact();
        assertEquals(instance1.setContactFlag(123).getContactFlag(), 123);
        instance1 = new WeChatContact();
        assertEquals(instance1.setVerifyFlag(123).getVerifyFlag(), 123);
        instance1 = new WeChatContact();
        assertEquals(instance1.setImageUrl("imageUrl").getImageUrl(), "imageUrl");
        instance1 = new WeChatContact();
        assertEquals(instance1.setPinYinInitial("pinYinInitial").getPinYinInitial(), "pinYinInitial");
        instance1 = new WeChatContact();
        assertEquals(instance1.setPinYinQuanPin("pinYinQuanPin").getPinYinQuanPin(), "pinYinQuanPin");
        instance1 = new WeChatContact();
        assertEquals(instance1.setSeq("seq").getSeq(), "seq");
        instance1 = new WeChatContact();
        assertEquals(instance1.setJson("json").getJson(), "json");
    }

    @Test
    public void test_getSeq_withImageUrl() {
        WeChatContact instance1 = new WeChatContact();
        
        instance1.setSeq(null);
        instance1.setImageUrl("test1&seq=123&test");
        assertEquals(instance1.getSeq(), "123");

        instance1.setSeq(null);
        instance1.setImageUrl("test1&seq=456");
        assertEquals(instance1.getSeq(), "456");

        instance1.setSeq(null);
        instance1.setImageUrl("test1&seq=   123     &test");
        assertEquals(instance1.getSeq(), "123");
        
        instance1.setSeq(null);
        instance1.setImageUrl("test1&seq=   456   ");
        assertEquals(instance1.getSeq(), "456");
    }

    @Test
    public void test_getSeq_withoutImageUrl() {
        WeChatContact instance1 = new WeChatContact();
        instance1.setSeq(null);
        instance1.setUserId("test1");
        assertEquals(instance1.getSeq(), "test1");
    }
}
