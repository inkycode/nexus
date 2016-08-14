package com.inkycode.nexus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkycode.nexus.descriptors.ServiceDescriptor;

/**
 *
 * The Framework class is a singleton to be used at runtime.
 *
 */
public class Framework {

    private static Logger LOG = LoggerFactory.getLogger(Framework.class);

    private static Framework framework;

    private final Map<Class<?>, Set<ServiceInstance<?>>> serviceMap;

    private final Map<Class<?>, Set<ServiceInstance<?>>> serviceFactoryMap;

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

    private Framework() {
        this.serviceMap = new HashMap<Class<?>, Set<ServiceInstance<?>>>();
        this.serviceFactoryMap = new HashMap<Class<?>, Set<ServiceInstance<?>>>();

        try {
            final Enumeration<URL> servicesUrls = this.getClass().getClassLoader().getResources("META-INF/services.json");

            while (servicesUrls.hasMoreElements()) {
                try (InputStream inputStream = servicesUrls.nextElement().openStream()) {
                    final ServiceDescriptor[] services = new ObjectMapper().reader().withType(ServiceDescriptor[].class).readValue(inputStream);

                    for (final ServiceDescriptor service : services) {

                        try {
                            final Class<?> serviceClass = Class.forName(service.getService().getName());
                            final Class<?> serviceProviderClass = Class.forName(service.getProvider().getName());
                            final Class<?> serviceFactoryClass = service.getFactory() != null ? Class.forName(service.getFactory().getName()) : null;
                            final int priority = service.getPriority();

                            final Set<ServiceInstance<?>> serviceInstanceSet = this.serviceMap.containsKey(serviceClass) ? this.serviceMap.get(serviceClass) : new TreeSet<ServiceInstance<?>>();

                            final ServiceInstance<?> serviceInstance = ServiceInstance.getInstance(serviceProviderClass, priority);

                            serviceInstanceSet.add(serviceInstance);

                            this.serviceMap.put(serviceClass, serviceInstanceSet);

                            if (serviceFactoryClass != null) {
                                this.serviceFactoryMap.put(serviceFactoryClass, serviceInstanceSet);
                            }

                        } catch (final Exception e) {
                            LOG.error("Unable to register service");
                        }
                    }
                } catch (final IOException e) {
                    LOG.error("Invalid service properties");
                }
            }

            for (final Set<ServiceInstance<?>> serviceInstanceSet : this.serviceMap.values()) {
                for (final ServiceInstance<?> serviceInstance : serviceInstanceSet) {
                    this.injectServices(serviceInstance.getInstance().getClass(), serviceInstance.getInstance());
                }
            }

            for (final Class<?> service : this.serviceMap.keySet()) {
                for (final ServiceInstance<?> serviceInstance : this.serviceMap.get(service)) {

                    if (this.serviceFactoryMap.containsKey(service)) {
                        for (final ServiceInstance<?> factoryServiceInstance : this.serviceFactoryMap.get(service)) {
                            this.notifyFactory(factoryServiceInstance.getInstance(), serviceInstance.getInstance(), service);
                        }
                    }
                }
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
            return ((ServiceInstance<T>) this.serviceMap.get(service).toArray()[0]).getInstance();
        }

        return null;
    }

    private static class ServiceInstance<T> implements Comparable<ServiceInstance<T>> {

        private final T instance;

        private final int priority;

        private ServiceInstance(final Class<T> service, final int priority) throws InstantiationException, IllegalAccessException {
            this.instance = service.newInstance();

            this.priority = priority;
        }

        private T getInstance() {
            return this.instance;
        }

        private int getPriority() {
            return this.priority;
        }

        @Override
        public int compareTo(final ServiceInstance<T> b) {
            if (this.getPriority() == b.getPriority())
                return 1;

            return this.getPriority() > b.getPriority() ? -1 : 1;
        }

        private static <K> ServiceInstance<K> getInstance(final Class<K> type, final int priority) throws InstantiationException, IllegalAccessException {
            return new ServiceInstance<K>(type, priority);
        }
    }

}
