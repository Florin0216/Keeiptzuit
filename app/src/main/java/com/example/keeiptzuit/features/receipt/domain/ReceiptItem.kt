package com.example.keeiptzuit.features.receipt.domain

data class ReceiptItem(
    val name: String = "",
    val unitPrice: String = "0.00",
    val quantity: String = "1",
    val quantityType: String? = "BUC",
    val totalPrice: String = "0.00"
)
