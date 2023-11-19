package com.example.typedu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.typedu.databinding.ActivityLongParagraphBinding
import com.example.typedu.databinding.ActivityShortParagraphBinding

class LongParagraphActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityLongParagraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}