package com.example.systemposfront




import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class ForgotPass : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.singup_merchant)
        val intent = intent
        println(intent.hasExtra("action"))
        if (intent.hasExtra("action")) {

            var str = intent.getStringExtra("action");
            println(str)

            if(str =="succes") {
                val builder: AlertDialog.Builder =
                    AlertDialog.Builder(this@ForgotPass)

                builder.setMessage("success")
                builder.setTitle("payment success !")
                builder.setCancelable(false)
                    .setNegativeButton(
                        "Cancel",
                        DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                            goParent()
                        })
                val alert = builder.create()
                alert.show()
            }
            if(str =="faillure") {
                val builder: AlertDialog.Builder =
                    AlertDialog.Builder(this@ForgotPass)

                builder.setMessage("faillure")
                builder.setTitle("echec !")
                builder.setCancelable(false)
                    .setNegativeButton(
                        "Cancel",
                        DialogInterface.OnClickListener { dialog, id ->
                            dialog.cancel()
                            goParentsho()
                        })
                val alert = builder.create()
                alert.show()
            }

        }


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