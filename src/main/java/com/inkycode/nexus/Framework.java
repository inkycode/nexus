package com.inkycode.nexus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * The Framework class is a singleton to be used at runtime.
 *
 */
public class Framework {

    private static Logger LOG = LoggerFactory.getLogger(Framework.class);

    private static Framework framework;

    /**
     * Factory method to generate a Framework instance.
     *
     * @return a Framework instance.
     */
    public static Framework getInstance() {
        if (framework == null) {
            framework = new Framework();
        }

        return framework;
    }

    private final Map<Class<?>, Object> serviceMap;
    private final Map<Class<?>, Object> factoryMap;

    private Framework() {
        this.serviceMap = new HashMap<Class<?>, Object>();
        this.factoryMap = new HashMap<Class<?>, Object>();

        try {
            final Enumeration<URL> servicesUrls = this.getClass().getClassLoader().getResources("META-INF/services.json");
            while (servicesUrls.hasMoreElements()) {
                try (InputStream inputStream = servicesUrls.nextElement().openStream()) {
                    final ServiceDescriptor[] services = new ObjectMapper().reader().withType(ServiceDescriptor[].class).readValue(inputStream);

                    for (final ServiceDescriptor service : services) {
                        try {
                            final Class<?> serviceClass = Class.forName(service.getService());
                            final Class<?> serviceProviderClass = Class.forName(service.getProvider());
                            final Class<?> serviceFactoryClass = service.getFactory() != null ? Class.forName(service.getFactory()) : null;

                            if (serviceClass != null && serviceProviderClass != null) {
                                try {
                                    final Object serviceInstance = serviceProviderClass.newInstance();

                                    if (serviceFactoryClass != null) {
                                        this.factoryMap.put(serviceFactoryClass, serviceInstance);

                                        for (final Class<?> registeredService : this.serviceMap.keySet()) {
                                            final Object registeredServiceInstance = this.serviceMap.get(registeredService);

                                            if (serviceFactoryClass.isInstance(registeredServiceInstance)) {
                                                this.notifyFactory(serviceInstance, registeredServiceInstance, registeredService);
                                            }
                                        }
                                    }

                                    this.serviceMap.put(serviceClass, serviceInstance);

                                } catch (final ReflectiveOperationException e) {
                                    LOG.error("Unable to register service");
                                }
                            }
                        } catch (final ClassNotFoundException e) {
                            LOG.error("Unable to find service, service provider or service factory");
                        }
                    }
                } catch (final IOException e) {
                    LOG.error("Invalid service properties");
                }
            }

            for (final Object serviceProider : this.serviceMap.values()) {
                this.injectServices(serviceProider.getClass(), serviceProider);
            }
        } catch (final IOException e) {
            LOG.error("Unable to read service properties");
        }

    }

    private <T> void injectServices(final Class<?> serviceInstanceClass, final T serviceInstance) {
        for (final Field field : serviceInstanceClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                try {
                    field.set(serviceInstance, this.getService(field.getType()));
                } catch (final ReflectiveOperationException e) {
                    LOG.error("Unable to inject service");
                }
            }
        }

        if (serviceInstanceClass.getSuperclass() != null) {
            this.injectServices(serviceInstanceClass.getSuperclass(), serviceInstance);
        }
    }

    private void notifyFactory(final Object factoryInstance, final Object serviceInstance, final Class<?> service) {
        final Class<?> factoryClass = factoryInstance.getClass();

        try {
            final Method bindValueMethod = factoryClass.getMethod("bindValue", service);

            if (bindValueMethod != null) {
                try {
                    bindValueMethod.invoke(factoryInstance, serviceInstance);
                } catch (final ReflectiveOperationException e) {
                    LOG.error("Unable to invoke bind method");
                }
            }
        } catch (final NoSuchMethodException e) {
            LOG.error("Unable to bind value method");
        }
    }

    /**
     * Attempts to obtain a provider for the given service.
     *
     * @param service
     *            the service to obtain a provider for.
     * @param <T>
     *            the type of service.
     * @return a provider for the given service, or null if one can not be
     *         found.
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(final Class<T> service) {
        if (this.serviceMap.containsKey(service)) {
            return (T) this.serviceMap.get(service);
        }

        return null;
    }

}
