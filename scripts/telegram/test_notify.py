#!/usr/bin/env python3
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
import importlib.util
from pathlib import Path
import unittest
from unittest.mock import patch

MODULE_PATH = Path(__file__).with_name("notify.py")
spec = importlib.util.spec_from_file_location("chrona_telegram_notify", MODULE_PATH)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class NotifyTests(unittest.TestCase):
    def setUp(self) -> None:
        self.repo = "FebriCahyaa/Chrona"

    def test_ci_message_contains_operational_context(self) -> None:
        event = {"workflow_run": {
            "name": "Chrona CI",
            "conclusion": "success",
            "event": "push",
            "head_branch": "main",
            "head_sha": "abcdef1234567890",
            "run_number": 42,
            "html_url": "https://github.com/FebriCahyaa/Chrona/actions/runs/42",
            "run_started_at": "2026-09-19T06:00:00Z",
            "updated_at": "2026-09-19T06:01:27Z",
        }}
        with patch.object(module, "commit_details", return_value=("abcdef123456", "fix(ui): stabilize dashboard")):
            message = module.build_message("ci", event, self.repo, None)
        self.assertIn("Chrona CI Update", message)
        self.assertIn("abcdef123456", message)
        self.assertIn("fix(ui): stabilize dashboard", message)
        self.assertIn("87s", message)
        self.assertLessEqual(len(message), module.MAX_MESSAGE)

    def test_pr_message_is_distinct_from_dependabot(self) -> None:
        event = {"action": "opened", "pull_request": {
            "number": 123,
            "title": "feat(worldclock): add regions",
            "state": "open",
            "head": {"sha": "1234567890abcdef"},
            "base": {"ref": "main"},
            "user": {"login": "contributor"},
            "html_url": "https://github.com/FebriCahyaa/Chrona/pull/123",
        }}
        with patch.object(module, "commit_details", return_value=("1234567890ab", "feat(worldclock): add regions")):
            normal = module.build_message("pr", event, self.repo, None)
            bot = module.build_message("dependabot", {**event, "pull_request": {**event["pull_request"], "user": {"login": "dependabot[bot]"}}}, self.repo, None)
        self.assertIn("Chrona Pull Request", normal)
        self.assertIn("Chrona Dependabot", bot)
        self.assertNotIn("Chrona Dependabot", normal)

    def test_release_message_contains_release_identity(self) -> None:
        event = {"release": {
            "tag_name": "v0.6.0",
            "name": "Chrona 0.6.0",
            "prerelease": False,
            "html_url": "https://github.com/FebriCahyaa/Chrona/releases/tag/v0.6.0",
        }}
        with patch.object(module, "release_commit", return_value=("deadbeefcafe", "release: Chrona v0.6.0")):
            message = module.build_message("release", event, self.repo, None)
        self.assertIn("Chrona Release", message)
        self.assertIn("v0.6.0", message)
        self.assertIn("deadbeefcafe", message)

    def test_html_is_escaped(self) -> None:
        event = {"workflow_run": {"name": "x < y", "conclusion": "success", "head_sha": "1"}}
        with patch.object(module, "commit_details", return_value=("1", "title < unsafe")):
            message = module.build_message("ci", event, self.repo, None)
        self.assertIn("x &lt; y", message)
        self.assertIn("title &lt; unsafe", message)


if __name__ == "__main__":
    unittest.main()
