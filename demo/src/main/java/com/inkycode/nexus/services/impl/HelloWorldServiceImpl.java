package com.inkycode.nexus.services.impl;

import com.inkycode.nexus.annotations.Service;
import com.inkycode.nexus.services.HelloWorldService;

@Service
public class HelloWorldServiceImpl implements HelloWorldService {

    @Override
    public void sayHello() {
        System.out.println("Hello World!");
    }

}
