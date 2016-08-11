package com.inkycode.nexus.annotations.processor.tasks;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static javax.lang.model.element.ElementKind.CLASS;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.inkycode.nexus.ServiceDescriptor;
import com.inkycode.nexus.annotations.Service;

/**
 *
 * A task to generate a services descriptor file for scanned classes.
 *
 */
public class ServiceTask implements ProcessingStep {

    private List<ServiceDescriptor> services;

    private final ProcessingEnvironment processingEnv;

    /**
     * Generates a new service task with the given processing environment.
     *
     * @param processingEnv
     *            the processing environment.
     */
    public ServiceTask(final ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<? extends Class<? extends Annotation>> annotations() {
        return ImmutableSet.of(Service.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Element> process(final SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
        this.services = new ArrayList<ServiceDescriptor>();

        for (final Class<? extends Annotation> annotationClass : elementsByAnnotation.keySet()) {
            for (final Element element : elementsByAnnotation.get(annotationClass)) {
                final TypeElement typeElement = MoreElements.asType(element);

                if (typeElement.getKind() == CLASS) {
                    final Service serviceAnnotation = Service.class.cast(typeElement.getAnnotation(annotationClass));

                    if (serviceAnnotation != null) {
                        final ServiceDescriptor serviceDescriptor = new ServiceDescriptor(this.processingEnv, serviceAnnotation, typeElement);

                        if (serviceDescriptor.isValid()) {
                            this.services.add(serviceDescriptor);
                        }
                    }
                }
            }
        }

        Collections.sort(this.services, new Comparator<ServiceDescriptor>() {

            @Override
            public int compare(final ServiceDescriptor a, final ServiceDescriptor b) {
                if (a.getFactory() != null && b.getFactory() != null) {
                    return 0;
                } else if (a.getFactory() != null) {
                    return 1;
                } else if (b.getFactory() != null) {
                    return -1;
                }

                return 0;
            }

        });

        final Filer filer = this.processingEnv.getFiler();
        try {
            final FileObject servicesFileObject = filer.createResource(CLASS_OUTPUT, "", "META-INF/services.json");

            try (OutputStream outputStream = servicesFileObject.openOutputStream()) {
                new ObjectMapper().setSerializationInclusion(NON_NULL).writerWithDefaultPrettyPrinter().writeValue(outputStream, this.services);
            }
        } catch (final IOException e) {

        }

        return new HashSet<>();
    }

}
