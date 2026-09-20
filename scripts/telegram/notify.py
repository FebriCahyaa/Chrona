#!/usr/bin/env python3
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

"""Render Chrona CI, PR, Dependabot and release events for Telegram."""

from __future__ import annotations

import argparse
import html
import json
import os
import urllib.error
import urllib.parse
import urllib.request
from pathlib import Path

MAX_MESSAGE = 4096


def esc(value: object) -> str:
    return html.escape(str(value or ""), quote=False)


def github_get(url: str, token: str | None) -> dict:
    headers = {
        "Accept": "application/vnd.github+json",
        "User-Agent": "Chrona-Telegram",
    }
    if token:
        headers["Authorization"] = f"Bearer {token}"
    request = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(request, timeout=20) as response:
        return json.load(response)


def load_event() -> dict:
    path = Path(os.environ.get("GITHUB_EVENT_PATH", ""))
    if not path.is_file():
        raise SystemExit("GITHUB_EVENT_PATH is not available")
    return json.loads(path.read_text(encoding="utf-8"))


def commit_details(repo: str, sha: str, token: str | None) -> tuple[str, str, str, list[str]]:
    if not sha:
        return "unknown", "(no commit)", "unknown", []
    try:
        data = github_get(
            f"https://api.github.com/repos/{repo}/commits/{urllib.parse.quote(sha, safe='')}",
            token,
        )
        commit = data.get("commit", {})
        title = (commit.get("message") or "(no commit)").splitlines()[0]
        author = (
            (commit.get("author") or {}).get("name")
            or (data.get("author") or {}).get("login")
            or "unknown"
        )
        parents = [item.get("sha", "") for item in data.get("parents", [])]
        return sha[:12], title, author, parents
    except Exception:
        return sha[:12], "(commit metadata unavailable)", "unknown", []


def compact_changelog(repo: str, sha: str, parents: list[str], token: str | None) -> list[str]:
    if not sha or not parents:
        return []
    try:
        data = github_get(
            f"https://api.github.com/repos/{repo}/compare/"
            f"{urllib.parse.quote(parents[0], safe='')}..."
            f"{urllib.parse.quote(sha, safe='')}",
            token,
        )
        result = []
        for item in data.get("commits", [])[-6:]:
            title = (
                ((item.get("commit") or {}).get("message") or "(no commit)")
                .splitlines()[0]
            )
            result.append(f"• {esc(title)}")
        return result
    except Exception:
        return []


def build_ci(event: dict, repo: str, token: str | None) -> str:
    if "workflow_run" in event:
        run = event["workflow_run"]
        status = run.get("conclusion") or run.get("status") or "unknown"
        workflow = run.get("name", "unknown")
        event_name = run.get("event", "unknown")
        branch = run.get("head_branch", "unknown")
        sha_input = run.get("head_sha", "")
        run_url = run.get("html_url", "")
    else:
        run = event
        status = "pushed" if event.get("ref") else "manual"
        workflow = os.environ.get("GITHUB_WORKFLOW", "Chrona Update Commit")
        event_name = os.environ.get("GITHUB_EVENT_NAME", "unknown")
        branch = os.environ.get("GITHUB_REF_NAME") or event.get("ref", "unknown").rsplit("/", 1)[-1]
        sha_input = event.get("after") or os.environ.get("GITHUB_SHA", "")
        run_url = (
            f"{os.environ.get('GITHUB_SERVER_URL', 'https://github.com')}/"
            f"{repo}/actions/runs/{os.environ.get('GITHUB_RUN_ID', '')}"
        )

    sha, title, author, parents = commit_details(repo, sha_input, token)
    changes = compact_changelog(repo, sha_input, parents, token) or [f"• {esc(title)}"]
    icon = (
        "✅" if status in {"success", "pushed"}
        else "❌" if status in {"failure", "cancelled", "timed_out"}
        else "⏳"
    )
    return (
        f"{icon} <b>CHRONA UPDATE COMMIT</b>\n\n"
        f"<b>Workflow:</b> {esc(workflow)}\n"
        f"<b>Status:</b> {esc(str(status).upper())}\n"
        f"<b>Event:</b> {esc(event_name)}\n"
        f"<b>Branch:</b> <code>{esc(branch)}</code>\n"
        f"<b>Commit:</b> <code>{esc(sha)}</code>\n"
        f"<b>Commit message:</b> {esc(title)}\n"
        f"<b>Author:</b> {esc(author)}\n\n"
        f"<b>Changes</b>\n{'\n'.join(changes)}\n\n"
        f"<a href=\"{esc(run_url)}\">Open GitHub Actions</a>"
    )


def build_pr(event: dict, repo: str, token: str | None, dependabot: bool) -> str:
    pr = event.get("pull_request", {})
    sha_input = (pr.get("head") or {}).get("sha", "")
    sha, title, author, parents = commit_details(repo, sha_input, token)
    changes = compact_changelog(repo, sha_input, parents, token) or [f"• {esc(title)}"]
    heading = "CHRONA DEPENDABOT" if dependabot else "CHRONA PULL REQUEST"
    icon = "🤖" if dependabot else "🔀"
    return (
        f"{icon} <b>{heading}</b>\n\n"
        f"<b>Action:</b> {esc(event.get('action', 'updated'))}\n"
        f"<b>PR:</b> #{esc(pr.get('number', 'unknown'))}\n"
        f"<b>Title:</b> {esc(pr.get('title', title))}\n"
        f"<b>Base:</b> <code>{esc((pr.get('base') or {}).get('ref', 'unknown'))}</code>\n"
        f"<b>Head:</b> <code>{esc((pr.get('head') or {}).get('ref', 'unknown'))}</code>\n"
        f"<b>Commit:</b> <code>{esc(sha)}</code>\n"
        f"<b>Author:</b> {esc(author)}\n\n"
        f"<b>Changes</b>\n{'\n'.join(changes)}\n\n"
        f"<a href=\"{esc(pr.get('html_url', ''))}\">Open Pull Request</a>"
    )


def build_release(event: dict, repo: str, token: str | None) -> str:
    release = event.get("release", {})
    tag = os.environ.get("CHRONA_RELEASE_TAG") or release.get("tag_name", "unknown")
    sha_input = os.environ.get("CHRONA_RELEASE_SHA") or release.get("target_commitish", "")
    sha, title, author, parents = commit_details(repo, sha_input, token)
    changes = compact_changelog(repo, sha_input, parents, token) or [f"• {esc(title)}"]
    url = os.environ.get("CHRONA_RELEASE_URL") or release.get("html_url", "")
    prerelease = os.environ.get("CHRONA_RELEASE_PRERELEASE", "false").lower() == "true"
    prerelease = prerelease or bool(release.get("prerelease"))
    status = "PRE-RELEASE" if prerelease else "RELEASED"
    return (
        f"🚀 <b>CHRONA RELEASE</b>\n\n"
        f"<b>Version:</b> <code>{esc(tag)}</code>\n"
        f"<b>Status:</b> {status}\n"
        f"<b>Commit:</b> <code>{esc(sha)}</code>\n"
        f"<b>Author:</b> {esc(author)}\n\n"
        f"<b>Highlights</b>\n{'\n'.join(changes)}\n\n"
        f"<a href=\"{esc(url)}\">Open GitHub Release</a>"
    )


def build_message(kind: str, event: dict, repo: str, token: str | None) -> str:
    if kind == "ci":
        return build_ci(event, repo, token)
    if kind == "pr":
        return build_pr(event, repo, token, False)
    if kind == "dependabot":
        return build_pr(event, repo, token, True)
    if kind == "release":
        return build_release(event, repo, token)
    raise ValueError(kind)


def send_message(token: str, chat_id: str, message: str) -> None:
    payload = urllib.parse.urlencode({
        "chat_id": chat_id,
        "text": message,
        "parse_mode": "HTML",
        "disable_web_page_preview": "true",
    }).encode("utf-8")
    request = urllib.request.Request(
        f"https://api.telegram.org/bot{token}/sendMessage",
        data=payload,
        headers={"Content-Type": "application/x-www-form-urlencoded"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(request, timeout=20) as response:
            data = json.load(response)
    except urllib.error.HTTPError as exc:
        body = exc.read().decode("utf-8", errors="replace")
        raise SystemExit(f"Telegram API request failed: HTTP {exc.code}: {body[:500]}") from exc
    if not data.get("ok"):
        raise SystemExit(f"Telegram API rejected the message: {data.get('description', 'unknown error')}")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--kind", choices=("ci", "pr", "dependabot", "release"), required=True)
    parser.add_argument("--dry-run", action="store_true")
    args = parser.parse_args()

    event = load_event()
    repo = os.environ["GITHUB_REPOSITORY"]
    token = os.environ.get("GITHUB_TOKEN")
    message = build_message(args.kind, event, repo, token)
    if len(message) > MAX_MESSAGE:
        message = message[: MAX_MESSAGE - 1].rstrip() + "…"

    if args.dry_run:
        print(message)
        return 0

    bot_token = os.environ.get("TELEGRAM_BOT_TOKEN")
    chat_id = os.environ.get("TELEGRAM_CHAT_ID")
    if not bot_token or not chat_id:
        raise SystemExit("TELEGRAM_BOT_TOKEN and TELEGRAM_CHAT_ID are required")
    send_message(bot_token, chat_id, message)
    print("Telegram notification sent.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
