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

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatUser extends WeChatContact {

    public static String USER_FILE_HELPER = "filehelper";

    private String alias;
    private String city;
    private String province;
    private int sex; // 0: no, 1: male, 2: female

    /**
     *
     */
    public WeChatUser() {
    }

    /**
     *
     * @param userName
     */
    public WeChatUser(String userName) {
        super(userName);
    }

    /**
     *
     * @return
     */
    public int getSex() {
        return sex;
    }

    /**
     *
     * @param sex
     * @return
     */
    public WeChatUser setSex(int sex) {
        this.sex = sex;
        return this;
    }

    /**
     *
     * @return
     */
    public String getAlias() {
        return alias;
    }

    /**
     *
     * @param alias
     * @return
     */
    public WeChatUser setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     *
     * @return
     */
    public String getCity() {
        return city;
    }

    /**
     *
     * @param city
     * @return
     */
    public WeChatUser setCity(String city) {
        this.city = city;
        return this;
    }

    /**
     *
     * @return
     */
    public String getProvince() {
        return province;
    }

    /**
     *
     * @param province
     * @return
     */
    public WeChatUser setProvince(String province) {
        this.province = province;
        return this;
    }
}
