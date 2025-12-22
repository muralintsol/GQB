# GURUKULA BOARD - Tester Guide

## 📱 Installation Instructions

### Step 1: Enable Unknown Sources
1. Go to **Settings** → **Security** (or **Apps** → **Special Access**)
2. Enable **"Install from Unknown Sources"** or **"Install Unknown Apps"**
3. Select your file manager or browser

### Step 2: Install APK
1. Download the APK file (`app-release.apk` or `app-debug.apk`)
2. Open the downloaded file
3. Tap **"Install"**
4. Wait for installation to complete
5. Tap **"Open"** or find "GURUKULA BOARD" in your app drawer

---

## 🔐 First Time Login

### Prerequisites
- **Internet connection** required for first login
- Admin user must be created in Firebase (by developer)

### Login Credentials
- **Mobile Number**: Your registered mobile number
- **PIN**: Your 4-digit birth year PIN

**Note**: If you don't have credentials, contact the administrator.

---

## 📋 Testing Checklist

### ✅ Basic Functionality
- [ ] App launches successfully
- [ ] Login works with valid credentials
- [ ] Main dashboard displays correctly
- [ ] Navigation between screens works

### ✅ Question Bank
- [ ] View question list
- [ ] Search questions
- [ ] Filter questions (by subject, class, etc.)
- [ ] View question details
- [ ] Create new question (if admin)
- [ ] Edit question (if admin)
- [ ] Approve/Reject questions (if admin)

### ✅ Paper Generation
- [ ] Generate question paper
- [ ] Select exam type (PU Board, NEET, JEE)
- [ ] Configure paper settings
- [ ] Preview generated paper
- [ ] Export paper as PDF
- [ ] Print paper
- [ ] Share paper
- [ ] Save paper

### ✅ Saved Papers
- [ ] View saved papers list
- [ ] Open saved paper
- [ ] Delete saved paper (if applicable)

### ✅ NCERT Features (v1.2.0)
- [ ] Access NCERT Books screen
- [ ] View book list (if books are processed)
- [ ] View book details and chapters
- [ ] Generate MCQs from chapter
- [ ] Generate PPT/HTML slides from chapter

### ✅ Admin Features (Admin Users Only)
- [ ] Access Admin Dashboard
- [ ] View user management
- [ ] Create new users
- [ ] Manage user roles

### ✅ Offline Functionality
- [ ] App works without internet (after initial sync)
- [ ] Cached questions are accessible
- [ ] Saved papers are accessible

---

## 🐛 Reporting Issues

When reporting bugs, please include:

1. **Device Information**
   - Device model
   - Android version
   - App version (shown in About/Settings)

2. **Steps to Reproduce**
   - What you were trying to do
   - Step-by-step actions
   - What happened vs. what should happen

3. **Screenshots/Videos**
   - Screenshots of error messages
   - Screen recording if possible

4. **Error Details**
   - Exact error message (if any)
   - When it occurs
   - Frequency (always, sometimes, once)

---

## 💡 Feature Requests

If you have suggestions for improvements:
- Describe the feature
- Explain why it would be useful
- Provide use case examples

---

## 📞 Support

For issues or questions:
- Contact the development team
- Check `DEPLOYMENT_STATUS.md` for known issues
- Review `NCERT_INTEGRATION_GUIDE.md` for NCERT features

---

## ⚠️ Known Limitations

- **NCERT Features**: Requires zip files to be processed first
- **MCQ Generation**: Rule-based (may need refinement)
- **Large PDFs**: Processing may take time
- **Offline Mode**: Requires initial sync with internet

---

## 🎯 Testing Focus Areas

### Priority 1: Core Features
- Login/Authentication
- Question Bank CRUD
- Paper Generation
- PDF Export

### Priority 2: New Features
- NCERT Integration
- MCQ Generation
- PPT Generation

### Priority 3: Edge Cases
- Network connectivity issues
- Large data sets
- Different screen sizes
- Different Android versions

---

**Thank you for testing GURUKULA BOARD!** 🎉

