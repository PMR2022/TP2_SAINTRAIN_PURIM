package com.example.tp2_pmr.models

class ItemTD(private var desc: String = "",private var done: Boolean = false, id: Int = -1, idList: Int = -1, userHash: String? = null) {
    constructor(desc: String) : this(desc, false)
    fun setDesc(newDesc: String){
        desc = newDesc;
    }
    fun getDesc(): String{
        return desc;
    }
    fun setDone(newDone: Boolean){
        done = newDone;
    }
    fun getDone(): Boolean{
        return done;
    }
    override fun toString(): String{
        return "- "+desc+": ["+ (if(done) "x" else " ") + "]";
    }
}