package com.colgateTotal77.tracker.screens.operations.Camera.fiscal

open class ReceiptException(message: String, cause: Throwable? = null) : Exception(message, cause)

class ReceiptNotFoundException(description: String) : ReceiptException("Receipt not found: $description")

class CaptchaRequiredException(description: String? = null) :
    ReceiptException(description ?: "Tax API requires captcha")

class ReceiptApiException(code: String, description: String) :
    ReceiptException("Tax API rejected request: code=$code, description=$description")

class ReceiptHttpException(status: Int) : ReceiptException("Tax API HTTP $status")

class ReceiptDataException(message: String, cause: Throwable? = null) : ReceiptException(message, cause)
