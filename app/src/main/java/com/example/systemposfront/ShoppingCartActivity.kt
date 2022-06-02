package com.example.systemposfront
import CurrencyAdapter
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.systemposfront.bo.*
import com.example.systemposfront.bo.Currency
import com.example.systemposfront.controller.CouponController
import com.example.systemposfront.controller.MerchantController
import com.example.systemposfront.interfaces.AccountEnd
import com.example.systemposfront.security.TokenManager
import io.paperdb.Paper
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class ShoppingCartActivity : AppCompatActivity()
{

    lateinit var adapter: ShoppingCartAdapter
    lateinit var  total_price:TextView
    lateinit var checkout: Button
    lateinit var valider: Button
    lateinit var couponcode:TextView
    private lateinit var apiService: MerchantController
    private lateinit var apiServiceCoupon: CouponController
    private lateinit var merchant : Merchant
    private  var coupon : Coupon?=null
    private  var currency= listOf<Currency>()
    lateinit var session: TokenManager
    private lateinit var curencyAdapter: CurrencyAdapter
    lateinit var currencys_recyclerview: RecyclerView
    var contentEt:String?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Paper.init(this)
        session = TokenManager(applicationContext)
        setContentView(R.layout.activity_shopping_cart)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        //  val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        // supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material)
        //upArrow?.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        println(intent.hasExtra("action"))
        if (intent.hasExtra("action")) {

            var str = intent.getStringExtra("action");
            println(str)

            if(str.equals("succes")) {
                val dialogBuilder = AlertDialog.Builder(this@ShoppingCartActivity)
                dialogBuilder.setTitle("Success")
                val inflater = LayoutInflater.from(this@ShoppingCartActivity)
                val dialogLayout = inflater.inflate(R.layout.dialogue, null)
                dialogBuilder.setView(dialogLayout)
                    .setCancelable(false)
                    .setNegativeButton("OK", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                        goParent()
                    })
                val alert = dialogBuilder.create()
                alert.show()
            }
           else if(str.equals("faillure")) {

               println("ic errrrrrrrrrrrrrrrrrror")
                val dialogBuilder = AlertDialog.Builder(this@ShoppingCartActivity)
                dialogBuilder.setTitle("Success")
                val inflater = LayoutInflater.from(this@ShoppingCartActivity)
                val dialogLayout = inflater.inflate(R.layout.dialoguefailure, null)
                dialogBuilder.setView(dialogLayout)
                    .setCancelable(false)
                    .setNegativeButton("OK", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                        goParentsho()
                    })
                val alert = dialogBuilder.create()
                alert.show()

//                val dialogBuilder = AlertDialog.Builder(this@ShoppingCartActivity)
//                dialogBuilder.setTitle("faillure")
//                val inflater = LayoutInflater.from(this@ShoppingCartActivity)
//                val dialogLayout = inflater.inflate(R.layout.dialoguefailure, null)
//                dialogBuilder.setView(dialogLayout)
//                    .setCancelable(false)
//                    .setNegativeButton("OK", DialogInterface.OnClickListener {
//                            dialog, id -> dialog.cancel()
//
//                    })
            }

        }

        var list: MutableList<CartItem>? =ShoppingCart.getCart()
        if(list!=null){
            adapter = ShoppingCartAdapter(this, list)}
        adapter.notifyDataSetChanged()
        var shopping_cart_recyclerView: RecyclerView = findViewById(R.id.shopping_cart_recyclerView)
        shopping_cart_recyclerView.adapter = adapter

        shopping_cart_recyclerView.layoutManager = LinearLayoutManager(this)

        modifierprix()
        valider=findViewById(R.id.validercoupon)
        valider.setOnClickListener(object : View.OnClickListener {

            override fun onClick(view: View?) {
                couponcode=findViewById(R.id.coupncode)
                AccountEnd.authToken=session.gettokenDetails()
                apiServiceCoupon = AccountEnd.retrofit.create(CouponController::class.java)
                getCoupon(couponcode.text.toString())



            }
        })
        checkout=findViewById(R.id.chekout)
        checkout.setOnClickListener(object : View.OnClickListener {

            override fun onClick(view: View?) {
                AccountEnd.authToken=session.gettokenDetails()
                apiService = AccountEnd.retrofit.create(MerchantController::class.java)

                afficher_devise()



            }
        })

        val eventSourceListener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: okhttp3.Response) {
                super.onOpen(eventSource, response)
                Log.d(ContentValues.TAG, "Connection Opened")
                println("Connection Opened")
            }

            override fun onClosed(eventSource: EventSource) {
                super.onClosed(eventSource)
                Log.d(ContentValues.TAG, "Connection Closed")
                println("Connection Closed")
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                super.onEvent(eventSource, id, type, data)
                Log.d(ContentValues.TAG, "On Event Received! Data -: $data")
                println(data)


                if(!data.equals("connexion")) {
                    var json = JSONObject(data)
                    if (json.getInt("type") == 1) {
                        goparent(
                            json.getString("firstname"),
                            json.getString("lastName"),
                            json.getString("nameCurency"),
                            json.getString("somme"),
                        )

                    }
                    if (json.getInt("type") == 0) {
                        println("faillure")
                        goechec()
                    }
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                super.onFailure(eventSource, t, response)
                Log.d(ContentValues.TAG, "On Failure -: ${response?.body}")
                //     println(t!!.message)
            }
        }

        val client = OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .build()

        val request = Request.Builder()
            .url("http://192.168.86.23:9090/data/subscribes")
            // .header("Accept", "application/json; q=0.5")
            // .addHeader("Accept", "text/event-stream")

            .build()
        println("hello i am here")

        EventSources.createFactory(client)
            .newEventSource(request = request, listener = eventSourceListener)



    }

    private fun goechec() {
        val bundle = Bundle()
        val intent = Intent(this, ShoppingCartActivity::class.java)
        bundle.putString("action", "faillure")
        intent.putExtras(bundle)

        startActivity(intent)
    }

    private fun getCoupon( code:String) {

        apiServiceCoupon.getCouponCode(code).enqueue(object : retrofit2.Callback<Coupon> {
            // apiService.getCategorie().enqueue(object : retrofit2.Callback<List<Category>> {
            override fun onFailure(call: Call<Coupon>, t: Throwable) {

                println(t.message + "*******************************")
                println("null")
                t.message?.let { Log.d("Data error", it) }
                val builder: AlertDialog.Builder = AlertDialog.Builder(this@ShoppingCartActivity)
                builder.setMessage("Wrong Code ?")
                builder.setTitle("Alert !")
                builder.setCancelable(false)
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })
                val alert = builder.create()
                alert.show()

            }

            override fun onResponse(call: Call<Coupon>, response: Response<Coupon>)
            {

                coupon = response.body()!!
                println(coupon)
                println("here")
                var  simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                val formattedDate = simpleDateFormat.parse(coupon!!.expirationDate)
                var calendar = Calendar.getInstance()
                var dateTime = simpleDateFormat.format(calendar.time)
                var acutel=simpleDateFormat.parse(dateTime)

                if(formattedDate.before(acutel)){
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this@ShoppingCartActivity)
                    builder.setMessage("Code is expired ?")
                    builder.setTitle("Alert !")
                    builder.setCancelable(false)
                        .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                                dialog, id -> dialog.cancel()
                        })
                    val alert = builder.create()
                    alert.show()
                }
                else{
                    if(coupon!!.num_Uses==coupon!!.maxnum_Uses){
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this@ShoppingCartActivity)
                        builder.setMessage("Copon Coupon is cannot be used!!")
                        builder.setTitle("Alert !")
                        builder.setCancelable(false)
                            .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                                    dialog, id -> dialog.cancel()
                            })
                        val alert = builder.create()
                        alert.show()
                    }


                    else {


                        var prix = calculerprix(coupon!!.reduction)
                        val df = DecimalFormat("0.00") // import java.text.DecimalFormat;
                        val builder: AlertDialog.Builder = AlertDialog.Builder(this@ShoppingCartActivity)
                        builder.setMessage(
                            "Code is validated \n the total price is " + df.format(
                                prix
                            ) + "$"
                        )
                        builder.setTitle("Success !")
                        builder.setCancelable(false)
                            .setNegativeButton(
                                "Cancel",
                                DialogInterface.OnClickListener { dialog, id ->
                                    dialog.cancel()
                                })
                        val alert = builder.create()
                        alert.show()

                        modifierprixApresCoupon(df.format(prix))
                    }

                }
            }

        })}
    fun calculerprix(reduction: Double?):Double{
        var totalPrice:Double=0.0
        for(cart in ShoppingCart.getCart()!!) {
            var prixremise:Double
            if(cart.product.reduction!! >0){
                prixremise= cart.product.prix?.minus(((cart.product.prix!! * cart.product.reduction!!)/100))!!
            }
            else{
                prixremise= cart.product.prix!!
            }

            totalPrice += prixremise * cart.quantity

        }
        totalPrice -= ((totalPrice * reduction!!) / 100)

        return totalPrice
    }
    private fun modifierprixApresCoupon(totalPrice: String) {

        total_price=findViewById(R.id.total_price)
        total_price.text = "$${totalPrice}"

    }

    private fun afficher_devise() {


        apiService.getMerchant(1).enqueue(object : retrofit2.Callback<Merchant> {
            // apiService.getCategorie().enqueue(object : retrofit2.Callback<List<Category>> {
            override fun onFailure(call: Call<Merchant>, t: Throwable) {

                println(t.message + "*******************************")
                t.message?.let { Log.d("Data error", it) }

            }

            override fun onResponse(call: Call<Merchant>, response: Response<Merchant>)
            {
                merchant = response.body()!!
                println(merchant)
                println(merchant.currencies)


                val dialogBuilder = AlertDialog.Builder(this@ShoppingCartActivity)
                dialogBuilder.setTitle("Currency choice")
                // set message of alert dialog

                val inflater = layoutInflater
                val dialogLayout  = inflater.inflate(R.layout.currency_rececle, null)
                val inc  = dialogLayout.findViewById<RecyclerView>(R.id.currency_rec)
                inc.layoutManager=StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                curencyAdapter = CurrencyAdapter(this@ShoppingCartActivity, merchant.currencies)
                inc.adapter = curencyAdapter
                curencyAdapter.coupon=coupon
                dialogBuilder.setView(dialogLayout)
                    // if the dialog is cancelable
                    .setCancelable(true)

                    // positive button text and action
                    // negative button text and action
                    .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                            dialog, id -> dialog.cancel()
                    })
                val alert = dialogBuilder.create()
                // set title for alert dialog box
                // show alert dialog
                alert.show()

            }

        })


    }

    private fun goparent(string: String, string1: String, string2: String, string3: String) {
        ShoppingCart.deleteCart()
        val bundle = Bundle()
        val intent = Intent(this, ShoppingCartActivity::class.java)
        bundle.putString("action", "succes")
        bundle.putString("firstname", string)
        bundle.putString("lastName", string1)
        bundle.putString("nameCurency", string2)
        bundle.putString("somme", string3)
        intent.putExtras(bundle)

        startActivity(intent)



    }

    fun modifierprix() {
        var totalPrice:Double=0.0
        for(cart in ShoppingCart.getCart()!!) {
            var prixremise:Double
            if(cart.product.reduction!! >0){
                prixremise= cart.product.prix?.minus(((cart.product.prix!! * cart.product.reduction!!)/100))!!
            }
            else{
                prixremise= cart.product.prix!!
            }

            totalPrice += prixremise * cart.quantity

        }
        val df = DecimalFormat("0.00") // import java.text.DecimalFormat;

        total_price=findViewById(R.id.total_price)
        total_price.text = df.format(totalPrice)
    }

    fun refreshActivtiy() {
        recreate();
    }
    private fun goParentsho() {
        val intent = Intent(this, ShoppingCartActivity::class.java)
        startActivity(intent)
    }

    private fun goParent() {
        val intent = Intent(this, ProfilActivity::class.java)
        startActivity(intent)
    }



}