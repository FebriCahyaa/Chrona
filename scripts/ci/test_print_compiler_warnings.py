import unittest

import print_compiler_warnings as pcw

REPORT = """<html><script>
// begin-report-data
{"diagnostics": [
  {"locations": [{"path": "/home/runner/work/Chrona/Chrona/app/src/main/kotlin/A.kt", "line": 81}],
   "problem": [{"text": "Kotlin compiler warning"}],
   "problemDetails": [{"text": "'listen' is deprecated."}]},
  {"locations": [{"path": "/x/app/src/main/kotlin/B.kt", "line": 3}],
   "problem": [{"text": "No cast needed."}]}
]}
// end-report-data
</script></html>"""


class PrintCompilerWarningsTest(unittest.TestCase):
    def test_extracts_every_located_diagnostic(self):
        found = sorted(pcw.iter_diagnostics(pcw.load_report(REPORT)))
        self.assertEqual(found, [
            ("/home/runner/work/Chrona/Chrona/app/src/main/kotlin/A.kt", 81,
             "'listen' is deprecated."),
            ("/x/app/src/main/kotlin/B.kt", 3, "No cast needed."),
        ])

    def test_paths_are_made_repo_relative(self):
        self.assertEqual(pcw.relative("/home/runner/work/Chrona/Chrona/app/src/main/A.kt"),
                         "app/src/main/A.kt")

    def test_missing_data_block_returns_none(self):
        self.assertIsNone(pcw.load_report("<html></html>"))


if __name__ == "__main__":
    unittest.main()
