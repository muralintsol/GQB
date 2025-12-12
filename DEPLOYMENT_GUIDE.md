# GURUKULA BOARD - Step-by-Step Deployment Guide

Follow these steps carefully. Confirm completion of each step before proceeding to the next.

---

## Step 1: Firebase Project Setup

### 1.1 Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click **"Add project"** or **"Create a project"**
3. Enter project name: `Gurukula Board` (or your preferred name)
4. Click **"Continue"**
5. Enable Google Analytics (optional - can skip for now)
6. Click **"Create project"**
7. Wait for project creation to complete
8. Click **"Continue"**

**✅ Confirm:** Have you created the Firebase project? (Yes/No)

---

### 1.2 Add Android App to Firebase
1. In Firebase Console, click **"Add app"** and select **Android** icon
2. Android package name: `com.gurukulaboard`
3. App nickname: `Gurukula Board` (optional)
4. Debug signing certificate SHA-1: (Skip for now - we'll add later)
5. Click **"Register app"**

**✅ Confirm:** Have you registered the Android app in Firebase? (Yes/No)

---

### 1.3 Download google-services.json
1. Click **"Download google-services.json"**
2. Save the file to your computer
3. Copy the file to: `GurukulaBoard/app/google-services.json`
   - Replace the placeholder file that exists there

**✅ Confirm:** Have you downloaded and placed google-services.json in app/ folder? (Yes/No)

---

### 1.4 Enable Firebase Services

#### Enable Firestore Database:
1. In Firebase Console, go to **"Firestore Database"** (left sidebar)
2. Click **"Create database"**
3. Select **"Start in test mode"** (we'll update rules later)
4. Choose a location closest to your users (e.g., `asia-south1` for India)
5. Click **"Enable"**

**✅ Confirm:** Have you enabled Firestore Database? (Yes/No)

#### Enable Storage:
1. In Firebase Console, go to **"Storage"** (left sidebar)
2. Click **"Get started"**
3. Select **"Start in test mode"**
4. Use same location as Firestore
5. Click **"Done"**

**✅ Confirm:** Have you enabled Firebase Storage? (Yes/No)

---

### 1.5 Set Firestore Security Rules
1. In Firebase Console, go to **"Firestore Database"** → **"Rules"** tab
2. Replace the rules with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      // MVP: Allow all authenticated users
      // NOTE: For production, implement proper server-side authentication
      allow read, write: if request.auth != null;
    }
  }
}
```

3. Click **"Publish"**

**✅ Confirm:** Have you set up Firestore security rules? (Yes/No)

---

## Step 2: Android Studio Setup

### 2.1 Install Android Studio
1. Download Android Studio from [developer.android.com](https://developer.android.com/studio)
2. Install Android Studio
3. Open Android Studio
4. Complete the setup wizard

**✅ Confirm:** Is Android Studio installed and opened? (Yes/No)

---

### 2.2 Open Project
1. In Android Studio, click **"Open"**
2. Navigate to the `GurukulaBoard` folder
3. Select the folder and click **"OK"**
4. Wait for Gradle sync to complete (this may take a few minutes)

**✅ Confirm:** Is the project opened and Gradle sync completed? (Yes/No)

---

### 2.3 Verify google-services.json
1. Check that `app/google-services.json` exists
2. Verify it contains your Firebase project details

**✅ Confirm:** Is google-services.json present and correct? (Yes/No)

---

### 2.4 Sync Project
1. In Android Studio, click **"File"** → **"Sync Project with Gradle Files"**
2. Wait for sync to complete
3. Check for any errors in the "Build" output window

**✅ Confirm:** Has Gradle sync completed without errors? (Yes/No)

---

## Step 3: Build Configuration Check

### 3.1 Verify Build Configuration
1. Open `app/build.gradle`
2. Verify `applicationId` is `com.gurukulaboard`
3. Verify `minSdk` is `30`
4. Verify all dependencies are present

**✅ Confirm:** Is build.gradle configured correctly? (Yes/No)

---

### 3.2 Test Build
1. Click **"Build"** → **"Make Project"** (or press Ctrl+F9 / Cmd+F9)
2. Wait for build to complete
3. Check for any build errors

**✅ Confirm:** Does the project build successfully? (Yes/No)

---

## Step 4: Testing on Device/Emulator

### 4.1 Setup Android Emulator (or connect physical device)

#### Option A: Use Physical Device
1. Enable **Developer Options** on your Android device:
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
2. Enable **USB Debugging**:
   - Settings → Developer Options → USB Debugging
3. Connect device to computer via USB
4. In Android Studio, select your device from device dropdown

#### Option B: Use Emulator
1. In Android Studio, click **"Device Manager"** (toolbar icon)
2. Click **"Create Device"**
3. Select a device (e.g., Pixel 6)
4. Select system image (API 30 or higher)
5. Click **"Finish"**
6. Click **"Play"** button to start emulator

**✅ Confirm:** Do you have a device/emulator ready? (Yes/No)

---

### 4.2 Run App
1. Click **"Run"** button (green play icon) or press Shift+F10
2. Select your device/emulator
3. Wait for app to install and launch
4. Test login functionality (you'll need to create a user first via admin)

**✅ Confirm:** Does the app install and run successfully? (Yes/No)

---

## Step 5: Create First Admin User

### 5.1 Create Super Admin User via Firebase Console
Since we don't have a UI for creating the first admin yet, we'll create it directly in Firestore:

1. Go to Firebase Console → Firestore Database
2. Click **"Start collection"**
3. Collection ID: `users`
4. Document ID: (leave empty, auto-generate)
5. Add these fields:
   - `mobileNumber` (string): Your mobile number (10 digits)
   - `pin` (string): Hash of your PIN (use a simple hash calculator or set a known hash)
   - `role` (string): `SUPER_ADMIN`
   - `name` (string): Your name
   - `createdAt` (timestamp): Current timestamp
6. Click **"Save"**

**Note:** For MVP, PIN hashing is simple. In production, use proper bcrypt.

**✅ Confirm:** Have you created the first admin user in Firestore? (Yes/No)

---

## Step 6: Generate Signed APK/AAB

### 6.1 Create Keystore
1. Open Terminal/Command Prompt in Android Studio
2. Navigate to your project directory
3. Run:
```bash
keytool -genkeypair -v -storetype PKCS12 -keystore gurukula-board-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias gurukula-board
```
4. Enter password (remember this!)
5. Fill in certificate information
6. Confirm

**✅ Confirm:** Have you created the keystore file? (Yes/No)

---

### 6.2 Create keystore.properties
1. In project root, create file `keystore.properties`
2. Add:
```
storePassword=YOUR_STORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=gurukula-board
storeFile=../gurukula-board-key.jks
```
3. Replace passwords with your actual passwords

**✅ Confirm:** Have you created keystore.properties? (Yes/No)

---

### 6.3 Update build.gradle for Signing
1. Open `app/build.gradle`
2. Add before `android {` block:
```kotlin
def keystorePropertiesFile = rootProject.file("keystore.properties")
def keystoreProperties = new Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
}
```
3. Inside `android {` block, add:
```kotlin
signingConfigs {
    release {
        keyAlias keystoreProperties['keyAlias']
        keyPassword keystoreProperties['keyPassword']
        storeFile file(keystoreProperties['storeFile'])
        storePassword keystoreProperties['storePassword']
    }
}
```
4. In `buildTypes { release {`, add:
```kotlin
signingConfig signingConfigs.release
```

**✅ Confirm:** Have you configured signing in build.gradle? (Yes/No)

---

### 6.4 Generate Release Build
1. Click **"Build"** → **"Generate Signed Bundle / APK"**
2. Select **"Android App Bundle"** (recommended) or **"APK"**
3. Select your keystore file
4. Enter passwords
5. Select **"release"** build variant
6. Click **"Finish"**
7. Wait for build to complete

**✅ Confirm:** Have you generated the signed AAB/APK? (Yes/No)

---

## Step 7: Testing Release Build

### 7.1 Install Release APK
1. Locate the generated APK/AAB in `app/release/`
2. Transfer to Android device
3. Install APK (enable "Install from Unknown Sources" if needed)
4. Test the app

**✅ Confirm:** Does the release build work correctly? (Yes/No)

---

## Step 8: Google Play Store (Optional)

### 8.1 Create Google Play Developer Account
1. Go to [Google Play Console](https://play.google.com/console/)
2. Pay one-time $25 registration fee
3. Complete account setup

**✅ Confirm:** Do you have a Google Play Developer account? (Yes/No)

---

### 8.2 Create App Listing
1. Click **"Create app"**
2. Fill in app details:
   - App name: GURUKULA BOARD
   - Default language: English
   - App or game: App
   - Free or paid: Free
3. Accept declarations
4. Click **"Create app"**

**✅ Confirm:** Have you created the app listing? (Yes/No)

---

### 8.3 Upload AAB
1. Go to **"Production"** → **"Create new release"**
2. Upload the generated AAB file
3. Add release notes
4. Click **"Save"**

**✅ Confirm:** Have you uploaded the AAB? (Yes/No)

---

### 8.4 Complete Store Listing
1. Fill in store listing details:
   - App name, description, screenshots
   - Feature graphic
   - Privacy policy URL (required)
2. Complete content rating questionnaire
3. Set up pricing and distribution

**✅ Confirm:** Is store listing complete? (Yes/No)

---

### 8.5 Submit for Review
1. Complete all required sections
2. Click **"Submit for review"**
3. Wait for Google's review (typically 1-7 days)

**✅ Confirm:** Have you submitted the app for review? (Yes/No)

---

## Step 9: Post-Deployment

### 9.1 Monitor Firebase Usage
1. Check Firebase Console → Usage and billing
2. Set up billing alerts
3. Monitor Firestore reads/writes

**✅ Confirm:** Have you set up monitoring? (Yes/No)

---

### 9.2 Update Security Rules for Production
1. Implement Cloud Functions for authentication
2. Update Firestore rules to use custom claims
3. Add proper validation

**✅ Confirm:** Do you understand production security requirements? (Yes/No)

---

## Quick Reference

### Common Commands:
- Build: `./gradlew build`
- Clean: `./gradlew clean`
- Install Debug: `./gradlew installDebug`
- Generate Signed APK: Build → Generate Signed Bundle/APK

### Important Files:
- `app/google-services.json` - Firebase configuration
- `keystore.properties` - Signing configuration (keep secret!)
- `app/build.gradle` - Build configuration

### Support:
- Firebase Docs: https://firebase.google.com/docs
- Android Docs: https://developer.android.com
- Stack Overflow: https://stackoverflow.com

---

## Notes:
- Keep `keystore.properties` and `.jks` files secure and backed up
- Never commit keystore files to Git
- Test thoroughly before production release
- Monitor Firebase costs regularly
- Update security rules before production use

