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
package com.huhehu.weijin.ui.model;

import java.util.Arrays;
import java.util.List;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 *
 * @author Henning <henning@huhehu.com>
 */
public class ObservableListModelNGTest {

    public ObservableListModelNGTest() {
    }

    @Test
    public void test_constructor_list() {
        ObservableListModel instance = new ObservableListModelImpl("test1", "test2");
        assertEquals(instance.size(), 2);
        assertEquals(instance.get(0), "test1");
        assertEquals(instance.get(1), "test2");
    }

    @Test
    public void test_constructor() {
        ObservableListModel instance = new ObservableListModelImpl();
        assertEquals(instance.size(), 0);
    }

    @Test
    public void test_doAdd() {
        ObservableListModel instance = new ObservableListModelImpl();
        instance.doAdd(0, "test1");
        assertEquals(instance.size(), 1);
        assertEquals(instance.get(0), "test1");
    }

    @Test
    public void test_doRemove() {
        ObservableListModel instance = new ObservableListModelImpl("test1", "test2", "test3");
        instance.doRemove(1);
        assertEquals(instance.size(), 2);
        assertEquals(instance.get(0), "test1");
        assertEquals(instance.get(1), "test3");
    }

    @Test
    public void test_doSet() {
        ObservableListModel instance = new ObservableListModelImpl("test1", "test2", "test3");
        instance.doSet(1, "test4");
        assertEquals(instance.size(), 3);
        assertEquals(instance.get(0), "test1");
        assertEquals(instance.get(1), "test4");
        assertEquals(instance.get(2), "test3");
    }

    public static class ObservableListModelImpl extends ObservableListModel<String> {

        public ObservableListModelImpl() {
            super();
        }

        public ObservableListModelImpl(String... elements) {
            super(Arrays.asList(elements));
        }
    }

}
