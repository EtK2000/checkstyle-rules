# Deslop Command

Review and fix AI-generated code patterns ("slop") in changed Java files by
dispatching the work to the `deslop-fixer` agent. The agent applies edits
directly without user approval and returns a compact summary, keeping the
main-thread context cost minimal.

## Instructions

1. Resolve the file list. Default base is `HEAD` (uncommitted changes only).
   If `$ARGUMENTS` is non-empty, treat it as a git ref / revision range to
   diff against instead (e.g. `/deslop master` or
   `/deslop origin/master...HEAD`). Use Bash:
   ```
   git diff --name-only ${ARGUMENTS:-HEAD} -- '*.java'
   ```
   Resolve the resulting paths to absolute paths (prepend the repo root) so
   the stamp-audit hook's path regex matches them.
2. If the resulting list is empty, report "deslop-fixer: no Java files in
   diff" and stop. Do not invoke the agent.
3. Otherwise, invoke the `deslop-fixer` agent. The agent reads the patterns
   spec further down in this file, applies edits via the `Edit` tool, and
   returns a compact summary. **Do not pre-read or pre-analyze the files
   yourself** — the whole point of dispatching to an agent is to keep file
   reads out of main context.

   Agent invocation:
   ```
   Tool: Agent
   subagent_type: deslop-fixer
   description: "Deslop sweep on changed Java files"
   prompt: |
     Apply the deslop patterns in .claude/commands/deslop.md to the
     following files. Edit them directly via the Edit tool. Return the
     compact summary specified in your agent definition.

     Files:
       <absolute-path-1>
       <absolute-path-2>
       ...
   ```
4. Print the agent's summary verbatim. Do not paraphrase, do not re-list
   files, do not add narration. Then end the turn.

## How auto-invocation interacts with manual /deslop

The `audit-reminder` Stop hook also requests a deslop sweep automatically
when Claude has edited deslop-eligible files in the session. Manual
`/deslop` invocation and stop-hook-driven invocation use the same agent.
The pre-edit hash snapshot taken by `audit-gate.py` ensures coverage and
security audit stamps are *promoted* (not invalidated) when deslop edits a
file that was already at a stamped state — files that did not need an
audit before `/deslop` ran will not need one after.

The agent-spec sections below (Patterns, Test resource handling, Do NOT,
Output) are the spec the `deslop-fixer` agent reads. Do not invoke them
from the main turn; the agent reads them itself.

### Patterns to fix

Focus only on patterns the project's own checkstyle config does NOT catch. Running `./gradlew check` (or `./gradlew checkstyleFixAll`) handles formatting, modifier order, annotation sort, field/method sorting, `var` preference, redundant `this`, brace rules, `final` on parameters, etc. Don't duplicate that work here.

**Default stance for every pattern below: look for an excuse to remove, not an excuse to keep.** The evaluation question is "what would the strictest reader cut?" not "what scrap of context could justify keeping this?" A comment that adds only a sliver of internal-mechanism detail is slop, not "useful context."

- **Slop comments** — A comment is slop if the same information is already conveyed by any combination of: method name, variable/parameter names, Javadoc, test name, class name, or surrounding code structure. This goes beyond verbatim restatement of the next line. Common missed patterns:
  - Section markers in tests that restate fixture method names (`// fooBarCase` above assertions for `fooBarCase`). Line numbers already map to the fixture.
  - Section headers in parsing/scanning code (`// find X`, `// scan past Y`, `// replace Z with W`) when the code is a standard pattern (indexOf, identifier scan, depth-tracking loop) and the method name/Javadoc already describe the intent.
  - "Why it's clean/not flagged" comments in test fixtures when the method name already says the same thing (`// no initializer` in `localVariableNoInit`).
  - Comments that restate the method's Javadoc (`// only fix if type is var` when the Javadoc says "when the declared type is var").
  - Comments that restate the test name and assertion (`// should return X` when the test is `testReturnsX` and the assertion checks X).
  - **"Why" comments where the why is already in the method name** (`// !! cancels — assertTrue stays positive` above a test named `doubleNegationCancelsToPositive`). A "why" comment is only valuable when the why is non-obvious; if the name already conveys it, the comment is restatement.
  - **Implementation narration disguised as rationale** (`// X is found by scanForwardForChar after skipping the block comment`, `// parseImport strips trailing comments before the regex match`). These describe the production code's internal mechanism; they belong in that code's Javadoc, not echoed in the test or fixture. The behavioral contract is already conveyed by the test/fixture's name + data + assertion.
  - **Fixture comments elaborating which check branch is exercised** (`// receiver simple name resolves to neither Assert nor Assertions; falls to import-rule branch`). The fixture's job is to exemplify the input shape, not to narrate which internal branch handles it.
  Keep genuine TODO/FIXME, explanations of non-obvious logic (e.g. why a guard exists, AST structure descriptions, why code is ordered a certain way), and comments that explain what dense inline test strings represent. Don't touch pre-existing comments on code you didn't change.
- **Excessive null checks on `@NonNull` params** — If a method parameter is annotated `@NonNull` (or inferred so by project convention), remove defensive `if (param == null)` checks at the top. The annotation is the contract.
- **Swallowed exceptions / redundant try-catch** — `try { foo(); } catch (Exception e) { throw new RuntimeException(e); }` that adds no value. Rethrow directly, or let the original propagate if the signature allows.
- **Helper methods for one-time use** — Per `CLAUDE.md`: "Don't create helpers, utilities, or abstractions for one-time operations." If a private method is called from exactly one place and isn't significantly clearer than the inline form, inline it.
- **Redundant intermediate variables** — `var x = foo(); return x;` → `return foo();` when the name adds nothing.
- **Over-generic names** — Variables/params named `param`, `value`, `data`, `result`, `tmp` when a specific domain name fits. Pick the specific name (e.g. `violations`, `detailAST`, `tokenType`).
- **Verbose assertion messages that duplicate the test name** — Per `CLAUDE.md` and `docs/testing.md`: "Only add messages to assertions when they provide non-obvious context. Don't add messages when the test name already describes the expected behavior." Remove `assertEquals("list should be empty", 0, list.size())` when the test is named `testEmptyOnFreshInstance`.
- **Stray logging / debug output** — `System.out.println`, `System.err.println`, or logger calls that weren't part of the intended change. Remove unless the code legitimately logs (checks generally don't).
- **Suppressions added without asking** — `@SuppressWarnings("...")`, `@SuppressLint("...")`, `NOPMD` comments. Per global rules these require explicit permission. If you find any added by AI, remove them and surface the underlying issue to the user instead of silently suppressing.
- **Style drift not covered by checkstyle** — patterns the config can't express. Example: if sibling tests use `try (MockedStatic<T> mock = Mockito.mockStatic(...))` and one uses `MockedStatic<T> mock = ...; try { ... } finally { mock.close(); }`, align. Skip anything that's already a checkstyle rule.
- **Emdashes in code/comments** — Per writing style rule: no emdashes. Replace with commas, periods, or restructure.

### Test resource handling

Test resource files under `src/test/resources/` (typically `.../inputs/<dir>/Input*.java`) are in scope for deslop — Claude adds slop comments to them too, and leaving slop there means the fixtures drift from the project's style. But they have three hard constraints:

1. **`// violation: <message>` and `// violation (warning): <message>` markers must be preserved verbatim.** These are what the test framework asserts against. Never remove, rename, paraphrase, or merge them.
2. **Line numbers of violation-bearing lines must not shift**, OR you must update the corresponding test's line assertions to match. Tests typically assert `assertEquals(<line>, violations.get(N).getLine())`. If you remove a slop comment above a violation line, the violation shifts up by one, breaking the assertion.
3. **Intentional anti-patterns must not be "fixed".** The file exists because it contains code the check should flag. If you see wrong-order modifiers, missing annotations, unsorted methods, etc. in an `Input*Violation.java` file, that's the point — leave them alone. Clean fixtures (`Input*Clean.java`) follow project style and anti-patterns there are real slop.

**Rules of thumb:**

- **Comments BELOW the last `// violation` line are always safe to remove.** They can't shift violation lines.
- **Comments ABOVE a `// violation` line require either (a) skipping the removal or (b) updating the corresponding test file's line assertions.** Default to (b) when the math is mechanical: count the comment lines you're deleting and shift each affected assertion by that count. Use (a) only when the test's line assertions are spread across many methods/files you can't easily locate, or when the same fixture is referenced by multiple tests. "Risk of breaking the test" is not a reason to skip — running `./gradlew check` after will verify the math. If you do (b), update every affected assertion in the same edit — do not leave partial shifts.
- **Headers like `// This file tests the X case` at the top of a fixture**: remove if they're pure slop (restate what the filename already says), keep if they provide genuinely useful context for an unusual fixture setup.
- **Inline comments inside methods/classes** of a fixture are almost always slop. Apply the line-shift rule.
- **"Unsure" means actually unsure, not borderline.** If you can't tell whether a comment is an asserted test marker or an unusual fixture annotation, leave it and surface to the user. Don't use this rule as an escape hatch for borderline-slop cases where the comment merely adds an internal-mechanism scrap — apply the strict reading and remove it.

**Process for test resources specifically:**
1. Identify all `// violation` lines in the file and their line numbers.
2. Find the corresponding test file (typically `src/test/java/.../<CheckName>Test.java` — same basename with `Test.java` suffix one level up).
3. Note which line numbers are asserted in that test.
4. Remove slop comments only in positions where removal either (a) doesn't shift any asserted line, or (b) shifts asserted lines in a predictable way AND you update the test's assertions to match.
5. After editing, the set of violations produced by the check on the fixture, and their line numbers, should match what the test expects.
6. **If you modified any `Input*Violation.java` (or any other fixture with `// violation` markers), you MUST run `./gradlew check` after editing and fix every test that fails as a result.** A failed line-number assertion is the expected signal that your shift math was off; debug the count of comment lines removed and update the assertions to match. Don't declare done with a broken test "to be fixed later."

### Do NOT

- Do not "fix" intentional anti-patterns in `Input*Violation.java` files — those are the subject of the test.
- Do not remove or edit `// violation` markers.
- Do not make changes outside the scope of slop cleanup (no refactoring "while I'm here", no new features, no silent bug fixes). If you spot a real bug, mention it to the user instead.
- Do not add tests, docs, or comments. Deslop only removes or restyles — it doesn't add.
- Do not delete code you don't understand. If something looks weird but you can't prove it's slop, leave it and flag it to the user.

## Output

The `deslop-fixer` agent produces a compact summary in the format
specified in its agent definition (`.claude/agents/deslop-fixer.md`). The
main turn prints that summary verbatim and ends.

**Always remind the user** to run `./gradlew check` after the sweep — a
wrong line-shift update on test resources will produce a failing line-
number assertion. This reminder is the one piece of text the main turn
adds beyond the agent's summary.