package com.kostylev.cryptoprice.view

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kostylev.cryptoprice.models.Coin

class ViewModelCoin : ViewModel() {
    private val _data: MutableLiveData<ArrayList<Coin.CoinItem>> by lazy {
        MutableLiveData<ArrayList<Coin.CoinItem>>()
    }
    val data: MutableLiveData<ArrayList<Coin.CoinItem>> get() = _data

    fun updateData(list: ArrayList<Coin.CoinItem>) {
        _data.postValue(list)
    }
}