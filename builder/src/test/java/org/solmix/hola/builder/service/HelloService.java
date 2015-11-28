package org.solmix.hola.builder.service;

import java.util.List;

public interface HelloService
{

    String echo(String sayString);
    
    List<Person> listPerson();
    
    List<Person> filterPerson( List<Person> all);
    
    void test()throws HelloException;
    
}
