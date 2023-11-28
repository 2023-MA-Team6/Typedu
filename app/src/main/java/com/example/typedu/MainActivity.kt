package com.example.typedu

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.typedu.databinding.ActivityMainBinding
import com.example.typedu.databinding.DialogSetArticleBinding
import com.example.typedu.databinding.DialogSetLangaugeBinding
import com.example.typedu.databinding.DialogSetTargetBinding
import java.util.Locale

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

        val alertDialog =  AlertDialog.Builder(this)
            .setView(setTargetDialog.root)
            .setCancelable(true)
            .create()

        setTargetDialog.cancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    fun showArticleDialog() {
        val setArticleDialog = DialogSetArticleBinding.inflate(layoutInflater)
        val titles = resources.getStringArray(R.array.article_title)
        val adapter = ArrayAdapter(this, R.layout.listview_typing_list, titles)

        setArticleDialog.articlesList.adapter = adapter

         val alertDialog = AlertDialog.Builder(this)
            .setView(setArticleDialog.root)
            .setCancelable(true)
            .create()

        setArticleDialog.nextBtn.setOnClickListener {
            showTargetDialog()
        }
        setArticleDialog.cancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    fun showLanguageSetDialog() {
        val setLanguageSetDialog = DialogSetLangaugeBinding.inflate(layoutInflater)
        val languages = resources.getStringArray(R.array.language)
        val adapter = ArrayAdapter(this, R.layout.spinner_language_list, languages)

        setLanguageSetDialog.languageSpinner.adapter = adapter

        val alertDialog = AlertDialog.Builder(this)
            .setView(setLanguageSetDialog.root)
            .create()
        setLanguageSetDialog.cancelBtn.setOnClickListener {
            alertDialog.dismiss()
            recreate()
        }

        setLanguageSetDialog.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                val selectedLanguage = setLanguageSetDialog.languageSpinner.selectedItem as String
                Log.d("언어", selectedLanguage)
                when (selectedLanguage){
                    "한국어" -> setLocate("ko-rKR")
                    "English" -> {
                        setLocate("en")
                    }
                    "日本語" -> setLocate("ja")
                }
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        alertDialog.show()
    }

    // 언어 설정 - "en", "ja", "ko-rKR" 입력하면 그에 맞게 언어가 바뀜
    private fun setLocate(Lang: String) {
        Log.d("로그", "setLocate")
        val locale = Locale(Lang)
        Locale.setDefault(locale)

        val config = Configuration()

        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My_Lang", Lang)
        editor.apply()
    }
}