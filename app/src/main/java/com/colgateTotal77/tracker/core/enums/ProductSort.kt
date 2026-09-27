package com.colgateTotal77.tracker.core.enums

enum class ProductSort(val label: String) {
    AVG_PRICE_ASC("Average Price (Low to High)"),
    AVG_PRICE_DESC("Average Price (High to Low)"),
    LAST_PRICE_ASC("Last Price (Low to High)"),
    LAST_PRICE_DESC("Last Price (High to Low)"),
    PURCHASE_COUNT_DESC("Most Purchased"),
    PURCHASE_COUNT_ASC("Least Purchased");
}