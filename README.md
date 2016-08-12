# Nexus
[![Build Status](https://travis-ci.org/inkycode/nexus.svg?branch=master)](https://travis-ci.org/inkycode/nexus) [![Download](https://api.bintray.com/packages/inkycoder/maven/com.inkycode.nexus/images/download.svg)](https://bintray.com/inkycoder/maven/com.inkycode.nexus/_latestVersion)
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

@Service
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
    Framework.getInstance().getService(CommandService.class).run();
}


```
