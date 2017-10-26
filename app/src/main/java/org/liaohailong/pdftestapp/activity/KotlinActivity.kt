package org.liaohailong.pdftestapp.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import org.liaohailong.pdftestapp.BaseActivity
import org.liaohailong.pdftestapp.R

/**
 * 第一个用kotlin写的activity
 * Created by LHL on 2017/10/26.
 */
open class KotlinActivity : BaseActivity() {
    companion object {
        fun show(context: Context) {
            val intent = Intent(context, KotlinActivity::class.java)
            context.startActivity(intent)
        }
    }

    var data = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin)
        for (i in 0 until 5) {
            data.add("我是第" + i + "号数据")
        }
        for (datum in data) {
            Toast.makeText(this, datum, Toast.LENGTH_SHORT).show()
        }
    }
}