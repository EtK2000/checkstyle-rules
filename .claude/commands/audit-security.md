# Audit Security Command

Run the `security-auditor` agent to audit robustness / hostile-input safety for: $ARGUMENTS

## Step 1: Resolve scope

### If $ARGUMENTS is non-empty

Expand it into an explicit file set:
- The source file(s) under audit (e.g. `src/main/java/com/etk2000/checkstyle/FooCheck.java`, `src/main/java/com/etk2000/checkstyle/gradle/fix/FooFixer.java`)
- Any utilities the source calls into (e.g. `AstUtil.java`, `ReflectionUtil.java`) if relevant to the audit domain
- Any fixer the source pairs with (check → fixer, or fixer → check, since column mapping / cross-fixer interference spans both)

If the user passed only a name or partial path, expand it. If you can't unambiguously resolve, ask before invoking.

### If $ARGUMENTS is empty

Propose a default scope from recent work. The user commits files mid-session to track what they've reviewed — **a committed file is NOT an audited file**. Still audit it.

a. Collect candidate files from THREE sources and UNION them:
   1. **Uncommitted changes:** run `git status --porcelain` and `git diff --name-only HEAD`
   2. **Session edits:** recall from THIS conversation every file you edited via Edit/Write/MultiEdit/NotebookEdit. Include files you edited earlier in the session even if the user has since committed them — they still need auditing. If the session has been compressed and your memory is unreliable, say so explicitly when you confirm scope.
   3. **Recent local commits** (safety net): run `git log --name-only --pretty=format: HEAD@{2.hours.ago}..HEAD 2>/dev/null` or similar. Flag results as "possibly from this session" so the user can deselect if they're from a different context.

b. Filter the union to **this audit's scope** (production source only — tests and fixtures don't need security review):
   - `src/main/java/com/etk2000/checkstyle/*Check.java` — in scope
   - `src/main/java/com/etk2000/checkstyle/gradle/fix/*Fixer.java` — in scope (highest priority — fixers can corrupt user code)
   - Shared production utils (`AstUtil.java`, `ReflectionUtil.java`, `CheckstyleFixTask.java`, `CheckstyleFixAction.java`, `CheckstyleFixer.java`, `AnnotationFixerUtil.java`, `FixResult.java`, `FixableCheckNames.java`) — in scope
   - Anything under `src/test/` — out of scope, drop it
   - Test fixtures (`Input*.java`) — out of scope, drop it
   - Anything else — out of scope

c. Auto-expand each candidate to include its natural companions (check ↔ paired fixer; source file + utils it calls into) so the agent has full context.

d. If the filtered set is EMPTY, ask the user directly: "I don't see any production-source edits in this audit's scope. Which files should I audit?" Do not invoke the agent.

e. If the filtered set is NON-EMPTY, show it as a numbered list with the source of each entry (uncommitted / session edit / recent commit), for example:
   ```
   Proposed audit scope:
     1. FooFixer.java           [session edit, already committed]
     2. FooCheck.java           [session edit, uncommitted]
     3. AstUtil.java            [recent commit — possibly from a different session, confirm]
   Audit these? (y = all, n = cancel, or list numbers / ranges to audit a subset)
   ```
   Wait for explicit approval before proceeding. Default presentation is the full union (option A); the user can trim.

## Step 2: Invoke the agent

Invoke `Agent` with `subagent_type="security-auditor"`. In the prompt, list every file path explicitly and include any known context (e.g. "this fixer was recently extended to handle multiline input — focus there"). Do not let the agent guess scope.

3. When the agent returns its report:
   - Show me the full report verbatim
   - For each HIGH finding, propose a concrete code fix (file:line diff sketch). Do NOT apply the fix yet.
   - For MEDIUM findings, ask which I want addressed now
   - For LOW findings, list them — no action unless I ask
   - If the report says CLEAN, summarize what was audited (so I can verify the scope was right)

4. If the report has a "Verification needed" section:
   - Each entry is an empirical check the agent could not resolve statically (typically regex backtracking, fixer oscillation, or runtime matcher behavior)
   - Offer to run each verification item yourself — for regex checks, this means writing a small standalone Java program (or a one-off JUnit test) that executes the exact pattern against the exact input with a timeout, then reporting pass/fail
   - For each verification you run, feed the result back into the audit: if it fails, re-invoke the agent with the new evidence so it can promote the item to a proper finding
   - Ask me before writing these verification programs — I may want to defer some, run them differently, or accept a verification as a known risk

5. Do NOT start applying fixes until I confirm which findings to address. After fixes are applied, remind me that `test-coverage-auditor` should verify regression tests exist for each fixed issue.