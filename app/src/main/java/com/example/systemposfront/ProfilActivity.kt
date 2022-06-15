
package com.example.systemposfront


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.systemposfront.bo.Category
import com.example.systemposfront.bo.Merchant
import com.example.systemposfront.bo.Product
import com.example.systemposfront.bo.ShoppingCart
import com.example.systemposfront.controller.CategorieController
import com.example.systemposfront.controller.MerchantController
import com.example.systemposfront.controller.ProductController
import com.example.systemposfront.interfaces.AccountEnd
import com.example.systemposfront.security.TokenManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import io.paperdb.Paper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL


class ProfilActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var cart_size:TextView
    lateinit var session: TokenManager
    private lateinit var apiService: ProductController
    private lateinit var apiServiceProcat: ProductController
    private lateinit var apimerchant:MerchantController
    private lateinit var cat: CategorieController

    private var notificationManager: NotificationManagerCompat? = null
    private lateinit var productAdapter: MovieAdapter
    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private var cats = listOf<Category>()
    private var products = ArrayList<Product>()
    lateinit var menuNav: Menu
    lateinit var mNavigationView: NavigationView
    lateinit var recyclerView:RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Paper.init(this)

        session = TokenManager(applicationContext)
        setContentView(R.layout.activity_profil)
      //  createNotificationChannels()
        notificationManager = NotificationManagerCompat.from(this);
        mNavigationView  = findViewById(R.id.nav_view)
        menuNav    = mNavigationView.menu
        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        cart_size=findViewById(R.id.cart_size)
        setSupportActionBar(toolbar)
        drawer = findViewById(R.id.drawer_layout)

        toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        mNavigationView.setNavigationItemSelectedListener(this@ProfilActivity)
        val header = mNavigationView.getHeaderView(0)
        var imagePro=header.findViewById<ImageView>(R.id.nav_header_imageView)
        var detail=header.findViewById<TextView>(R.id.nav_header_textView)

         recyclerView = findViewById(R.id.products_recyclerview)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        /***********************les information du compte**********************/
        AccountEnd.authToken = session.gettokenDetails()
        apimerchant = AccountEnd.retrofit.create(MerchantController::class.java)
        apimerchant.getMerchant(session.getidAccount()).enqueue(object : retrofit2.Callback<Merchant> {
            override fun onResponse(call: Call<Merchant>, response: Response<Merchant>) {
                if (response.body() != null) {
                    var merchant = response.body()!!
                    println(merchant.firstName!!)
                    detail.text =
                        merchant.firstName + "  " + merchant.lastName + "\n" + merchant.numTel
                    session.addinfo(merchant.firstName!!,merchant.lastName!!)
                    Picasso.get().load(merchant.image?.Url).into(imagePro)
                } else {
                    println("error")
                }

            }

            override fun onFailure(call: Call<Merchant>, t: Throwable) {
                println(t.message)
            }

        })





        /****************************les categories************************************/
        AccountEnd.authToken = session.gettokenDetails()
        cat = AccountEnd.retrofit.create(CategorieController::class.java)
        getCtegories()


        cart_size.text = ShoppingCart.getShoppingCartSize().toString()
        println(ShoppingCart.getShoppingCartSize().toString()+"--------------------------------------")



        /***************************les produits****************************************/
        if (intent.hasExtra("action")) {
            apiService = AccountEnd.retrofit.create(ProductController::class.java)
            getproductcat(intent.getLongExtra("id",0L))
            }
        else{
            apiService = AccountEnd.retrofit.create(ProductController::class.java)
            getProducts()
        }



        val showCart : FloatingActionButton =findViewById(R.id.basketButton)
        // ShoppingCart.deleteCart()
        showCart.setOnClickListener {
            startActivity(Intent(this, ShoppingCartActivity::class.java))
        }

    }


    private fun getCtegories() {
        cat.getCategorie().enqueue(object : retrofit2.Callback<List<Category>> {
            override fun onFailure(call: Call<List<Category>>, t: Throwable) {
                println(t.message + "*******************************")
                t.message?.let { Log.d("Data error", it) }
            }

            override fun onResponse(call: Call<List<Category>>, response: Response<List<Category>>) {
                println("okkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk")
                cats = response.body()!!
                println(cats)
                menuNav.add(2, 0,0,"Tous Categorie")

                for (name in cats) {
                    println("${name.nameCategory}" + "****************************************************************")
                    menuNav.add( 2, "${name.id?.toInt()}".toInt(),0,"${name.nameCategory}")
                    // menuNav.add("${name.nameCategory}")

                }
            }
        })
    }


    fun getProducts() {


       /* val requestid= RequestBody.create("multipart/form-data".toMediaTypeOrNull(),"2")
        val requestpage= RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "1")
        val requestsize= RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "500")
        val requestsorted= RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "id")
        val requestreverse= RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "true")*/
        apiService.getProducts(session.getidAccount()).enqueue(object : retrofit2.Callback<ArrayList<Product>> {
            // apiService.getCategorie().enqueue(object : retrofit2.Callback<List<Category>> {
            override fun onFailure(call: Call<ArrayList<Product>>, t: Throwable) {

                println(t.message + "*******************************")
                t.message?.let { Log.d("Data error", it) }

            }

            override fun onResponse(
                call: Call<ArrayList<Product>>,
                response: Response<ArrayList<Product>>
            ) {

                if(response.body()!=null) {
                    products = response.body()!!
                    println(products)
                    productAdapter = MovieAdapter(products as ArrayList<Product>)
                    recyclerView.adapter = productAdapter
                }
                else{
                    println("il y a rien")
                }
            }

        })

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        toggle.syncState()
    }
    private fun getproductcat(id: Long): Int {
      /*  val requestidcat= RequestBody.create("multipart/form-data".toMediaTypeOrNull(),id.toString())
        val requestid= RequestBody.create("multipart/form-data".toMediaTypeOrNull(),"1")
        val requestpage= RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "1")
        val requestsize= RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "500")
        val requestsorted= RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "id")
        val requestreverse= RequestBody.create("multipart/form-data".toMediaTypeOrNull(), "true")*/
        AccountEnd.authToken=session.gettokenDetails()
        apiService = AccountEnd.retrofit.create(ProductController::class.java)
        apiService.getProductCat(id,session.getidAccount()).enqueue(object : retrofit2.Callback<ArrayList<Product>> {
            // apiService.getCategorie().enqueue(object : retrofit2.Callback<List<Category>> {
            override fun onFailure(call: Call<ArrayList<Product>>, t: Throwable) {

                println(t.message + "*******************************")
                t.message?.let { Log.d("Data error", it) }

            }

            override fun onResponse(call: Call<ArrayList<Product>>, response: Response<ArrayList<Product>>)
            {
                products = response.body()!!
                println(products)
                productAdapter = MovieAdapter(products as ArrayList<Product>)
                recyclerView.adapter = productAdapter
            }

        })
        return 1
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

            if(item.itemId==0){
                apiService = AccountEnd.retrofit.create(ProductController::class.java)
                getProducts()
                drawer.closeDrawer(GravityCompat.START)
                return true
            }
            if(item.itemId!=0&&item.itemId!=R.id.addProduct&&item.itemId!=R.id.addCat){

                goSame(item.itemId.toLong())
                drawer.closeDrawer(GravityCompat.START)
                return true}

         if(item.itemId==R.id.addProduct){
            goToSecondActivity("addproduct")
            return true
        }
         if(item.itemId==R.id.addCat){
            goToSecondActivity("addCategorie")
            return true
        }
        return false
    }

    fun refreshActivtiy() {
        recreate();
    }
    private fun goToSecondActivity( type:String ) {
        if(type.equals("addproduct")) {
            val bundle = Bundle()
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
        }
        if(type.equals("addCategorie")) {
            val intent = Intent(this, AddCategorieActivity::class.java)
            startActivity(intent)
        }


    }
    private fun goSame(id:Long) {
        val bundle = Bundle()
        val intent = Intent(this, ProfilActivity::class.java)
        bundle.putString("action", "faillure")
        bundle.putLong("id", id)
        intent.putExtras(bundle)

        startActivity(intent)
    }
    fun modifiercounter() {
        cart_size.text = ShoppingCart.getShoppingCartSize().toString()
        println(ShoppingCart.getShoppingCartSize().toString()+"--------------------------------------")
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        val searchItem = menu.findItem(R.id.appSearchBar)
        val searchView = searchItem.actionView as SearchView
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d("newText1", query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                Log.d("newText", newText)
                productAdapter.getFilter().filter(newText)
                return false
            }
        })



        val notification=menu.findItem(R.id.notification)
        notification.setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener {
            val intent = Intent(this@ProfilActivity, NotificationActivity::class.java)
            startActivity(intent)
             false
        })




        return true
    }


}
