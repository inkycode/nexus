package com.inkycode.nexus.services.impl;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.inkycode.nexus.annotations.Service;
import com.inkycode.nexus.services.GreeterService;
import com.inkycode.nexus.services.GreetingService;

@Service(factory = GreetingService.class)
public class GreeterServiceImpl implements GreeterService {

    Map<Class<? extends GreetingService>, GreetingService> greetings = new HashMap<Class<? extends GreetingService>, GreetingService>();

    @Override
    public void greet(final Class<? extends GreetingService> greeter) {
        if (this.greetings.containsKey(greeter)) {
            System.out.println(this.greetings.get(greeter).getGreeting());
        }
    }

    @Inject
    public void addGreeting(final GreetingService greeting) {
        this.greetings.put(greeting.getClass(), greeting);
    }

}
