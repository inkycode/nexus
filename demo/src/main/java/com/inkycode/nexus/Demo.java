package com.inkycode.nexus;

import com.inkycode.nexus.services.CommandService;
import com.inkycode.nexus.services.GreeterService;
import com.inkycode.nexus.services.impl.EnglishGreeting;
import com.inkycode.nexus.services.impl.FrenchGreeting;

public class Demo {

    public static void main(final String[] args) {
        Framework.getInstance().start();

        Framework.getInstance().getService(CommandService.class).run();

        final GreeterService greeter = Framework.getInstance().getService(GreeterService.class);
        greeter.greet(EnglishGreeting.class);
        greeter.greet(FrenchGreeting.class);
    }

}
