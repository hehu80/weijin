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
package com.huhehu.weijin.wechat;

import com.huhehu.weijin.wechat.contacts.WeChatUser;
import org.json.JSONObject;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatJsonExceptionNGTest {

    public WeChatJsonExceptionNGTest() {
    }

    @Test
    public void test_constructor_Json() {
        JSONObject json = new JSONObject();
        assertEquals(json, new WeChatJsonException(json).getJson());
    }

    @Test
    public void test_constructor_Json_Cause() {
        JSONObject json = new JSONObject();
        assertEquals(json, new WeChatJsonException(json, new Exception()).getJson());
    }

    @Test
    public void test_constructor_JsonString() {
        JSONObject json = new JSONObject();
        json.put("test", 123);
        assertEquals(json.toString(), new WeChatJsonException(json.toString()).getJson().toString());
    }

    @Test
    public void test_constructor_JsonString_Cause() {
        JSONObject json = new JSONObject();
        json.put("test", 123);
        assertEquals(json.toString(), new WeChatJsonException(json.toString(), new Exception()).getJson().toString());
    }
}