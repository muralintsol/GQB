# NCERT PDF Integration Guide

## Overview

The NCERT PDF Integration system allows you to:
1. Process NCERT PDF files from zip archives
2. Extract hierarchical index structure (Chapters → Topics → Subtopics)
3. Store PDFs in Firebase Storage
4. Generate MCQs from specific content sections
5. Generate HTML slides (for PPT conversion) from content

## Setup Instructions

### 1. Prepare Zip Files

Place your NCERT zip files in one of these locations:
- **Recommended**: Copy zip files to app's external storage: `/sdcard/Android/data/com.gurukulaboard/files/NCERT BOOKS/`
- **Alternative**: Use file picker (to be implemented)

The zip files should follow the naming convention:
- `kebo1dd.zip` = Biology Class 11 (English)
- `kech1dd.zip` = Chemistry Class 11 (English)
- `keph1dd.zip` = Physics Class 11 (English)
- `kemh1dd.zip` = Mathematics Class 11 (English)
- `lebo1dd.zip` = Biology Class 11 (Alternative)
- `kech2dd.zip` = Chemistry Class 12 (English)
- etc.

### 2. Process Zip Files

1. Open the app
2. Navigate to **NCERT Books** from the main menu
3. Click **"Scan and Process Zip Files"**
4. Wait for processing to complete (may take several minutes)

The system will:
- Extract PDFs from zip files
- Upload PDFs to Firebase Storage
- Parse index structure from each PDF
- Store metadata in Firestore

### 3. Browse Books and Index

- View all processed books in the NCERT Management screen
- Click on a book to see its chapter/topic/subtopic structure
- Each chapter shows page ranges and topic counts

## Generating MCQs

### Steps:

1. Navigate to **NCERT Books** → Select a book → Select a chapter
2. Click **"Generate MCQ"**
3. Enter number of questions (default: 5)
4. Click **"Generate"**
5. Review generated MCQs
6. Click **"Save All to Question Bank"** to save them

### MCQ Generation Features:

- Rule-based generation from content
- Extracts key concepts and definitions
- Creates 4-option MCQs with correct answers
- Links questions to source chapter/topic

## Generating PPT (HTML Slides)

### Steps:

1. Navigate to **NCERT Books** → Select a book → Select a chapter
2. Click **"Generate PPT"**
3. Click **"Generate Slides"**
4. Preview slides in the WebView
5. Click **"Export"** to save as HTML file
6. Click **"Share"** to share the HTML

### PPT Generation Features:

- Creates HTML slides with embedded CSS
- Title slide, content slides, and summary slide
- Can be converted to PowerPoint using:
  - Online converters (HTML to PPT)
  - Microsoft PowerPoint (File → Open → HTML)
  - Google Slides (Import HTML)

## Data Structure

### Firestore Collections:

1. **ncertBooks** - Book metadata
   - subject, classLevel, language
   - firebaseStorageUrl, localFilePath
   - indexId, status

2. **ncertIndexes** - Hierarchical index structure
   - bookId
   - chapters (with topics and subtopics)
   - totalPages

3. **ncertContent** - Cached extracted content
   - bookId, chapterName, topicName, subtopicName
   - content (text)
   - cachedAt

## File Locations

- **Extracted PDFs**: App's external files directory
- **Generated HTML**: App's external files directory
- **Firebase Storage**: `ncert/{subject}/{class}/{filename}`

## Troubleshooting

### Zip Files Not Found

- Ensure zip files are in the correct location
- Check file permissions
- Try copying files to: `/sdcard/Android/data/com.gurukulaboard/files/NCERT BOOKS/`

### Index Parsing Fails

- Some PDFs may have non-standard index formats
- Check if PDF has a table of contents page
- Manual index entry may be needed for some books

### MCQ Generation Quality

- Rule-based generation works best with structured content
- For better quality, consider integrating AI/LLM API
- Review and edit generated MCQs before saving

### PPT Export Issues

- HTML files can be opened in any browser
- Use online converters or PowerPoint to convert to PPT
- Ensure JavaScript is enabled in WebView for preview

## Future Enhancements

- AI-based MCQ generation (OpenAI API integration)
- Direct PowerPoint (.pptx) file generation
- Batch processing of multiple chapters
- Topic/subtopic level content selection
- Image extraction from PDFs
- Formula and table support

