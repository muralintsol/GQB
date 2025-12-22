# Build and Deploy Scripts

This project includes build scripts to compile, build, and deploy the GURUKULA BOARD app to an Android emulator.

## Available Scripts

### PowerShell Script (Recommended for Windows)
- **File**: `build-and-deploy.ps1`
- **Usage**: 
  ```powershell
  .\build-and-deploy.ps1
  ```
  
  **Options**:
  - `-Clean`: Clean the project before building
  - `-BuildOnly`: Only build, don't install
  - `-InstallOnly`: Only install (assumes APK already exists)

  **Examples**:
  ```powershell
  # Full build and deploy
  .\build-and-deploy.ps1
  
  # Clean build and deploy
  .\build-and-deploy.ps1 -Clean
  
  # Build only
  .\build-and-deploy.ps1 -BuildOnly
  
  # Install only (if APK exists)
  .\build-and-deploy.ps1 -InstallOnly
  ```

### Batch Script (Alternative for Windows)
- **File**: `build-and-deploy.bat`
- **Usage**: 
  ```cmd
  build-and-deploy.bat
  ```
  Simply double-click the file or run from command prompt.

## Prerequisites

1. **Android Studio** installed
   - Default location: `C:\Program Files\Android\Android Studio`
   - Or: `%LOCALAPPDATA%\Programs\Android Studio`

2. **Android SDK** installed
   - Default location: `%LOCALAPPDATA%\Android\Sdk`

3. **Android Emulator** running or available
   - The script will attempt to start an emulator if none is running
   - Or start manually from Android Studio

## What the Scripts Do

1. **Environment Setup**
   - Sets JAVA_HOME to Android Studio's JDK
   - Sets ANDROID_HOME to Android SDK
   - Adds necessary tools to PATH

2. **Emulator Check**
   - Checks if an emulator is running
   - If not, attempts to start the first available AVD
   - Waits for emulator to be ready

3. **Build**
   - Cleans project (if `-Clean` flag is used)
   - Builds debug APK using Gradle
   - Falls back to Android Studio's Gradle if project Gradle fails

4. **Deploy**
   - Uninstalls existing app (to ensure clean install)
   - Installs new APK on emulator
   - Launches the app automatically

## Troubleshooting

### Build Fails with JDK Image Transformation Error

This is a known issue with Gradle and Android Gradle Plugin. Solutions:

1. **Use Android Studio UI** (Recommended)
   - Open project in Android Studio
   - Click `Build → Make Project` (Ctrl+F9)
   - Click `Run` button (Shift+F10)

2. **Clear Gradle Cache**
   ```powershell
   Remove-Item -Recurse -Force "$env:USERPROFILE\.gradle\caches\transforms-3"
   ```

3. **Update Android Gradle Plugin**
   - Edit `build.gradle` (project level)
   - Update AGP version if needed

### Emulator Not Found

1. Create an AVD in Android Studio:
   - Tools → Device Manager
   - Create Virtual Device
   - Select device and system image

2. Or start emulator manually:
   ```cmd
   "%LOCALAPPDATA%\Android\Sdk\emulator\emulator.exe" -avd YOUR_AVD_NAME
   ```

### Permission Denied (PowerShell)

If you get a permission error running the PowerShell script:

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
```

## Manual Build Steps

If scripts don't work, you can build manually:

1. **Open in Android Studio**
   - File → Open → Select project folder

2. **Build**
   - Build → Make Project (Ctrl+F9)

3. **Run**
   - Click Run button (Shift+F10)
   - Or: Run → Run 'app'

## APK Location

After successful build, the APK is located at:
```
app\build\outputs\apk\debug\app-debug.apk
```

## Notes

- First build may take 5-10 minutes (downloading dependencies)
- Subsequent builds are much faster
- The scripts use Android Studio's embedded JDK to avoid path issues
- If build fails, Android Studio UI usually works better

