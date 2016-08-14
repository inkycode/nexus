package com.inkycode.nexus.services.impl;

import javax.inject.Inject;

import com.inkycode.nexus.annotations.Service;
import com.inkycode.nexus.services.CommandService;
import com.inkycode.nexus.services.HelloWorldService;

@Service
public class CommandServiceImpl implements CommandService {

    @Inject
    private HelloWorldService helloWorldService;

    @Override
    public void run() {
        this.helloWorldService.sayHello();
        this.helloWorldService.sayHello();
    }

}
