package com.foo.simple.consumer;

import com.foo.simple.api.SimpleService;

public class SimpleManager
{

    private SimpleService service;

    
    public SimpleService getService() {
        return service;
    }

    
    public void setService(SimpleService service) {
        this.service = service;
    }
}
