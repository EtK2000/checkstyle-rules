#!/usr/bin/env bash
# PreToolUse(Bash) hook: deny destructive operations Claude shouldn't run on its own.
#
# Project rules:
#   * git is whitelisted, not blacklisted. Only read-only inspection plus
#     `git mv` / `git rm` are allowed. Everything else (commit, push, add,
#     fetch, stash, reset, clean, checkout, restore, merge, rebase, pull, ...)
#     is denied. See process_git_invocation below.
#   * Plain `mv` is blocked. Use `git mv` inside the repo; outside the repo
#     this hook has no business moving files.
#
# When blocked, the user can run the command themselves. There is no escape hatch.
#
# Known limitations (the hook is a regex lexer, not a shell parser):
#   * Variable indirection (`c=status; git $c`).
#   * Two-step concealment via filesystem write + read.
#   * Payloads inside $(...) / `...` that the unwrap pass doesn't reach,
#     e.g. `bash -c "$(echo git stash)"` unwraps to `$(echo git stash)`,
#     and the statement-boundary regex won't bridge across `echo`.
#   * Shell keywords hiding commands (`if ...; then git x; fi`).
#   * Inline interpreter payloads beyond the Python deletion calls handled
#     below, e.g. `ruby -e 'File.delete(...)'`, `node -e 'fs.unlinkSync(...)'`,
#     `python -c 'os.rename(...)'`.

set -uo pipefail

command -v jq >/dev/null 2>&1 || exit 0

input=$(cat)
cmd=$(printf '%s' "$input" | jq -r '.tool_input.command // empty')
[[ -z "$cmd" ]] && exit 0

# === Unwrap pass ===
# Pull payloads out of `bash -c '...'`, `node -e "..."`, `python -c '...'`, etc., so
# the quote-strip pass doesn't erase the inner command before regex matching.
# ANSI-C `$'...'` is normalized to `'...'`. One pass only. Nested wrappers don't
# fully unwrap. `eval` is hard-blocked below; not unwrapped here.
unwrapped=$(printf '%s' "$cmd" \
  | sed -E "s/[\$]'/'/g" \
  | sed -E "s/(bash|sh|zsh|dash|ksh)[[:space:]]+-c[[:space:]]+'([^']*)'/\2/g" \
  | sed -E "s/(bash|sh|zsh|dash|ksh)[[:space:]]+-c[[:space:]]+\"([^\"]*)\"/\2/g" \
  | sed -E "s/(node|nodejs)[[:space:]]+-e[[:space:]]+'([^']*)'/\2/g" \
  | sed -E "s/(node|nodejs)[[:space:]]+-e[[:space:]]+\"([^\"]*)\"/\2/g" \
  | sed -E "s/(python|python3|ruby|perl)[[:space:]]+-c[[:space:]]+'([^']*)'/\2/g" \
  | sed -E "s/(python|python3|ruby|perl)[[:space:]]+-c[[:space:]]+\"([^\"]*)\"/\2/g")

# === Quote-strip pass ===
# Replace each quoted string with single placeholder `X` so tokenization downstream
# preserves argument count (flags that take a value still consume one token).
stripped=$(printf '%s' "$unwrapped" | sed -E "s/'[^']*'/X/g" | sed -E 's/"[^"]*"/X/g')

deny() {
  jq -nc --arg reason "$1" '{
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: $reason
    }
  }'
  exit 0
}

has() { printf '%s' "$stripped" | grep -Eq "$1"; }

# ============================================================================
# eval: runs a constructed string the hook can't lex.
# ============================================================================
if has '(^|[^[:alnum:]_-])eval([[:space:]]|$)'; then
  deny "I cannot run 'eval'. It executes a constructed string the hook can't lex."
fi

# ============================================================================
# Shell receiving code from outside the command line.
# ============================================================================
if has '\|[[:space:]]*(bash|sh|zsh|dash|ksh)([[:space:]]|$)' \
   || has '(^|[^[:alnum:]_-])(bash|sh|zsh|dash|ksh)[[:space:]]+([^;|&]*[[:space:]])?(<<|<\()'; then
  deny "I cannot run a shell taking code from a pipe / heredoc / here-string / process substitution. It's a channel the hook can't inspect. Run the intended command directly instead."
fi

# ============================================================================
# --no-verify: fix the underlying check, don't skip it.
# Miss: `-n` short form is intentionally not caught (means different things
# to different subcommands). `git -c core.hooksPath=/dev/null` is covered by
# the git unknown-global-flag deny below.
# ============================================================================
if has '(^|[[:space:]])--no-verify([[:space:]]|$)'; then
  deny "I cannot run with --no-verify. Fix the underlying check rather than skipping it."
fi

# ============================================================================
# Git whitelist.
# Per `git ...` invocation: skip the safe global flags, dispatch on subcommand.
# ============================================================================
process_git_invocation() {
  local invocation="$1"
  local -a tokens
  read -ra tokens <<< "$invocation"
  local i=0 tok subcmd=""
  while ((i < ${#tokens[@]})); do
    tok="${tokens[i]}"
    case "$tok" in
      --no-pager|-p|-P|--paginate|--no-paginate)
        ((i += 1))
        ;;
      -*)
        deny "I cannot run 'git $tok ...'. Only paging flags (--no-pager, --paginate / --no-paginate, -p, -P) are allowed."
        ;;
      *)
        subcmd="$tok"
        break
        ;;
    esac
  done

  # Bare `git` or `git` with only paging flags is harmless, so allow.
  [[ -z "$subcmd" ]] && return

  local -a sub_args=()
  if ((${#tokens[@]} > i + 1)); then
    sub_args=("${tokens[@]:$((i + 1))}")
  fi
  local arg

  case "$subcmd" in
    stash)
      deny "I cannot run 'git stash' (any subcommand). Alternatives: 'git show HEAD:<path>' to read a file's committed version, 'git diff' to see what changed, and file paths / task ordering / 'git log' to judge whether a failure pre-existed."
      ;;
    branch)
      if ((${#sub_args[@]} > 0)); then
        for arg in "${sub_args[@]}"; do
          case "$arg" in
            -d|-D|--delete)
              deny "I cannot delete branches."
              ;;
          esac
        done
      fi
      ;;
    tag)
      if ((${#sub_args[@]} > 0)); then
        for arg in "${sub_args[@]}"; do
          case "$arg" in
            -d|--delete)
              deny "I cannot delete tags."
              ;;
          esac
        done
      fi
      ;;
    reflog)
      if ((${#sub_args[@]} > 0)); then
        case "${sub_args[0]}" in
          expire|delete)
            deny "I cannot run 'git reflog ${sub_args[0]}'."
            ;;
        esac
      fi
      ;;
    worktree)
      if ((${#sub_args[@]} > 0)) && [[ "${sub_args[0]}" != "list" ]]; then
        deny "I cannot run 'git worktree ${sub_args[0]}'. Only 'git worktree list' is allowed."
      fi
      ;;
    status|diff|log|show|blame|ls-files|ls-tree|rev-parse|cat-file|grep|describe|mv|rm|for-each-ref|check-ignore|name-rev|shortlog|verify-commit|verify-tag)
      :  # allowed inspection / pre-approved mutation
      ;;
    *)
      deny "I cannot run 'git $subcmd'. Only relevant read-only inspection (status/diff/log/show/blame/...) and 'git mv'/'git rm' are allowed."
      ;;
  esac
}

# Find each statement-leading `git ...` invocation and dispatch.
while IFS= read -r segment; do
  [[ -z "$segment" ]] && continue
  args=$(printf '%s' "$segment" | sed -E 's/^[^a-zA-Z]*git[[:space:]]+//')
  process_git_invocation "$args"
done < <(printf '%s' "$stripped" | grep -oE "(^|[;|&(\`])[[:space:]]*git[[:space:]]+[^;|&)\`]+" || true)

# ============================================================================
# File deletion: rm / unlink / find / shred / dd / truncate / Python deletion calls.
# ============================================================================
# rm: carve-outs for `git rm`, `npm rm`, `pnpm rm` (index/package removal).
rm_check=$(printf '%s' "$stripped" | sed -E 's/(^|[^[:alnum:]_-])(git|npm|pnpm)[[:space:]]+rm([[:space:]]|$)/\1\2 X /g')
if printf '%s' "$rm_check" | grep -Eq '(^|[^[:alnum:]_-])rm[[:space:]]'; then
  deny "I cannot run 'rm'. Use 'git rm' for tracked files; for untracked files, ask the user to delete them."
fi

if has '(^|[^[:alnum:]_-])unlink[[:space:]]'; then
  deny "I cannot run 'unlink'. Use 'git rm' for tracked files; for untracked files, ask the user to delete them."
fi

if has '(^|[^[:alnum:]_-])find[[:space:]][^;|&]*(-delete([^[:alnum:]_-]|$)|-exec(dir)?[[:space:]]+(rm|unlink)([^[:alnum:]_-]|$))'; then
  deny "I cannot run a find with -delete or -exec/-execdir rm/unlink."
fi

if has '(^|[^[:alnum:]_-])shred[[:space:]]'; then
  deny "I cannot run 'shred'."
fi

if has '(^|[^[:alnum:]_-])dd[[:space:]][^;|&]*of=' \
   && ! has '(^|[^[:alnum:]_-])dd[[:space:]][^;|&]*of=/dev/null([^[:alnum:]_-]|$)'; then
  deny "I cannot run 'dd' with an of= target. It's easy to clobber the wrong device or file."
fi

if has '(^|[^[:alnum:]_-])truncate[[:space:]][^;|&]*-s[[:space:]]+0([^[:alnum:]_-]|$)'; then
  deny "I cannot run 'truncate -s 0'. It zeros out a file's contents."
fi

# Python deletion calls: os.remove / os.removedirs / shutil.rmtree, plus the
# method forms `.unlink(` / `.rmdir(` (os.unlink, pathlib's Path.unlink/rmdir).
# Matched against $unwrapped rather than $stripped, so a payload the unwrap pass
# doesn't reach still gets seen: `python3.13 -c ...`, `python -B -c ...`, and
# heredocs all keep the call text verbatim in the command string.
# The trailing `(` is required, which keeps `grep os.remove foo.py` allowed, and
# `os.` must be preceded by a non-identifier char so `videos.remove(x)` (list
# removal) doesn't trip it.
# Miss: `from os import remove; remove(p)` — bare `remove(` is far too common
# to blacklist.
if printf '%s' "$unwrapped" \
   | grep -Eq '(^|[^[:alnum:]_.])(os\.(remove|removedirs)|shutil\.rmtree)[[:space:]]*\(|\.(unlink|rmdir)[[:space:]]*\('; then
  deny "I cannot delete files from Python (os.remove / os.unlink / os.removedirs / shutil.rmtree / Path.unlink / Path.rmdir). Use 'git rm' for tracked files; for untracked files, ask the user to delete them."
fi

# ============================================================================
# mv: never allowed. Use 'git mv' inside the repo; outside the repo this hook has no
# business moving files.
# ============================================================================
mv_stripped=$(printf '%s' "$stripped" | sed -E 's/(^|[^[:alnum:]_-])git[[:space:]]+mv([[:space:]]|$)/\1git X /g')
if printf '%s' "$mv_stripped" | grep -Eq '(^|[^[:alnum:]_-])mv[[:space:]]'; then
  deny "I cannot run plain 'mv'. Use 'git mv' for renames inside the repo; this hook never moves files outside it."
fi

exit 0