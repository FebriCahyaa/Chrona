#!/usr/bin/env python3
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

import importlib.util
import os
from pathlib import Path
import unittest
from unittest.mock import patch

MODULE_PATH = Path(__file__).with_name("notify.py")
spec = importlib.util.spec_from_file_location("chrona_telegram_notify", MODULE_PATH)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class NotifyTests(unittest.TestCase):
    repo = "FebriCahyaa/Chrona"

    def test_push_ci_message(self) -> None:
        event = {
            "ref": "refs/heads/main",
            "after": "abcdef1234567890",
            "head_commit": {"message": "fix(world-clock): stabilize dashboard"},
        }
        env = {
            "GITHUB_SHA": "abcdef1234567890",
            "GITHUB_REF_NAME": "main",
            "GITHUB_WORKFLOW": "Chrona Update Commit",
            "GITHUB_EVENT_NAME": "push",
            "GITHUB_SERVER_URL": "https://github.com",
            "GITHUB_RUN_ID": "42",
        }
        with patch.dict(os.environ, env, clear=False), patch.object(
            module,
            "commit_details",
            return_value=("abcdef123456", "fix(world-clock): stabilize dashboard", "Febrian", []),
        ):
            message = module.build_message("ci", event, self.repo, None)
        self.assertIn("CHRONA UPDATE COMMIT", message)
        self.assertIn("abcdef123456", message)
        self.assertIn("fix(world-clock): stabilize dashboard", message)
        self.assertLessEqual(len(message), module.MAX_MESSAGE)

    def test_pr_and_dependabot_are_distinct(self) -> None:
        event = {
            "action": "opened",
            "pull_request": {
                "number": 123,
                "title": "feat(world-clock): update",
                "head": {"ref": "feature", "sha": "1234567890abcdef"},
                "base": {"ref": "main"},
                "user": {"login": "contributor"},
                "html_url": "https://github.com/FebriCahyaa/Chrona/pull/123",
            },
        }
        with patch.object(
            module,
            "commit_details",
            return_value=("1234567890ab", "feat(world-clock): update", "contributor", []),
        ):
            normal = module.build_message("pr", event, self.repo, None)
            bot = module.build_message(
                "dependabot",
                {**event, "pull_request": {**event["pull_request"], "user": {"login": "dependabot[bot]"}}},
                self.repo,
                None,
            )
        self.assertIn("CHRONA PULL REQUEST", normal)
        self.assertIn("CHRONA DEPENDABOT", bot)
        self.assertNotIn("CHRONA DEPENDABOT", normal)

    def test_release_identity_from_environment(self) -> None:
        event = {
            "release": {
                "tag_name": "v0.6.0",
                "name": "Chrona 0.6.0",
                "prerelease": False,
                "html_url": "https://github.com/FebriCahyaa/Chrona/releases/tag/v0.6.0",
            },
        }
        with patch.dict(
            os.environ,
            {
                "CHRONA_RELEASE_TAG": "v0.6.0",
                "CHRONA_RELEASE_SHA": "deadbeefcafe1234",
                "CHRONA_RELEASE_URL": "https://example.invalid/release",
            },
            clear=False,
        ), patch.object(
            module,
            "commit_details",
            return_value=("deadbeefcafe", "release: Chrona v0.6.0", "Febrian", []),
        ):
            message = module.build_message("release", event, self.repo, None)
        self.assertIn("CHRONA RELEASE", message)
        self.assertIn("v0.6.0", message)
        self.assertIn("deadbeefcafe", message)

    def test_release_prerelease_comes_from_environment(self) -> None:
        event = {"release": {"tag_name": "v0.6.0"}}
        with patch.dict(
            os.environ,
            {
                "CHRONA_RELEASE_TAG": "v0.6.0",
                "CHRONA_RELEASE_SHA": "deadbeefcafe1234",
                "CHRONA_RELEASE_PRERELEASE": "true",
            },
            clear=False,
        ), patch.object(
            module,
            "commit_details",
            return_value=("deadbeefcafe", "release: Chrona v0.6.0-rc1", "Febrian", []),
        ):
            message = module.build_message("release", event, self.repo, None)
        self.assertIn("PRE-RELEASE", message)

    def test_html_is_escaped(self) -> None:
        event = {
            "ref": "refs/heads/main",
            "after": "1",
            "head_commit": {"message": "title < unsafe"},
        }
        env = {
            "GITHUB_WORKFLOW": "x < y",
            "GITHUB_REF_NAME": "main",
            "GITHUB_EVENT_NAME": "push",
        }
        with patch.dict(os.environ, env, clear=False), patch.object(
            module,
            "commit_details",
            return_value=("1", "title < unsafe", "Febrian", []),
        ):
            message = module.build_message("ci", event, self.repo, None)
        self.assertIn("x &lt; y", message)
        self.assertIn("title &lt; unsafe", message)


if __name__ == "__main__":
    unittest.main()
