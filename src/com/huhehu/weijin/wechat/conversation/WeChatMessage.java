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

import com.huhehu.weijin.wechat.WeChatObject;
import com.huhehu.weijin.wechat.contacts.WeChatContact;
import java.io.Serializable;
import org.json.JSONObject;

public class WeChatMessage extends WeChatObject implements Serializable {

    public static int TYPE_TEXT = 1;
    public static int TYPE_CHAT_CHANGE = 51;
    public static int TYPE_AUDIO = 34;
    public static int TYPE_IMAGE = 3;
    public static int TYPE_FILE = 49;
    public static int TYPE_REJECT_MESSAGE = 10002;

    private String id;
    private String content;
    private String fromUserName;
    private String toUserName;
    private boolean received;
    private int msgType;

    public static WeChatMessage fromJson(JSONObject json) {
        WeChatMessage message = new WeChatMessage();
        message.setId(json.getString("MsgId"));
        message.setContent(json.getString("Content"));
        message.setFromUserName(json.getString("FromUserName"));
        message.setToUserName(json.getString("ToUserName"));
        message.setMsgType(json.getInt("MsgType"));
        return message;
    }

    public String getId() {
        return id;
    }

    public WeChatMessage setId(String id) {
        this.id = id;
        return this;
    }

    public String getContent() {
        return content;
    }

    public WeChatMessage setContent(String content) {
        this.content = content;
        return this;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public WeChatMessage setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
        return this;
    }

    public WeChatMessage setFromUserName(WeChatContact fromUser) {
        this.fromUserName = fromUser.getUserName();
        return this;
    }

    public String getToUserName() {
        return toUserName;
    }

    public WeChatMessage setToUserName(String toUserName) {
        this.toUserName = toUserName;
        return this;
    }

    public WeChatMessage setToUserName(WeChatContact toUser) {
        this.toUserName = toUser.getUserName();
        return this;
    }

    public int getMsgType() {
        return msgType;
    }

    public WeChatMessage setMsgType(int msgType) {
        this.msgType = msgType;
        return this;
    }

    public boolean isReceived() {
        return received;
    }

    public WeChatMessage setReceived(boolean received) {
        this.received = received;
        return this;
    }

    @Override
    public String getWeChatId() {
        return id;
    }
}
