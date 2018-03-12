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

import com.huhehu.weijin.wechat.WeChatObject;
import org.json.JSONObject;

import java.io.Serializable;

public class WeChatContact extends WeChatObject implements Serializable {

    private long uin;
    private String userName;
    private String nickName;
    private String imageUrl;
    private int contactFlag;
    private String remarkName;
    private String signature;
    private int verifyFlag;
    private String pinYinInitial;
    private String pinYinQuanPin;
    private String seq;

    public WeChatContact() {
    }

    public WeChatContact(String userName) {
        this.userName = userName;
    }

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
        return contact;
    }

    public long getUin() {
        return uin;
    }

    public WeChatContact setUin(long uin) {
        this.uin = uin;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public WeChatContact setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getNickName() {
        return nickName;
    }

    public WeChatContact setNickName(String nickName) {
        this.nickName = nickName;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

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

    public int getContactFlag() {
        return contactFlag;
    }

    public WeChatContact setContactFlag(int contactFlag) {
        this.contactFlag = contactFlag;
        return this;
    }

    public String getRemarkName() {
        return remarkName;
    }

    public WeChatContact setRemarkName(String remarkName) {
        this.remarkName = remarkName;
        return this;
    }

    public String getSignature() {
        return signature;
    }

    public WeChatContact setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public int getVerifyFlag() {
        return verifyFlag;
    }

    public WeChatContact setVerifyFlag(int verifyFlag) {
        this.verifyFlag = verifyFlag;
        return this;
    }

    public String getPinYinInitial() {
        return pinYinInitial;
    }

    public WeChatContact setPinYinInitial(String pinYinInitial) {
        this.pinYinInitial = pinYinInitial;
        return this;
    }

    public String getPinYinQuanPin() {
        return pinYinQuanPin;
    }

    public WeChatContact setPinYinQuanPin(String pinYinQuanPin) {
        this.pinYinQuanPin = pinYinQuanPin;
        return this;
    }

    public String getSeq() {
        return seq == null ? userName : seq;
    }

    public WeChatContact setSeq(String seq) {
        this.seq = seq;
        return this;
    }

    @Override
    public String getWeChatId() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            if (o != null && (o instanceof WeChatContact) && uin != 0) {
                return uin == ((WeChatContact) o).uin;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

}
