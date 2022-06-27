package com.example.tp2_pmr.models

class ListTD(private var title: String = "") {
    private var Items = mutableListOf<ItemTD>();
    fun setTitle(newTitle: String){
        title = newTitle;
    }
    fun getTitle(): String{
        return title;
    }
    fun setItems(newList: MutableList<ItemTD>){
        Items = newList.toMutableList();
    }
    fun getItems(): MutableList<ItemTD>{
        return Items;
    }
    fun search(desc: String): ItemTD?{
        return Items.find{it.getDesc() == desc};
    }
    override fun toString(): String{
        return "List "+title+": "+Items.size+" items";
    }
}