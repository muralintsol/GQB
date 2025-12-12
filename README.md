# GURUKULA BOARD

Android application for managing question banks for Karnataka PU Board (Class 11-12) and competitive exams (NEET, JEE, K-CET).

## Features

- Question Bank Management
- Web Scraping (NCERT & Karnataka PU)
- PDF Processing
- Question Paper Generation
- Export & Print
- Offline Support

## Requirements

- Android 11.0 (API 30) or higher
- Kotlin
- Firebase project setup required

## Setup

1. Clone the repository
2. Create a Firebase project and add `google-services.json` to `app/` directory
3. Sync Gradle files
4. Build and run

## Package Structure

- `com.gurukulaboard.auth` - Authentication module
- `com.gurukulaboard.questionbank` - Question bank management
- `com.gurukulaboard.scraping` - Web scraping functionality
- `com.gurukulaboard.pdf` - PDF processing
- `com.gurukulaboard.paper` - Question paper generation
- `com.gurukulaboard.export` - Export and print functionality
- `com.gurukulaboard.sync` - Offline sync module
- `com.gurukulaboard.admin` - Admin features
- `com.gurukulaboard.models` - Data models

