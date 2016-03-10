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
    
    public void start(){
        
        long start = System.currentTimeMillis();
        for(int i=0;i<10;i++){
            service.getUID();
        }
        System.out.println(System.currentTimeMillis()-start);
        /*Thread t = new Thread(){
          @Override
        public void run(){
              while(true){
                  System.out.println(service.getUID());
                  try {
                    Thread.sleep(3);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
              }
             
          }
        };
        t.start();*/
    }
}
