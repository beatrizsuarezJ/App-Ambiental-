package com.example.childcare

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class AdapterKids (private val hours: ArrayList<Kids>, private val itemClickListener: AdapterKids.OnItemClickListener) :
    RecyclerView.Adapter<AdapterKids.MyViewHolder>(){

    interface OnItemClickListener {
        fun onItemClick(user: Kids)
    }


    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val nombre:TextView = itemView.findViewById(R.id.nombre)
        val asist:TextView = itemView.findViewById(R.id.asistencia)
        val btncard:CardView = itemView.findViewById(R.id.btncard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):AdapterKids.MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_kids,parent,false)
        return AdapterKids.MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AdapterKids.MyViewHolder, position: Int){

        val currentUser = hours[position]


        holder.nombre.text = currentUser.nombre


       holder.asist.text = currentUser.asistencia

        val verde = Color.rgb(0, 128, 0)
        val rojo = Color.rgb(255,0,0)

        if(holder.asist.text == "false"){
            holder.asist.setTextColor(rojo)
            holder.asist.text = "No asistio"
        }else{
            holder.asist.setTextColor(verde)
            holder.asist.text = "Asistio"
        }

        holder.btncard.setCardBackgroundColor(Color.TRANSPARENT)

        holder.btncard.setOnClickListener {
            itemClickListener.onItemClick(currentUser)
        }

    }

    override fun getItemCount(): Int {
        return hours.size
    }


}