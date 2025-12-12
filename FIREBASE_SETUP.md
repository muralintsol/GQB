# Firebase Setup Instructions

## Steps to Configure Firebase

1. **Create Firebase Project**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Click "Add project"
   - Enter project name: "Gurukula Board" (or your preferred name)
   - Follow the setup wizard

2. **Add Android App**
   - In Firebase Console, click "Add app" and select Android
   - Package name: `com.gurukulaboard`
   - Download `google-services.json`
   - Replace the placeholder file in `app/google-services.json` with the downloaded file

3. **Enable Firebase Services**
   - **Firestore Database**: 
     - Go to Firestore Database
     - Click "Create database"
     - Start in test mode (we'll update security rules)
   
   - **Storage**:
     - Go to Storage
     - Click "Get started"
     - Start in test mode

4. **Set Up Security Rules**

   Copy the security rules from the plan document or use these MVP rules:

   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if request.auth != null;
       }
     }
   }
   ```

   **Note**: For production, implement proper server-side authentication validation.

5. **Enable Offline Persistence**
   - Firestore offline persistence is enabled in code (AppModule.kt)
   - This allows the app to work offline with cached data

## Collections Structure

The app uses these Firestore collections:
- `users` - User accounts
- `questions` - Question bank
- `questionPapers` - Generated papers
- `scrapingHistory` - Scraping history tracking

