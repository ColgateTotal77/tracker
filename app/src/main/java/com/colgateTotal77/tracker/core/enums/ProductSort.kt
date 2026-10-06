package com.colgateTotal77.tracker.core.enums

import com.colgateTotal77.tracker.R

enum class ProductSort(val labelRes: Int) {
    LAST_PRICE_ASC(R.string.sort_last_price_asc),
    LAST_PRICE_DESC(R.string.sort_last_price_desc),
    PURCHASE_COUNT_DESC(R.string.sort_most_purchased),
    PURCHASE_COUNT_ASC(R.string.sort_least_purchased),
    SPENT_COUNT_DESC(R.string.sort_most_spent),
    SPENT_COUNT_ASC(R.string.sort_least_spent);
}