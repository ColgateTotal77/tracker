package com.colgateTotal77.tracker.core.enums

enum class ProductSort(val label: String) {
    LAST_PRICE_ASC("Last Price (Low to High)"),
    LAST_PRICE_DESC("Last Price (High to Low)"),
    PURCHASE_COUNT_DESC("Most Purchased"),
    PURCHASE_COUNT_ASC("Least Purchased"),
    SPENT_COUNT_DESC("Most Spent"),
    SPENT_COUNT_ASC("Least Spent");
}