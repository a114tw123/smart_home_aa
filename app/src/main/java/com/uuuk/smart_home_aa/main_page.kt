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
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main_page.*
import kotlinx.android.synthetic.main.bot_bt.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class main_page : AppCompatActivity(), View.OnClickListener {

    override fun onClick(v: View?) {
       when(v?.id){
           //R.id.bt_main->{startActivity(Intent(this,main_page::class.java)) }
           R.id.bt_data->{startActivity(Intent(this,data_page::class.java)) }
           R.id.bt_contorl->{ startActivity(Intent(this,contorl_page::class.java))}
           R.id.bt_setting->{ startActivity(Intent(this,setting_page::class.java))}

       }
    }
    var resumed=true
    private var last_page= ""
    private var surr=true//t室內f室外

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)
        bt_main.setOnClickListener(this)
        bt_data.setOnClickListener(this)
        bt_contorl.setOnClickListener(this)
        bt_setting.setOnClickListener(this)
        title="主要頁面"

        last_page= intent.getStringExtra("last_page")
        set_bot_bt()
        get_all(true)
        bt_surr.setOnClickListener {
            if (surr){
                bt_surr.setCompoundDrawablesWithIntrinsicBounds(null,null,
                    ContextCompat.getDrawable(this,R.drawable.outdoor),null)
                bt_surr.text="室外"
                get_all(false)
                surr=!surr
            }
            else{
                bt_surr.setCompoundDrawablesWithIntrinsicBounds(null,null,
                    ContextCompat.getDrawable(this,R.drawable.indoor),null)
                bt_surr.text="室內"
                get_all(false)
                surr=!surr
            }
        }
    }

    override fun onResume() {
        if (resumed){
            Handler().postDelayed({
                get_all(true)
            }, 1000*60)
        }
        super.onResume()
    }

    override fun onBackPressed() {
        if (last_page=="Main")
            moveTaskToBack(true)
        super.onBackPressed()
    }

    private fun set_bot_bt(){
        bt_main.textSize= 24F
        bt_data.setTextColor(Color.parseColor("#777777"))
        bt_contorl.setTextColor(Color.parseColor("#777777"))
        bt_setting.setTextColor(Color.parseColor("#777777"))
    }

    private fun get_all(resume:Boolean){
        val key="FyPda"
        val request= Request.Builder()
            .url("http://piserv007.asuscomm.com:80/get/all?key=$key")
            .get()
            .build()
        val call= OkHttpClient().newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.d("get ", e.toString())
            }
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call?, response: Response?) {
                val res = JSONObject(response!!.body()!!.string())
                val tv_updata_list=arrayOf<TextView>(tv_pm010_updata,tv_pm025_updata,tv_pm100_updata,tv_h_updata,tv_t_updata)
                val tv_data_list=arrayOf<TextView>(tv_pm010,tv_pm025,tv_pm100,tv_h,tv_t)
                val tv_unit= arrayOf<TextView>(pm010,pm025,pm100,h,t)
                val pm_color_list=intArrayOf(
                ContextCompat.getColor(this@main_page,R.color.pm_excellent), ContextCompat.getColor(this@main_page,R.color.pm_good),
                ContextCompat.getColor(this@main_page,R.color.pm_L_polluted), ContextCompat.getColor(this@main_page,R.color.pm_M_polluted),
                ContextCompat.getColor(this@main_page,R.color.pm_H_polluted),ContextCompat.getColor(this@main_page, R.color.pm_S_polluted))

                val data_list = ArrayList<Double>()
                val date_list = ArrayList<String>()
                if (res.getBoolean("ok")) {
                    for (i in 0 until res.getJSONArray("datas").length()) {
                        data_list.add(res.getJSONArray("datas").getJSONObject(i).getDouble("data"))
                        date_list.add(res.getJSONArray("datas").getJSONObject(i).getString("time").substring(5))
                    }
                    runOnUiThread {
                        if (surr) {//t室內f室外
                            for (i in tv_data_list.indices) {
                                tv_unit[i].visibility=View.VISIBLE
                                tv_updata_list[i].text = "更新時間\n"+date_list[i]
                                tv_data_list[i].text = data_list[i].toString()+"\n"
                                if (i == 0 || i == 1) {//pm2.5和pm1.0
                                    when(data_list[i]){
                                        in 0.0..15.4->{tv_data_list[i].setBackgroundColor(pm_color_list[0])}
                                        in 15.5..35.4->{tv_data_list[i].setBackgroundColor(pm_color_list[1])}
                                        in 35.5..55.4->{tv_data_list[i].setBackgroundColor(pm_color_list[2])}
                                        in 55.5..150.4->{tv_data_list[i].setBackgroundColor(pm_color_list[3])}
                                        in 150.5..250.4->{tv_data_list[i].setBackgroundColor(pm_color_list[4])}
                                        else->{tv_data_list[i].setBackgroundColor(pm_color_list[5])}
                                    }
                                }
                                if (i == 2) {
                                    when(data_list[i]){
                                        in 0.0..54.0   ->{tv_data_list[i].setBackgroundColor(pm_color_list[0])}
                                        in 55.0..125.0 ->{tv_data_list[i].setBackgroundColor(pm_color_list[1])}
                                        in 126.0..254.0->{tv_data_list[i].setBackgroundColor(pm_color_list[2])}
                                        in 255.0..354.0->{tv_data_list[i].setBackgroundColor(pm_color_list[3])}
                                        in 355.0..424.0->{tv_data_list[i].setBackgroundColor(pm_color_list[4])}
                                        else->{tv_data_list[i].setBackgroundColor(pm_color_list[5])}
                                    }
                                }
                            }
                        }
                        else{//室外
                            for (i in tv_data_list.indices) {
                                tv_unit[i].visibility=View.VISIBLE
                                tv_updata_list[i].text = "更新時間\n"+date_list[i+6]
                                tv_data_list[i].text = data_list[i+6].toString()+"\n"
                                if (i == 0 || i == 1) {//pm2.5和pm1.0
                                    when(data_list[i+6]){
                                        in 0.0..15.4    ->{tv_data_list[i].setBackgroundColor(pm_color_list[0])}
                                        in 15.5..35.4   ->{tv_data_list[i].setBackgroundColor(pm_color_list[1])}
                                        in 35.5..55.4   ->{tv_data_list[i].setBackgroundColor(pm_color_list[2])}
                                        in 55.5..150.4  ->{tv_data_list[i].setBackgroundColor(pm_color_list[3])}
                                        in 150.5..250.4 ->{tv_data_list[i].setBackgroundColor(pm_color_list[4])}
                                        else->{tv_data_list[i].setBackgroundColor(pm_color_list[5])}
                                    }
                                }
                                if (i == 2) {
                                    when(data_list[i+6]){
                                        in 0.0..54.0   ->{tv_data_list[i].setBackgroundColor(pm_color_list[0])}
                                        in 55.0..125.0 ->{tv_data_list[i].setBackgroundColor(pm_color_list[1])}
                                        in 126.0..254.0->{tv_data_list[i].setBackgroundColor(pm_color_list[2])}
                                        in 255.0..354.0->{tv_data_list[i].setBackgroundColor(pm_color_list[3])}
                                        in 355.0..424.0->{tv_data_list[i].setBackgroundColor(pm_color_list[4])}
                                        else->{tv_data_list[i].setBackgroundColor(pm_color_list[5])}
                                    }
                                }
                            }
                        }
                        if (resume) {onResume()}
                    }
                }
            }
        })
    }

    override fun onPause() {
        resumed=false
        super.onPause()
    }
}
