@echo off
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-exe.ps1" %*
exit /b %ERRORLEVEL%
