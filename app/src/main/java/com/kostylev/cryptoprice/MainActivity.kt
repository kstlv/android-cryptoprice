package com.kostylev.cryptoprice

import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.kostylev.cryptoprice.adapters.RecyclerViewAdapterCoin
import com.kostylev.cryptoprice.databinding.ActivityMainBinding
import com.kostylev.cryptoprice.helpers.RecyclerViewItemClickListener
import com.kostylev.cryptoprice.models.Coin
import com.kostylev.cryptoprice.network.ApiCoinGecko
import com.kostylev.cryptoprice.network.Config
import com.kostylev.cryptoprice.viewmodels.ViewModelCoin
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val viewModel: ViewModelCoin by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        getData()

        binding.isCoinScreen = false
        binding.isListScreen = true

        binding.recyclerList.addOnItemTouchListener(
            RecyclerViewItemClickListener(
                this,
                binding.recyclerList,
                object : RecyclerViewItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        showMessage("position: $position")

                        binding.isCoinScreen = true
                        binding.isListScreen = false
                    }

                    override fun onItemLongClick(view: View?, position: Int) {

                    }
                })
        )
    }

    override fun onResume() {
        super.onResume()
        applyCustomTheme()
    }

    private fun applyCustomTheme() {
        if (this.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_NO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.statusBarColor = Color.WHITE
            }

            supportActionBar?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }

        supportActionBar?.elevation = 0f
    }

    private fun getData(){
        val retrofit = Retrofit.Builder().baseUrl(Config.BASE_URL).addConverterFactory(
            GsonConverterFactory.create()).build()
        val api = retrofit.create(ApiCoinGecko::class.java)
        val getListCall: Call<ArrayList<Coin.CoinItem>> = api.getList("USD", "market_cap_desc", 50, 1)

        val adapter = RecyclerViewAdapterCoin()
        binding.recyclerList.adapter = adapter
        // binding.recyclerList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        getListCall.enqueue(object : Callback<ArrayList<Coin.CoinItem>> {

            override fun onResponse(
                call: Call<ArrayList<Coin.CoinItem>>,
                response: Response<ArrayList<Coin.CoinItem>>
            ) {
                // Log.d("Response", "onResponse: ${response.body()?.get(0)?.name}")
                viewModel.updateData(response.body()!!)
                binding.recyclerList.scheduleLayoutAnimation()
            }

            override fun onFailure(
                call: Call<ArrayList<Coin.CoinItem>>,
                t: Throwable
            ) {
                t.printStackTrace()
            }
        })

        viewModel.data.observe(this, {
            adapter.update(it)
        })
    }

    private fun showMessage(message: String){
        val builder = AlertDialog.Builder(this).setTitle(getString(R.string.information)).setMessage(message)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }
}