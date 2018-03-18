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

import org.json.JSONObject;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatJsonException extends WeChatException {

    private JSONObject json;

    public WeChatJsonException(String json) {
        this(new JSONObject(json));
    }

    public WeChatJsonException(JSONObject json) {
        super("failed to parse object from JSON");
        this.json = json;
    }

    public WeChatJsonException(String json, Throwable cause) {
        this(new JSONObject(json), cause);
    }

    public WeChatJsonException(JSONObject json, Throwable cause) {
        super("failed to parse object from JSON", cause);
        this.json = json;
    }

    public JSONObject getJson() {
        return json;
    }
}
