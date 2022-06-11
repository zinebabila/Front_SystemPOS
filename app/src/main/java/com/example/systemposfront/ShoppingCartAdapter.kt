package com.example.systemposfront

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.StrictMode
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.systemposfront.bo.CartItem
import com.example.systemposfront.bo.ShoppingCart
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL


class ShoppingCartAdapter(var context: Context, var cartItems: List<CartItem>) :
    RecyclerView.Adapter<ShoppingCartAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ShoppingCartAdapter.ViewHolder {

        // The layout design used for each list item
        val layout = LayoutInflater.from(context).inflate(R.layout.cart_list_item, parent, false)

        return ViewHolder(layout)
    }

    // This returns the size of the list.
    override fun getItemCount(): Int = cartItems.size

    override fun onBindViewHolder(viewHolder: ShoppingCartAdapter.ViewHolder, position: Int) {

        //we simply call the `bindItem` function here
        viewHolder.bindItem(cartItems[position])
    }
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemImage: ImageView
        var itemTitle: TextView
        var itemDetail: TextView


        init {
            itemImage = itemView.findViewById(R.id.product_im)
            itemTitle = itemView.findViewById(R.id.product_na)
            itemDetail = itemView.findViewById(R.id.product_pric)

        }

        fun bindItem(cart: CartItem) {
            itemTitle.text = "${cart.product?.title.toString()}"
            if(cart.product.reduction!! >0){
            itemDetail.text = (cart.product.prix?.minus(((cart.product.prix!! * cart.product.reduction!!)/100))).toString()+"$"}
            else{
                itemDetail.text  ="${cart.product.prix.toString()}$"
            }

            val SDK_INT = Build.VERSION.SDK_INT
            if (SDK_INT > 8) {
                val policy = StrictMode.ThreadPolicy.Builder()
                    .permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val `in`: InputStream =
                    URL("http://192.168.2.106:9090/images/get/"+cart.product.images?.id!!).openConnection().getInputStream()
                var profilePic = BitmapFactory.decodeStream(`in`)

                val stream = ByteArrayOutputStream()
                profilePic.compress(Bitmap.CompressFormat.PNG, 100, stream)

                itemImage.setImageBitmap(profilePic)
                // imagePro.setImageBitmap(StringToBitMap(response.body()!!))
            }
            // This displays the cart item information for each item


         //   ShoppingCart.addItem(item,t2.text.toString().toInt())
           // itemAdd = itemView.findViewById(R.id.addToCart)
            val inc=itemView.findViewById<LinearLayout>(R.id.incc)
            val t1  = itemView.findViewById< TextView>(R.id.t11)
            val t2  = itemView.findViewById< TextView>(R.id.t22)
            val t3  = itemView.findViewById< TextView>(R.id.t33)
            val remov=itemView.findViewById<ImageView>(R.id.button)
             t2.text="${cart.quantity.toString()}"

            t1.setOnClickListener(View.OnClickListener {
                var y: Int = t2.getText().toString().toInt()
                y--
                if (y == 0) {
                    ShoppingCart.removeItem(cart,itemView.context as ShoppingCartActivity)
                    (itemView.context as ShoppingCartActivity).refreshActivtiy()
                } else {
                    t2.setText(y.toString())
                    ShoppingCart.modifier(cart,t2.text.toString().toInt())
                    (itemView.context as ShoppingCartActivity).modifierprix()
                //    (itemView.context as ProfilActivity).modifiercounter()
                   // cart.quantity=t2.text.toString().toInt()


                }
            })

            t3.setOnClickListener(View.OnClickListener {
                var y: Int = t2.getText().toString().toInt()
                y++
                if(y>=cart.product.qteStock){
                    val dialog = Dialog(itemView.context as ProfilActivity, R.style.DialogStyle)
                    dialog.setContentView(R.layout.layout_custom_dialog)
                    dialog.getWindow()?.setBackgroundDrawableResource(R.drawable.bg_window)
                    var texttitle:TextView=dialog.findViewById(R.id.txttite)
                    texttitle.text="Alert !"

                    var textview:TextView=dialog.findViewById(R.id.txtDesc)
                    textview.text="No enought Stock"
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
                    t2.setText(y.toString())
                    ShoppingCart.modifier(cart,t2.text.toString().toInt())
                    (itemView.context as ShoppingCartActivity).modifierprix()
                }

               // (itemView.context as ProfilActivity).modifiercounter()
               // cart.quantity=t2.text.toString().toInt()


            })
            remov.setOnClickListener(View.OnClickListener {

                ShoppingCart.removeItem(cart,itemView.context as ShoppingCartActivity)
                (itemView.context as ShoppingCartActivity).refreshActivtiy()

            })
        }


    }

}


