"""Unit tests for fetch_material_symbols_icons.py: python3 -m unittest discover -s scripts/assets

Network calls are mocked; nothing here touches the real GitHub API.
"""

from __future__ import annotations

import tempfile
import unittest
from pathlib import Path
from unittest.mock import patch

import fetch_material_symbols_icons as fmsi


class ReadIconNamesTest(unittest.TestCase):
    def test_skips_blank_lines_and_comments(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            manifest = Path(tmp) / "icons.txt"
            manifest.write_text("# comment\n\nalarm\n  timer  # trailing comment\n", encoding="utf-8")
            self.assertEqual(fmsi.read_icon_names(manifest), ["alarm", "timer"])

    def test_rejects_a_name_that_is_not_a_valid_android_resource_suffix(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            manifest = Path(tmp) / "icons.txt"
            manifest.write_text("18_up_rating\n", encoding="utf-8")
            with self.assertRaises(ValueError):
                fmsi.read_icon_names(manifest)


class FetchIconTest(unittest.TestCase):
    def test_builds_the_default_rounded_24px_url(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            destination = Path(tmp) / "ic_symbol_alarm.xml"
            with patch.object(fmsi, "get_bytes", return_value=b"<vector />") as mocked:
                fmsi.fetch_icon("master", "alarm", destination)
            mocked.assert_called_once_with(
                "https://raw.githubusercontent.com/google/material-design-icons/master/"
                "symbols/android/alarm/materialsymbolsrounded/alarm_24px.xml"
            )
            self.assertEqual(destination.read_bytes(), b"<vector />")

    def test_rejects_a_non_xml_response(self) -> None:
        with tempfile.TemporaryDirectory() as tmp:
            destination = Path(tmp) / "ic_symbol_alarm.xml"
            with patch.object(fmsi, "get_bytes", return_value=b"404: Not Found"):
                with self.assertRaises(RuntimeError):
                    fmsi.fetch_icon("master", "alarm", destination)
            self.assertFalse(destination.exists())


if __name__ == "__main__":
    unittest.main()
