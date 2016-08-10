package com.inkycode.nexus;

import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.inkycode.nexus.annotations.Service;

public class ServiceDescriptor {

    private String service;
    private String provider;
    private String factory;

    public ServiceDescriptor() {
    }

    public ServiceDescriptor(final ProcessingEnvironment processingEnv, final Service service, final TypeElement provider) {
        this.provider = provider.getQualifiedName().toString();

        final AnnotationMirror serviceAnnotationMirror = getAnnotationMirror(provider, Service.class);

        if (serviceAnnotationMirror != null) {
            final TypeElement valueTypeElement = getAnnotationTypeElementValue(processingEnv, serviceAnnotationMirror, "value", Auto.class);
            final TypeElement factoryTypeElement = getAnnotationTypeElementValue(processingEnv, serviceAnnotationMirror, "factory", Auto.class);

            if (Auto.class.getName().equals(valueTypeElement.getQualifiedName().toString())) {
                if (provider.getInterfaces().size() > 0) {
                    this.service = ((TypeElement) processingEnv.getTypeUtils().asElement(provider.getInterfaces().get(0))).getQualifiedName().toString();
                } else {
                    for (final TypeMirror superTypeTypeMirror : processingEnv.getTypeUtils().directSupertypes(provider.asType())) {
                        final TypeElement superTypeTypeElement = (TypeElement) processingEnv.getTypeUtils().asElement(superTypeTypeMirror);
                        if (superTypeTypeElement.getInterfaces().size() > 0) {
                            this.service = ((TypeElement) processingEnv.getTypeUtils().asElement(superTypeTypeElement.getInterfaces().get(0))).getQualifiedName().toString();

                            break;
                        }
                    }
                }
            } else {
                this.service = valueTypeElement.getQualifiedName().toString();
            }

            if (Auto.class.getName().equals(factoryTypeElement.getQualifiedName().toString())) {
                // Factory is not valid if value is auto
            } else {
                this.factory = factoryTypeElement.getQualifiedName().toString();
            }
        }
    }

    public String getService() {
        return this.service;
    }

    public String getProvider() {
        return this.provider;
    }

    public String getFactory() {
        return this.factory;
    }

    @JsonIgnore
    public boolean isValid() {
        return (this.service != null && this.provider != null);
    }

    private static AnnotationMirror getAnnotationMirror(final TypeElement typeElement, final Class<?> clazz) {
        final String clazzName = clazz.getName();
        for (final AnnotationMirror m : typeElement.getAnnotationMirrors()) {
            if (m.getAnnotationType().toString().equals(clazzName)) {
                return m;
            }
        }
        return null;
    }

    private static AnnotationValue getAnnotationValue(final AnnotationMirror annotationMirror, final String key) {
        for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static TypeElement getAnnotationTypeElementValue(final ProcessingEnvironment processingEnv, final AnnotationMirror annotationMirror, final String key, final Class<?> defaultClass) {
        final AnnotationValue annotationValue = getAnnotationValue(annotationMirror, key);

        if (annotationValue != null) {
            return (TypeElement) processingEnv.getTypeUtils().asElement((TypeMirror) getAnnotationValue(annotationMirror, key).getValue());
        }

        return processingEnv.getElementUtils().getTypeElement(defaultClass.getName());
    }

}