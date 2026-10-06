package com.colgateTotal77.tracker.core.enums

import com.colgateTotal77.tracker.R

enum class TransactionSource(val labelRes: Int) {
    NOTIFICATION(R.string.source_notification),
    QR_CODE(R.string.source_qr_code),
    MANUAL(R.string.source_manual)
}