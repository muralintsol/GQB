package com.gurukulaboard.utils

object Validators {
    
    fun isValidMobileNumber(mobileNumber: String): Boolean {
        return mobileNumber.length == Constants.MOBILE_NUMBER_LENGTH && 
               mobileNumber.all { it.isDigit() }
    }
    
    fun isValidPin(pin: String): Boolean {
        return pin.length == Constants.PIN_LENGTH && 
               pin.all { it.isDigit() } &&
               pin.toIntOrNull()?.let { it in 1900..2100 } == true
    }
    
    fun hashPin(pin: String): String {
        // Simple hash - in production, use proper hashing like bcrypt
        return pin.hashCode().toString()
    }
}

