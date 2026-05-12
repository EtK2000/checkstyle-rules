# Audit Tests Command

Run the `test-coverage-auditor` agent to audit test coverage for: $ARGUMENTS

## Step 1: Resolve scope

### If $ARGUMENTS is non-empty

Expand it into an explicit file set:
- The source file(s) under audit (e.g. `src/main/java/com/etk2000/checkstyle/FooCheck.java`)
- All test files for it (e.g. `src/test/java/com/etk2000/checkstyle/FooCheckTest.java`, plus tier/variant tests if any)
- All input resource files for it (e.g. `src/test/resources/com/etk2000/checkstyle/inputs/foo/Input*.java`)
- Any related fixer + fixer test (under `src/main/java/com/etk2000/checkstyle/gradle/fix/` and its test path)
- Any cross-check counterparts (per testing.md "Cross-check testing")

If the user passed only a check name or partial path, expand it. If you can't unambiguously resolve, ask before invoking.

### If $ARGUMENTS is empty

Propose a default scope from recent work. The user commits files mid-session to track what they've reviewed — **a committed file is NOT an audited file**. Still audit it.

a. Collect candidate files from THREE sources and UNION them:
   1. **Uncommitted changes:** run `git status --porcelain` and `git diff --name-only HEAD`
   2. **Session edits:** recall from THIS conversation every file you edited via Edit/Write/MultiEdit/NotebookEdit. Include files you edited earlier in the session even if the user has since committed them — they still need auditing. If the session has been compressed and your memory is unreliable, say so explicitly when you confirm scope.
   3. **Recent local commits** (safety net): run `git log --name-only --pretty=format: HEAD@{2.hours.ago}..HEAD 2>/dev/null` or similar. Flag results as "possibly from this session" so the user can deselect if they're from a different context.

b. Filter the union to **this audit's scope**:
   - `src/main/java/com/etk2000/checkstyle/*Check.java` — in scope
   - `src/main/java/com/etk2000/checkstyle/gradle/fix/*Fixer.java` — in scope (though primary scope for /audit-security)
   - `src/test/java/com/etk2000/checkstyle/**/*Test.java` — in scope
   - `src/test/resources/com/etk2000/checkstyle/inputs/**/Input*.java` — in scope
   - Shared utils (`AstUtil.java`, `ReflectionUtil.java`, etc.) — in scope
   - Anything else — out of scope, drop it

c. Auto-expand each candidate to its full audit set (check → test + inputs + fixer pair, etc.) as in the non-empty branch.

d. If the filtered set is EMPTY, ask the user directly: "I don't see any recent edits in this audit's scope. Which files should I audit?" Do not invoke the agent.

e. If the filtered set is NON-EMPTY, show it as a numbered list with the source of each entry (uncommitted / session edit / recent commit), for example:
   ```
   Proposed audit scope:
     1. FooCheck.java           [session edit, already committed]
     2. FooCheckTest.java       [session edit, uncommitted]
     3. InputFooClean.java      [session edit, uncommitted]
     4. BarCheck.java           [recent commit — possibly from a different session, confirm]
   Audit these? (y = all, n = cancel, or list numbers / ranges to audit a subset)
   ```
   Wait for explicit approval before proceeding. Default presentation is the full union (option A); the user can trim.

f. If you see files in scope for `/audit-security` (production `*Check.java` or `*Fixer.java`) mention it: "These files also need /audit-security — run that after this." Do not auto-invoke it.

## Step 2: Invoke the agent

Invoke `Agent` with `subagent_type="test-coverage-auditor"`. In the prompt, list every file path explicitly. Do not let the agent guess.

## Step 3: Present the report

- Show me the full report verbatim
- For each HIGH priority gap, propose a concrete plan to add the missing test (file, method name, body sketch)
- For MED gaps, ask if I want them addressed now or noted for later
- For LOW gaps, just list them — no proposals unless I ask

## Step 4: Wait for confirmation

Do NOT start writing test code until I confirm which gaps to address.