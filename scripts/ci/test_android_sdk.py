"""Unit tests for android_sdk.py: python3 -m unittest discover -s scripts/ci"""

from __future__ import annotations

import unittest

import android_sdk

LISTING = """\
Loading package information...\r[=====] 100% Computing updates...
Installed packages:
  Path                 | Version | Description                    | Location
  -------              | ------- | -------                        | -------
  build-tools;35.0.0   | 35.0.0  | Android SDK Build-Tools 35     | build-tools/35.0.0

Available Packages:
  Path                        | Version | Description
  -------                     | ------- | -------
  build-tools;36.1.0          | 36.1.0  | Android SDK Build-Tools 36.1
  build-tools;37.0.0          | 37.0.0  | Android SDK Build-Tools 37
  build-tools;37.1.0-rc1      | 37.1.0 rc1 | Android SDK Build-Tools 37.1-rc1
  platform-tools              | 37.0.0  | Android SDK Platform-Tools
  platforms;android-36        | 2       | Android SDK Platform 36
  platforms;android-37        | 1       | Android SDK Platform 37
  platforms;android-37.2      | 1       | Android SDK Platform 37.2
  platforms;android-37-ext19  | 1       | Android SDK Platform 37-ext19
  platforms;android-CinnamonBun | 1     | Android SDK Platform CinnamonBun

Available Updates:
  ID           | Installed | Available
  build-tools;35.0.0 | 35.0.0 | 35.0.1
"""


class AndroidSdkTest(unittest.TestCase):
    def setUp(self) -> None:
        self.paths = android_sdk.parse_available(LISTING)

    def test_parses_only_available_section(self) -> None:
        self.assertIn("platforms;android-37.2", self.paths)
        self.assertNotIn("build-tools;35.0.0", self.paths)
        self.assertNotIn("Path", self.paths)

    def test_stable_picks_newest_numbered_platform(self) -> None:
        self.assertEqual(
            android_sdk.choose_platform(self.paths, 0), ("platforms;android-37.2", "37.2")
        )

    def test_preview_channels_prefer_codename_platform(self) -> None:
        self.assertEqual(
            android_sdk.choose_platform(self.paths, 3),
            ("platforms;android-CinnamonBun", "CinnamonBun"),
        )

    def test_canary_placeholder_platform_is_never_selected(self) -> None:
        # Google ships this stub before the next Android version has a real
        # preview codename; its android.jar can't compile a real app.
        paths = ["platforms;android-37.2", "platforms;android-CANARY"]
        self.assertEqual(
            android_sdk.choose_platform(paths, 3), ("platforms;android-37.2", "37.2")
        )

    def test_canary_placeholder_alone_falls_back_to_error(self) -> None:
        with self.assertRaises(SystemExit):
            android_sdk.choose_platform(["platforms;android-CANARY"], 3)

    def test_whole_api_level_platform(self) -> None:
        self.assertEqual(
            android_sdk.choose_platform(["platforms;android-37"], 0), ("platforms;android-37", "37")
        )

    def test_build_tools_prefers_newest_release_candidate(self) -> None:
        self.assertEqual(android_sdk.choose_build_tools(self.paths), "37.1.0-rc1")

    def test_final_build_tools_beats_its_release_candidate(self) -> None:
        paths = ["build-tools;37.1.0-rc1", "build-tools;37.1.0", "build-tools;37.0.0"]
        self.assertEqual(android_sdk.choose_build_tools(paths), "37.1.0")


if __name__ == "__main__":
    unittest.main()
