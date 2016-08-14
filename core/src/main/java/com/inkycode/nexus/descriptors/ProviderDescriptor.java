package com.inkycode.nexus.descriptors;

public class ProviderDescriptor {

    private ClassDescriptor provider;

    private int priority;

    public ProviderDescriptor() {
    }

    public ClassDescriptor getProvider() {
        return this.provider;
    }

    public int getPriority() {
        return this.priority;
    }
}
