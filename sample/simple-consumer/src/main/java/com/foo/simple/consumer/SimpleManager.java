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
    
    @SuppressWarnings("static-access")
    public void start(){
        try {
            Thread.currentThread().sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread t = new Thread(){
          @Override
        public void run(){
              while(true){
                  System.out.println(service.getUID());
                  try {
                    Thread.sleep(100000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
              }
             
          }
        };
        t.start();
    }
}
