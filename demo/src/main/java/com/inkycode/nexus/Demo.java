package com.inkycode.nexus;

import com.inkycode.nexus.services.CommandService;

public class Demo {

    public static void main(final String[] args) {
        Framework.getInstance().start();

        Framework.getInstance().getService(CommandService.class).run();
    }

}
