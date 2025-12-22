# GURUKULA BOARD - Build and Deploy Script
# This script builds and deploys the app to the Android emulator using Android Studio's tools

param(
    [switch]$Clean,
    [switch]$BuildOnly,
    [switch]$InstallOnly
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "GURUKULA BOARD - Build & Deploy Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set Android Studio paths
$androidStudioPath = "C:\Program Files\Android\Android Studio"
$androidSdkPath = "$env:LOCALAPPDATA\Android\Sdk"
$gradlePath = "$androidStudioPath\gradle\gradle-8.2\bin\gradle.bat"

# Check if Android Studio path exists, try alternative
if (-not (Test-Path $androidStudioPath)) {
    $androidStudioPath = "$env:LOCALAPPDATA\Programs\Android Studio"
    $gradlePath = "$androidStudioPath\gradle\gradle-8.2\bin\gradle.bat"
}

# Set JDK path
$jdkPath = "$androidStudioPath\jbr"
if (-not (Test-Path $jdkPath)) {
    $jdkPath = "C:\Program Files\Android\Android Studio\jbr"
}

# Verify paths
if (-not (Test-Path $jdkPath)) {
    Write-Host "ERROR: Android Studio JDK not found at: $jdkPath" -ForegroundColor Red
    Write-Host "Please ensure Android Studio is installed." -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $androidSdkPath)) {
    Write-Host "ERROR: Android SDK not found at: $androidSdkPath" -ForegroundColor Red
    Write-Host "Please ensure Android SDK is installed." -ForegroundColor Red
    exit 1
}

# Set environment variables
$env:JAVA_HOME = $jdkPath
$env:ANDROID_HOME = $androidSdkPath
$env:ANDROID_SDK_ROOT = $androidSdkPath
$env:PATH = "$jdkPath\bin;$androidSdkPath\platform-tools;$androidSdkPath\tools;$env:PATH"

Write-Host "Environment Setup:" -ForegroundColor Green
Write-Host "  JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Gray
Write-Host "  ANDROID_HOME: $env:ANDROID_HOME" -ForegroundColor Gray
Write-Host ""

# Change to project directory
$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectDir

Write-Host "Project Directory: $projectDir" -ForegroundColor Green
Write-Host ""

# Check for emulator
Write-Host "Checking for running emulator..." -ForegroundColor Yellow
$devices = & "$androidSdkPath\platform-tools\adb.exe" devices
$emulatorRunning = $devices -match "emulator"

if (-not $emulatorRunning) {
    Write-Host "No emulator detected. Starting emulator..." -ForegroundColor Yellow
    
    # List available AVDs
    $avds = & "$androidSdkPath\emulator\emulator.exe" -list-avds
    if ($avds.Count -eq 0) {
        Write-Host "ERROR: No Android Virtual Devices found." -ForegroundColor Red
        Write-Host "Please create an AVD in Android Studio first." -ForegroundColor Red
        exit 1
    }
    
    $avdName = $avds[0]
    Write-Host "Starting AVD: $avdName" -ForegroundColor Yellow
    
    # Start emulator in background
    Start-Process -FilePath "$androidSdkPath\emulator\emulator.exe" -ArgumentList "-avd", $avdName -WindowStyle Minimized
    
    # Wait for emulator to boot
    Write-Host "Waiting for emulator to boot (this may take 30-60 seconds)..." -ForegroundColor Yellow
    & "$androidSdkPath\platform-tools\adb.exe" wait-for-device
    Write-Host "Emulator is ready!" -ForegroundColor Green
    Write-Host ""
    
    # Wait a bit more for system to be fully ready
    Start-Sleep -Seconds 10
}

# Clean if requested
if ($Clean) {
    Write-Host "Cleaning project..." -ForegroundColor Yellow
    & "$projectDir\gradlew.bat" clean
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Clean failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "Clean completed successfully!" -ForegroundColor Green
    Write-Host ""
}

# Build the project
if (-not $InstallOnly) {
    Write-Host "Building project..." -ForegroundColor Yellow
    Write-Host "This may take a few minutes on first build..." -ForegroundColor Gray
    Write-Host ""
    
    # Try using project's gradlew first
    $buildResult = & "$projectDir\gradlew.bat" assembleDebug --no-daemon 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Standard build failed. Trying with Android Studio's Gradle..." -ForegroundColor Yellow
        
        # Try with Android Studio's embedded Gradle if available
        if (Test-Path $gradlePath) {
            $buildResult = & $gradlePath -p $projectDir assembleDebug --no-daemon 2>&1
        } else {
            Write-Host "ERROR: Build failed!" -ForegroundColor Red
            Write-Host "Build output:" -ForegroundColor Red
            Write-Host $buildResult
            Write-Host ""
            Write-Host "Suggestion: Try building from Android Studio (Build -> Make Project)" -ForegroundColor Yellow
            exit 1
        }
    }
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Build failed!" -ForegroundColor Red
        Write-Host "Build output:" -ForegroundColor Red
        Write-Host $buildResult
        Write-Host ""
        Write-Host "Suggestion: Try building from Android Studio (Build -> Make Project)" -ForegroundColor Yellow
        exit 1
    }
    
    Write-Host "Build completed successfully!" -ForegroundColor Green
    Write-Host ""
}

# Install on emulator
if (-not $BuildOnly) {
    Write-Host "Installing app on emulator..." -ForegroundColor Yellow
    
    # Find the APK
    $apkPath = "$projectDir\app\build\outputs\apk\debug\app-debug.apk"
    
    if (-not (Test-Path $apkPath)) {
        Write-Host "ERROR: APK not found at: $apkPath" -ForegroundColor Red
        Write-Host "Please build the project first." -ForegroundColor Red
        exit 1
    }
    
    # Uninstall existing app (optional, comment out if you want to keep data)
    Write-Host "Uninstalling existing app (if present)..." -ForegroundColor Gray
    & "$androidSdkPath\platform-tools\adb.exe" uninstall com.gurukulaboard 2>&1 | Out-Null
    
    # Install new APK
    Write-Host "Installing new APK..." -ForegroundColor Yellow
    $installResult = & "$androidSdkPath\platform-tools\adb.exe" install -r $apkPath 2>&1
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Installation failed!" -ForegroundColor Red
        Write-Host $installResult
        exit 1
    }
    
    Write-Host "App installed successfully!" -ForegroundColor Green
    Write-Host ""
    
    # Launch the app
    Write-Host "Launching app..." -ForegroundColor Yellow
    & "$androidSdkPath\platform-tools\adb.exe" shell am start -n com.gurukulaboard/.auth.LoginActivity
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "SUCCESS! App deployed and launched!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "The app should now be running on your emulator." -ForegroundColor Green
    Write-Host ""
}

