# GURUKULA BOARD - APK Build Script
# This script builds the release APK using Android Studio's Gradle

param(
    [switch]$Debug,
    [switch]$Release
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "GURUKULA BOARD - APK Build Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set Android Studio paths
$androidStudioPath = "C:\Program Files\Android\Android Studio"
if (-not (Test-Path $androidStudioPath)) {
    $androidStudioPath = "$env:LOCALAPPDATA\Programs\Android Studio"
}

# Set JDK path
$jdkPath = "$androidStudioPath\jbr"
if (-not (Test-Path $jdkPath)) {
    Write-Host "ERROR: Android Studio JDK not found" -ForegroundColor Red
    Write-Host "Please ensure Android Studio is installed." -ForegroundColor Red
    exit 1
}

# Set Android SDK path
$androidSdkPath = "$env:LOCALAPPDATA\Android\Sdk"
if (-not (Test-Path $androidSdkPath)) {
    Write-Host "ERROR: Android SDK not found at: $androidSdkPath" -ForegroundColor Red
    exit 1
}

# Set environment variables
$env:JAVA_HOME = $jdkPath
$env:ANDROID_HOME = $androidSdkPath
$env:ANDROID_SDK_ROOT = $androidSdkPath
$env:PATH = "$jdkPath\bin;$androidSdkPath\platform-tools;$androidSdkPath\tools;$env:PATH"

Write-Host "Environment Setup:" -ForegroundColor Green
Write-Host "  JAVA_HOME: $env:JAVA_HOME"
Write-Host "  ANDROID_HOME: $env:ANDROID_HOME"
Write-Host ""

# Determine build type
$buildType = "release"
if ($Debug) {
    $buildType = "debug"
}

Write-Host "Building $buildType APK..." -ForegroundColor Yellow
Write-Host ""

# Clean build
Write-Host "Cleaning project..." -ForegroundColor Cyan
& .\gradlew.bat clean
if ($LASTEXITCODE -ne 0) {
    Write-Host "Clean failed!" -ForegroundColor Red
    exit 1
}

# Build APK
Write-Host "Building APK..." -ForegroundColor Cyan
if ($buildType -eq "debug") {
    & .\gradlew.bat assembleDebug
} else {
    & .\gradlew.bat assembleRelease
}

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "Build failed!" -ForegroundColor Red
    Write-Host ""
    Write-Host "TROUBLESHOOTING:" -ForegroundColor Yellow
    Write-Host "1. Try building in Android Studio instead:" -ForegroundColor White
    Write-Host "   Build → Generate Signed Bundle / APK" -ForegroundColor White
    Write-Host "2. Ensure Firebase is configured (google-services.json exists)" -ForegroundColor White
    Write-Host "3. Check that all dependencies are synced" -ForegroundColor White
    exit 1
}

# Find APK
$apkPath = ""
if ($buildType -eq "debug") {
    $apkPath = "app\build\outputs\apk\debug\app-debug.apk"
} else {
    $apkPath = "app\build\outputs\apk\release\app-release.apk"
}

if (Test-Path $apkPath) {
    $apkInfo = Get-Item $apkPath
    $sizeMB = [math]::Round($apkInfo.Length / 1MB, 2)
    
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "APK Location: $apkPath" -ForegroundColor Cyan
    Write-Host "APK Size: $sizeMB MB" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "You can now:" -ForegroundColor Yellow
    Write-Host "1. Install on device: adb install $apkPath" -ForegroundColor White
    Write-Host "2. Share with testers" -ForegroundColor White
    Write-Host "3. Upload to Firebase App Distribution" -ForegroundColor White
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "Build completed but APK not found at expected location" -ForegroundColor Yellow
    Write-Host "Expected: $apkPath" -ForegroundColor Yellow
    Write-Host ""
}

