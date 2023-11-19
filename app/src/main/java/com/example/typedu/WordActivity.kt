package com.example.typedu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.typedu.databinding.ActivityShortParagraphBinding
import com.example.typedu.databinding.ActivityWordBinding

class WordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWordBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}