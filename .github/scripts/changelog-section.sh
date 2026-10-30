#!/usr/bin/env bash
# Prints the body of the "## [<version>]" section of a Keep a Changelog file.
# Fails when the section is missing or empty, so a release cannot be published without notes.
set -euo pipefail

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "usage: $0 <version> [changelog-file]" >&2
  exit 2
fi

version="$1"
changelog="${2:-CHANGELOG.md}"

if [[ ! -f "$changelog" ]]; then
  echo "error: $changelog not found" >&2
  exit 1
fi

section="$(awk -v header="## [${version}]" '
  index($0, "## [") == 1 {
    if (found) exit
    if (substr($0, 1, length(header)) == header) { found = 1; next }
  }
  # Skip the link reference definitions that close the file (e.g. "[1.0.0]: https://...").
  found && $0 !~ /^\[[^]]+\]: / { print }
' "$changelog" | sed -e '/./,$!d' | sed -e ':a' -e '/^\n*$/{$d;N;ba' -e '}')"

if [[ -z "${section//[[:space:]]/}" ]]; then
  echo "error: $changelog has no non-empty '## [${version}]' section" >&2
  exit 1
fi

printf '%s\n' "$section"
