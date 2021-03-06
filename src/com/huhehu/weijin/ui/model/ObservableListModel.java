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

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.collections.ModifiableObservableListBase;

/**
 *
 * @author Henning <henning@huhehu.com>
 * @param <E>
 */
public class ObservableListModel<E> extends ModifiableObservableListBase<E> {

    private List<E> elements;

    /**
     *
     * @param elements
     */
    public ObservableListModel(List<? extends E> elements) {
        this.elements = new ArrayList<E>(elements);
    }

    /**
     *
     */
    public ObservableListModel() {
        this.elements = new ArrayList<E>();
    }

    /**
     *
     * @param index
     * @return
     */
    @Override
    public E get(int index) {
        return elements.get(index);
    }

    /**
     *
     * @return
     */
    @Override
    public int size() {
        return elements.size();
    }

    /**
     *
     * @param index
     * @param element
     */
    @Override
    protected void doAdd(int index, E element) {
        elements.add(index, element);
    }

    /**
     *
     * @param index
     * @param element
     * @return
     */
    @Override
    protected E doSet(int index, E element) {
        return elements.set(index, element);
    }

    /**
     *
     * @param index
     * @return
     */
    @Override
    protected E doRemove(int index) {
        return elements.remove(index);
    }

    protected void doLater(Runnable later) {
        Platform.runLater(later);
    }
}
