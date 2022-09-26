package com.kostylev.cryptoprice.network

import com.kostylev.cryptoprice.models.Coin
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiCoinGecko {

    @GET("coins/markets")
    fun getList(
        @Query("vs_currency") vsCurrency: String,
        @Query("order") order: String,
        @Query("per_page") perPage: Int,
        @Query("page") page: Int
    ): Call<ArrayList<Coin.CoinItem>>

}