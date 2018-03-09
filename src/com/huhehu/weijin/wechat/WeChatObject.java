/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.huhehu.weijin.wechat;

import com.huhehu.weijin.wechat.contacts.WeChatContact;
import com.huhehu.weijin.wechat.conversation.WeChatMessage;
import java.util.Objects;

/**
 *
 * @author henning
 */
public abstract class WeChatObject {

    public abstract String getWeChatId();

    @Override
    public String toString() {
        if (getWeChatId() != null) {
            return getWeChatId().toString();
        } else {
            return super.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (o instanceof String) {
            return getWeChatId() != null && getWeChatId().equals(o);
        } else if (o instanceof WeChatObject) {
            return getWeChatId() != null && getWeChatId().equals(((WeChatObject) o).getWeChatId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        if (getWeChatId() != null) {
            return Objects.hash(getWeChatId());
        } else {
            return super.hashCode();
        }
    }
}
