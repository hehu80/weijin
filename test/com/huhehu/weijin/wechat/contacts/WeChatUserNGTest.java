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

import com.huhehu.weijin.wechat.WeChatJsonException;
import static com.huhehu.weijin.wechat.WeChatUtil.getTimestamp;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import java.time.Instant;
import org.json.JSONObject;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatUserNGTest {

    public WeChatUserNGTest() {
    }

    @Test
    public void test_constructor_userId() {
        assertEquals("test", new WeChatUser("test").getUserId());
    }

    @Test
    public void test_equals_null() {
        WeChatUser instance1 = new WeChatUser("test1");
        WeChatUser instance2 = null;
        assertFalse(instance1.equals(instance2));
    }

    @Test
    public void test_equals_differentClass() {
        WeChatUser instance1 = new WeChatUser("test1");
        WeChatMessage instance2 = new WeChatMessage().setMessageId("test1");
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void test_equals_differentSubClass() {
        WeChatUser instance1 = new WeChatUser("test1");
        WeChatGroup instance2 = new WeChatGroup("test1");
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
    }

    @Test
    public void test_equals_differentId() {
        WeChatUser instance1 = new WeChatUser("test1");
        WeChatUser instance2 = new WeChatUser("test2");
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void test_equals_sameId() {
        WeChatUser instance1 = new WeChatUser("test1");
        WeChatUser instance2 = new WeChatUser("test1");
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
    }

    @Test
    public void test_equals_nullId() {
        WeChatUser instance1 = new WeChatUser(null);
        WeChatUser instance2 = new WeChatUser("test2");
        assertFalse(instance1.equals(instance2));
        assertFalse(instance2.equals(instance1));
    }

    @Test
    public void test_equals_sameInstance() {
        WeChatUser instance1 = new WeChatUser("test1");
        WeChatUser instance2 = instance1;
        assertTrue(instance1.equals(instance2));
        assertTrue(instance2.equals(instance1));
    }

    @Test
    public void test_SetterGetter() {
        WeChatUser instance1;

        instance1 = new WeChatUser();
        assertEquals(instance1.setAlias("alias").getAlias(), "alias");
        instance1 = new WeChatUser();
        assertEquals(instance1.setCity("city").getCity(), "city");
        instance1 = new WeChatUser();
        assertEquals(instance1.setProvince("province").getProvince(), "province");
        instance1 = new WeChatUser();
        assertEquals(instance1.setSex(123).getSex(), 123);
    }

    @Test
    public void test_fromValidJson() {
        try {
            JSONObject json = getValidJson();
            WeChatUser instance1 = (WeChatUser) WeChatContact.fromJson(json);
            assertEquals(instance1.getAlias(), "alias");
            assertEquals(instance1.getCity(), "city");
            assertEquals(instance1.getContactFlag(), 1);
            assertEquals(instance1.getImageUrl(), "imageUrl");
            assertEquals(instance1.getJson(), json.toString());
            assertEquals(instance1.getNickName(), "nickName");
            assertEquals(instance1.getPinYinInitial(), "pinYinInitial");
            assertEquals(instance1.getPinYinQuanPin(), "pinYinQuanPin");
            assertEquals(instance1.getProvince(), "province");
            assertEquals(instance1.getRemarkName(), "remarkName");
            assertEquals(instance1.getSeq(), "userId");
            assertEquals(instance1.getSex(), 2);
            assertEquals(instance1.getSignature(), "signature");
            assertEquals(instance1.getUin(), 3);
            assertEquals(instance1.getUserId(), "userId");
            assertEquals(instance1.getVerifyFlag(), 4);
        } catch (WeChatJsonException e) {
            fail("shouldn't throw exception with valid JSON", e);
        }
    }

    @Test
    public void test_fromValidButEmptyJson() {
        try {
            JSONObject json = new JSONObject();
            json.put("UserName", "userId");
            json.put("HeadImgUrl", "imageUrl");
            WeChatUser instance1 = (WeChatUser) WeChatContact.fromJson(json);
            assertEquals(instance1.getImageUrl(), "imageUrl");
            assertEquals(instance1.getJson(), json.toString());
            assertEquals(instance1.getUserId(), "userId");
        } catch (WeChatJsonException e) {
            fail("shouldn't throw exception with valid JSON", e);
        }
    }

    @Test
    public void test_fromInvalidJson_noUserName() {
        try {
            JSONObject json = getValidJson();
            json.remove("UserName");
            WeChatContact.fromJson(json);
            fail("should throw exception with invalid JSON");
        } catch (WeChatJsonException e) {
        }
    }

    @Test
    public void test_fromInvalidJson_noHeadImg() {
        try {
            JSONObject json = getValidJson();
            json.remove("HeadImgUrl");
            WeChatContact.fromJson(json);
            fail("should throw exception with invalid JSON");
        } catch (WeChatJsonException e) {
        }
    }

    private JSONObject getValidJson() {
        JSONObject json = new JSONObject();
        json.put("Uin", 3);
        json.put("UserName", "userId");
        json.put("NickName", "nickName");
        json.put("HeadImgUrl", "imageUrl");
        json.put("ContactFlag", 1);
        json.put("RemarkName", "remarkName");
        json.put("Signature", "signature");
        json.put("VerifyFlag", 4);
        json.put("PYInitial", "pinYinInitial");
        json.put("PYQuanPin", "pinYinQuanPin");
        json.put("Sex", 2);
        json.put("Province", "province");
        json.put("City", "city");
        json.put("Alias", "alias");
        return json;
    }
}
