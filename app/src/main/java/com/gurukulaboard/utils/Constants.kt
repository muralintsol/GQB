package com.gurukulaboard.utils

object Constants {
    // Firebase Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_QUESTIONS = "questions"
    const val COLLECTION_QUESTION_PAPERS = "questionPapers"
    const val COLLECTION_SCRAPING_HISTORY = "scrapingHistory"
    
    // SharedPreferences Keys
    const val PREF_NAME = "gurukula_board_prefs"
    const val PREF_USER_ID = "user_id"
    const val PREF_USER_ROLE = "user_role"
    const val PREF_USER_NAME = "user_name"
    const val PREF_IS_LOGGED_IN = "is_logged_in"
    
    // Default difficulty distribution
    const val DEFAULT_EASY_PERCENT = 30
    const val DEFAULT_MEDIUM_PERCENT = 50
    const val DEFAULT_HARD_PERCENT = 20
    
    // URLs
    const val NCERT_URL = "https://ncert.nic.in/"
    const val KARNATAKA_PU_URL = "https://dpue-exam.karnataka.gov.in/kseabdpueqpue/QuestionBankPage"
    
    // Validation
    const val MOBILE_NUMBER_LENGTH = 10
    const val PIN_LENGTH = 4
}

