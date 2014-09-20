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

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 总结Java方法(函数)传值和传引用的问题:
 * 其实java函数中的参数都是传递值的，所不同的是对于基本数据类型传递的是参数的一份拷贝，对于类类型传递的是该类参数的引用的拷贝
 * ，当在函数体中修改参数值时，无论是基本类型的参数还是引用类型的参数
 * ，修改的只是该参数的拷贝，不影响函数实参的值，如果修改的是引用类型的成员值，则该实参引用的成员值是可以改变的
 * 
 * @author solmix.f@gmail.com
 * @version 0.0.1 2014年6月22日
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class ParameterTransferTest
{

    /**
     * 基本数据类型参数：传值，方法不会改变实参的值。
     */
    @Test
    public void test1() {
        int a = 0;
        Function f = new Function();
        f.changeInt(a);
        Assert.assertEquals(0, a);
    }

    /**
     * 对象类型参数：传引用,在函数内部改变形参引用不会改变实际参数的值
     */
   
    @Test
    public void test2() {
        String a = "Hello world!";
        Function f = new Function();
        /**
         * 在函数内部改变形参应用不会,改变实际参数的值
         */
        f.test2_1(a);
        Assert.assertEquals("Hello world!", a);
        Map map = new TreeMap();
        map.put("AA", "bbbbb");
        /**
         * 在test2_2中把形参map重新重新指向了一个新的Hashmap,但是由于java对象类型的参数传递的是引用的拷贝,
         * 所以,map指向新的HashMap后,对新对象的操作不会影响原有对象,实参依旧指向原来的参数
         */
        f.test2_2(map);
        Assert.assertEquals("bbbbb", map.get("AA"));
    }

    /**
     * 对象类型参数：传引用,通过形参改变了内容,会改变实参的值
     */
    @Test
    public void test3() {
        StringBuffer sb = new StringBuffer();
        sb.append("Hello");
        Function f = new Function();
        /**
         * 改变了StringBuffer的内容,改变了值,因为java形参传递的是引用的拷贝,而这个拷贝依旧指向原来的对象,而改变内容的时候,
         * 他们都改变的同一个对象,所以改变生效
         */
        f.test3(sb);
        Assert.assertFalse("Hello".equals(sb.toString()));
    }

    class Function
    {

        public void changeInt(int a) {
            a = 5;
        }

        public void test2_1(String a) {
            a = "hello world!";
        }

        public void test2_2(Map map) {
            map = new HashMap();
            map.put("AA", "bbbbb");
        }

        public void test3(StringBuffer a) {
            a.append(" world!");
        }
    }
}
