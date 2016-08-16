package com.inkycode.nexus;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    }

    private void registerService(final ServiceDescriptor serviceDescriptor) throws IllegalAccessException, InstantiationException {
        final Class<?> serviceClass = serviceDescriptor.getServiceClass();
        final Class<?> serviceProviderClass = serviceDescriptor.getProviderClass();
        final Class<?> serviceFactoryClass = serviceDescriptor.getFactoryClass();
        final int priority = serviceDescriptor.getPriority();

        if (serviceClass != null && serviceProviderClass != null) {
            this.registerService(serviceClass, serviceProviderClass, serviceFactoryClass, priority);
        }

        // TODO: do something here
    }

    private void registerService(final Class<?> service, final Class<?> provider, final Class<?> factory, final int priority) throws IllegalAccessException, InstantiationException {
        // If we already have a set for this service in the
        // service map then use that, otherwise create a new
        // set.
        final Set<ServiceInstance<?>> serviceInstanceSet = this.serviceMap.containsKey(service) ? this.serviceMap.get(service) : new TreeSet<ServiceInstance<?>>();

        final ServiceInstance<?> serviceInstance = ServiceInstance.getInstance(provider, priority);

        serviceInstanceSet.add(serviceInstance);

        this.serviceMap.put(service, serviceInstanceSet);

        if (factory != null) {
            this.serviceFactoryMap.put(factory, serviceInstanceSet);
        }
    }

    private void injectServices(final Map<Class<?>, Set<ServiceInstance<?>>> serviceMap) {
        for (final Set<ServiceInstance<?>> serviceInstanceSet : serviceMap.values()) {
            for (final ServiceInstance<?> serviceInstance : serviceInstanceSet) {
                this.injectServices(serviceInstance.getInstance().getClass(), serviceInstance.getInstance());
            }
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

    private void notifyFactories(final Map<Class<?>, Set<ServiceInstance<?>>> serviceMap, final Map<Class<?>, Set<ServiceInstance<?>>> serviceFactoryMap) {
        for (final Class<?> service : serviceMap.keySet()) {
            for (final ServiceInstance<?> serviceInstance : serviceMap.get(service)) {

                if (serviceFactoryMap.containsKey(service)) {
                    for (final ServiceInstance<?> factoryServiceInstance : serviceFactoryMap.get(service)) {
                        this.notifyFactory(factoryServiceInstance.getInstance(), serviceInstance.getInstance(), service);
                    }
                }
            }
        }
    }

    private void notifyFactory(final Object factoryInstance, final Object serviceInstance, final Class<?> service) {
        final Class<?> factoryClass = factoryInstance.getClass();

        for (final Method method : factoryClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Inject.class)) {
                final Class<?>[] methodParameterTypes = method.getParameterTypes();

                // The method must have one parameter and it must be instance
                // assignable by the service instance.
                if (methodParameterTypes.length == 1 && methodParameterTypes[0].isInstance(serviceInstance)) {
                    try {
                        method.invoke(factoryInstance, serviceInstance);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        LOG.error("Unable to invoke service instance bind method, check the method signature.");
                    }
                }
            }

        }
        // try {
        // final Method bindValueMethod = factoryClass.getMethod("bindValue",
        // service);
        //
        // if (bindValueMethod != null) {
        // try {
        // bindValueMethod.invoke(factoryInstance, serviceInstance);
        // } catch (final ReflectiveOperationException e) {
        // LOG.error("Unable to invoke bind method");
        // }
        // }
        // } catch (final NoSuchMethodException e) {
        // LOG.error("Unable to bind value method");
        // }
    }

    /**
     * Starts the framework and initializes services from the services metadata
     * file.
     */
    public void start() {
        try {
            final Enumeration<URL> servicesUrls = this.getClass().getClassLoader().getResources("META-INF/services.json");

            while (servicesUrls.hasMoreElements()) {
                try (InputStream inputStream = servicesUrls.nextElement().openStream()) {
                    final ServiceDescriptor[] serviceDescriptors = new ObjectMapper().reader().withType(ServiceDescriptor[].class).readValue(inputStream);

                    for (final ServiceDescriptor serviceDescriptor : serviceDescriptors) {

                        try {
                            this.registerService(serviceDescriptor);
                        } catch (final IllegalAccessException | InstantiationException e) {
                            LOG.error("Unable to register service");
                        }

                    }
                } catch (final IOException e) {
                    LOG.error("Invalid service properties");
                }
            }

            this.injectServices(this.serviceMap);

            this.notifyFactories(this.serviceMap, this.serviceFactoryMap);

        } catch (final IOException e) {
            LOG.error("Unable to read service properties");
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
