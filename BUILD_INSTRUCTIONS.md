# APK Build Instructions - GURUKULA BOARD v1.2.0

## Quick Build Guide

### Option 1: Android Studio (Recommended) ⭐

1. **Open Project**
   ```
   File → Open → Select C:\GQB folder
   ```

2. **Wait for Gradle Sync**
   - Let Android Studio sync all dependencies
   - Check for any errors in Build window

3. **Build Release APK**
   ```
   Build → Generate Signed Bundle / APK
   → Select "APK"
   → Click "Next"
   → Create/Select Keystore (or use debug for testing)
   → Select "release" build variant
   → Click "Finish"
   ```

4. **APK Location**
   ```
   app\release\app-release.apk
   ```

### Option 2: PowerShell Script

```powershell
# Build Release APK
.\build-apk.ps1 -Release

# Build Debug APK (for testing)
.\build-apk.ps1 -Debug
```

**Note**: If command-line build fails with JDK image error, use Android Studio instead.

---

## Pre-Build Checklist

- [ ] Firebase project created
- [ ] `google-services.json` in `app/` directory
- [ ] Firestore Database enabled
- [ ] Firebase Storage enabled
- [ ] At least one admin user created in Firestore

---

## Post-Build Steps

1. **Test APK**
   - Install on device: `adb install app-release.apk`
   - Or transfer APK to device and install manually

2. **Share with Testers**
   - Upload to Google Drive/Dropbox
   - Or use Firebase App Distribution
   - Share `TESTER_GUIDE.md` with testers

3. **Monitor**
   - Check Firebase Console for errors
   - Collect tester feedback
   - Track issues

---

## Troubleshooting

### Build Fails
- **JDK Image Error**: Use Android Studio instead of command-line
- **Missing google-services.json**: Download from Firebase Console
- **Gradle Sync Issues**: Invalidate caches and restart Android Studio

### APK Installation Fails
- Enable "Install from Unknown Sources"
- Check device storage space
- Verify APK is not corrupted

---

## File Locations

- **Release APK**: `app\build\outputs\apk\release\app-release.apk`
- **Debug APK**: `app\build\outputs\apk\debug\app-debug.apk`
- **Build Script**: `build-apk.ps1`
- **Documentation**: `DEPLOYMENT_STATUS.md`, `TESTER_GUIDE.md`

---

**Status**: ✅ Ready for Build
**Last Updated**: 2025-01-27

