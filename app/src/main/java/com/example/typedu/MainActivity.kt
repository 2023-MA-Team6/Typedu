package com.example.typedu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.typedu.databinding.ActivityMainBinding
import com.example.typedu.databinding.DialogSetArticleBinding
import com.example.typedu.databinding.DialogSetLangaugeBinding
import com.example.typedu.databinding.DialogSetTargetBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 자판 연습 버튼 클릭
        binding.keyPrBtn.setOnClickListener {
            Log.d("MainPage", "[메인] 자판 연습 버튼 클릭")
        }

        // 낱말 연습 버튼 클릭
        binding.wordPrBtn.setOnClickListener {
            Log.d("MainPage", "[메인] 낱말 연습 버튼 클릭")
        }

        // 짧은 글 연습 버튼 클릭
        binding.sentencePrBtn.setOnClickListener {
            Log.d("MainPage", "[메인] 짧은 글 연습 버튼 클릭")

            showTargetDialog()
        }

        // 긴 글 연습 버튼 클릭
        binding.articlePrBtn.setOnClickListener {
            Log.d("MainPage", "[메인] 긴 글 연습 버튼 클릭")

            showArticleDialog()
        }


        // 언어 설정 버튼 클릭
        binding.languageSettingBtn.setOnClickListener {
            Log.d("MainPage", "[메인] 언어 설정 버튼 클릭")

            showLanguageSetDialog();
        }
    }

    // 다이얼로그 -> 커스텀 다이얼로그로 변경 필요
    fun showTargetDialog() {
        val setTargetDialog = DialogSetTargetBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setView(setTargetDialog.root)
            .create()
            .show()
    }

    fun showArticleDialog() {
        val setArticleDialog = DialogSetArticleBinding.inflate(layoutInflater)
        val titles = resources.getStringArray(R.array.article_title)
        val adapter = ArrayAdapter(this, R.layout.listview_typing_list, titles)

        setArticleDialog.articlesList.adapter = adapter

        AlertDialog.Builder(this)
            .setView(setArticleDialog.root)
            .create()
            .show()

        setArticleDialog.nextBtn.setOnClickListener {
            showTargetDialog()
        }
        setArticleDialog.cancelBtn.setOnClickListener {
            // 취소
        }
        setArticleDialog.articlesList.setOnItemClickListener { parent, view, position, id ->
            Log.d("ArticleDialog", "$position")
        }
    }

    fun showLanguageSetDialog() {
        val setLanguageSetDialog = DialogSetLangaugeBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setView(setLanguageSetDialog.root)
            .create()
            .show()
    }
}