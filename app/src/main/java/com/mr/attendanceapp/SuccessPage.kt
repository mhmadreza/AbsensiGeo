package com.mr.attendanceapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView

class SuccessPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success_page)
        kembali()
    }

    private fun kembali() {
        val kembaliSucces = findViewById<TextView>(R.id.btn_kembaliSuccess)

        kembaliSucces.setOnClickListener(View.OnClickListener {
            val pindahHome = Intent(this, MainActivity::class.java)
            startActivity(pindahHome)
        })

    }
}