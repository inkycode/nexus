package com.inkycode.nexus.descriptors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inkycode.nexus.Auto;
import com.inkycode.nexus.annotations.Service;

/**
 *
 * A descriptor for a service which allows for easy serialization when
 * generating a services descriptor.
 *
 */
public class ServiceDescriptor {

    private ClassDescriptor service;
    private ClassDescriptor provider;
    private ClassDescriptor factory;

    private int priority;

    /**
     * Default constructor.
     */
    public ServiceDescriptor() {
    }

    /**
     * Generates a service descriptor for the given service and provider.
     *
     * @param processingEnv
     *            the processing environment.
     * @param service
     *            the service to generate a descriptor for.
     * @param provider
     *            the service provider.
     */
    public ServiceDescriptor(final ProcessingEnvironment processingEnv, final Service service, final TypeElement provider) {
        this.provider = new ClassDescriptor(provider);

        final AnnotationMirror serviceAnnotationMirror = getAnnotationMirror(provider, Service.class);

        if (serviceAnnotationMirror != null) {
            final AnnotationDescriptor annotationDescriptor = new AnnotationDescriptor(processingEnv, serviceAnnotationMirror, new String[] { "value", "factory", "priority" }, new Object[] { Auto.class, Auto.class, 0 });

            this.priority = annotationDescriptor.getValue("priority", Integer.class);

            final String valueType = annotationDescriptor.getValue("value", String.class);
            final String factoryType = annotationDescriptor.getValue("factory", String.class);

            final TypeElement valueTypeElement = processingEnv.getElementUtils().getTypeElement(valueType);
            final TypeElement factoryTypeElement = processingEnv.getElementUtils().getTypeElement(factoryType);

            if (Auto.class.getName().equals(valueType)) {
                if (provider.getInterfaces().size() > 0) {
                    this.service = new ClassDescriptor(((TypeElement) processingEnv.getTypeUtils().asElement(provider.getInterfaces().get(0))));
                } else {
                    for (final TypeMirror superTypeTypeMirror : processingEnv.getTypeUtils().directSupertypes(provider.asType())) {
                        final TypeElement superTypeTypeElement = (TypeElement) processingEnv.getTypeUtils().asElement(superTypeTypeMirror);
                        if (superTypeTypeElement.getInterfaces().size() > 0) {
                            this.service = new ClassDescriptor(((TypeElement) processingEnv.getTypeUtils().asElement(superTypeTypeElement.getInterfaces().get(0))));

                            break;
                        }
                    }
                }
            } else {
                this.service = new ClassDescriptor(valueTypeElement);
            }

            if (Auto.class.getName().equals(factoryTypeElement.getQualifiedName().toString())) {
                // Factory is not valid if value is auto
            } else {
                this.factory = new ClassDescriptor(factoryTypeElement);
            }
        }
    }

    /**
     * Returns the service.
     *
     * @return the service.
     */
    public ClassDescriptor getService() {
        return this.service;
    }

    /**
     * Returns the provider.
     *
     * @return the provider.
     */
    public ClassDescriptor getProvider() {
        return this.provider;
    }

    /**
     * Returns the factory.
     *
     * @return the factory.
     */
    public ClassDescriptor getFactory() {
        return this.factory;
    }

    /**
     * Returns the priority.
     *
     * @return the priority.
     */
    public int getPriority() {
        return this.priority;
    }

    /**
     * Determines if the service descriptor is valid.
     *
     * @return true if valid, false otherwise.
     */
    @JsonIgnore
    public boolean isValid() {
        return (this.service != null && this.provider != null);
    }

    private static AnnotationMirror getAnnotationMirror(final TypeElement typeElement, final Class<?> annotation) {
        final String annotationName = annotation.getName();

        for (final AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
            if (annotationMirror.getAnnotationType().toString().equals(annotationName)) {
                return annotationMirror;
            }
        }

        return null;
    }
}