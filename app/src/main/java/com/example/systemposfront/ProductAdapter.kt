package com.example.systemposfront

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.systemposfront.bo.CartItem
import com.example.systemposfront.bo.Product
import com.example.systemposfront.bo.ShoppingCart
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe


class ProductAdapter(var context: Context, var products: List<Product> = arrayListOf()) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ProductAdapter.ViewHolder {
        // The layout design used for each list item
        val view = LayoutInflater.from(context).inflate(R.layout.product_row_item, p0, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int = products.size

    override fun onBindViewHolder(viewHolder: ProductAdapter.ViewHolder, position: Int) {

        viewHolder.bindProduct(products[position])


        // (context as ProfilActivity).coordinator

    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemImage: ImageView
        var itemTitle: TextView
        var itemDetail: TextView
        var itemremise:TextView
        var priceremise:TextView
        var itemAdd: AppCompatImageButton


        init {
            itemImage = itemView.findViewById(R.id.product_image)
            itemTitle = itemView.findViewById(R.id.product_name)
            itemDetail = itemView.findViewById(R.id.product_price)
            itemremise = itemView.findViewById(R.id.remise)
            priceremise = itemView.findViewById(R.id.product_priceremise)
            itemAdd = itemView.findViewById(R.id.addToCart)
           //  ShoppingCart.deleteCart()
        }

        fun bindProduct(product: Product) {

            itemTitle.text = "${product.title.toString()}"
            if(product.reduction!! >0){
            itemDetail.text = "${product.prix.toString()}$"
                priceremise.text=(product.prix?.minus(((product.prix!! * product.reduction!!)/100))).toString()+"$"
            itemremise.text ="${product.reduction.toString()}"
                itemremise.setBackgroundResource(R.drawable.back)

            }
            else{
                itemDetail.text ="${product.prix.toString()}$"
            }

            Picasso.get().load(product.images[0].urlImage).fit().into(itemImage)
if(product.qteStock!=0){
            Observable.create(ObservableOnSubscribe<MutableList<CartItem>> {

                itemAdd.setOnClickListener { view ->

                    //notify users
                    /*  Snackbar.make(
                        (itemView.context as ProfilActivity).coordinator,
                        "${product.title} added to your cart",
                        Snackbar.LENGTH_LONG
                    ).show()
                    */
                   val dialogBuilder = AlertDialog.Builder(itemView.context as ProfilActivity)
                    dialogBuilder.setTitle("Quantity choice")
                    // set message of alert dialog
                    dialogBuilder.setMessage("You want to add ${product.title} to your cart" +
                            "choose quantity")
                    val inflater = LayoutInflater.from(itemView.context as ProfilActivity)
                    val dialogLayout = inflater.inflate(R.layout.dialog_signin, null)
                   val inc  = dialogLayout.findViewById<LinearLayout>(R.id.inc)
                    inc.setVisibility(View.VISIBLE)
                    val t2  = dialogLayout.findViewById< TextView>(R.id.t2)
                    dialogBuilder.setView(dialogLayout)
                        // if the dialog is cancelable
                        .setCancelable(false)
                        // positive button text and action
                        // negative button text and action

                        .setPositiveButton("OK") { dialogInterface,
                                                   i -> Toast.makeText((itemView.context as ProfilActivity).applicationContext,
                            t2.text.toString()+ "  ${product.title} added to your cart " ,
                            Toast.LENGTH_SHORT).show()

                            val item = CartItem(product )
                            println(t2.text.toString().toInt())

                            ShoppingCart.addItem(item,t2.text.toString().toInt())
                            (itemView.context as ProfilActivity).refreshActivtiy()
                        }
                        .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                                dialog, id -> dialog.cancel()
                        })
                       // .setPositiveButton("OK") { dialog, (itemView.context as ProfilActivity).refreshActivtiy()

                    // create dialog box


                    val t1  = dialogLayout.findViewById< TextView>(R.id.t1)

                    val t3  = dialogLayout.findViewById< TextView>(R.id.t3)


                    t1.setOnClickListener(View.OnClickListener {
                        var y: Int = t2.getText().toString().toInt()
                        y--
                        if (y == 0) {
                            inc.setVisibility(View.GONE)
                        } else {
                            t2.setText(y.toString())
                        }
                         })

                    t3.setOnClickListener(View.OnClickListener {
                        var y: Int = t2.getText().toString().toInt()
                        y++
                        if(y>=product.qteStock){
                            val builder: AlertDialog.Builder = AlertDialog.Builder(itemView.context as ProfilActivity)
                            builder.setMessage("No enought stock ?")
                            builder.setTitle("Alert !")
                            builder.setCancelable(false)
                                .setNegativeButton("Cancel", DialogInterface.OnClickListener {
                                        dialog, id -> dialog.cancel()
                                })
                            val alert = builder.create()
                            alert.show()
                        }
                        else{
                            t2.setText(y.toString())
                        }

                        // (itemView.context as ProfilActivity).modifiercounter()
                        // cart.quantity=t2.text.toString().toInt()


                    })

                    val alert = dialogBuilder.create()
                    // set title for alert dialog box
                    // show alert dialog
                    alert.show()

                }

            }).subscribe { cart ->

                var quantity = 0

                cart.forEach { cartItem ->

                    quantity += cartItem.quantity
                }

                (itemView.context as ProfilActivity).cart_size.text = quantity.toString()

                Toast.makeText(itemView.context, "Cart size $quantity", Toast.LENGTH_SHORT).show()
            }
        }

        }


        /* fun withEditText(view: View) {
             val builder = AlertDialog.Builder(itemView.context as ProfilActivity)
             val inflater = LayoutInflater.from(itemView.context as ProfilActivity)
             builder.setTitle("With EditText")
             val dialogLayout = inflater.inflate(R.layout.dialog_signin, null)
             val editText  = dialogLayout.findViewById<EditText>(R.id.editText)
            builder.setView(dialogLayout)
             builder.setPositiveButton("OK") { dialogInterface, i -> Toast.makeText((itemView.context as ProfilActivity).applicationContext, "EditText is " + editText.text.toString(), Toast.LENGTH_SHORT).show() }
             builder.show()
         }*/
    }
}



