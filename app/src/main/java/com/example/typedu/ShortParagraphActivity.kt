package com.example.typedu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.typedu.databinding.ActivityMainBinding
import com.example.typedu.databinding.ActivityShortParagraphBinding

class ShortParagraphActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityShortParagraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}