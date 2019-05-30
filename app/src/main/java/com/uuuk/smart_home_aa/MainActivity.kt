package com.uuuk.smart_home_aa

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.superlht.htloading.view.HTLoading
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    var token=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        get_token()
        HTLoading(this).show()
    }

    override fun onResume() {
        super.onResume()
        val pref= getSharedPreferences("register", MODE_PRIVATE)
        val token_pref=pref.getString("token_pref","")
        val reg_pref=pref.getBoolean("reg_pref",false)
        if (token==token_pref &&reg_pref){
            HTLoading(this).setSuccessText("加载成功！").showSuccess()
            val to_main_page= Intent(this,main_page::class.java)
            to_main_page.putExtra("last_page","Main")
            to_main_page.putExtra("token",token)
            startActivity(to_main_page)
        }
    }

    private fun get_token(){
        FirebaseInstanceId.getInstance()
            .instanceId.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w( "getInstanceId failed", task.exception)
                return@OnCompleteListener
            }
            token = task.result?.token!!
            register(token)
            Log.d("token", token)
        })
    }

    private fun register(token: String){
        val pref= getSharedPreferences("register", MODE_PRIVATE)
        val token_pref=pref.getString("token_pref","")
        val reg_pref=pref.getBoolean("reg_pref",false)

        if (token!=token_pref || !reg_pref){
            pref.edit().putString("token_pref",token).apply()
            val key="FyPda"
            val body= FormBody.Builder()
                .add("token",token)
                .build()
            val request= Request.Builder()
                .url("http://piserv007.asuscomm.com:80/fb/register?key=$key")
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
                    pref.edit().putBoolean("reg_pref",res.getBoolean("ok")).apply()
                    Log.d("post",res.toString())
                    runOnUiThread{
                        onResume()
                    }
                }
            })
        }
        onResume()
    }

}
