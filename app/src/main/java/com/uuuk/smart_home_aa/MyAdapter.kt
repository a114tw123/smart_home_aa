package com.uuuk.smart_home_aa

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView


class MyAdapter : BaseAdapter(){


    private lateinit var list: ArrayList<String>
    lateinit var weeklist: Array<String>
    lateinit var clocklist: Array<String>
    lateinit var context:Context
    fun getlist(array: ArrayList<String>,weeklist:Array<String>,clocklist:Array<String>,context:Context){
        this.list=array
        this.weeklist=weeklist
        this.clocklist=clocklist
        this.context=context
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any? {
        return list[position]
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        val holder:ViewHolder
        var view=convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, null)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        val str=list[position].split(",")   //week,ST,ET,token,count
        holder.tv_item.text="星期"+weeklist[str[0].toInt()]+" "+clocklist[str[1].toInt()]+ "到"+clocklist[str[2].toInt()]

        holder.bt_item.setOnClickListener {
            val DB=DBhelper(context).writableDatabase
            DB.execSQL("DELETE FROM nodisturb WHERE count = "+str[4])
            DB.close()
            setting_page().post_del(str[3],str[4])
            list.minusAssign(list[position])
            this.notifyDataSetChanged()
        }
        return view
    }

    inner class ViewHolder(view: View){
        val tv_item=view.findViewById<TextView>(R.id.tv_item)
        val bt_item=view.findViewById<Button>(R.id.bt_item)
    }
}