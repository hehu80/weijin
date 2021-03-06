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

import com.huhehu.weijin.wechat.WeChatException;
import com.huhehu.weijin.wechat.WeChatJsonException;
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
    private int messageType;
    private Instant time;
    private String json;

    /**
     *
     */
    public WeChatMessage() {
    }

    /**
     *
     * @param messageId
     */
    public WeChatMessage(String messageId) {
        this.messageId = messageId;
    }

    /**
     *
     * @param json
     * @return
     */
    public static WeChatMessage fromJson(JSONObject json) throws WeChatJsonException {
        try {
            if (!json.has("MsgId")) {
                throw new WeChatException("MsgId required but not found!");
            }

            WeChatMessage message = new WeChatMessage();
            message.setMessageId(json.getString("MsgId"));
            message.setContent(json.has("Content") ? json.getString("Content") : "");
            message.setFromUser(json.has("FromUserName") ? new WeChatContact(json.getString("FromUserName")) : null);
            message.setToUser(json.has("ToUserName") ? new WeChatContact(json.getString("ToUserName")) : null);
            message.setMessageType(json.has("MsgType") ? json.getInt("MsgType") : 0);
            message.setTime(json.has("CreateTime") ? getTimestamp(json.getLong("CreateTime")) : null);
            message.setJson(json.toString());
            return message;
        } catch (Exception e) {
            throw new WeChatJsonException(json);
        }
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
     * @param messageId
     * @return
     */
    public WeChatMessage setMessageId(String messageId) {
        this.messageId = messageId;
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
    public int getMessageType() {
        return messageType;
    }

    /**
     *
     * @param messageType
     * @return
     */
    public WeChatMessage setMessageType(int messageType) {
        this.messageType = messageType;
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
            return Objects.equals(messageId, ((WeChatMessage) obj).messageId);
        }
    }
}
