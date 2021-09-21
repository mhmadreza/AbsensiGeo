package com.mr.attendanceapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class FailedPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_failed_page)
        kembali()
    }

    private fun kembali() {
        val kembaliError = findViewById<TextView>(R.id.btn_kembaliError)

        kembaliError.setOnClickListener(View.OnClickListener {
            val kembaliHome = Intent(this, MainActivity::class.java)
            startActivity(kembaliHome)
        })
    }
}