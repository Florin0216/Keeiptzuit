package com.example.keeiptzuit.features.receipt.domain

data class Receipt(
    val id: String = "",
    val date: String = "",
    val totalAmount: String = "0.00",
    val items: List<ReceiptItem> = emptyList()
)
