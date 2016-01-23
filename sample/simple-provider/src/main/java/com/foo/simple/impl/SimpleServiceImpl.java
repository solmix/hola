package com.foo.simple.impl;

import java.util.UUID;

import com.foo.simple.api.SimpleService;


public class SimpleServiceImpl implements SimpleService
{

    @Override
    public String getUID() {
        return UUID.randomUUID().toString();
    }

}
