package com.inkycode.nexus.descriptors;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AnnotationDescriptor {

    private final Map<String, Object> valueMap;

    public AnnotationDescriptor() {
        this.valueMap = new HashMap<String, Object>();
    }

    public AnnotationDescriptor(final ProcessingEnvironment processingEnv, final AnnotationMirror annotationMirror, final String[] valueKeys, final Object[] valueDefaults) {
        this();

        for (int i = 0; i < valueKeys.length; i++) {
            final String valueKey = valueKeys[i];
            final Object valueDefault = valueDefaults[i];
            Object value = null;

            if (valueDefault.getClass() == Class.class) {
                value = getAnnotationClassValue(processingEnv, annotationMirror, valueKey, (Class<?>) valueDefault).toString();
            } else if (valueDefault.getClass() == Integer.class) {
                value = getAnnotationIntegerValue(processingEnv, annotationMirror, valueKey, (int) valueDefault);
            }

            this.valueMap.put(valueKey, value);
        }
    }

    @JsonIgnore
    public <T> T getValue(final String key, final Class<T> type) {
        return type.cast(this.valueMap.get(key));
    }

    private static AnnotationValue getAnnotationValue(final AnnotationMirror annotationMirror, final String key) {
        for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().toString().equals(key)) {
                return entry.getValue();
            }
        }

        return null;
    }

    private static TypeElement getAnnotationClassValue(final ProcessingEnvironment processingEnv, final AnnotationMirror annotationMirror, final String key, final Class<?> defaultValue) {
        final AnnotationValue annotationValue = getAnnotationValue(annotationMirror, key);

        if (annotationValue != null) {
            return (TypeElement) processingEnv.getTypeUtils().asElement((TypeMirror) getAnnotationValue(annotationMirror, key).getValue());
        }

        return processingEnv.getElementUtils().getTypeElement(defaultValue.getName());
    }

    private static int getAnnotationIntegerValue(final ProcessingEnvironment processingEnv, final AnnotationMirror annotationMirror, final String key, final int defaultValue) {
        final AnnotationValue annotationValue = getAnnotationValue(annotationMirror, key);

        if (annotationValue != null) {
            return (int) annotationValue.getValue();
        }

        return defaultValue;
    }
}
