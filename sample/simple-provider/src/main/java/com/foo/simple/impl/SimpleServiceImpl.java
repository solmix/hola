package com.foo.simple.impl;

import com.foo.simple.api.SimpleService;


public class SimpleServiceImpl implements SimpleService
{

    @Override
    public String getUID() {
        return "Hello";
    }

}
