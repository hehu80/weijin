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

import static com.huhehu.weijin.wechat.WeChatUtil.getTimestamp;
import com.huhehu.weijin.wechat.contacts.WeChatContact;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.json.JSONObject;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatMessage implements Serializable {

    public static int TYPE_TEXT = 1;
    public static int TYPE_CHAT_CHANGE = 51;
    public static int TYPE_AUDIO = 34;
    public static int TYPE_IMAGE = 3;
    public static int TYPE_FILE = 49;
    public static int TYPE_REJECT_MESSAGE = 10002;

    private String messageId;
    private String content;
    private WeChatContact fromUser;
    private WeChatContact toUser;
    private boolean received;
    private int msgType;
    private Instant time;
    private String json;

    /**
     *
     */
    public WeChatMessage() {
    }

    /**
     *
     * @param content
     */
    public WeChatMessage(String content) {
        this.content = content;
    }

    /**
     *
     * @param json
     * @return
     */
    public static WeChatMessage fromJson(JSONObject json) {
        WeChatMessage message = new WeChatMessage();
        message.setId(json.getString("MsgId"));
        message.setContent(json.getString("Content"));
        message.setFromUser(new WeChatContact(json.getString("FromUserName")));
        message.setToUser(new WeChatContact(json.getString("ToUserName")));
        message.setMsgType(json.getInt("MsgType"));
        message.setTime(getTimestamp(json.getLong("CreateTime")));
        message.setJson(json.toString());
        return message;
    }

    /**
     *
     * @return
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     *
     * @param id
     * @return
     */
    public WeChatMessage setId(String id) {
        this.messageId = id;
        return this;
    }

    /**
     *
     * @return
     */
    public String getContent() {
        return content;
    }

    /**
     *
     * @param content
     * @return
     */
    public WeChatMessage setContent(String content) {
        this.content = content;
        return this;
    }

    /**
     *
     * @return
     */
    public WeChatContact getFromUser() {
        return fromUser;
    }

    /**
     *
     * @param fromUser
     * @return
     */
    public WeChatMessage setFromUser(WeChatContact fromUser) {
        this.fromUser = fromUser;
        return this;
    }

    /**
     *
     * @return
     */
    public WeChatContact getToUser() {
        return toUser;
    }

    /**
     *
     * @param toUser
     * @return
     */
    public WeChatMessage setToUser(WeChatContact toUser) {
        this.toUser = toUser;
        return this;
    }

    /**
     *
     * @return
     */
    public int getMsgType() {
        return msgType;
    }

    /**
     *
     * @param msgType
     * @return
     */
    public WeChatMessage setMsgType(int msgType) {
        this.msgType = msgType;
        return this;
    }

    /**
     *
     * @return
     */
    public boolean isReceived() {
        return received;
    }

    /**
     *
     * @param received
     * @return
     */
    public WeChatMessage setReceived(boolean received) {
        this.received = received;
        return this;
    }

    /**
     *
     * @return
     */
    public Instant getTime() {
        return time;
    }

    /**
     *
     * @param time
     * @return
     */
    public WeChatMessage setTime(Instant time) {
        this.time = time;
        return this;
    }

    public String getJson() {
        return json;
    }

    public WeChatMessage setJson(String json) {
        this.json = json;
        return this;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        if (content != null) {
            return content;
        } else {
            return messageId;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.messageId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof WeChatMessage)) {
            return false;
        } else {
            return messageId != null && messageId.equals(((WeChatMessage) obj).messageId);
        }
    }
}
