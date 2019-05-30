package com.uuuk.smart_home_aa

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBhelper(context: Context): SQLiteOpenHelper(context, "NoDisturb.db", null, 1) {

    val tableName = "nodisturb"
    override fun onCreate(db: SQLiteDatabase?) {//建立資料庫
        val sql = "CREATE TABLE if not exists $tableName" +
                "( id integer PRIMARY KEY autoincrement, count INTEGER, token TEXT, week INTEGER, STime INTEGER, ETime INTEGER)"
        db!!.execSQL(sql)
    }
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {//更新資料

    }
}