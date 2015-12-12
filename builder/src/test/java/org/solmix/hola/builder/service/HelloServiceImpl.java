package org.solmix.hola.builder.service;

import java.util.ArrayList;
import java.util.List;


public class HelloServiceImpl implements HelloService
{

    @Override
    public String echo(String sayString) {
        return sayString;
    }

    @Override
    public List<Person> listPerson() {
        Person p  = new Person();
        p.setName("person A");
        p.setAge(12);
        List<Person> ps  = new ArrayList<Person>();
        ps.add(p);
        return ps;
    }

    @Override
    public List<Person> filterPerson(List<Person> all) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void test() throws HelloException {
      throw new HelloException("exception hello");

    }

}
