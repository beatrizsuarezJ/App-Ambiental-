package com.example.childcare

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class GenericAdapter<T>(
    private val items: List<T>,
    private val layoutResId: Int,
    private val bind: (item: T, view: View) -> Unit
) : RecyclerView.Adapter<GenericAdapter.GenericViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<T> {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return GenericViewHolder(view, bind)
    }

    override fun onBindViewHolder(holder: GenericViewHolder<T>, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class GenericViewHolder<T>(
        private val view: View,
        private val bindFunction: (item: T, view: View) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        fun bind(item: T) {
            bindFunction(item, view)
        }
    }
}
