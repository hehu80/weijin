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

import org.json.JSONObject;

import java.io.Serializable;

public class WeChatContact implements Serializable {
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

    public WeChatContact() {

    }

    public WeChatContact(String userName) {
        this.userName = userName;
    }

    public static WeChatContact fromJson(JSONObject json) {
        WeChatContact contact;
        if (json.has("MemberCount")) {
            WeChatGroup group = new WeChatGroup();
            group.setMemberCount(json.getInt("MemberCount"));
            group.setOwnerUin(json.getInt("OwnerUin"));

            contact = group;
        } else {
            WeChatUser user = new WeChatUser();
            user.setSex(json.getInt("Sex"));
//            user.setAlias(json.getString("Alias"));
//            user.setCity(json.getString("City"));
//            user.setProvince(json.getString("Province"));
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

    public void setUin(long uin) {
        this.uin = uin;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getContactFlag() {
        return contactFlag;
    }

    public void setContactFlag(int contactFlag) {
        this.contactFlag = contactFlag;
    }


    public String getRemarkName() {
        return remarkName;
    }

    public void setRemarkName(String remarkName) {
        this.remarkName = remarkName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public int getVerifyFlag() {
        return verifyFlag;
    }

    public void setVerifyFlag(int verifyFlag) {
        this.verifyFlag = verifyFlag;
    }


    public String getPinYinInitial() {
        return pinYinInitial;
    }

    public void setPinYinInitial(String pinYinInitial) {
        this.pinYinInitial = pinYinInitial;
    }

    public String getPinYinQuanPin() {
        return pinYinQuanPin;
    }

    public void setPinYinQuanPin(String pinYinQuanPin) {
        this.pinYinQuanPin = pinYinQuanPin;
    }

    @Override
    public String toString() {
        if (userName != null) {
            return userName.toString();
        } else
            return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o instanceof String) {
            return userName != null && userName.equals(o);
        } else if (o instanceof WeChatContact) {
            return userName != null && userName.equals(((WeChatContact) o).userName);
        } else {
            return false;
        }
    }
}
