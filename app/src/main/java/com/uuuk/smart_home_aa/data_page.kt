package com.uuuk.smart_home_aa

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.github.mikephil.charting.charts.Chart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import kotlinx.android.synthetic.main.activity_data_page.*
import kotlinx.android.synthetic.main.bot_bt.*
import kotlinx.android.synthetic.main.markerview.view.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt


class data_page : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View?) {
        when(v?.id){
            R.id.bt_main->{startActivity(Intent(this,main_page::class.java).putExtra("last_page","data_page")) }
            //R.id.bt_data->{startActivity(Intent(this,data_page::class.java)) }
            R.id.bt_contorl->{ startActivity(Intent(this,contorl_page::class.java))}
            R.id.bt_setting->{ startActivity(Intent(this,setting_page::class.java))}
            R.id.bt_sel ->{door_dialog()}
        }
    }

    lateinit var data_menu:Array<String>
    lateinit var date_menu:Array<String>
    lateinit var menu1:Array<String>
    lateinit var menu2:Array<String>
    private var door_state=-1

    @SuppressLint("SimpleDateFormat")
    val calendar = Calendar.getInstance()
    var date_scl=0//0本日1本周2本月

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_page)

        bt_main.setOnClickListener(this)
        bt_data.setOnClickListener(this)
        bt_contorl.setOnClickListener(this)
        bt_setting.setOnClickListener(this)
        bt_sel.setOnClickListener(this)

        title="詳細資料"
        data_menu=resources.getStringArray(R.array.data)
        date_menu=resources.getStringArray(R.array.date)
        menu1=resources.getStringArray(R.array.menu1)
        menu2=resources.getStringArray(R.array.menu2)
        sp_date.adapter=ArrayAdapter(this,android.R.layout.simple_list_item_1,resources.getStringArray(R.array.date))
        sp_date.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
               date_scl=position
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        set_bot_bt()
        chart_init()
    }

    private fun set_bot_bt(){
        bt_data.textSize= 24F
        bt_main.setTextColor(Color.parseColor("#777777"))
        bt_contorl.setTextColor(Color.parseColor("#777777"))
        bt_setting.setTextColor(Color.parseColor("#777777"))
    }

    private fun chart_init(){
        val chart_array= arrayOf<LineChart>(lc1,lc2)
        for (i in chart_array.indices){
            chart_array[i].getPaint(Chart.PAINT_INFO).textSize=50f
            chart_array[i].description=null
            chart_array[i].setNoDataTextColor(Color.rgb(10,10,10))
            chart_array[i].data= LineData()
            chart_array[i].axisLeft.axisMaximum=100f
            chart_array[i].axisLeft.axisMinimum=0f
            chart_array[i].axisRight.axisMaximum=100f
            chart_array[i].axisRight.axisMinimum=0f
        }
    }

    private fun door_dialog(){
        AlertDialog.Builder(this)
            .setSingleChoiceItems(arrayOf("室內","室外"),door_state){ _, which ->
                door_state=which
            }
            .setPositiveButton("確定") { _, _ ->
                data_dialog(door_state)//0室內,1室外
            }
            .setNeutralButton("取消"){ _, _ ->
            }
            .show()
    }

    private fun data_dialog(door_state:Int) {
        if (door_state == 0) {//室內
            val checkedStatusList = ArrayList<Boolean>()
            for (s in menu1) {
                checkedStatusList.add(false)
            }
            AlertDialog.Builder(this)
                .setMultiChoiceItems(menu1, checkedStatusList.toBooleanArray()) { _, which, isChecked
                    -> checkedStatusList[which] = isChecked }
                .setPositiveButton("確定") { _, _ ->
                    val sb = StringBuilder()
                    var checked=false
                    for (i in checkedStatusList.indices){
                        if (checkedStatusList[i]){
                            checked=true
                            when(i) {
                                0,1,2,5 -> {
                                    lc1.clear()
                                    lc1.axisLeft.resetAxisMaximum()
                                    lc1.axisLeft.resetAxisMaximum()
                                    lc1.axisRight.resetAxisMaximum()
                                    lc1.axisRight.resetAxisMaximum()
                                    lc1.setNoDataText("讀取資料中")
                                }
                                3,4 -> {
                                    lc2.clear()
                                    lc2.axisLeft.resetAxisMaximum()
                                    lc2.axisLeft.resetAxisMaximum()
                                    lc2.axisRight.resetAxisMaximum()
                                    lc2.axisRight.resetAxisMaximum()
                                    lc2.setNoDataText("讀取資料中")
                                }
                            }
                            sb.append(menu1[i])
                            sb.append(" ")
                            get_data(i + 1,date_scl)//mid1~6
                        }
                    }
                    if (checked){
                        Toast.makeText(this, "你選擇的是${date_menu[date_scl]}$sb", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this, "未選擇資料", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("上一步"){ _ , _ ->
                    door_dialog()
                }
                .setNeutralButton("取消") { _, _ ->
                }
                .show()
        }
        else {//室外
            val checkedStatusList = ArrayList<Boolean>()
            for (s in menu2) {
                checkedStatusList.add(false)
            }
            AlertDialog.Builder(this)
                .setMultiChoiceItems(menu2, checkedStatusList.toBooleanArray()) { _, which, isChecked
                    -> checkedStatusList[which] = isChecked }
                .setPositiveButton("確定") { _, _ ->
                    val sb = StringBuilder()
                    var checked=false
                    for (i in checkedStatusList.indices){
                        if (checkedStatusList[i]){
                            checked=true
                            when(i) {
                                0,1,2 -> {
                                    lc1.clear()
                                    lc1.axisLeft.resetAxisMaximum()
                                    lc1.axisLeft.resetAxisMaximum()
                                    lc1.axisRight.resetAxisMaximum()
                                    lc1.axisRight.resetAxisMaximum()
                                    lc1.setNoDataText("讀取資料中")
                                }
                                3,4 -> {
                                    lc2.clear()
                                    lc2.axisLeft.resetAxisMaximum()
                                    lc2.axisLeft.resetAxisMaximum()
                                    lc2.axisRight.resetAxisMaximum()
                                    lc2.axisRight.resetAxisMaximum()
                                    lc2.setNoDataText("讀取資料中")
                                }
                            }
                            sb.append(menu2[i])
                            sb.append(" ")
                            get_data(i + 7,date_scl)//mid7~11
                        }
                    }
                    if (checked){
                        Toast.makeText(this, "你選擇的是${date_menu[date_scl]}$sb", Toast.LENGTH_SHORT).show()
                    }
                    else{
                        Toast.makeText(this, "未選擇資料", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("上一步"){ _ , _ ->
                    door_dialog()
                }
                .setNeutralButton("取消") { _, _ ->
                }
                .show()
        }
    }

    private fun get_data(mid:Int,date_scl:Int){
        var y=""
        var m=""
        var st=""
        var et=""
        when(date_scl){
            0->{
                y=calendar.get(Calendar.YEAR).toString()
                if (calendar.get(Calendar.MONTH)+1<10){
                    m="0"+(calendar.get(Calendar.MONTH)+1).toString()
                }
                else{
                    m=(calendar.get(Calendar.MONTH)+1).toString()
                }
                if (calendar.get(Calendar.DAY_OF_MONTH)<10){
                    st="0"+calendar.get(Calendar.DAY_OF_MONTH).toString()+"_00:00"
                    et="0"+calendar.get(Calendar.DAY_OF_MONTH).toString()+"_23:59"
                }
                else{
                    st=calendar.get(Calendar.DAY_OF_MONTH).toString()+"_00:00"
                    et=calendar.get(Calendar.DAY_OF_MONTH).toString()+"_23:59"
                }
            }
            1->{
                y=calendar.get(Calendar.YEAR).toString()
                if (calendar.get(Calendar.MONTH)+1<10){
                    m="0"+(calendar.get(Calendar.MONTH)+1).toString()
                }
                else{
                    m=(calendar.get(Calendar.MONTH)+1).toString()
                }
                if (calendar.get(Calendar.DAY_OF_MONTH)<10){
                    if (calendar.get(Calendar.DAY_OF_MONTH)<7){
                            st="01_00:00"
                        }
                    else{st="0"+(calendar.get(Calendar.DAY_OF_MONTH)-7).toString()+"_00:00"}
                    et="0"+calendar.get(Calendar.DAY_OF_MONTH).toString()+"_23:59"
                }
                else{
                    st=calendar.get(Calendar.DAY_OF_MONTH).toString()+"_00:00"
                    et=calendar.get(Calendar.DAY_OF_MONTH).toString()+"_23:59"
                }
            }
            2->{
                y=calendar.get(Calendar.YEAR).toString()
                if (calendar.get(Calendar.MONTH)+1<10){
                    m="0"+(calendar.get(Calendar.MONTH)+1).toString()
                }
                else{
                    m=(calendar.get(Calendar.MONTH)+1).toString()
                }
                st="01_00:00"
                if (calendar.get(Calendar.DAY_OF_MONTH)<10){
                    et="0"+calendar.get(Calendar.DAY_OF_MONTH).toString()+"_23:59"
                }
                else{
                    et=calendar.get(Calendar.DAY_OF_MONTH).toString()+"_23:59"
                }
            }
            3->{
                y="2019"
                m="05"
                st="10_00:00"
                et="17_00:00"
            }
        }
        val color_list= intArrayOf(R.color.indoor_pm010, R.color.indoor_pm025, R.color.indoor_pm100, R.color.indoor_h, R.color.indoor_t,
            R.color.fan_rs, R.color.outdoor_pm010, R.color.outdoor_pm025, R.color.outdoor_pm100, R.color.outdoor_h, R.color.outdoor_t)
        val key="FyPda"
        val body= FormBody.Builder()
            .add("y",y)
            .add("m",m)
            .add("st",st)
            .add("et",et)
            .add("mid",mid.toString())
            .build()
        val request= Request.Builder()
            .url("http://piserv007.asuscomm.com:80/get/m/data?key=$key")
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
                    val data_list_o= arrayListOf<Float>()
                    val date_list_o= arrayListOf<String>()
                    val data_list=arrayListOf<Float>()
                    val date_list= arrayListOf<String>()
                    val entries = arrayListOf<Entry>()

                    if (mid==6){//風扇轉速需運算
                        for (i in 0 until res.getJSONArray("datas").length()){
                            data_list_o.add((res.getJSONArray("datas").getJSONObject(i).getDouble("data")/2.55f).roundToInt().toFloat())
                            date_list_o.add(res.getJSONArray("datas").getJSONObject(i).getString("time"))
                        }
                    }
                    else{
                        for (i in 0 until res.getJSONArray("datas").length()){
                            data_list_o.add(res.getJSONArray("datas").getJSONObject(i).getDouble("data").toFloat())
                            date_list_o.add(res.getJSONArray("datas").getJSONObject(i).getString("time"))
                        }
                    }

                    val div=24//最多顯示24筆資料
                    if (res.getJSONArray("datas").length()>div){//大於僅取24筆
                        val skip=res.getJSONArray("datas").length()/div
                        val data_list_r=arrayListOf<Float>()
                        val date_list_r= arrayListOf<String>()
                        for (i in 0 until div){
                            data_list_r.add(data_list_o.reversed()[skip*i])
                            date_list_r.add(date_list_o.reversed()[skip*i])
                        }
                        for (i in 0 until div){
                            data_list.add(data_list_r.reversed()[i])
                            date_list.add(date_list_r.reversed()[i])
                            entries.add(Entry(i.toFloat(),data_list[i]))
                        }

                    }
                    else{//小於全部顯示
                        for (i in 0 until res.getJSONArray("datas").length()){
                            data_list.add(data_list_o[i])
                            date_list.add(date_list_o[i])
                            entries.add(Entry(i.toFloat(),data_list[i]))
                        }
                    }

                    val dataset = LineDataSet(entries,data_menu[mid-1])
                    dataset.color=ContextCompat.getColor(this@data_page,color_list[mid-1])
                    when(mid){
                        1,2,3,6,7,8,9->{set_chart(dataset,lc1,mid,date_list)}
                        4,5,10,11->{set_chart(dataset,lc2,mid,date_list)}
                    }
                }
                else{
                    if (res.get("msg")=="some field no value or not a number"){
                        runOnUiThread{
                            when(mid){
                                1,2,3,6,7,8,9->{
                                    lc1.clear()
                                    lc1.setNoDataText("此時段沒有資料 請選擇其他時段")}
                                4,5,10,11->{
                                    lc2.clear()
                                    lc2.setNoDataText("此時段沒有資料 請選擇其他時段")}
                            }
                        }
                    }
                }
            }
        })

    }

    @SuppressLint("SetTextI18n")
    private fun set_chart(dataset:LineDataSet,lc:LineChart,mid:Int,date_list:ArrayList<String>){

        for (i in date_list.indices){
            date_list[i]=date_list[i].replace("_","日")
        }

        val xAxis = lc.xAxis
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(date_list)

        val yAxisRight = lc.axisRight
        yAxisRight.granularity = 1f
        yAxisRight.axisMaximum=100f
        yAxisRight.axisMinimum=0f
        if (mid==4||mid==6||mid==10){//將座摽對應至y軸右側
            dataset.axisDependency= yAxisRight.axisDependency
        }

        val yAxisLeft = lc.axisLeft
        yAxisLeft.granularity = 1f
        if (mid==5||mid==11){//設定溫度上下限
            yAxisLeft.axisMaximum=50f
            yAxisLeft.axisMinimum=10f
        }


        dataset.setDrawValues(false)//不顯示數值
        dataset.lineWidth=2f

        lc.legend.isWordWrapEnabled=true//legend換行
        lc.marker=MyMarkerView(this)//點選時下方顯示數值

        if(lc.data==null){
            lc.data=LineData(dataset)
        }
        else lc.data.addDataSet(dataset)

        runOnUiThread{
            when(mid){
                1,2,3,6->{tv_title.text="室內"+resources.getString(R.string.chart_title1)}
                4,5->{tv_title2.text="室內"+resources.getString(R.string.chart_title2)}
                7,8,9->{tv_title.text="室外"+resources.getString(R.string.chart_title1)}
                10,11->{tv_title2.text="室外"+resources.getString(R.string.chart_title2)}
            }
            lc.animateX(1500)
            lc.notifyDataSetChanged()
            lc.invalidate()
        }

    }

    inner class MyMarkerView(context: Context) : MarkerView(context, R.layout.markerview) {

        @SuppressLint("SetTextI18n")
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            tvContent.text = "" + e!!.y
            super.refreshContent(e, highlight)
        }
    }

}

