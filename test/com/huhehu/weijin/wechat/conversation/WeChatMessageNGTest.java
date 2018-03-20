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
package com.huhehu.weijin.wechat.conversation;

import com.huhehu.weijin.wechat.WeChatJsonException;
import static com.huhehu.weijin.wechat.WeChatUtil.getTimestamp;
import com.huhehu.weijin.wechat.contacts.WeChatContact;
import java.time.Instant;
import org.json.JSONObject;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatMessageNGTest {

    public WeChatMessageNGTest() {
    }

    @Test
    public void test_constructor_messageId() {
        assertEquals(new WeChatMessage("test1").getMessageId(), "test1");
    }

    @Test
    public void test_hashCode_same() {
        WeChatMessage instance1 = new WeChatMessage("test1");
        WeChatMessage instance2 = new WeChatMessage("test1");
        assertEquals(instance1.hashCode(), instance2.hashCode());
    }

    @Test
    public void test_hashCode_notSame() {
        WeChatMessage instance1 = new WeChatMessage("test1");
        WeChatMessage instance2 = new WeChatMessage("test2");
        assertNotEquals(instance1.hashCode(), instance2.hashCode());
    }

    @Test
    public void test_equals_null() {
        WeChatMessage instance1 = new WeChatMessage("test1");
        WeChatMessage instance2 = null;
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void test_equals_differentClass() {
        WeChatMessage instance1 = new WeChatMessage("test1");
        WeChatContact instance2 = new WeChatContact("test1");
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void test_equals_differentId() {
        WeChatMessage instance1 = new WeChatMessage("test1");
        WeChatMessage instance2 = new WeChatMessage("test2");
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void test_equals_sameId() {
        WeChatMessage instance1 = new WeChatMessage("test1");
        WeChatMessage instance2 = new WeChatMessage("test1");
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
    }

    @Test
    public void test_equals_nullId() {
        WeChatMessage instance1 = new WeChatMessage(null);
        WeChatMessage instance2 = new WeChatMessage("test2");
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void test_equals_sameInstance() {
        WeChatMessage instance1 = new WeChatMessage("test1");
        WeChatMessage instance2 = instance1;
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
    }

    @Test
    public void test_toString_content() {
        WeChatMessage instance1 = new WeChatMessage()
                .setContent("content").setMessageId("messageId");
        assertEquals(instance1.toString(), "content");
    }

    @Test
    public void test_toString_noContent() {
        WeChatMessage instance1 = new WeChatMessage()
                .setContent(null).setMessageId("messageId");
        assertEquals(instance1.toString(), "messageId");
    }

    @Test
    public void test_SetterGetter() {
        WeChatMessage instance1;

        instance1 = new WeChatMessage();
        assertEquals(instance1.setContent("content").getContent(), "content");
        instance1 = new WeChatMessage();
        assertEquals(instance1.setFromUser(new WeChatContact("fromUser")).getFromUser(), new WeChatContact("fromUser"));
        instance1 = new WeChatMessage();
        assertEquals(instance1.setToUser(new WeChatContact("touser")).getToUser(), new WeChatContact("touser"));
        instance1 = new WeChatMessage();
        assertEquals(instance1.setJson("json").getJson(), "json");
        instance1 = new WeChatMessage();
        assertEquals(instance1.setMessageId("123").getMessageId(), "123");
        instance1 = new WeChatMessage();
        assertEquals(instance1.setMessageType(123).getMessageType(), 123);
        instance1 = new WeChatMessage();
        assertEquals(instance1.isReceived(), false);
        assertEquals(instance1.setReceived(true).isReceived(), true);
        instance1 = new WeChatMessage();
        Instant time = Instant.now();
        assertEquals(instance1.setTime(time).getTime(), time);
    }

    @Test
    public void test_fromValidJson() {
        try {
            long time = getTimestamp(Instant.now());
            JSONObject json = getValidJson(time);
            WeChatMessage instance1 = WeChatMessage.fromJson(json);
            assertEquals(instance1.getContent(), "content");
            assertEquals(instance1.getFromUser(), new WeChatContact("fromUserName"));
            assertEquals(instance1.getToUser(), new WeChatContact("toUserName"));
            assertEquals(instance1.getMessageId(), "messageId");
            assertEquals(instance1.getMessageType(), 123);
            assertEquals(instance1.getTime(), getTimestamp(time));
            assertEquals(instance1.getJson(), json.toString());
        } catch (WeChatJsonException e) {
            fail("shouldn't throw exception with valid JSON", e);
        }
    }

    @Test
    public void test_fromValidButEmptyJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("MsgId", "messageId");
            WeChatMessage instance1 = WeChatMessage.fromJson(json);
            assertEquals(instance1.getMessageId(), "messageId");
        } catch (WeChatJsonException e) {
            fail("shouldn't throw exception with valid JSON", e);
        }
    }

    @Test
    public void test_fromInvalidJson_noMessageId() {
        try {
            long time = getTimestamp(Instant.now());
            JSONObject json = getValidJson(time);
            json.remove("MsgId");
            WeChatContact.fromJson(json);
            fail("should throw exception with invalid JSON");
        } catch (WeChatJsonException e) {
        }
    }

    private JSONObject getValidJson(long time) {
        JSONObject json = new JSONObject();
        json.put("MsgId", "messageId");
        json.put("Content", "content");
        json.put("FromUserName", "fromUserName");
        json.put("ToUserName", "toUserName");
        json.put("MsgType", 123);
        json.put("CreateTime", time);
        return json;
    }
}
