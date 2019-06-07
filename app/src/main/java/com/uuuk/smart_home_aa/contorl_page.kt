package com.uuuk.smart_home_aa

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.android.synthetic.main.activity_contorl_page.*
import kotlinx.android.synthetic.main.bot_bt.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*


class contorl_page : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.bt_main -> {startActivity(Intent(this, main_page::class.java).putExtra("last_page", "contorl_page"))}
            R.id.bt_data -> {startActivity(Intent(this, data_page::class.java))}
            //R.id.bt_contorl->{ startActivity(Intent(this,contorl_page::class.java))}
            R.id.bt_setting -> {startActivity(Intent(this, setting_page::class.java))}

        }
    }

    var resumed=true
    var fan_state=true//t自動f手動
    var ac_state=true//t開啟f關閉
    var first=true
    var fan_val=0
    var ac_val=0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contorl_page)
        bt_main.setOnClickListener(this)
        bt_data.setOnClickListener(this)
        bt_contorl.setOnClickListener(this)
        bt_setting.setOnClickListener(this)
        title="控制設備"

        chart_init()
        set_bot_bt()
        chart_updata()
        fan_init()
        ac_init()
        bt_fan_sw.setOnClickListener {
            if (fan_state){
                fan_state=!fan_state
                bt_fan_sw.background=ContextCompat.getDrawable(this,R.drawable.fan_m)
                tv_fan_val.text=resources.getString(R.string.fan_manual)+"\n"+(sb.progress+1)
                fan_control(sb.progress+1)
                sb.isEnabled=true
            }
            else{
                fan_state=!fan_state
                bt_fan_sw.background=ContextCompat.getDrawable(this,R.drawable.fan_a)
                tv_fan_val.text=resources.getString(R.string.fan_auto)
                fan_control(0)
                sb.isEnabled=false
            }
        }

        bt_ac_sw.setOnClickListener {
            if (ac_state){
                ac_state=!ac_state
                ac_control(ac_state,ac_val)
            }
            else{
                ac_state=!ac_state
                ac_control(ac_state,ac_val)
            }
        }

        bt_ac_down.setOnClickListener {
            if (ac_state){
                if (ac_val>20){
                    ac_control(ac_state,--ac_val)
                }
                else toast("溫度已達到下限")
            }
            else toast("冷氣未開啟")
        }

        bt_ac_up.setOnClickListener{
            if (ac_state){
                if (ac_val<28){
                    ac_control(ac_state,++ac_val)
                }
                else toast("溫度已達到上限")
            }
            else toast("冷氣未開啟")
        }

        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                fan_val=progress+1
                tv_fan_val.text=resources.getString(R.string.fan_manual)+"\n"+(fan_val)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                sb.isEnabled=false
                fan_control(fan_val)
            }
        })

    }

    private fun set_bot_bt(){
        bt_contorl.textSize= 24F
        bt_data.setTextColor(Color.parseColor("#777777"))
        bt_main.setTextColor(Color.parseColor("#777777"))
        bt_setting.setTextColor(Color.parseColor("#777777"))
    }

    private fun chart_init(){
        val fanrs=100f
        val entries = arrayListOf(PieEntry(fanrs,""),PieEntry(100-fanrs,""))
        val colors = arrayListOf(ContextCompat.getColor(this,R.color.fan_rs),ContextCompat.getColor(this,R.color.no_color))
        val dataSet = PieDataSet(entries,"")
        dataSet.colors = colors
        dataSet.setDrawValues(false)
        pc.centerText="取得資料中"
        pc.setCenterTextSize(14f)
        pc.setTouchEnabled(false)
        pc.description=null
        pc.legend.isEnabled=false
        pc.data=PieData(dataSet)
        pc.animateY(1500)
        pc.invalidate()
    }

    override fun onResume() {
        super.onResume()
        if (resumed){
            Handler().postDelayed({
                chart_updata()
            }, 1000*60)
        }
    }

    override fun onPause() {
        resumed=false
        super.onPause()
    }

    private fun chart_updata(){
        val key="FyPda"
        val calendar=Calendar.getInstance()
        val y=calendar.get(Calendar.YEAR).toString()
        var m=""
        if ((calendar.get(Calendar.MONTH)+1)<10){
            m=("0"+(calendar.get(Calendar.MONTH)+1))
        }
        else{
            m=(calendar.get(Calendar.MONTH)+1).toString()
        }
        val body= FormBody.Builder()
            .add("y",y)
            .add("m",m)
            .add("mid","6")
            .build()
        val request= Request.Builder()
            .url("http://piserv007.asuscomm.com:80/get/last?key=$key")
            .post(body)
            .build()
        val call= OkHttpClient().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("post ", e.toString())
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call?, response: Response?) {
                val res = JSONObject(response!!.body()!!.string())
                if (res.getBoolean("ok")) {
                    val fanrs=res.getJSONObject("data").getInt("data")/2.55.toFloat()
                    val time=res.getJSONObject("data").getString("time").substring(3)
                    val entries = arrayListOf(PieEntry(fanrs,"${fanrs.toInt()}.0%"),PieEntry(100-fanrs,""))
                    val colors = arrayListOf(ContextCompat.getColor(this@contorl_page,R.color.fan_rs),
                        ContextCompat.getColor(this@contorl_page,R.color.no_color))
                    val dataSet = PieDataSet(entries,"")
                    dataSet.colors = colors
                    dataSet.setDrawValues(false)
                    pc.clear()
                    pc.centerText="風扇轉速"
                    pc.setCenterTextSize(25f)
                    pc.setTouchEnabled(false)
                    val description= Description()
                    description.text="更新時間 $time"
                    description.textSize=12f
                    pc.description=description
                    pc.legend.isEnabled=false
                    pc.data=PieData(dataSet)
                    runOnUiThread {
                        pc.animateY(1000)
                        pc.invalidate()
                        if (first){
                            first=!first
                        }
                        else{
                            onResume()
                        }
                    }
                }
            }
        })
    }

    private fun fan_control(fan_val:Int){
        val key="FyPda"
        val body= FormBody.Builder()
            .add("speed",fan_val.toString())
            .build()
        val request= Request.Builder()
            .url("http://piserv007.asuscomm.com:80/set/stat/fan?key=$key")
            .post(body)
            .build()
        val call= OkHttpClient().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("post ", e.toString())
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call?, response: Response?) {
                val res= JSONObject(response!!.body()!!.string())
                if (res.getBoolean("ok")){
                    if(fan_val in 1..10){
                        runOnUiThread{
                            Handler().postDelayed({
                                sb.isEnabled=true
                            }, 500)
                        }
                    }
                }
            }
        })
    }

    private fun ac_control(ac_state:Boolean,ac_val:Int){
        val power= if (ac_state){
            "1"
        } else "0"
        val key="FyPda"
        val body= FormBody.Builder()
            .add("power",power)
            .add("val",ac_val.toString())
            .build()
        val request= Request.Builder()
            .url("http://piserv007.asuscomm.com:80/set/stat/air?key=$key")
            .post(body)
            .build()
        val call= OkHttpClient().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("post ", e.toString())
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call?, response: Response?) {
                val res= JSONObject(response!!.body()!!.string())
                if (res.getBoolean("ok")){
                    runOnUiThread {
                        if (ac_state){
                            tv_temp.text="$ac_val°C"
                            bt_ac_sw.background=ContextCompat.getDrawable(this@contorl_page,R.drawable.ac_on)
                        }
                        else {
                            tv_temp.text="溫度"
                            bt_ac_sw.background=ContextCompat.getDrawable(this@contorl_page,R.drawable.ac_off)
                        }
                    }
                }
            }
        })
    }

    private fun fan_init(){
        val key="FyPda"
        val body= FormBody.Builder().build()
        val request= Request.Builder()
            .url("http://piserv007.asuscomm.com:80/get/stat/fan?key=$key")
            .post(body)
            .build()
        val call= OkHttpClient().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("post ", e.toString())
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call?, response: Response?) {
                val res= JSONObject(response!!.body()!!.string())
                if (res.getBoolean("ok")){
                    fan_val=res.getJSONObject("datas").getString("val").toInt()
                    runOnUiThread {
                        if (fan_val>0){//手動
                            sb.progress=fan_val-1
                            bt_fan_sw.background=ContextCompat.getDrawable(this@contorl_page,R.drawable.fan_m)
                            tv_fan_val.text=resources.getString(R.string.fan_manual)+"\n"+(fan_val)
                            sb.isEnabled=true
                            fan_state=false
                        }
                        else{
                            sb.progress=3
                            bt_fan_sw.background=ContextCompat.getDrawable(this@contorl_page,R.drawable.fan_a)
                            tv_fan_val.text=resources.getString(R.string.fan_auto)
                            sb.isEnabled=false
                            fan_state=true
                        }
                    }
                }
            }
        })
    }

    private fun ac_init() {
        val key="FyPda"
        val body= FormBody.Builder().build()
        val request= Request.Builder()
            .url("http://piserv007.asuscomm.com:80/get/stat/air?key=$key")
            .post(body)
            .build()
        val call= OkHttpClient().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("post ", e.toString())
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call?, response: Response?) {
                val res = JSONObject(response!!.body()!!.string())
                if (res.getBoolean("ok")) {
                    ac_val=res.getJSONObject("datas").getString("val").toInt()
                    if(res.getJSONObject("datas").getString("power") == "0"){
                        ac_state=false
                        runOnUiThread {
                            bt_ac_sw.background=ContextCompat.getDrawable(this@contorl_page,R.drawable.ac_off)
                        }
                    }
                    else{
                        runOnUiThread {
                            tv_temp.text="$ac_val°C"
                        }
                    }
                }
            }
        })
    }

    private fun toast(message: CharSequence?, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }
}