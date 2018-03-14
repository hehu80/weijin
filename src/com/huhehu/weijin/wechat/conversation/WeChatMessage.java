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
import static com.huhehu.weijin.wechat.WeChatUtil.getTimestamp;
import com.huhehu.weijin.wechat.contacts.WeChatContact;
import java.io.Serializable;
import java.time.Instant;
import org.json.JSONObject;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
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
    private Instant time;

    /**
     *
     * @param json
     * @return
     */
    public static WeChatMessage fromJson(JSONObject json) {
        WeChatMessage message = new WeChatMessage();
        message.setId(json.getString("MsgId"));
        message.setContent(json.getString("Content"));
        message.setFromUserName(json.getString("FromUserName"));
        message.setToUserName(json.getString("ToUserName"));
        message.setMsgType(json.getInt("MsgType"));
        message.setTime(getTimestamp(json.getLong("CreateTime")));
        message.setJson(json.toString());
        return message;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * @return
     */
    public WeChatMessage setId(String id) {
        this.id = id;
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
    public String getFromUserName() {
        return fromUserName;
    }

    /**
     *
     * @param fromUserName
     * @return
     */
    public WeChatMessage setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
        return this;
    }

    /**
     *
     * @param fromUser
     * @return
     */
    public WeChatMessage setFromUserName(WeChatContact fromUser) {
        this.fromUserName = fromUser.getUserName();
        return this;
    }

    /**
     *
     * @return
     */
    public String getToUserName() {
        return toUserName;
    }

    /**
     *
     * @param toUserName
     * @return
     */
    public WeChatMessage setToUserName(String toUserName) {
        this.toUserName = toUserName;
        return this;
    }

    /**
     *
     * @param toUser
     * @return
     */
    public WeChatMessage setToUserName(WeChatContact toUser) {
        this.toUserName = toUser.getUserName();
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

    /**
     *
     * @return
     */
    @Override
    public String getWeChatId() {
        return id;
    }
}
