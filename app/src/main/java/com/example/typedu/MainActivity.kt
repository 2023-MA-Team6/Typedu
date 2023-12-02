package com.example.typedu

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.typedu.databinding.ActivityMainBinding
import com.example.typedu.databinding.DialogSetArticleBinding
import com.example.typedu.databinding.DialogSetLangaugeBinding
import com.example.typedu.databinding.DialogSetTargetBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var backPressedTime: Long = 0L
    private var targetScore:Int? = 0 // 목표 타수
    private var targetAccuracy:Int? = 0 // 목표 정확도
    private var article:Int = 0
    private var selectedParagraph : String = "null"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 자판 연습 버튼 클릭
        binding.keyPrBtn.setOnClickListener {
            Log.d("MainPage", "[메인] 자판 연습 버튼 클릭")
            val intent = Intent(this, KeyboardActivity::class.java)
            startActivity(intent)
        }

        // 낱말 연습 버튼 클릭
        binding.wordPrBtn.setOnClickListener {
            Log.d("MainPage", "[메인] 낱말 연습 버튼 클릭")
            val intent = Intent(this, WordActivity::class.java)
            startActivity(intent)
        }

        // 짧은 글 연습 버튼 클릭
        binding.sentencePrBtn.setOnClickListener {
            Log.d("MainPage", "[메인] 짧은 글 연습 버튼 클릭")
            showTargetDialog_short()
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

    // 타수 설정
    fun setTarget(scoreInput: EditText, accuracyInput:EditText) {
        if(scoreInput.text.toString() != "")
            targetScore = Integer.parseInt(scoreInput.text.toString())
        else
            targetScore = 250

        if(accuracyInput.text.toString() != "")
            targetAccuracy = Integer.parseInt(accuracyInput.text.toString())
        else
            targetAccuracy = 90
    }

    fun showTargetDialog_short() {
        val setTargetDialog = DialogSetTargetBinding.inflate(layoutInflater)

        val alertDialog =  AlertDialog.Builder(this)
            .setView(setTargetDialog.root)
            .setCancelable(true)
            .create()

        setTargetDialog.cancelBtn.setOnClickListener {
            alertDialog.dismiss()
            Log.d("MainPage_Target_Dialog_short", "[메인_짧은글] 목표 설정 닫기")
        }
        setTargetDialog.nextBtn.setOnClickListener {
            setTarget(setTargetDialog.targetScoreInput, setTargetDialog.targetAccuracyInput)
            Log.d("MainPage_Target_Dialog_short", "[메인_짧은글] 목표 설정 / 타수: $targetScore / 정확도: $targetAccuracy")
            alertDialog.dismiss()

            val intent = Intent(this, ShortParagraphActivity::class.java)
            intent.putExtra("ShortParagraphTargetScore", targetScore.toString())
            intent.putExtra("ShortParagraphTargetAccuracy", targetAccuracy.toString())
            startActivity(intent)
        }

        alertDialog.show()
    }

    fun showTargetDialog_long() {
        val setTargetDialog = DialogSetTargetBinding.inflate(layoutInflater)

        val alertDialog =  AlertDialog.Builder(this)
            .setView(setTargetDialog.root)
            .setCancelable(true)
            .create()

        setTargetDialog.cancelBtn.setOnClickListener {
            alertDialog.dismiss()
            Log.d("MainPage_Target_Dialog_long", "[메인_긴글] 목표 설정 닫기")

            showArticleDialog()
        }
        setTargetDialog.nextBtn.setOnClickListener {
            setTarget(setTargetDialog.targetScoreInput, setTargetDialog.targetAccuracyInput)
            alertDialog.dismiss()
            Log.d("MainPage_Target_Dialog_long", "[메인_긴글] 목표 설정 / 타수: $targetScore / 정확도: $targetAccuracy")

            val intent = Intent(this, LongParagraphActivity::class.java)
            intent.putExtra("LongParagraphTargetScore", targetScore.toString())
            intent.putExtra("LongParagraphTargetAccuracy", targetAccuracy.toString())
            intent.putExtra("selectedParagraph",selectedParagraph)
            startActivity(intent)
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

        setArticleDialog.cancelBtn.setOnClickListener {
            alertDialog.dismiss()
            Log.d("MainPage_Article_Dialog", "[메인_긴글] 글 선택 닫기")
        }
        setArticleDialog.nextBtn.setOnClickListener {
            article = setArticleDialog.articlesList.checkedItemPosition
            Log.d("MainPage_Article_Dialog", "[메인_긴글] 선택한 글: $article")

            if(article == -1) // 글 선택하지 않을 시
                Toast.makeText(this, "글을 선택하거나 업로드해주세요", Toast.LENGTH_SHORT).show()
            else { // 다음 다이얼로그 (목표 설정)
                alertDialog.dismiss()

                selectedParagraph = setArticleDialog.articlesList.adapter.getItem(article).toString()

                val intent = Intent(this, LongParagraphActivity::class.java)
                Log.d("selectedParagraph", selectedParagraph)
                showTargetDialog_long()
            }
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


    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime <= 2000) {
            finish()
        } else {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }
}