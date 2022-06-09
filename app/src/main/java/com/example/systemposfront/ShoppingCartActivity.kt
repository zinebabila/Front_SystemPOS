package com.example.systemposfront
import CurrencyAdapter
import android.app.*
import android.app.Notification
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Build

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
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
    var CHANNEL_1_ID = "channel1"
    var  CHANNEL_2_ID = "channel2"
    private var notificationManager: NotificationManagerCompat? = null
    private var notificationManagerPro: NotificationManagerCompat? = null

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
                val dialog = Dialog(this, R.style.DialogStyle)
                dialog.setContentView(R.layout.layout_costumer_dialogue_pay)
                dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.bg_window)
                var texttitle:TextView=dialog.findViewById(R.id.txttite2)
                texttitle.text="Successful payment"
                val animationView: LottieAnimationView = dialog.findViewById(R.id.animation_view2)
                animationView.setAnimation(R.raw.successful)
                animationView.repeatCount = 100
                animationView.playAnimation()
                var textview:TextView=dialog.findViewById(R.id.txtDesc2)
                var nom = intent.getStringExtra("firstname");
                var prenom = intent.getStringExtra("lastName");
                var currency = intent.getStringExtra("nameCurency");
                var sum = intent.getStringExtra("somme");

                textview.text="the client "+nom+" " +prenom+" "+" has paid "+ sum+" "+currency

                val btnClose: ImageView = dialog.findViewById(R.id.btn_close2)

                btnClose.setOnClickListener(View.OnClickListener { dialog.dismiss()
                    intent.removeExtra("action")
                })
                var btnYes:Button=dialog.findViewById(R.id.btn_yes2)
                btnYes.setOnClickListener(View.OnClickListener { dialog.dismiss()
                    intent.removeExtra("action")
                })

                dialog.show()

            }
            if(str.equals("faillure")) {
                val dialog = Dialog(this, R.style.DialogStyle)
                dialog.setContentView(R.layout.layout_costumer_dialogue_pay)
                dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.bg_window)
                var texttitle:TextView=dialog.findViewById(R.id.txttite2)
                texttitle.text="Payment failed"
                val animationView: LottieAnimationView = dialog.findViewById(R.id.animation_view2)
                animationView.setAnimation(R.raw.paymentfailed)
                animationView.repeatCount = 100
                animationView.playAnimation()
                var textview:TextView=dialog.findViewById(R.id.txtDesc2)
                textview.text=""

                val btnClose: ImageView = dialog.findViewById(R.id.btn_close2)

                btnClose.setOnClickListener(View.OnClickListener { dialog.dismiss()
                    intent.removeExtra("action")
                })
                var btnYes:Button=dialog.findViewById(R.id.btn_yes2)
                btnYes.setOnClickListener(View.OnClickListener { dialog.dismiss()
                    intent.removeExtra("action")
                })

                dialog.show()

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
                        var stringRve:String=json.getString("descripReview").toString()
                        var stringPro:String=json.getString("descripProduct").toString()
                        println(stringRve)
                            println("is Review notification")
                            var title: String = "Hello Command Notification"
                        var message:String =""
                        if(!stringPro.equals("null")){
                              message = message+stringPro+"\n"}
                        if(!stringRve.equals("null")){
                             message =message+"\n"+stringRve}
                            var activity: Intent =
                                Intent(this@ShoppingCartActivity, ShoppingCartActivity::class.java)
                            var contentintent: PendingIntent =
                                PendingIntent.getActivity(this@ShoppingCartActivity, 0, activity, 0)
                            val notification: Notification =
                                NotificationCompat.Builder(this@ShoppingCartActivity, CHANNEL_1_ID)
                                    .setSmallIcon(R.drawable.bg_window)
                                    .setContentTitle(title)
                                    .setContentText(message)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                                    .setColor(Color.BLUE)
                                    .setContentIntent(contentintent)
                                    .setAutoCancel(true)
                                    .setOnlyAlertOnce(true)
                                    .build()
                            notificationManager?.notify(1, notification);


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
            .url("http://192.168.86.32:9090/data/subscribes")
            // .header("Accept", "application/json; q=0.5")
            // .addHeader("Accept", "text/event-stream")

            .build()
        println("hello i am here")

        EventSources.createFactory(client)
            .newEventSource(request = request, listener = eventSourceListener)
        notificationManager = NotificationManagerCompat.from(this);
        notificationManagerPro = NotificationManagerCompat.from(this);
         createNotificationChannels()



    }
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(
                CHANNEL_1_ID,
                "Channel 1",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel1.description = "This is Channel 1"
            val channel2 = NotificationChannel(
                CHANNEL_2_ID,
                "Channel 2",
                NotificationManager.IMPORTANCE_LOW
            )
            channel2.description = "This is Channel 2"
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(channel1)
            manager.createNotificationChannel(channel2)
        }
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

                val dialog = Dialog(this@ShoppingCartActivity, R.style.DialogStyle)
                dialog.setContentView(R.layout.layout_custom_dialog)
                dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.bg_window)
                var texttitle:TextView=dialog.findViewById(R.id.txttite)
                texttitle.text="Alert !"

                var textview:TextView=dialog.findViewById(R.id.txtDesc)
                textview.text="Wrong Code ?"
                val btnClose: ImageView = dialog.findViewById(R.id.btn_close)

                btnClose.setOnClickListener(View.OnClickListener { dialog.dismiss()
                })
                var btnYes:Button=dialog.findViewById(R.id.btn_yes)
                btnYes.setOnClickListener(View.OnClickListener {
                    dialog.dismiss()
                } )
                dialog.show()
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
                    val dialog = Dialog(this@ShoppingCartActivity, R.style.DialogStyle)
                    dialog.setContentView(R.layout.layout_custom_dialog)
                    dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.bg_window)
                    var texttitle:TextView=dialog.findViewById(R.id.txttite)
                    texttitle.text="Alert !"

                    var textview:TextView=dialog.findViewById(R.id.txtDesc)
                    textview.text="Code is expired ?"
                    val btnClose: ImageView = dialog.findViewById(R.id.btn_close)

                    btnClose.setOnClickListener(View.OnClickListener { dialog.dismiss()
                    })
                    var btnYes:Button=dialog.findViewById(R.id.btn_yes)
                    btnYes.setOnClickListener(View.OnClickListener {
                        dialog.dismiss()
                    } )
                    dialog.show()
                }
                else{
                    if(coupon!!.num_Uses==coupon!!.maxnum_Uses){

                        val dialog = Dialog(this@ShoppingCartActivity, R.style.DialogStyle)
                        dialog.setContentView(R.layout.layout_custom_dialog)
                        dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.bg_window)
                        var texttitle:TextView=dialog.findViewById(R.id.txttite)
                        texttitle.text="Alert !"

                        var textview:TextView=dialog.findViewById(R.id.txtDesc)
                        textview.text="Copon Coupon is cannot be used!!"
                        val btnClose: ImageView = dialog.findViewById(R.id.btn_close)

                        btnClose.setOnClickListener(View.OnClickListener { dialog.dismiss()
                        })
                        var btnYes:Button=dialog.findViewById(R.id.btn_yes)
                        btnYes.setOnClickListener(View.OnClickListener {
                            dialog.dismiss()
                        } )
                        dialog.show()
                    }


                    else {
                        var prix = calculerprix(coupon!!.reduction)
                        val df = DecimalFormat("0.00") // import java.text.DecimalFormat;
                        val dialog = Dialog(this@ShoppingCartActivity, R.style.DialogStyle)
                        dialog.setContentView(R.layout.costom_dialogue_scc)
                        dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.bg_window)
                        var texttitle:TextView=dialog.findViewById(R.id.txttite1)
                        texttitle.text="Success !"

                        var textview:TextView=dialog.findViewById(R.id.txtDesc1)
                        textview.text="Code is validated \n the total price is " + df.format(
                            prix
                        ) + "$"
                        val btnClose: ImageView = dialog.findViewById(R.id.btn_close1)

                        btnClose.setOnClickListener(View.OnClickListener { dialog.dismiss()
                        })
                        var btnYes:Button=dialog.findViewById(R.id.btn_yes1)
                        btnYes.setOnClickListener(View.OnClickListener {
                            dialog.dismiss()
                        } )
                        dialog.show()
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


        apiService.getMerchant(session.getidAccount()).enqueue(object : retrofit2.Callback<Merchant> {
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



}