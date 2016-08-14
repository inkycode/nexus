package com.inkycode.nexus.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.inkycode.nexus.Auto;

/**
 *
 * Annotate a class as a service so that nexus will generate a service
 * descriptor.
 *
 */
@Retention(CLASS)
@Target(TYPE)
public @interface Service {
    /**
     * The service interface for which the annotated class is a provider of.
     *
     * If left as Auto then the direct implements interface is used, if
     * possible. If no direct implements interface is provided then scan super
     * classes until one can be found.
     *
     * @return the service interface
     */
    Class<?> value() default Auto.class;

    /**
     * The service interface for which the annotated class is a factory of.
     *
     * If left as Auto then the service is not considered as a factory. If a
     * service interface is provided then the service provider will be notified
     * of new instances of the given service interface.
     *
     * @return the factory service interface.
     */
    Class<?> factory() default Auto.class;

    /**
     * The service providers priority.
     *
     * This value is used to When determining which service provider to return
     * if multiple service providers are available.
     *
     * A priority of Integer.MAX_VALUE will be higher than one of
     * Integer.MIN_VALUE.
     *
     * @return the priority.
     */
    int priority() default 0;
}
