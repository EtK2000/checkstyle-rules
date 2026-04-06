# Checkstyle Task Name Conflict With Java Plugin

## Current State

`CheckstylePlugin.registerTasks()` uses `project.getTasks().register()` to create `checkstyleMain`
and `checkstyleTest`. This works when no other plugin has created tasks with those names.

## Bug

When the `java` plugin is applied before our plugin, Gradle's built-in `checkstyle` plugin
(which our plugin applies via `project.getPluginManager().apply("checkstyle")`) auto-creates
`checkstyleMain` and `checkstyleTest` for each source set. Our `register()` then fails:

```
Cannot add task 'checkstyleMain' as a task with that name already exists.
```

This means the plugin is broken for standard Java projects:
```groovy
apply plugin: 'java'
apply plugin: 'com.etk2000.checkstyle'  // fails
```

Reversing the order also fails because our plugin needs the `compileOnly` configuration, which
is created by `java`:
```groovy
apply plugin: 'com.etk2000.checkstyle'  // fails: Configuration 'compileOnly' not found
apply plugin: 'java'
```

## Why It Hasn't Been Caught

The plugin has only been used in Android projects. `com.android.application` and
`com.android.library` create the `compileOnly` configuration but don't trigger Checkstyle's
source-set-based auto-task creation (they aren't the standard `java` plugin). The unit test
(`CheckstylePluginTest`) uses `ProjectBuilder` without applying `java`, so the conflict never
occurs.

## Fix

Replace `register()` with logic that handles both cases:

1. If the task already exists (created by Checkstyle's source set convention), configure it
   using `named()`:
   ```java
   project.getTasks().named("checkstyleMain", Checkstyle.class, task -> {
       task.dependsOn(extractTaskName);
       task.include("**/*.java");
       // ...
   });
   ```

2. If the task doesn't exist, create it with `register()` as before.

The simplest approach:
```java
private static void configureOrRegister(
    Project project, String name, Class<Checkstyle> type, Action<Checkstyle> config
) {
    try {
        project.getTasks().named(name, type, config);
    }
    catch (UnknownTaskException e) {
        project.getTasks().register(name, type, config);
    }
}
```

Or use `findByName()` first:
```java
if (project.getTasks().findByName("checkstyleMain") != null)
    project.getTasks().named("checkstyleMain", Checkstyle.class, config);
else
    project.getTasks().register("checkstyleMain", Checkstyle.class, config);
```

The `compileOnly` issue also needs fixing. Our plugin should create the configuration if it
doesn't exist, or defer the dependency addition to `afterEvaluate`:
```java
var compileOnly = project.getConfigurations().findByName("compileOnly");
if (compileOnly == null)
    compileOnly = project.getConfigurations().create("compileOnly");
```

## Testing

The fix should be verified by the consumer test project (see `consumer-test-project.md`). At
minimum, test both orderings:
1. `java` then `com.etk2000.checkstyle`
2. `com.etk2000.checkstyle` then `java`
3. `com.android.application` then `com.etk2000.checkstyle` (existing working case)