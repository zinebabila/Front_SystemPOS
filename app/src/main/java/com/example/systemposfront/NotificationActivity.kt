package com.example.systemposfront

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.systemposfront.bo.Notification
import com.example.systemposfront.controller.NotificationController
import com.example.systemposfront.interfaces.AccountEnd
import com.example.systemposfront.security.TokenManager
import retrofit2.Call
import retrofit2.Response

class NotificationActivity : AppCompatActivity() {
    private lateinit var apiService: NotificationController
    private lateinit var productAdapter: Notif_Adapter
    lateinit var session: TokenManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notifi_activity)
        session = TokenManager(applicationContext)

        val toolbar: Toolbar = findViewById(R.id.toolbar1)
        //  val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        // supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material)
        //upArrow?.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        AccountEnd.authToken = session.gettokenDetails()
        apiService = AccountEnd.retrofit.create(NotificationController::class.java)
        getNotification()
    }
    fun getNotification() {

        apiService.getNotification(session.getidAccount()).enqueue(object : retrofit2.Callback<ArrayList<Notification>> {
            // apiService.getCategorie().enqueue(object : retrofit2.Callback<List<Category>> {
            override fun onFailure(call: Call<ArrayList<Notification>>, t: Throwable) {

                println(t.message + "*******************************")
                // t.message?.let { Log.d("Data error", it) }

            }

            override fun onResponse(
                call: Call<ArrayList<Notification>>,
                response: Response<ArrayList<Notification>>
            ) {

                var products = response.body()!!
                println(products)

                productAdapter = Notif_Adapter(this@NotificationActivity,products)
                var shopping_cart_recyclerView: RecyclerView = findViewById(R.id.noti_RecycleView)
                shopping_cart_recyclerView.layoutManager = LinearLayoutManager(this@NotificationActivity)
                shopping_cart_recyclerView.adapter = productAdapter


            }

        })

    }
}


