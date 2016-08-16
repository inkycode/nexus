package com.inkycode.nexus.services.impl;

import com.inkycode.nexus.annotations.Service;
import com.inkycode.nexus.services.GreetingService;

@Service
public class FrenchGreeting implements GreetingService {

    @Override
    public String getGreeting() {
        return "bonjour.";
    };

}
