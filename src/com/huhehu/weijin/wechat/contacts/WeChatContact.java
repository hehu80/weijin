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
import org.json.JSONObject;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatContact implements Serializable {

    private long uin;
    private String userId;
    private String nickName;
    private String imageUrl;
    private int contactFlag;
    private String remarkName;
    private String signature;
    private int verifyFlag;
    private String pinYinInitial;
    private String pinYinQuanPin;
    private String seq;
    private String json;

    /**
     *
     */
    public WeChatContact() {
    }

    /**
     *
     * @param userId
     */
    public WeChatContact(String userId) {
        this.userId = userId;
    }

    /**
     *
     * @param json
     * @return
     */
    public static WeChatContact fromJson(JSONObject json) {
        WeChatContact contact;
        if (json.getString("HeadImgUrl").contains("webwxgetheadimg")) {
            WeChatGroup group = new WeChatGroup();
            group.setMemberCount(json.getInt("MemberCount"));
            group.setOwnerUin(json.getInt("OwnerUin"));
            contact = group;
        } else {
            WeChatUser user = new WeChatUser();
            user.setSex(json.getInt("Sex"));
            user.setProvince(json.has("Province") ? json.getString("Province") : "");
            user.setCity(json.has("City") ? json.getString("City") : "");
            contact = user;
        }
        contact.setUin(json.getLong("Uin"));
        contact.setUserName(json.getString("UserName"));
        contact.setNickName(json.getString("NickName"));
        contact.setImageUrl(json.getString("HeadImgUrl"));
        contact.setContactFlag(json.getInt("ContactFlag"));
        contact.setRemarkName(json.getString("RemarkName"));
        contact.setSignature(json.getString("Signature"));
        contact.setVerifyFlag(json.getInt("VerifyFlag"));
        contact.setPinYinInitial(json.getString("PYInitial"));
        contact.setPinYinQuanPin(json.getString("PYQuanPin"));
        contact.setJson(json.toString());
        return contact;
    }

    /**
     *
     * @return
     */
    public long getUin() {
        return uin;
    }

    /**
     *
     * @param uin
     * @return
     */
    public WeChatContact setUin(long uin) {
        this.uin = uin;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public WeChatContact setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    /**
     *
     * @param userName
     * @return
     */
    public WeChatContact setUserName(String userName) {
        this.userId = userName;
        return this;
    }

    /**
     *
     * @return
     */
    public String getNickName() {
        return nickName;
    }

    /**
     *
     * @param nickName
     * @return
     */
    public WeChatContact setNickName(String nickName) {
        this.nickName = nickName;
        return this;
    }

    /**
     *
     * @return
     */
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     *
     * @param imageUrl
     * @return
     */
    public WeChatContact setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        if (imageUrl != null) {
            int seqStart = imageUrl.indexOf("seq=") + 4;
            int seqEnd = imageUrl.indexOf("&", seqStart);
            setSeq(imageUrl.substring(seqStart, seqEnd));
        } else {
            setSeq(null);
        }
        return this;
    }

    /**
     *
     * @return
     */
    public int getContactFlag() {
        return contactFlag;
    }

    /**
     *
     * @param contactFlag
     * @return
     */
    public WeChatContact setContactFlag(int contactFlag) {
        this.contactFlag = contactFlag;
        return this;
    }

    /**
     *
     * @return
     */
    public String getRemarkName() {
        return remarkName;
    }

    /**
     *
     * @param remarkName
     * @return
     */
    public WeChatContact setRemarkName(String remarkName) {
        this.remarkName = remarkName;
        return this;
    }

    /**
     *
     * @return
     */
    public String getSignature() {
        return signature;
    }

    /**
     *
     * @param signature
     * @return
     */
    public WeChatContact setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    /**
     *
     * @return
     */
    public int getVerifyFlag() {
        return verifyFlag;
    }

    /**
     *
     * @param verifyFlag
     * @return
     */
    public WeChatContact setVerifyFlag(int verifyFlag) {
        this.verifyFlag = verifyFlag;
        return this;
    }

    /**
     *
     * @return
     */
    public String getPinYinInitial() {
        return pinYinInitial;
    }

    /**
     *
     * @param pinYinInitial
     * @return
     */
    public WeChatContact setPinYinInitial(String pinYinInitial) {
        this.pinYinInitial = pinYinInitial;
        return this;
    }

    /**
     *
     * @return
     */
    public String getPinYinQuanPin() {
        return pinYinQuanPin;
    }

    /**
     *
     * @param pinYinQuanPin
     * @return
     */
    public WeChatContact setPinYinQuanPin(String pinYinQuanPin) {
        this.pinYinQuanPin = pinYinQuanPin;
        return this;
    }

    /**
     *
     * @return
     */
    public String getSeq() {
        return seq == null ? userId : seq;
    }

    /**
     *
     * @param seq
     * @return
     */
    public WeChatContact setSeq(String seq) {
        this.seq = seq;
        return this;
    }

    /**
     *
     * @return
     */
    public String getJson() {
        return json;
    }

    /**
     *
     * @param json
     * @return
     */
    public WeChatContact setJson(String json) {
        this.json = json;
        return this;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        if (nickName != null) {
            return nickName;
        } else {
            return userId;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.userId);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof WeChatContact)) {
            return false;
        } else {
            return userId != null && userId.equals(((WeChatContact) obj).userId);
        }
    }
}
