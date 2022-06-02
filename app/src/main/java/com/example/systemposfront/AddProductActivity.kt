package com.example.systemposfront

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.example.systemposfront.bo.*
import com.example.systemposfront.controller.CategorieController
import com.example.systemposfront.controller.ProductController
import com.example.systemposfront.interfaces.AccountEnd
import com.example.systemposfront.security.TokenManager
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AddProductActivity : AppCompatActivity() {
    private var etName: EditText? = null
    private var des: EditText? = null
    private var reduction: EditText? = null
    private var qtestock: EditText? = null
    private var etImageURL: EditText? = null
    private var etPrice: EditText? = null
    private var cat: Spinner? = null
    lateinit var session: TokenManager
    private var cats = listOf<Category>()
    private lateinit var apiCat: CategorieController
    private lateinit var apiProduct: ProductController
    private lateinit var category:Category
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = TokenManager(applicationContext)
        setContentView(R.layout.activity_add_product)
        val toolbar: Toolbar = findViewById(R.id.toolbar2)
        //  val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        // supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val upArrow = ContextCompat.getDrawable(this, R.drawable.abc_ic_ab_back_material)
        //upArrow?.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP)
        supportActionBar?.setHomeAsUpIndicator(upArrow)
        etName = findViewById(R.id.etName1)
        des = findViewById(R.id.descrip)
        reduction = findViewById(R.id.reduction)
        qtestock = findViewById(R.id.qtestock)
        etImageURL = findViewById(R.id.image)
        etPrice = findViewById(R.id.Price)
        cat = findViewById(R.id.dynamic_spinner)

     cats= getCtegories()

         category=Category()

        cat?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                category.id=cats[position].id
                println("hereeeeeeeeeeeeeeeeeeeeeee"+cats[position].id)

            }

        }


        findViewById<View>(R.id.canc2).setOnClickListener {

            goToSecondActivity()
        }
        findViewById<View>(R.id.addP).setOnClickListener {
            if(CheckAllFields(etName!!,qtestock!!,etPrice!!,etImageURL)) {
                addProduct()
                goToSecondActivity()
            }
        }


    }

    private fun CheckAllFields(etName: EditText, qtestock: EditText, etPrice: EditText, etImageURL: EditText?): Boolean {
        if (etName.length() == 0) {
            etName.error = "Title is required"
            return false
        }
        if (qtestock.length() == 0) {
            qtestock.error = "Stock is required"
            return false
        }
        if (etPrice.length() == 0) {
            etPrice.error = "Price is required"
            return false
        }

        if (etImageURL?.length() == 0) {
                etImageURL?.error = "image is required"
                return false
            }

        return true

    }

    private fun goToSecondActivity( ) {
            val intent = Intent(this, ProfilActivity::class.java)
            startActivity(intent)

    }
                 fun getCtegories():List<Category> {

                    AccountEnd.authToken = session.gettokenDetails()
                    apiCat = AccountEnd.retrofit.create(CategorieController::class.java)
            apiCat.getCategorie().enqueue(object : retrofit2.Callback<List<Category>> {
                override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                    println(t.message + "*******************************")
                    t.message?.let { Log.d("Data error", it) }
                }

                override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                    println("okkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk")
                     cats = response.body()!!


                    val arrayAdapter = ArrayAdapter(this@AddProductActivity, android.R.layout.simple_spinner_item, cats)
                    cat?.adapter  = arrayAdapter





                }
            })
                     return cats
        }


                private fun addProduct() {
                    var p = Product()
                    p.title = etName?.text.toString()
                    p.description = des?.text.toString()
                    p.reduction = reduction?.text.toString().toDouble()
                    p.prix = etPrice?.text.toString().toDouble()
                    p.qteStock = qtestock?.text.toString().toInt()
                    var image = ImageProducts()
                    image.urlImage = etImageURL?.text.toString()
                    p.images.add(image)
                    var account=Account()
                    var merchant=Merchant()
                    account.id=session.getidAccount()
                    merchant.account=account
                    p.merchant=merchant
                    p.category=category
                    println("addddddddddddddddddddd")
                 //   println(p)
                    AccountEnd.authToken = session.gettokenDetails()
                    apiProduct = AccountEnd.retrofit.create(ProductController::class.java)
                        apiProduct.addProduct(p).enqueue(object : Callback<Product> {
                        override fun onResponse(call: Call<Product>, response: Response<Product>) {

                            print("okkkkkkkkkkkk")
                        }
                        override fun onFailure(call: Call<Product>, t: Throwable) {
                            println(t.message)
                        }
                    })


                }
}