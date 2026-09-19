#!/usr/bin/env python3
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
"""Render and send Chrona GitHub events to a dedicated Telegram bot/chat."""
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
        "User-Agent": "Chrona-GitHub-Telegram-Bot",
    }
    if token:
        headers["Authorization"] = f"Bearer {token}"
    request = urllib.request.Request(url, headers=headers)
    with urllib.request.urlopen(request, timeout=20) as response:
        return json.load(response)


def commit_details(repo: str, sha: str, token: str | None) -> tuple[str, str]:
    if not sha:
        return "unknown", "unknown"
    try:
        data = github_get(f"https://api.github.com/repos/{repo}/commits/{sha}", token)
        message = data.get("commit", {}).get("message", "").splitlines()[0] or "(no commit title)"
        return sha[:12], message
    except Exception:
        return sha[:12], "(commit title unavailable)"


def release_commit(repo: str, tag: str, token: str | None) -> tuple[str, str]:
    try:
        ref = github_get(f"https://api.github.com/repos/{repo}/git/ref/tags/{urllib.parse.quote(tag, safe='')}", token)
        sha = ref["object"]["sha"]
        if ref["object"].get("type") == "tag":
            tag_data = github_get(f"https://api.github.com/repos/{repo}/git/tags/{sha}", token)
            sha = tag_data["object"]["sha"]
        return commit_details(repo, sha, token)
    except Exception:
        return "unknown", "(release commit title unavailable)"


def load_event() -> dict:
    path = Path(os.environ.get("GITHUB_EVENT_PATH", ""))
    if not path.is_file():
        raise SystemExit("GITHUB_EVENT_PATH is not available")
    return json.loads(path.read_text(encoding="utf-8"))


def duration_seconds(start: str | None, end: str | None) -> int | None:
    if not start or not end:
        return None
    try:
        from datetime import datetime
        a = datetime.fromisoformat(start.replace("Z", "+00:00"))
        b = datetime.fromisoformat(end.replace("Z", "+00:00"))
        return max(0, int((b - a).total_seconds()))
    except ValueError:
        return None


def build_ci(event: dict, repo: str, token: str | None) -> str:
    run = event.get("workflow_run", event)
    conclusion = run.get("conclusion") or run.get("status") or "unknown"
    emoji = "✅" if conclusion == "success" else "❌" if conclusion in {"failure", "cancelled", "timed_out"} else "⏳"
    sha, title = commit_details(repo, run.get("head_sha", ""), token)
    duration = duration_seconds(run.get("run_started_at"), run.get("updated_at"))
    duration_line = f"<b>Duration:</b> {duration}s\n" if duration is not None else ""
    return (
        f"{emoji} <b>Chrona CI Update</b>\n\n"
        f"<b>Workflow:</b> {esc(run.get('name', 'unknown'))}\n"
        f"<b>Status:</b> {esc(conclusion)}\n"
        f"<b>Event:</b> {esc(run.get('event', 'unknown'))}\n"
        f"<b>Branch:</b> {esc(run.get('head_branch', 'unknown'))}\n"
        f"<b>Commit:</b> <code>{esc(sha)}</code>\n"
        f"<b>Commit title:</b> {esc(title)}\n"
        f"<b>Run:</b> #{esc(run.get('run_number', 'unknown'))}\n"
        f"{duration_line}\n"
        f"<a href=\"{esc(run.get('html_url', ''))}\">Open Actions run</a>"
    )


def build_pr(event: dict, repo: str, token: str | None, dependabot: bool = False) -> str:
    pr = event.get("pull_request", {})
    action = event.get("action", "updated")
    sha, title = commit_details(repo, pr.get("head", {}).get("sha", ""), token)
    icon = "🤖" if dependabot else "🔔"
    heading = "Chrona Dependabot" if dependabot else "Chrona Pull Request"
    return (
        f"{icon} <b>{heading}</b>\n\n"
        f"<b>Action:</b> {esc(action)}\n"
        f"<b>PR:</b> #{esc(pr.get('number', 'unknown'))}\n"
        f"<b>Title:</b> {esc(pr.get('title', title))}\n"
        f"<b>Commit:</b> <code>{esc(sha)}</code>\n"
        f"<b>Commit title:</b> {esc(title)}\n"
        f"<b>Base:</b> {esc(pr.get('base', {}).get('ref', 'unknown'))}\n"
        f"<b>State:</b> {esc(pr.get('state', 'unknown'))}\n"
        f"<b>Author:</b> {esc(pr.get('user', {}).get('login', 'unknown'))}\n\n"
        f"<a href=\"{esc(pr.get('html_url', ''))}\">Open Pull Request</a>"
    )


def build_release(event: dict, repo: str, token: str | None) -> str:
    release = event.get("release", {})
    tag = release.get("tag_name", "unknown")
    sha, title = release_commit(repo, tag, token)
    prerelease = bool(release.get("prerelease"))
    kind = "Pre-release" if prerelease else "Release"
    return (
        f"🚀 <b>Chrona {kind}</b>\n\n"
        f"<b>Version:</b> {esc(tag)}\n"
        f"<b>Title:</b> {esc(release.get('name', tag))}\n"
        f"<b>Commit:</b> <code>{esc(sha)}</code>\n"
        f"<b>Commit title:</b> {esc(title)}\n\n"
        f"<a href=\"{esc(release.get('html_url', ''))}\">Open Release</a>"
    )


def build_message(kind: str, event: dict, repo: str, token: str | None) -> str:
    if kind == "ci":
        return build_ci(event, repo, token)
    if kind == "pr":
        return build_pr(event, repo, token, dependabot=False)
    if kind == "dependabot":
        return build_pr(event, repo, token, dependabot=True)
    if kind == "release":
        return build_release(event, repo, token)
    raise ValueError(f"Unknown event kind: {kind}")


def send_message(token: str, chat_id: str, message: str) -> None:
    payload = urllib.parse.urlencode(
        {"chat_id": chat_id, "text": message, "parse_mode": "HTML", "disable_web_page_preview": "true"}
    ).encode("utf-8")
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
    github_token = os.environ.get("GITHUB_TOKEN")
    message = build_message(args.kind, event, repo, github_token)
    if len(message) > MAX_MESSAGE:
        message = message[: MAX_MESSAGE - 20].rstrip() + "…</code>"

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
