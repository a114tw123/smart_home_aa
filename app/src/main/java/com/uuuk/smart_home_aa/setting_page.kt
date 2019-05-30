package com.uuuk.smart_home_aa

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.NumberPicker
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_setting_page.*
import kotlinx.android.synthetic.main.bot_bt.*
import okhttp3.*
import java.io.IOException


class setting_page : AppCompatActivity(), View.OnClickListener {

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.bt_main->{startActivity(Intent(this,main_page::class.java).putExtra("last_page","setting_page"))}
            R.id.bt_data->{startActivity(Intent(this,data_page::class.java))}
            R.id.bt_contorl->{startActivity(Intent(this,contorl_page::class.java))}
            //R.id.bt_setting->{ startActivity(Intent(this,setting_page::class.java))}
        }
    }

    lateinit var token:String
    var count=0
    val DBhelper=DBhelper(this)
    val datalist= arrayListOf<String>()
    val myAdapter=MyAdapter()
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_page)
        bt_main.setOnClickListener(this)
        bt_data.setOnClickListener(this)
        bt_contorl.setOnClickListener(this)
        bt_setting.setOnClickListener(this)
        title="設定勿擾"

        set_bot_bt()
        get_token()
        get_db()
        val weeklist=resources.getStringArray(R.array.week)
        val clocklist= resources.getStringArray(R.array.clock)

        myAdapter.getlist(datalist,weeklist,clocklist,this)
        listview.adapter=myAdapter

        set_pk(pk_week,weeklist)
        set_pk(pk_start,clocklist)
        set_pk(pk_end,clocklist)

        bt_add.setOnClickListener {
            var repeat=false
            for (i in datalist.indices){
                val str=datalist[i].split(",")
                if (pk_week.value==str[0].toInt()&&pk_start.value==str[1].toInt()&&pk_end.value==str[2].toInt()){
                    repeat=true
                }
            }
            if (!repeat){
                if (pk_start.value<pk_end.value) {
                    toast("星期"+weeklist[pk_week.value]+" "+clocklist[pk_start.value]+
                            "到"+clocklist[pk_end.value])
                    datalist+=pk_week.value.toString()+","+pk_start.value.toString()+","+pk_end.value.toString()+","+token+","+count
                    post_add(token,count,pk_week.value,pk_start.value,pk_end.value)
                    add_db()
                    myAdapter.notifyDataSetChanged()
                }
                else{
                    toast("開始時間需早於結束時間")
                }
            }
            else toast("勿擾時段已重複")
        }
    }

    private fun set_pk(picker: NumberPicker,array: Array<String>){
        picker.minValue=0
        picker.maxValue=array.size-1
        picker.displayedValues=array
        picker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
    }

    private fun toast(message: CharSequence?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

    private fun get_token(){
        FirebaseInstanceId.getInstance()
            .instanceId.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w( "getInstanceId failed", task.exception)
                return@OnCompleteListener
            }
            token = task.result?.token!!
            Log.d("token", token)
        })
    }

    private fun add_db(){
        val DB=DBhelper.writableDatabase
        val cv=ContentValues()
        cv.put("count",count++)
        cv.put("token",token)
        cv.put("week",pk_week.value)
        cv.put("STime",pk_start.value)
        cv.put("ETime",pk_end.value)
        DB.insert(DBhelper.tableName,null,cv)
        DB.close()
    }

    @SuppressLint("Recycle")
    private fun get_db(){
        val DB=DBhelper.readableDatabase
        val cursor=DB.rawQuery("select * from nodisturb", null)
        while (cursor.moveToNext()){
            count=cursor.getInt(cursor.getColumnIndex("count"))
            datalist+=cursor.getInt(cursor.getColumnIndex("week")).toString()+
                    ","+cursor.getInt(cursor.getColumnIndex("STime")).toString()+
                    ","+cursor.getInt(cursor.getColumnIndex("ETime")).toString()+
                    ","+cursor.getString(cursor.getColumnIndex("token")).toString()+","+count
            myAdapter.notifyDataSetChanged()
        }
        count++
        DB.close()
    }

    private fun post_add(token:String,nid:Int,week:Int,st:Int,et:Int){
        val key="FyPda"
        val body=FormBody.Builder()
            .add("token",token)
            .add("nid",nid.toString())
            .add("week",week.toString())
            .add("st",st.toString())
            .add("et",et.toString())
            .build()
        val request=Request.Builder()
            .url("http://piserv007.asuscomm.com:80/fb/addNoNotify?key=$key")
            .post(body)
            .build()
        val call=OkHttpClient().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("post ", e.toString())
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call?, response: Response?) {
                val res= response!!.body()!!.string()
                Log.d("post",res)
            }
        })
    }

    fun post_del(token: String,nid:String){
        val key="FyPda"
        val body=FormBody.Builder()
            .add("token",token)
            .add("nid",nid)
            .build()
        val request=Request.Builder()
            .url("http://piserv007.asuscomm.com:80/fb/delNoNotify?key=$key")
            .post(body)
            .build()
        val call=OkHttpClient().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("post ", e.toString())
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call?, response: Response?) {
                val res= response!!.body()!!.string()
                Log.d("post",res)
            }
        })
    }

    private fun set_bot_bt() {
        bt_setting.textSize = 24F
        bt_data.setTextColor(Color.parseColor("#777777"))
        bt_main.setTextColor(Color.parseColor("#777777"))
        bt_contorl.setTextColor(Color.parseColor("#777777"))
    }

}
