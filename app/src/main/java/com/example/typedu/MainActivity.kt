package com.example.typedu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.example.typedu.databinding.ActivityMainBinding
import com.example.typedu.databinding.DialogSetTargetBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 자판 연습 버튼 클릭
        binding.keyPrBtn.setOnClickListener {
        }

        // 낱말 연습 버튼 클릭
        binding.wordPrBtn.setOnClickListener {
        }

        // 짧은 글 연습 버튼 클릭
        binding.sentencePrBtn.setOnClickListener {
//            val setTargetDialog = DialogSetTargetBinding.inflate(layoutInflater)
//
//            AlertDialog.Builder(this).run {
//                setView(setTargetDialog.root)
//                show()
//            }

        }

        // 긴 글 연습 버튼 클릭
        binding.articlePrBtn.setOnClickListener {
            Log.d("MainPage", "긴 글 연습 버튼 클릭")
        }


        // 언어 설정 버튼 클릭
        binding.languageSettingBtn.setOnClickListener {

        }
    }
}