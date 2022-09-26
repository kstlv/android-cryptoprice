package com.kostylev.cryptoprice.models

import com.google.gson.annotations.SerializedName

class Coin : ArrayList<Coin.CoinItem>() {
    data class CoinItem(
        @SerializedName("id")
        val id: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("symbol")
        val symbol: String,
        @SerializedName("current_price")
        val price: String,
        @SerializedName("price_change_percentage_24h")
        val percentage: String,
        @SerializedName("image")
        val image: String
    )
}