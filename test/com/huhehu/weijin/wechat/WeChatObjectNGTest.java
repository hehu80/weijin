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

import java.util.Objects;
import static org.testng.Assert.*;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class WeChatObjectNGTest {

    public WeChatObjectNGTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    /**
     * Test of equals method, of class WeChatObject.
     */
    @Test
    public void testEquals_differentId() {
        WeChatObject instance1 = new WeChatObjectImpl("test1");
        WeChatObject instance2 = new WeChatObjectImpl("test2");
        assertFalse(instance1.equals(instance2));
    }

    /**
     * Test of equals method, of class WeChatObject.
     */
    @Test
    public void testEquals_sameId() {
        WeChatObject instance1 = new WeChatObjectImpl("test1");
        WeChatObject instance2 = new WeChatObjectImpl("test1");
        assertTrue(instance1.equals(instance2));
    }

    /**
     * Test of equals method, of class WeChatObject.
     */
    @Test
    public void testEquals_differentString() {
        WeChatObject instance1 = new WeChatObjectImpl("test1");
        String instance2 = "test2";
        assertFalse(instance1.equals(instance2));
    }

    /**
     * Test of equals method, of class WeChatObject.
     */
    @Test
    public void testEquals_sameString() {
        WeChatObject instance1 = new WeChatObjectImpl("test1");
        String instance2 = "test1";
        assertTrue(instance1.equals(instance2));
    }

    /**
     * Test of equals method, of class WeChatObject.
     */
    @Test
    public void testEquals_same() {
        WeChatObject instance1 = new WeChatObjectImpl("test1");
        WeChatObject instance2 = instance1;
        assertTrue(instance1.equals(instance2));
    }

    /**
     * Test of equals method, of class WeChatObject.
     */
    @Test
    public void testEquals_class() {
        WeChatObject instance1 = new WeChatObjectImpl("test1");
        Integer instance2 = 1;
        assertFalse(instance1.equals(instance2));
    }

    /**
     * Test of equals method, of class WeChatObject.
     */
    @Test
    public void testEquals_null() {
        WeChatObject instance1 = new WeChatObjectImpl("test1");
        WeChatObject instance2 = null;
        assertFalse(instance1.equals(instance2));
    }

    /**
     * Test of hashCode method, of class WeChatObject.
     */
    @Test
    public void testHashCode() {
        assertEquals(Objects.hash("test"), new WeChatObjectImpl("test").hashCode());
    }

    public class WeChatObjectImpl extends WeChatObject {

        private String weChatId;

        public WeChatObjectImpl(String weChatId) {
            this.weChatId = weChatId;
        }

        public String getWeChatId() {
            return weChatId;
        }
    }

}
