package com.example.systemposfront

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.StrictMode
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.systemposfront.bo.Notification
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class Notif_Adapter(var context: Context, var notifis: ArrayList<Notification> = arrayListOf()) :
    RecyclerView.Adapter<Notif_Adapter.ViewHolder>() {
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): Notif_Adapter.ViewHolder {
        // The layout design used for each list item
        val view = LayoutInflater.from(context).inflate(R.layout.notif_adapter_row, p0, false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int = notifis.size

    override fun onBindViewHolder(viewHolder: Notif_Adapter.ViewHolder, position: Int) {

        viewHolder.bindProduct(notifis[position])



    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemImage: ImageView
        var itemDes: TextView
        var itemDate: TextView

        init {
            itemImage = itemView.findViewById(R.id.notif_im)
            itemDes = itemView.findViewById(R.id.noti_des)
            itemDate = itemView.findViewById(R.id.noti_date)
        }

        fun bindProduct(notif: Notification) {
            if(notif.product!=null){
                Picasso.get().load(notif.product!!.images?.Url).into(itemImage)
            }
            else{
                println("************************")
                println(notif.review!!.customer?.image)
                Picasso.get().load(notif.review!!.customer?.image?.Url).into(itemImage)
            }
            itemDes.text=notif.description
            var  simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
            var ago: String? =null
            try {
                val time: Long = simpleDateFormat.parse(notif.dateNotification).getTime()
                val now = System.currentTimeMillis()
                ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS).toString()
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            itemDate.text=ago

        }



    }
}



