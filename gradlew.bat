@echo off
setlocal
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
echo Gradle 9.6.1 is required. Install Gradle or use Android Studio's bundled Gradle, then rerun.
exit /b 127
