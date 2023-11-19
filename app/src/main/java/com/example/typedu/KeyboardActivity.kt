package com.example.typedu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.typedu.databinding.ActivityKeyboardBinding
import com.example.typedu.databinding.ActivityShortParagraphBinding

class KeyboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityKeyboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}