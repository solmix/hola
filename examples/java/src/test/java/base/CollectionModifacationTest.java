/*
 * Copyright 2013 The Solmix Project
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.gnu.org/licenses/ 
 * or see the FSF site: http://www.fsf.org. 
 */

package base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年6月30日
 */

public class CollectionModifacationTest
{

    @Test
    public void test1() {
        ArrayList<String> l = new ArrayList<String>();
        l.add("aaa");
        final List a = Collections.unmodifiableList(l);
        boolean exception = false;
        try {
            a.add("bbb");
        } catch (Exception e) {
            exception = true;
            e.printStackTrace();
        }
        Assert.assertTrue(exception);

    }
}
