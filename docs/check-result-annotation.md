# Enforce @CheckReturnValue on Pure Methods

## CLAUDE.md Rule

> "Always add `@CheckResult` on methods that return a value without side effects"

## Current State

The project uses `@CheckReturnValue` from `javax.annotation` (jsr305). It's applied consistently on utility methods in `AstUtil.java`, `ReflectionUtil.java`, and private helpers in check classes. No automated enforcement exists.

## Feasibility

Detecting "without side effects" is the hard part. A practical check could flag public/protected non-void methods that lack the annotation, with exclusions for known side-effect patterns.

### What a Check Could Detect
- Non-void methods without `@CheckReturnValue`
- Skip `void` methods (no return value)
- Skip `@Override` methods (inherited contract, can't always add the annotation)
- Skip setters (methods starting with "set" returning void - already excluded by non-void filter)
- Skip constructors

### False Positive Cases

| Pattern | Risk | Example |
|---------|------|---------|
| Methods with side effects that return a value | Medium | `list.add(x)` returns boolean |
| Builder pattern methods | Low | `builder.setName("x")` returns `this` |
| Framework overrides | Eliminated | Skipped via `@Override` exclusion |
| Void methods | Eliminated | Skipped by non-void filter |

### Practical Approach

Start simple: flag non-void, non-`@Override` methods without `@CheckReturnValue`. This catches the obvious omissions. Developers can suppress for the rare side-effect-returning method.

The annotation is already a dependency (`com.google.code.findbugs:jsr305:3.0.2`), so no new imports needed.

## Implementation Estimate

~200 lines. Visit `METHOD_DEF`, check return type is not void, check for `@CheckReturnValue` in modifiers, skip `@Override` methods.

## Priority

Medium - the convention is already followed manually but has no enforcement. Would catch omissions on new code.