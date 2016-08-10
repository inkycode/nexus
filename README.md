https://travis-ci.org/inkycode/nexus.svg?branch=master

# Nexus
A tiny service container library for Java.

## Build
```
$ gradlew clean build
```

## Simple services
```java
interface HelloWorldService {

    void sayHello();

}

/* ... */

@service
public class HelloWorldServiceImpl implements HelloWorldService {

    @Override
    public void sayHello() {
        System.out.println("Hello World!");
    }

}

/* ... */

public static void main(String[] args) {
    Framework.getInstance().getService(HelloWorldService.class).sayHello();
}

```

## Service injection
```java
interface CommandService {

    void run();

}

/* ... */

@service
public class CommandServiceImpl implements CommandService {

    @inject
    private HelloWorldService helloWorldService;

    @Override
    public void run() {
        helloWorldService.sayHello();
        helloWorldService.sayHello();
    }

}

/* ... */

public static void main(String[] args) {
    Framework.getInstance().getService(CommandService.class).run();
}


```
