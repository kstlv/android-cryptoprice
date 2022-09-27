package com.kostylev.cryptoprice.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kostylev.cryptoprice.R
import com.kostylev.cryptoprice.databinding.ItemCoinBinding
import com.kostylev.cryptoprice.helpers.Properties
import com.kostylev.cryptoprice.models.Coin
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


class RecyclerViewAdapterCoin : RecyclerView.Adapter<RecyclerViewAdapterCoin.ViewHolder>() {
    var lists = ArrayList<Coin.CoinItem>()

    fun update(lists: ArrayList<Coin.CoinItem>) {
        this.lists = lists
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_coin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(lists[position])
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemCoinBinding.bind(itemView)
        fun bind(item: Coin.CoinItem) {
            binding.item = item

            val percentage = item.percentage;
            val percentageIsNegative = percentage[0] == '-'
            binding.textPercentage.setTextColor(Color.parseColor(if(percentageIsNegative) "#EB5757" else "#2A9D8F"))
            binding.textPercentage.text = (if(percentageIsNegative) "" else "+") + String.format("%.2f", percentage.toBigDecimal()) + "%"

            val numberFormat: NumberFormat = NumberFormat.getCurrencyInstance()
            numberFormat.maximumFractionDigits = 2 // количество десятичных знаков
            val currency = Properties.instance?.currency.toString()
            numberFormat.currency = Currency.getInstance(currency)
            val price = item.price.toBigDecimal()
            binding.textPrice.text = numberFormat.format(price)

            when (item.id) {
                "bitcoin" -> binding.imageCoinIcon.setImageResource(R.drawable.ic_coin_btc)
                "cardano" -> binding.imageCoinIcon.setImageResource(R.drawable.ic_coin_ada)
                "cosmos" -> binding.imageCoinIcon.setImageResource(R.drawable.ic_coin_atom)
                "binancecoin" -> binding.imageCoinIcon.setImageResource(R.drawable.ic_coin_bnb)
                "ethereum" -> binding.imageCoinIcon.setImageResource(R.drawable.ic_coin_eth)
                "ripple" -> binding.imageCoinIcon.setImageResource(R.drawable.ic_coin_xrp)
                else -> Glide.with(itemView.context).asBitmap().load(item.image).into(binding.imageCoinIcon)
            }
        }
    }
}
