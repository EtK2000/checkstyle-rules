# checkstyle-rules

> **Warning:** This project is vibecoded slop. Proceed with caution.

A Gradle plugin that bundles custom checkstyle checks and configuration. Apply one plugin and get
all rules, no per-module boilerplate.

## Usage

```gradle
plugins {
    id 'com.etk2000.checkstyle' version 'VERSION'
}
```

This automatically:

- Applies the built-in `checkstyle` plugin
- Configures checkstyle with the bundled rules
- Registers `checkstyleMain` and `checkstyleTest` tasks
- Hooks both tasks into `check`

## Suppressions

To suppress specific rules per project, create `config/checkstyle/suppressions.xml` in your project
root. It will be picked up automatically.

## Included checks

### Custom checks (AST-based)

| Check                              | Description                                                                                                                              |
|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| AnnotationAlphabeticalOrderCheck   | Annotations must be sorted alphabetically                                                                                                |
| ClassStructureOrderCheck           | Class members must follow section ordering (inner types, static fields, static methods, instance fields, constructors, instance methods) |
| ControlFlowBracesCheck             | No one-liners, no unnecessary braces on single-line bodies, braces required on multi-line bodies                                         |
| MultilineCallFormattingCheck       | Multiline call/signature formatting with ternary and inline block exceptions                                                             |
| NoArrayTrailingCommaCheck          | No trailing comma in array initializers                                                                                                  |
| NoBlankLineBetweenSingleCasesCheck | No blank lines between consecutive single-line switch cases                                                                              |
| NoCaseBracesCheck                  | No unnecessary braces in case blocks, braces required when a variable is defined                                                         |
| NoUnnecessaryThisCheck             | No `this.` unless shadowing or in field assignment                                                                                       |
| OverloadMethodOrderCheck           | Overloaded methods ordered by ascending parameter count                                                                                  |
| PreferPrefixIncrementCheck         | Use `++i`/`--i` instead of `i++`/`i--`                                                                                                   |
| ThreadAnnotationCheck              | Top-level classes must have a thread annotation                                                                                          |

### Regex rules

- No space indentation (use tabs)
- No blank line after class opening brace
- No blank line before closing brace
- No double blank lines
- Blank line after `break;` before next case
- No final method parameters
- No trailing whitespace
- No trailing newline at end of file

### Built-in checkstyle checks

UnusedImports, EmptyLineSeparator, RightCurly (else/catch/finally on own line),
NoEnumTrailingComma, FinalLocalVariable, AnnotationLocation