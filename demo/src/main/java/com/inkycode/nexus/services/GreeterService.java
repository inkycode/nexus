package com.inkycode.nexus.services;

public interface GreeterService {

    void greet(Class<? extends GreetingService> greeting);
}
