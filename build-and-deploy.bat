@echo off
REM GURUKULA BOARD - Build and Deploy Script (Batch version)
REM This script builds and deploys the app to the Android emulator

echo ========================================
echo GURUKULA BOARD - Build ^& Deploy Script
echo ========================================
echo.

REM Set Android Studio paths
set "ANDROID_STUDIO_PATH=C:\Program Files\Android\Android Studio"
set "ANDROID_SDK_PATH=%LOCALAPPDATA%\Android\Sdk"
set "JDK_PATH=%ANDROID_STUDIO_PATH%\jbr"

REM Check if Android Studio path exists, try alternative
if not exist "%ANDROID_STUDIO_PATH%" (
    set "ANDROID_STUDIO_PATH=%LOCALAPPDATA%\Programs\Android Studio"
    set "JDK_PATH=%ANDROID_STUDIO_PATH%\jbr"
)

REM Set environment variables
set "JAVA_HOME=%JDK_PATH%"
set "ANDROID_HOME=%ANDROID_SDK_PATH%"
set "ANDROID_SDK_ROOT=%ANDROID_SDK_PATH%"
set "PATH=%JDK_PATH%\bin;%ANDROID_SDK_PATH%\platform-tools;%ANDROID_SDK_PATH%\tools;%PATH%"

echo Environment Setup:
echo   JAVA_HOME: %JAVA_HOME%
echo   ANDROID_HOME: %ANDROID_HOME%
echo.

REM Change to project directory
cd /d "%~dp0"

echo Project Directory: %CD%
echo.

REM Check for emulator
echo Checking for running emulator...
"%ANDROID_SDK_PATH%\platform-tools\adb.exe" devices | findstr "emulator" >nul
if errorlevel 1 (
    echo No emulator detected. Please start an emulator first.
    echo You can start it from Android Studio or run:
    echo "%ANDROID_SDK_PATH%\emulator\emulator.exe" -avd YOUR_AVD_NAME
    pause
    exit /b 1
)

echo Emulator detected!
echo.

REM Clean (optional - uncomment to enable)
REM echo Cleaning project...
REM call gradlew.bat clean
REM if errorlevel 1 (
REM     echo ERROR: Clean failed!
REM     pause
REM     exit /b 1
REM )
REM echo.

REM Build the project
echo Building project...
echo This may take a few minutes on first build...
echo.
call gradlew.bat assembleDebug --no-daemon

if errorlevel 1 (
    echo.
    echo ERROR: Build failed!
    echo.
    echo Suggestion: Try building from Android Studio (Build -^> Make Project)
    echo The build may work better from Android Studio's UI.
    pause
    exit /b 1
)

echo.
echo Build completed successfully!
echo.

REM Install on emulator
echo Installing app on emulator...

REM Find the APK
set "APK_PATH=%CD%\app\build\outputs\apk\debug\app-debug.apk"

if not exist "%APK_PATH%" (
    echo ERROR: APK not found at: %APK_PATH%
    echo Please build the project first.
    pause
    exit /b 1
)

REM Uninstall existing app
echo Uninstalling existing app (if present)...
"%ANDROID_SDK_PATH%\platform-tools\adb.exe" uninstall com.gurukulaboard >nul 2>&1

REM Install new APK
echo Installing new APK...
"%ANDROID_SDK_PATH%\platform-tools\adb.exe" install -r "%APK_PATH%"

if errorlevel 1 (
    echo ERROR: Installation failed!
    pause
    exit /b 1
)

echo App installed successfully!
echo.

REM Launch the app
echo Launching app...
"%ANDROID_SDK_PATH%\platform-tools\adb.exe" shell am start -n com.gurukulaboard/.auth.LoginActivity

echo.
echo ========================================
echo SUCCESS! App deployed and launched!
echo ========================================
echo.
echo The app should now be running on your emulator.
echo.
pause

