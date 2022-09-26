package com.kostylev.cryptoprice

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.databinding.DataBindingUtil
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.google.android.material.chip.Chip
import com.google.gson.JsonParser
import com.kostylev.cryptoprice.adapters.RecyclerViewAdapterCoin
import com.kostylev.cryptoprice.databinding.ActivityMainBinding
import com.kostylev.cryptoprice.helpers.Properties
import com.kostylev.cryptoprice.helpers.RecyclerViewItemClickListener
import com.kostylev.cryptoprice.helpers.Screens
import com.kostylev.cryptoprice.models.Coin
import com.kostylev.cryptoprice.network.ApiCoinGecko
import com.kostylev.cryptoprice.network.Config
import com.kostylev.cryptoprice.viewmodels.ViewModelCoin
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val viewModel: ViewModelCoin by viewModels()

    private lateinit var currencySelected: String
    private var isFirstChip = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        val currencies = arrayOf("USD", "EUR", "RUB", "GBP")
        for (currency in currencies) {
            binding.chipGroup.addView(createChip(this, currency))
        }

        showScreen(Screens.LOADING)
        getData()

        binding.buttonUpdate.setOnClickListener {
            getData()
        }

        binding.swipeRefreshLayout.setOnRefreshListener(OnRefreshListener {
            getData()
            binding.swipeRefreshLayout.isRefreshing = false
        })

        enableClickableHtmlLinksTextView()

        binding.recyclerList.addOnItemTouchListener(
            RecyclerViewItemClickListener(
                this,
                binding.recyclerList,
                object : RecyclerViewItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        getDataCoinScreen(view)
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

    private fun setNightTheme(isNight: Boolean) {
        AppCompatDelegate.setDefaultNightMode(if (isNight) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO)
        delegate.applyDayNight()
    }

    private fun showScreen(screens: Screens){
        supportActionBar?.title = getString(R.string.list)
        binding.layoutCoin.animation = null
        binding.isErrorScreen = false
        binding.isLoadingScreen = false
        binding.isListScreen = false
        binding.isCoinScreen = false
        binding.chipGroup.visibility = View.GONE

        when (screens) {
            Screens.LOADING -> {
                binding.isLoadingScreen = true
                val textLoading = getString(R.string.loading)
                binding.textDescription.text = textLoading
                binding.textCategory.text = textLoading
            }
            Screens.ERROR -> {
                binding.isErrorScreen = true
                val textError = getString(R.string.error)
                binding.textDescription.text = textError
                binding.textCategory.text = textError
            }
            Screens.LIST -> {
                binding.isListScreen = true
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                binding.chipGroup.visibility = View.VISIBLE
            }
            Screens.COIN -> {
                binding.isCoinScreen = true
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }
    }

    private fun createChip(context: Context, title: String): Chip {
        return Chip(context).apply {
            text = title
            isCloseIconVisible = false
            isCheckedIconVisible = false
            if(isFirstChip){
                isChecked = true
                currencySelected = title
                isFirstChip = false
            }

            setOnClickListener {
                if(currencySelected != title){
                    currencySelected = title
                    showScreen(Screens.LOADING)
                    getData()
                }
            }
        }

    }

    private fun getData(){
        Properties.instance?.currency = currencySelected

        val retrofit = Retrofit.Builder().baseUrl(Config.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build()
        val api = retrofit.create(ApiCoinGecko::class.java)
        val getListCall: Call<ArrayList<Coin.CoinItem>> = api.getList(currencySelected, "market_cap_desc", 50, 1)

        val adapter = RecyclerViewAdapterCoin()
        binding.recyclerList.adapter = adapter
        // binding.recyclerList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        getListCall.enqueue(object : Callback<ArrayList<Coin.CoinItem>> {

            override fun onResponse(
                call: Call<ArrayList<Coin.CoinItem>>,
                response: Response<ArrayList<Coin.CoinItem>>
            ) {
                // Log.d("Response", "onResponse: ${response.body()?.get(0)?.name}")
                // binding.isLoading = false
                showScreen(Screens.LIST)
                viewModel.updateData(response.body()!!)
                binding.recyclerList.scheduleLayoutAnimation()
            }

            override fun onFailure(
                call: Call<ArrayList<Coin.CoinItem>>,
                t: Throwable
            ) {
                // binding.isLoading = false
                showScreen(Screens.ERROR)
                t.printStackTrace()
            }
        })

        viewModel.data.observe(this, {
            adapter.update(it)
        })
    }

    private fun getDataCoinScreen(view: View){
        showScreen(Screens.LOADING)
        binding.scrollView.fullScroll(ScrollView.FOCUS_UP) // возврат к изначальному положению ScrollView (самый верх)

        binding.imageCoin.setImageDrawable(view.findViewById<ImageFilterView>(R.id.imageCoinIcon).drawable)

        val coinId = view.findViewById<TextView>(R.id.textId).text.toString()
        if(coinId != "" && coinId.isNotEmpty()){
            Thread(Runnable {
                var json = "";
                try {
                    json = URL(Config.BASE_URL + "coins/$coinId").readText()
                } catch (e: IOException) {
                    //stringBuilder.append("Error : ").append(e.message).append("\n")
                }
                runOnUiThread {
                    if(json != ""){
                        val jsonParser = JsonParser()

                        val jsonObjectDescription = jsonParser.parse(json).asJsonObject.getAsJsonObject("description")
                        var description = jsonObjectDescription["en"].asString
                        if(!stringIsEmpty(description))
                            binding.textDescription.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) Html.fromHtml(description, Html.FROM_HTML_MODE_COMPACT) else Html.fromHtml(description) // html > textview
                        else
                            binding.textDescription.text = getString(R.string.no_data)

                        val jsonArrayCategories = jsonParser.parse(json).asJsonObject.getAsJsonArray("categories")
                        val category = if(jsonArrayCategories.size() != 0) jsonArrayCategories.joinToString(separator = "\n").replace("\"", "") else getString(R.string.no_data)
                        binding.textCategory.text = category

                        val coinName = view.findViewById<TextView>(R.id.textName).text.toString()
                        showScreen(Screens.COIN)
                        supportActionBar?.title = coinName
                        binding.layoutCoin.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.jump))
                    } else {
                        showScreen(Screens.ERROR)
                    }

                }
            }).start()
        }
    }

    private fun stringIsEmpty(text: String): Boolean{
        return text == "" || text == " " || text.isEmpty()
    }

    private fun enableClickableHtmlLinksTextView(){
        binding.textDescription.isClickable = true
        binding.textDescription.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun showMessage(message: String){
        val builder = AlertDialog.Builder(this).setTitle(getString(R.string.information)).setMessage(message)
        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_theme_day -> setNightTheme(false)
            R.id.action_theme_night -> setNightTheme(true)
            R.id.action_info -> showMessage(getString((R.string.about)))
            android.R.id.home -> showScreen(Screens.LIST) // Back icon
        }
        return super.onOptionsItemSelected(item)
    }

    // Device back button
    override fun onBackPressed() {
        if (binding.swipeRefreshLayout.visibility == View.VISIBLE) {
            super.onBackPressed()
        } else {
            showScreen(Screens.LIST)
        }
    }
}