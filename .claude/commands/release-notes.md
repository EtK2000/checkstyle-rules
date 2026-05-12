# Release Notes Command

Generate release notes for the next version based on all changes since the last pushed release tag.

## Step 1: Learn the format

Fetch the body of **every** GitHub release for this repo:

```
gh release list --json tagName -q '.[].tagName'
```

Then for each tag:

```
gh release view <tag> --json body -q .body
```

Read all of them. Identify the recurring structure, section headers, bullet style, bold patterns,
and the footer format. Use this as the template for the new notes.

## Step 2: Identify the baseline

Find the most recent pushed release tag:

```
git tag --sort=-v:refname | head -1
```

This is the **baseline tag**. The new release covers everything after it.

## Step 3: Gather changes

Collect ALL local changes since the baseline tag:

1. **Commits**: `git log <baseline>..HEAD --oneline`
2. **Full diffs**: `git diff <baseline>..HEAD --stat` for an overview, then
   `git diff <baseline>..HEAD` for details
3. **Changed files**: `git diff <baseline>..HEAD --name-only`

Read the full diff and every changed source file to understand what actually changed. Do not rely
solely on commit messages. Commit messages are written by an AI that may phrase things generically.
Read the code to understand what each change really does.

## Step 4: Categorize changes

Map every change to the appropriate section from the format you learned in Step 1. Typical sections:

- **New checks**: entirely new `*Check.java` files
- **Enhanced checks**: modifications to existing checks (new features, edge case fixes, expanded
  coverage)
- **New built-in checks enabled**: new entries added to the checkstyle XML config for upstream
  checks
- **Infrastructure**: build system, dependencies, plugin changes, test tooling, utilities
- **Refactoring**: internal restructuring, code quality improvements

Only include sections that have content. Do not invent empty sections.

## Step 5: Write the notes

For each item:

- Lead with **CheckName** in bold, followed by a colon and a concise description
- For enhanced checks, describe what changed (e.g. "now supports X", "fixed Y", "added Z")
- Use the same voice and level of technical detail as the existing releases
- Include a `---` before the footer
- End with the full changelog link:
  `**Full Changelog**: https://github.com/EtK2000/checkstyle-rules/compare/<baseline>...<new_tag>`
- For the new tag, infer the next version by bumping the minor version (e.g. 1.3.0 -> 1.4.0). If
  $ARGUMENTS contains a version, use that instead

## Step 6: Output

Output the release notes as a single markdown block. Do not wrap it in a code fence. Just print the
raw markdown so the user can run /copy on it directly.

If $ARGUMENTS is non-empty and is not a version number, treat it as additional instructions for
what to emphasize or include.