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

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public abstract class WeChatObject implements Serializable {

    private String json;

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
    public WeChatObject setJson(String json) {
        this.json = json;
        return this;
    }

    /**
     *
     * @return
     */
    public abstract String getWeChatId();

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        if (getWeChatId() != null) {
            return getWeChatId().toString();
        } else {
            return super.toString();
        }
    }

    /**
     *
     * @param o
     * @return
     */
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

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        if (getWeChatId() != null) {
            return Objects.hash(getWeChatId());
        } else {
            return super.hashCode();
        }
    }
}
