@echo off
setlocal
set GRADLE_VERSION=9.7.1
if not "%GRADLE_BIN%"=="" (
  "%GRADLE_BIN%" %*
  exit /b %ERRORLEVEL%
)
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
set CACHE=%USERPROFILE%\.gradle\chrona-wrapper\%GRADLE_VERSION%
set DIST=%CACHE%\gradle-%GRADLE_VERSION%
if not exist "%DIST%\bin\gradle.bat" (
  if not exist "%CACHE%" mkdir "%CACHE%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -UseBasicParsing -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%CACHE%\gradle.zip'"
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Force '%CACHE%\gradle.zip' '%CACHE%'"
)
call "%DIST%\bin\gradle.bat" %*
