@echo off
setlocal
cd /d "%~dp0"
if defined JAVA_HOME (
  "%JAVA_HOME%\bin\java.exe" -jar target\arsipdelta-1.0.0.jar
) else (
  java -jar target\arsipdelta-1.0.0.jar
)
pause
