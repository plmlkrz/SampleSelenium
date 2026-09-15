"""Multi-device Git sync preflight for work that moves between machines and harnesses.

Work on this repository moves between a desktop, a laptop, a phone session into the desktop,
and between Claude Code and Codex. Git is the only channel between them, so a collision
happens in one of four ways: a session starts from a stale base, the other device pushed to
the same branch, a device finished without pushing, or a binary file (Git LFS or otherwise) is
edited on two devices (binaries cannot merge; the later commit silently wins).

``--start`` (default) runs when a session begins. It fetches, then reports every one of those
conditions. With ``--fix`` it also fast-forwards, and only when that is provably lossless: a
clean tree and no local-only commits. It never rebases, resets, stashes, commits, or pushes.

``--end`` runs before leaving a device. It reports uncommitted work and unpushed commits and
prints the exact commands to hand the branch to the next device.

``--hook`` is ``--start --fix`` for harness SessionStart hooks: compact output, always exit 0
so a network failure never blocks a session.

This script is deliberately stdlib-only and repository-agnostic so the same file can be
copied verbatim into every repository that shares the multi-device workflow.

Exit status (non-hook): 0 in sync, 1 action or warning, 2 stop before editing.
"""

from __future__ import annotations

import argparse
import subprocess
import sys
from dataclasses import dataclass, field
from pathlib import Path

FETCH_TIMEOUT_SECONDS = 25
RECOMMENDED_CONFIG = {
    "fetch.prune": "true",
    "pull.ff": "only",
    "push.autoSetupRemote": "true",
}


@dataclass
class Report:
    lines: list[tuple[str, str]] = field(default_factory=list)

    def add(self, level: str, message: str) -> None:
        self.lines.append((level, message))

    def has(self, level: str) -> bool:
        return any(entry_level == level for entry_level, _ in self.lines)

    def exit_code(self) -> int:
        if self.has("STOP"):
            return 2
        if self.has("ACTION") or self.has("WARN"):
            return 1
        return 0


def _git(repo: Path, *args: str, timeout: float | None = None) -> subprocess.CompletedProcess:
    return subprocess.run(
        ["git", *args],
        cwd=repo,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        timeout=timeout,
        check=False,
    )


def _out(repo: Path, *args: str) -> str | None:
    result = _git(repo, *args)
    return result.stdout.strip() if result.returncode == 0 else None


def _count(repo: Path, revision_range: str) -> int:
    value = _out(repo, "rev-list", "--count", revision_range)
    return int(value) if value and value.isdigit() else 0


def _ref_exists(repo: Path, ref: str) -> bool:
    return _git(repo, "rev-parse", "--verify", "--quiet", ref).returncode == 0


def _dirty_paths(repo: Path) -> list[str]:
    # Not _out(): stripping would eat the leading status column of the first entry.
    status = _git(repo, "status", "--porcelain", "--untracked-files=no").stdout
    paths = []
    for line in status.splitlines():
        if len(line) < 4:
            continue
        path = line[3:]
        if " -> " in path:
            path = path.split(" -> ", 1)[1]
        paths.append(path.strip('"'))
    return paths


def _changed_paths(repo: Path, base: str, head: str) -> set[str]:
    listing = _out(repo, "diff", "--name-only", f"{base}...{head}") or ""
    return {line for line in listing.splitlines() if line}


def _binary_paths(repo: Path, paths: list[str]) -> list[str]:
    """Dirty paths Git cannot merge: LFS-filtered files and files Git diffs as binary."""
    if not paths:
        return []
    attrs = _git(repo, "check-attr", "filter", "--", *paths).stdout
    found = {
        line.split(": filter: ", 1)[0]
        for line in attrs.splitlines()
        if line.endswith(": filter: lfs")
    }
    numstat = _git(repo, "diff", "--numstat", "HEAD", "--", *paths).stdout
    for line in numstat.splitlines():
        parts = line.split("\t", 2)
        if len(parts) == 3 and parts[0] == "-" and parts[1] == "-":
            found.add(parts[2])
    return sorted(found)


def _upstream(repo: Path, branch: str, remote: str) -> str | None:
    configured = _out(repo, "rev-parse", "--abbrev-ref", "--symbolic-full-name", "@{u}")
    if configured:
        return configured
    candidate = f"{remote}/{branch}"
    return candidate if _ref_exists(repo, f"refs/remotes/{candidate}") else None


def _fast_forward(repo: Path, target: str, report: Report, reason: str) -> bool:
    result = _git(repo, "merge", "--ff-only", "--quiet", target)
    if result.returncode == 0:
        report.add("FIXED", f"Fast-forwarded to {target} ({reason}).")
        return True
    report.add("WARN", f"Fast-forward to {target} failed: {result.stderr.strip()}")
    return False


def _preamble(repo: Path, remote: str, fetch: bool, report: Report) -> str | None:
    if _out(repo, "rev-parse", "--is-inside-work-tree") != "true":
        report.add("STOP", f"Not a Git work tree: {repo}")
        return None
    if fetch:
        try:
            result = _git(repo, "fetch", "--prune", "--quiet", remote, timeout=FETCH_TIMEOUT_SECONDS)
            if result.returncode != 0:
                report.add("WARN", f"git fetch {remote} failed; remote state below may be stale: "
                           f"{result.stderr.strip()[:200]}")
        except subprocess.TimeoutExpired:
            report.add("WARN", f"git fetch {remote} timed out; remote state below may be stale.")
    branch = _out(repo, "symbolic-ref", "--quiet", "--short", "HEAD")
    if not branch:
        report.add("WARN", "Detached HEAD: create a branch before editing so work can be pushed.")
    return branch


def check_start(repo: Path, remote: str, base: str, fetch: bool, fix: bool) -> Report:
    report = Report()
    branch = _preamble(repo, remote, fetch, report)
    if report.has("STOP") or not branch:
        return report

    dirty = _dirty_paths(repo)
    upstream = _upstream(repo, branch, remote)

    # 1. The other device pushed to this same branch.
    if upstream:
        behind = _count(repo, f"HEAD..{upstream}")
        ahead = _count(repo, f"{upstream}..HEAD")
        if behind and ahead:
            report.add("STOP", f"'{branch}' has diverged from {upstream} ({ahead} local-only, "
                       f"{behind} remote-only commits): another device worked on this branch. "
                       f"Resolve with `git pull --rebase` before editing.")
        elif behind and dirty:
            report.add("STOP", f"{upstream} has {behind} commit(s) you do not have and this tree "
                       f"has uncommitted edits. Commit them, then `git pull --rebase`.")
        elif behind:
            if not (fix and _fast_forward(repo, upstream, report, "another device pushed this branch")):
                report.add("ACTION", f"{upstream} has {behind} new commit(s): `git pull --ff-only`.")
        elif ahead:
            report.add("ACTION", f"{ahead} local commit(s) not on {upstream}: push them "
                       f"(`git push`) so the other device sees them.")
    elif _count(repo, f"{base}..HEAD"):
        report.add("ACTION", f"'{branch}' has commits but no remote branch: "
                   f"`git push -u {remote} {branch}`.")

    # 2. The branch base is behind the integration branch.
    if _ref_exists(repo, base) and branch != base.split("/", 1)[-1]:
        behind_base = _count(repo, f"HEAD..{base}")
        own_commits = _count(repo, f"{base}..HEAD")
        if behind_base and not own_commits and not dirty and not report.has("STOP"):
            if not (fix and _fast_forward(repo, base, report, "branch has no commits of its own yet")):
                report.add("ACTION", f"Branch starts {behind_base} commit(s) behind {base}: "
                           f"`git merge --ff-only {base}` before editing.")
        elif behind_base:
            overlap = sorted(
                _changed_paths(repo, "HEAD", base)
                & (_changed_paths(repo, base, "HEAD") | set(dirty))
            )
            message = f"'{branch}' is {behind_base} commit(s) behind {base}."
            if overlap:
                shown = ", ".join(overlap[:8]) + (" ..." if len(overlap) > 8 else "")
                report.add("WARN", f"{message} Files changed on both sides: {shown}. "
                           f"Merge {base} in now, while the conflict is small.")
            else:
                report.add("INFO", f"{message} No overlapping files yet.")

    # 3. Binaries (LFS or not) cannot be merged; the later commit silently replaces the earlier.
    dirty_binaries = _binary_paths(repo, dirty)
    if dirty_binaries:
        moved_upstream = set()
        if _ref_exists(repo, base):
            moved_upstream = _changed_paths(repo, "HEAD", base) & set(dirty_binaries)
        for path in dirty_binaries:
            if path in moved_upstream:
                report.add("STOP", f"Binary {path} is modified here AND changed on {base}. "
                           f"Committing it would silently discard the other copy's data.")
            else:
                report.add("WARN", f"Binary {path} has local edits. Binaries cannot merge: "
                           f"commit it from one device only, and verify its contents first.")

    # 4. Per-machine defaults that make a forgotten pull fail loudly instead of merging.
    missing = [key for key, value in RECOMMENDED_CONFIG.items()
               if (_out(repo, "config", "--get", key) or "").lower() != value.lower()]
    if missing:
        commands = "; ".join(f"git config --global {key} {RECOMMENDED_CONFIG[key]}" for key in missing)
        report.add("INFO", f"Recommended per-machine Git settings missing: {commands}")

    if not any(level in {"STOP", "ACTION", "WARN"} for level, _ in report.lines):
        report.add("OK", f"'{branch}' is in sync with {upstream or base}.")
    return report


def check_end(repo: Path, remote: str, base: str, fetch: bool) -> Report:
    report = Report()
    branch = _preamble(repo, remote, fetch, report)
    if report.has("STOP") or not branch:
        return report

    dirty = _dirty_paths(repo)
    untracked = (_out(repo, "ls-files", "--others", "--exclude-standard") or "").splitlines()
    if dirty or untracked:
        report.add("ACTION", f"{len(dirty)} modified and {len(untracked)} untracked file(s) exist only "
                   f"on this device. Commit them (a WIP commit is fine): "
                   f"`git add -A && git commit -m \"WIP: <what is next>\"`.")
    for path in _binary_paths(repo, dirty):
        report.add("WARN", f"Binary {path} is modified. Commit it only if this device owns "
                   f"that data change; otherwise restore it before leaving.")

    upstream = _upstream(repo, branch, remote)
    if upstream:
        ahead = _count(repo, f"{upstream}..HEAD")
        behind = _count(repo, f"HEAD..{upstream}")
        if behind:
            report.add("STOP", f"{upstream} has {behind} commit(s) you do not have: "
                       f"`git pull --rebase` before pushing.")
        if ahead:
            report.add("ACTION", f"{ahead} commit(s) not pushed: `git push`.")
    elif _count(repo, f"{base}..HEAD") or dirty or untracked:
        report.add("ACTION", f"'{branch}' does not exist on {remote}: `git push -u {remote} {branch}`.")

    if not any(level in {"STOP", "ACTION", "WARN"} for level, _ in report.lines):
        report.add("OK", f"'{branch}' is committed and pushed; safe to continue on another device.")
    else:
        report.add("INFO", f"On the next device: `git fetch && git switch {branch} && git pull --ff-only`.")
    return report


def render(report: Report, mode: str, hook: bool) -> str:
    header = f"SYNC PREFLIGHT ({mode})"
    body = [f"[{level}] {message}" for level, message in report.lines
            if not (hook and level == "INFO" and "Recommended per-machine" in message)]
    if hook and report.exit_code():
        body.append("Tell the user about any STOP/ACTION/WARN line above before editing files.")
    return "\n".join([header, *body])


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    mode = parser.add_mutually_exclusive_group()
    mode.add_argument("--start", action="store_true", help="session start check (default)")
    mode.add_argument("--end", action="store_true", help="leaving-this-device check")
    parser.add_argument("--hook", action="store_true", help="SessionStart hook: --start --fix, exit 0")
    parser.add_argument("--fix", action="store_true", help="fast-forward when provably lossless")
    parser.add_argument("--no-fetch", action="store_true", help="skip git fetch")
    parser.add_argument("--remote", default="origin")
    parser.add_argument("--base", default=None, help="integration branch (default <remote>/main)")
    parser.add_argument("--repo", type=Path, default=Path.cwd())
    args = parser.parse_args(argv)

    base = args.base or f"{args.remote}/main"
    fetch = not args.no_fetch
    try:
        if args.end:
            report = check_end(args.repo, args.remote, base, fetch)
            mode_name = "end"
        else:
            report = check_start(args.repo, args.remote, base, fetch, fix=args.fix or args.hook)
            mode_name = "start"
    except (OSError, subprocess.SubprocessError) as exc:
        if args.hook:
            print(f"SYNC PREFLIGHT skipped: {exc}")
            return 0
        raise

    print(render(report, mode_name, args.hook))
    return 0 if args.hook else report.exit_code()


if __name__ == "__main__":
    sys.exit(main())
