package com.example.tp2_pmr.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.tp2_pmr.models.ItemTD
import com.example.tp2_pmr.models.Profile
import com.google.gson.Gson

class ItemTdAdapter(
    private val profile: Profile, private val dataSet: List<ItemTD>
) : RecyclerView.Adapter<ItemTdAdapter.ItemViewHolder>(), View.OnClickListener {

    override fun getItemCount(): Int = dataSet.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val itemView = CheckBox(parent.context)
        itemView.setOnClickListener(this)
        return ItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    override fun onClick(view: View) {
        val recyclerList = view.parent as RecyclerView
        val checkBox = view as CheckBox
        val itemTdIndex = recyclerList.getChildLayoutPosition(view)
        dataSet[itemTdIndex].setDone(checkBox.isChecked)

        // Saves new user profile
        val sharedPreferences = view.context.getSharedPreferences("Profiles",0)
        val profileGson = Gson().toJson(profile)
        sharedPreferences?.edit()?.apply {
            putString(profile.getLogin(), profileGson)
            apply()
        }
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox = itemView as? CheckBox

        fun bind(itemTD: ItemTD) {
            checkBox?.text = itemTD.getDesc()
            checkBox?.isChecked = itemTD.getDone()
        }
    }
}