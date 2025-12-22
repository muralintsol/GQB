@echo off
REM GURUKULA BOARD - Open Android Studio for Building APK
REM This script opens Android Studio with the project for manual APK build

echo ========================================
echo GURUKULA BOARD - Android Studio Build
echo ========================================
echo.

REM Try to find Android Studio
set "STUDIO_PATH=C:\Program Files\Android\Android Studio\bin\studio64.exe"
if not exist "%STUDIO_PATH%" (
    set "STUDIO_PATH=%LOCALAPPDATA%\Programs\Android Studio\bin\studio64.exe"
)

if not exist "%STUDIO_PATH%" (
    echo ERROR: Android Studio not found!
    echo.
    echo Please install Android Studio or update the path in this script.
    echo.
    pause
    exit /b 1
)

echo Opening Android Studio with project: %CD%
echo.
echo INSTRUCTIONS:
echo 1. Wait for Gradle sync to complete
echo 2. Click: Build → Generate Signed Bundle / APK
echo 3. Select "APK" → "release" variant
echo 4. APK will be at: app\release\app-release.apk
echo.

start "" "%STUDIO_PATH%" "%CD%"

echo.
echo Android Studio is opening...
echo Follow the instructions above to build the APK.
echo.
pause

