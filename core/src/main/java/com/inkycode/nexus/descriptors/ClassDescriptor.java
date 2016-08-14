package com.inkycode.nexus.descriptors;

import javax.lang.model.element.TypeElement;

public class ClassDescriptor {

    private String name;

    public ClassDescriptor() {
    }

    public ClassDescriptor(final TypeElement typeElement) {
        this.name = typeElement.getQualifiedName().toString();
    }

    public String getName() {
        return this.name;
    }
}
