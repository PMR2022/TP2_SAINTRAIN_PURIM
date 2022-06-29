package com.example.tp2_pmr.models

class Profile(private var login: String = "", private var Lists: MutableList<ListTD> = mutableListOf<ListTD>(), var hash: String? = null){
    constructor(Lists: MutableList<ListTD>) : this("",Lists,null);
    constructor(login: String, hash: String?) : this(login,mutableListOf<ListTD>(),hash);
    fun getLists(): MutableList<ListTD>{
        return Lists;
    }
    fun setLists(newLists: MutableList<ListTD>){
        Lists = newLists.toMutableList();
    }
    fun addList(newList: ListTD){
        Lists.add(newList);
    }
    fun getLogin(): String{
        return login;
    }
    fun setLogin(newLogin: String){
        login = newLogin;
    }
    override fun toString(): String{
        return "User "+login+": "+Lists.size+" lists";
    }
}