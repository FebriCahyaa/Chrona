from __future__ import annotations

import html
import json
import os
import sys
import urllib.parse
import urllib.request
from pathlib import Path
from typing import Any

API_TEMPLATE = "https://api.telegram.org/bot{token}/{method}"
MAX_MESSAGE = 3900
MAX_UPLOAD_BYTES = 49 * 1024 * 1024


def value(name: str, required: bool = False) -> str:
    result = os.environ.get(name, "").strip()
    if required and not result:
        raise RuntimeError(f"Missing environment variable: {name}")
    return result


def api_request(method: str, fields: dict[str, Any]) -> dict[str, Any]:
    token = value("TELEGRAM_BOT_TOKEN", True)
    encoded = urllib.parse.urlencode(
        {
            key: json.dumps(item, ensure_ascii=False)
            if isinstance(item, (dict, list))
            else str(item)
            for key, item in fields.items()
        }
    ).encode("utf-8")
    request = urllib.request.Request(
        API_TEMPLATE.format(token=token, method=method),
        data=encoded,
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        payload = json.loads(response.read().decode("utf-8"))
    if not payload.get("ok"):
        raise RuntimeError(payload.get("description", "Telegram API request failed"))
    return payload


def multipart_request(method: str, fields: dict[str, Any], file_field: str, path: Path) -> None:
    token = value("TELEGRAM_BOT_TOKEN", True)
    boundary = "ChronaTelegramBoundary"
    parts: list[bytes] = []
    for key, item in fields.items():
        parts.extend(
            [
                f"--{boundary}\r\n".encode("ascii"),
                f'Content-Disposition: form-data; name="{key}"\r\n\r\n'.encode("utf-8"),
                str(item).encode("utf-8"),
                b"\r\n",
            ]
        )
    parts.extend(
        [
            f"--{boundary}\r\n".encode("ascii"),
            f'Content-Disposition: form-data; name="{file_field}"; filename="{path.name}"\r\n'.encode("utf-8"),
            b"Content-Type: application/octet-stream\r\n\r\n",
            path.read_bytes(),
            b"\r\n",
            f"--{boundary}--\r\n".encode("ascii"),
        ]
    )
    request = urllib.request.Request(
        API_TEMPLATE.format(token=token, method=method),
        data=b"".join(parts),
        headers={"Content-Type": f"multipart/form-data; boundary={boundary}"},
        method="POST",
    )
    with urllib.request.urlopen(request, timeout=180) as response:
        payload = json.loads(response.read().decode("utf-8"))
    if not payload.get("ok"):
        raise RuntimeError(payload.get("description", "Telegram upload failed"))


def buttons(repo_url: str, run_url: str, extra: list[tuple[str, str, str]] | None = None) -> list[list[dict[str, str]]]:
    rows = [
        [
            {"text": "Repository", "url": repo_url, "style": "primary"},
            {"text": "Actions Run", "url": run_url, "style": "primary"},
        ]
    ]
    for label, url, style in extra or []:
        rows.append([{"text": label, "url": url, "style": style}])
    return rows


def send_message(text: str, keyboard: list[list[dict[str, str]]]) -> None:
    if not value("TELEGRAM_CHAT_ID"):
        return
    chunks = [text[i : i + MAX_MESSAGE] for i in range(0, len(text), MAX_MESSAGE)] or [""]
    for index, chunk in enumerate(chunks):
        api_request(
            "sendMessage",
            {
                "chat_id": value("TELEGRAM_CHAT_ID", True),
                "text": chunk,
                "parse_mode": "HTML",
                "disable_web_page_preview": "true",
                "reply_markup": {"inline_keyboard": keyboard} if index == len(chunks) - 1 else {"inline_keyboard": []},
            },
        )


def send_document(path: Path, caption: str, keyboard: list[list[dict[str, str]]]) -> None:
    if not value("TELEGRAM_CHAT_ID") or path.stat().st_size > MAX_UPLOAD_BYTES:
        return
    multipart_request(
        "sendDocument",
        {
            "chat_id": value("TELEGRAM_CHAT_ID", True),
            "caption": caption[:1000],
            "parse_mode": "HTML",
            "reply_markup": json.dumps({"inline_keyboard": keyboard}),
        },
        "document",
        path,
    )


def github_event() -> dict[str, Any]:
    event_path = value("GITHUB_EVENT_PATH", True)
    return json.loads(Path(event_path).read_text(encoding="utf-8"))


def github_api(path: str) -> dict[str, Any] | list[Any] | None:
    token = value("GITHUB_TOKEN")
    if not token:
        return None
    request = urllib.request.Request(
        f"https://api.github.com{path}",
        headers={
            "Accept": "application/vnd.github+json",
            "Authorization": f"Bearer {token}",
            "X-GitHub-Api-Version": "2026-03-10",
            "User-Agent": "Chrona-Telegram-Notifier",
        },
        method="GET",
    )
    try:
        with urllib.request.urlopen(request, timeout=30) as response:
            return json.loads(response.read().decode("utf-8"))
    except Exception:
        return None


def repo_context() -> tuple[str, str, str]:
    repository = value("GITHUB_REPOSITORY") or "FebriCahyaa/Chrona"
    server = value("GITHUB_SERVER_URL") or "https://github.com"
    run_id = value("GITHUB_RUN_ID")
    repo_url = f"{server}/{repository}"
    run_url = f"{repo_url}/actions/runs/{run_id}" if run_id else repo_url
    return repository, repo_url, run_url


def ref_commit() -> tuple[str, str]:
    ref = html.escape(value("GITHUB_REF_NAME") or value("RELEASE_VERSION") or "unknown")
    commit = html.escape((value("GITHUB_SHA") or "unknown")[:12])
    return ref, commit


def artifact_files() -> list[Path]:
    directory = value("TELEGRAM_ARTIFACT_DIR")
    if not directory:
        return []
    root = Path(directory)
    if not root.exists():
        return []
    return [item for item in sorted(root.rglob("*")) if item.is_file() and item.stat().st_size > 0]


def result_icon(result: str) -> str:
    return {"success": "✅", "failure": "❌", "cancelled": "⏹️", "skipped": "⏭️"}.get(result, "🟡")


def ci_start() -> None:
    _, repo_url, run_url = repo_context()
    ref, commit = ref_commit()
    text = (
        "🛠️ <b>Chrona CI Started</b>\n\n"
        f"<b>Ref:</b> <code>{ref}</code>\n"
        f"<b>Commit:</b> <code>{commit}</code>\n"
        f"<b>Actor:</b> <code>{html.escape(value('GITHUB_ACTOR') or 'unknown')}</code>\n\n"
        "Quality checks and debug build are now running."
    )
    send_message(text, buttons(repo_url, run_url))


def ci_report() -> None:
    _, repo_url, run_url = repo_context()
    ref, commit = ref_commit()
    quality = value("QUALITY_RESULT") or "unknown"
    build = value("BUILD_RESULT") or "unknown"
    success = quality == "success" and build == "success"
    state = "Completed" if success else "Failed"
    text = (
        f"{result_icon('success' if success else 'failure')} <b>Chrona CI {state}</b>\n\n"
        f"<b>Ref:</b> <code>{ref}</code>\n"
        f"<b>Commit:</b> <code>{commit}</code>\n"
        f"<b>Quality:</b> {result_icon(quality)} <code>{html.escape(quality)}</code>\n"
        f"<b>Build:</b> {result_icon(build)} <code>{html.escape(build)}</code>"
    )
    error_file = next((item for item in artifact_files() if "error" in item.name.lower()), None)
    if error_file:
        content = error_file.read_text(encoding="utf-8", errors="replace")[-2800:]
        text += f"\n\n🧩 <b>Error Extract</b>\n<pre>{html.escape(content)}</pre>"
    send_message(text, buttons(repo_url, run_url))
    for item in artifact_files():
        lower = item.name.lower()
        if item.suffix.lower() == ".apk" or "error" in lower or lower.endswith(".sarif"):
            send_document(item, f"📎 <b>Chrona CI Artifact</b>\n<code>{html.escape(item.name)}</code>", buttons(repo_url, run_url))


def pr_message(event: dict[str, Any], dependabot: bool = False) -> None:
    _, repo_url, run_url = repo_context()
    pull = event.get("pull_request", {})
    action = event.get("action", "updated")
    number = pull.get("number", "?")
    title = html.escape(pull.get("title", ""))
    author = html.escape(pull.get("user", {}).get("login", "unknown"))
    head = html.escape(pull.get("head", {}).get("ref", "unknown"))
    base = html.escape(pull.get("base", {}).get("ref", "unknown"))
    additions = pull.get("additions", 0)
    deletions = pull.get("deletions", 0)
    changed = pull.get("changed_files", 0)
    icon = "🤖" if dependabot else "🔀"
    label = "Dependabot Update" if dependabot else "Pull Request"
    text = (
        f"{icon} <b>Chrona {label} {html.escape(action)}</b>\n\n"
        f"<b>#{number}</b> {title}\n"
        f"<b>Author:</b> <code>{author}</code>\n"
        f"<b>Branch:</b> <code>{head}</code> → <code>{base}</code>\n"
        f"<b>Changes:</b> <code>+{additions} / -{deletions}</code> · <code>{changed} files</code>"
    )
    extra = [("Open PR", pull.get("html_url", repo_url), "success")]
    send_message(text, buttons(repo_url, run_url, extra))


def issue_message(event: dict[str, Any]) -> None:
    _, repo_url, run_url = repo_context()
    issue = event.get("issue", {})
    action = html.escape(event.get("action", "updated"))
    labels = issue.get("labels", [])
    label_text = ", ".join(html.escape(item.get("name", "")) for item in labels[:8]) or "none"
    text = (
        f"📌 <b>Chrona Issue {action}</b>\n\n"
        f"<b>#{issue.get('number', '?')}</b> {html.escape(issue.get('title', ''))}\n"
        f"<b>Author:</b> <code>{html.escape(issue.get('user', {}).get('login', 'unknown'))}</code>\n"
        f"<b>Labels:</b> {label_text}"
    )
    send_message(text, buttons(repo_url, run_url, [("Open Issue", issue.get("html_url", repo_url), "primary")]))


def release_report() -> None:
    _, repo_url, run_url = repo_context()
    version = html.escape(value("RELEASE_VERSION") or "unknown")
    prepare = value("PREPARE_RESULT") or "unknown"
    build = value("BUILD_RESULT") or "unknown"
    publish = value("PUBLISH_RESULT") or "unknown"
    success = publish == "success"
    text = (
        f"{'🚀' if success else '❌'} <b>Chrona Release {'Published' if success else 'Failed'}</b>\n\n"
        f"<b>Version:</b> <code>{version}</code>\n"
        f"<b>Prepare:</b> {result_icon(prepare)} <code>{html.escape(prepare)}</code>\n"
        f"<b>Build:</b> {result_icon(build)} <code>{html.escape(build)}</code>\n"
        f"<b>Publish:</b> {result_icon(publish)} <code>{html.escape(publish)}</code>"
    )
    extra = []
    if success:
        extra.append(("Open Release", f"{repo_url}/releases/tag/{urllib.parse.quote(version)}", "success"))
    send_message(text, buttons(repo_url, run_url, extra))
    for item in artifact_files():
        if item.suffix.lower() in {".apk", ".txt", ".log"} or "changelog" in item.name.lower():
            send_document(item, f"🚀 <b>Chrona Release Artifact</b>\n<code>{html.escape(item.name)}</code>", buttons(repo_url, run_url, extra))


def security_report() -> None:
    _, repo_url, run_url = repo_context()
    entries = [
        ("CodeQL", value("CODEQL_RESULT") or "unknown"),
        ("Dependency Review", value("DEPENDENCY_REVIEW_RESULT") or "unknown"),
        ("Dependency Graph", value("DEPENDENCY_SUBMISSION_RESULT") or "unknown"),
    ]
    text = "🛡️ <b>Chrona Security Report</b>\n\n" + "\n".join(
        f"<b>{html.escape(name)}:</b> {result_icon(result)} <code>{html.escape(result)}</code>"
        for name, result in entries
    )
    send_message(text, buttons(repo_url, run_url))


def generic_event(event_name: str, event: dict[str, Any], title: str) -> None:
    _, repo_url, run_url = repo_context()
    target = event.get("pull_request") or event.get("issue") or {}
    subject = html.escape(target.get("title", "Chrona automation event"))
    action = html.escape(event.get("action", "updated"))
    text = f"🔔 <b>{html.escape(title)}</b>\n\n<b>Action:</b> <code>{action}</code>\n<b>Subject:</b> {subject}\n<b>Event:</b> <code>{html.escape(event_name)}</code>"
    url = target.get("html_url") or repo_url
    send_message(text, buttons(repo_url, run_url, [("Open Event", url, "primary")]))


def main() -> int:
    if not value("TELEGRAM_BOT_TOKEN") or not value("TELEGRAM_CHAT_ID"):
        return 0
    event = value("TELEGRAM_EVENT", True)
    if event == "ci-start":
        ci_start()
    elif event == "ci-report":
        ci_report()
    elif event == "release-report":
        release_report()
    elif event == "security":
        security_report()
    elif event == "pull-request":
        pr_message(github_event())
    elif event == "dependabot":
        pr_message(github_event(), dependabot=True)
    elif event == "issue":
        issue_message(github_event())
    elif event == "crowdin":
        generic_event("crowdin-sync", {}, "Chrona Localization Sync")
    else:
        raise RuntimeError(f"Unsupported TELEGRAM_EVENT: {event}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as error:
        print(str(error), file=sys.stderr)
        raise
