@echo off
REM GURUKULA BOARD - Build Script
REM This script builds the APK using Gradle

echo ========================================
echo GURUKULA BOARD - Building APK
echo ========================================
echo.

REM Set Java Home (with quotes for spaces)
set "JAVA_HOME=C:\Program Files\Android\Android Studio\jbr"
if not exist "%JAVA_HOME%" (
    set "JAVA_HOME=%LOCALAPPDATA%\Programs\Android Studio\jbr"
)

REM Set Android Home
set "ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk"

REM Add to PATH
set "PATH=%JAVA_HOME%\bin;%ANDROID_HOME%\platform-tools;%PATH%"

echo Environment:
echo   JAVA_HOME: %JAVA_HOME%
echo   ANDROID_HOME: %ANDROID_HOME%
echo.

echo Cleaning project...
call gradlew.bat clean
if errorlevel 1 (
    echo Clean failed!
    pause
    exit /b 1
)

echo.
echo Building Debug APK...
call gradlew.bat assembleDebug
if errorlevel 1 (
    echo.
    echo Build failed!
    echo.
    echo TROUBLESHOOTING:
    echo 1. Try building in Android Studio instead
    echo 2. Ensure Firebase is configured
    echo 3. Check that all dependencies are synced
    echo.
    pause
    exit /b 1
)

echo.
echo ========================================
echo BUILD SUCCESSFUL!
echo ========================================
echo.
echo APK Location: app\build\outputs\apk\debug\app-debug.apk
echo.

if exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo APK found! Ready for testing.
) else (
    echo APK not found at expected location.
)

echo.
pause

