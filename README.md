# Nexus [![Build Status](https://travis-ci.org/inkycode/nexus.svg?branch=master)](https://travis-ci.org/inkycode/nexus) [![Download](https://api.bintray.com/packages/inkycoder/maven/com.inkycode.nexus/images/download.svg)](https://bintray.com/inkycoder/maven/com.inkycode.nexus/_latestVersion)

A tiny service framework for Java.

## Simple services
```java
interface HelloWorldService {

    void sayHello();

}

/* ... */

@Service
public class HelloWorldServiceImpl implements HelloWorldService {

    @Override
    public void sayHello() {
        System.out.println("Hello World!");
    }

}

/* ... */

public static void main(String[] args) {
    Framework.getInstance().start();

    Framework.getInstance().getService(HelloWorldService.class).sayHello();
}
```

## Service injection
```java
interface CommandService {

    void run();

}

/* ... */

@Service
public class CommandServiceImpl implements CommandService {

    @Inject
    private HelloWorldService helloWorldService;

    @Override
    public void run() {
        helloWorldService.sayHello();
        helloWorldService.sayHello();
    }

}

/* ... */

public static void main(String[] args) {
    Framework.getInstance().start();

    Framework.getInstance().getService(CommandService.class).run();
}
```

## Service factories
```java
interface GreeterService {

    void greet(Class<? extends GreetingService> greeting);

}

/* ... */

interface GreeterService {

    String getGreeting();

}

/* ... */

@Service
public class EnglishGreeting implements GreetingService {

    @Override
    public String getGreeting() {
        return "Hello, kind sir.";
    }

}

/* ... */

@Service
public class FrenchGreeting implements GreetingService {

    @Override
    public String getGreeting() {
        return "bonjour.";
    };

}

/* ... */

@Service
public class GreeterServiceImpl implements GreeterService {

    private final Map<Class<? extends GreetingService>, GreetingService> greetings = new HashMap<Class<? extends GreetingService>, GreetingService>();

    @Override
    public void greet(Class<? extends GreetingService> greeting) {
        if (this.greetings.containsKey(greeter)) {
            System.out.println(this.greetings.get(greeter).getGreeting());
        }
    }

    @Inject
    public void addGreeting(final GreetingService greeting) {
        this.greetings.put(greeting.getClass(), greeting);
    }

}

/* ... */

public static void main(String[] args) {
    Framework.getInstance().start();

    final GreeterService greeter = Framework.getInstance().getService(GreeterService.class);
    greeter.greet(EnglishGreeting.class);
    greeter.greet(FrenchGreeting.class);
}
```
