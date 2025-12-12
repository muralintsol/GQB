# Quick Start Guide - GURUKULA BOARD

## Before You Begin
Make sure you have:
- ✅ Java JDK 17 or higher installed
- ✅ Android Studio installed
- ✅ Android SDK installed
- ✅ Internet connection

---

## Step 1: Firebase Setup ⚠️ REQUIRED

### Create Firebase Project
1. Go to https://console.firebase.google.com/
2. Click "Add project"
3. Name: `Gurukula Board`
4. Click through setup wizard

### Add Android App
1. Click "Add app" → Android
2. Package name: `com.gurukulaboard`
3. Download `google-services.json`
4. Place in: `app/google-services.json` (replace existing)

### Enable Services
1. **Firestore**: Firestore Database → Create database → Test mode
2. **Storage**: Storage → Get started → Test mode

**🛑 STOP HERE - Confirm you've completed Firebase setup before continuing**

Reply with: `Firebase setup complete` when done.

---

## Step 2: Open Project in Android Studio

1. Open Android Studio
2. File → Open → Select `GurukulaBoard` folder
3. Wait for Gradle sync (5-10 minutes first time)

**🛑 Confirm:** Has Gradle sync completed? Reply: `Gradle sync complete`

---

## Step 3: Test Build

1. Click Build → Make Project
2. Check for errors in Build window

**🛑 Confirm:** Does it build successfully? Reply: `Build successful` or describe errors

---

## Step 4: Run on Device/Emulator

### Option A: Physical Device
1. Enable Developer Options (tap Build Number 7 times)
2. Enable USB Debugging
3. Connect via USB
4. Click Run button (green play icon)

### Option B: Emulator
1. Tools → Device Manager
2. Create Virtual Device
3. Select device and API 30+
4. Click Run button

**🛑 Confirm:** Does app launch? Reply: `App launched` or describe issues

---

## Step 5: Create First Admin User

1. Go to Firebase Console → Firestore Database
2. Click "Start collection"
3. Collection: `users`
4. Add document with fields:
   - `mobileNumber`: "9876543210" (your number, 10 digits)
   - `pin`: "2000" (your birth year, will be hashed)
   - `role`: "SUPER_ADMIN"
   - `name`: "Admin Name"
   - `createdAt`: (click timestamp icon)
5. Save

**Note:** PIN is stored as hash. For MVP testing, the hash of "2000" would be used.

**🛑 Confirm:** Is admin user created? Reply: `Admin created`

---

## Step 6: Test Login

1. Run app on device
2. Enter your mobile number
3. Enter PIN (birth year: 2000, 1995, etc.)
4. Click Login

**🛑 Confirm:** Can you login? Reply: `Login works` or describe issue

---

## Next Steps (After Testing)

Once basic functionality works:
- ✅ Generate signed APK for distribution
- ✅ Set up proper security rules
- ✅ Create more users via admin panel
- ✅ Test question bank features
- ✅ Test PDF upload
- ✅ Test paper generation

---

## Need Help?

If you encounter errors:
1. Check the error message carefully
2. Check Firebase console for service status
3. Verify google-services.json is correct
4. Ensure all dependencies downloaded (File → Invalidate Caches → Restart)

---

## Current Status Tracker

Use this to track your progress:

- [ ] Firebase project created
- [ ] google-services.json downloaded
- [ ] Firestore enabled
- [ ] Storage enabled
- [ ] Project opened in Android Studio
- [ ] Gradle sync completed
- [ ] Build successful
- [ ] App runs on device/emulator
- [ ] Admin user created
- [ ] Login tested
- [ ] Ready for next steps

