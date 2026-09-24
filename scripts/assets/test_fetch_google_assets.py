"""Unit tests for fetch_google_assets.py: python3 -m unittest discover -s scripts/assets

Network calls are mocked; nothing here touches the real GitHub API.
"""

from __future__ import annotations

import unittest
from unittest.mock import patch

import fetch_google_assets as fga


class FetchGoogleAssetsTest(unittest.TestCase):
    def test_default_branch_reads_the_repo_metadata_field(self) -> None:
        # Regression: material-design-icons defaults to "master", not "main".
        # Hardcoding "main" silently walked an unrelated/empty tree instead
        # of failing loudly, so this must come from the API, never a guess.
        with patch.object(fga, "get_json", return_value={"default_branch": "master"}) as mocked:
            self.assertEqual(fga.default_branch("google", "material-design-icons"), "master")
        mocked.assert_called_once_with(f"{fga.API_ROOT}/google/material-design-icons")

    def test_latest_asset_prefers_bracketed_variable_font_paths(self) -> None:
        tree = {
            "tree": [
                {"path": "static/MaterialSymbolsRounded-Regular.ttf", "type": "blob"},
                {"path": "variablefont/MaterialSymbolsRounded[FILL,GRAD,opsz,wght].ttf", "type": "blob"},
                {"path": "README.md", "type": "blob"},
            ]
        }
        with patch.object(fga, "get_json", return_value=tree):
            path = fga.latest_asset(
                "google",
                "material-design-icons",
                "master",
                lambda p: p.lower().endswith(".ttf") and "materialsymbolsrounded" in p.lower(),
            )
        self.assertEqual(path, "variablefont/MaterialSymbolsRounded[FILL,GRAD,opsz,wght].ttf")

    def test_latest_asset_raises_when_nothing_matches(self) -> None:
        with patch.object(fga, "get_json", return_value={"tree": [{"path": "README.md", "type": "blob"}]}):
            with self.assertRaises(RuntimeError):
                fga.latest_asset("owner", "repo", "main", lambda p: p.endswith(".ttf"))

    def test_fetch_builds_the_raw_url_from_the_resolved_branch(self) -> None:
        saved: dict = {}
        with (
            patch.object(fga, "default_branch", return_value="master"),
            patch.object(fga, "latest_asset", return_value="variablefont/Foo.ttf"),
            patch.object(fga, "save", side_effect=lambda url, dest: saved.update(url=url, dest=dest)),
        ):
            path = fga.fetch("google", "material-design-icons", lambda p: True, "/tmp/out.ttf")
        self.assertEqual(path, "variablefont/Foo.ttf")
        self.assertEqual(
            saved["url"],
            "https://raw.githubusercontent.com/google/material-design-icons/master/variablefont/Foo.ttf",
        )
        self.assertEqual(saved["dest"], "/tmp/out.ttf")


if __name__ == "__main__":
    unittest.main()
