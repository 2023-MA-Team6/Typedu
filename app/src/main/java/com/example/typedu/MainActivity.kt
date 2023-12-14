package com.example.typedu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val PICK_FILE_REQUEST_CODE = 1
    private var selectedLanguage = "한국어"
    private var backPressedTime: Long = 0L

    private var targetScore:Int? = 0 // 목표 타수
    private var targetAccuracy:Int? = 0 // 목표 정확도
    private var article:Int = 0
    private var selectedParagraph : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Typedu_monitor", "start")

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 자판 연습 버튼 클릭
        binding.keyPrBtn.setOnClickListener {
            Log.d("Typedu_monitor", "[메인] 자판 연습 버튼 클릭")
            val intent = Intent(this, KeyboardActivity::class.java)
            startActivity(intent)
        }

        // 낱말 연습 버튼 클릭
        binding.wordPrBtn.setOnClickListener {
            Log.d("Typedu_monitor", "[메인] 낱말 연습 버튼 클릭")
            val intent = Intent(this, WordActivity::class.java)
            startActivity(intent)
        }

        // 짧은 글 연습 버튼 클릭
        binding.sentencePrBtn.setOnClickListener {
            Log.d("Typedu_monitor", "[메인] 짧은 글 연습 버튼 클릭")
            showTargetDialogShort()
        }

        // 긴 글 연습 버튼 클릭
        binding.articlePrBtn.setOnClickListener {
            Log.d("Typedu_monitor", "[메인] 긴 글 연습 버튼 클릭")
            showArticleDialog()
        }

        // 언어 설정 버튼 클릭
        binding.languageSettingBtn.setOnClickListener {
            Log.d("Typedu_monitor", "[메인] 언어 설정 버튼 클릭")
            showLanguageSetDialog();
        }
    }

    private fun setTarget(scoreInput: EditText, accuracyInput:EditText) {
        targetScore =
            if(scoreInput.text.toString() != "")
                Integer.parseInt(scoreInput.text.toString())
            else
                250

        targetAccuracy =
            if(accuracyInput.text.toString() != "")
                Integer.parseInt(accuracyInput.text.toString())
            else
                90
    }

    private fun showTargetDialogShort() {
        val setTargetDialog = DialogSetTargetBinding.inflate(layoutInflater)

        val alertDialog =  AlertDialog.Builder(this)
            .setView(setTargetDialog.root)
            .setCancelable(true)
            .create()

        setTargetDialog.cancelBtn.setOnClickListener {
            alertDialog.dismiss()
            Log.d("Typedu_monitor", "[메인_짧은글] 목표 설정 닫기")
        }
        setTargetDialog.nextBtn.setOnClickListener {
            setTarget(setTargetDialog.targetScoreInput, setTargetDialog.targetAccuracyInput)
            Log.d("Typedu_monitor", "[메인_짧은글] 목표 설정 / 타수: $targetScore / 정확도: $targetAccuracy")
            alertDialog.dismiss()

            val intent = Intent(this, ShortParagraphActivity::class.java)
            intent.putExtra("ShortParagraphTargetScore", targetScore.toString())
            intent.putExtra("ShortParagraphTargetAccuracy", targetAccuracy.toString())
            startActivity(intent)
        }

        alertDialog.show()
    }

    private fun showTargetDialogLong() {
        val setTargetDialog = DialogSetTargetBinding.inflate(layoutInflater)

        val alertDialog =  AlertDialog.Builder(this)
            .setView(setTargetDialog.root)
            .setCancelable(true)
            .create()

        setTargetDialog.cancelBtn.setOnClickListener {
            alertDialog.dismiss()
            Log.d("Typedu_monitor", "[메인_긴글] 목표 설정 닫기")

            showArticleDialog()
        }

        setTargetDialog.nextBtn.setOnClickListener {
            setTarget(setTargetDialog.targetScoreInput, setTargetDialog.targetAccuracyInput)
            alertDialog.dismiss()
            Log.d("Typedu_monitor", "[메인_긴글] 목표 설정 / 타수: $targetScore / 정확도: $targetAccuracy")

            val intent = Intent(this, LongParagraphActivity::class.java)
            intent.putExtra("LongParagraphTargetScore", targetScore.toString())
            intent.putExtra("LongParagraphTargetAccuracy", targetAccuracy.toString())
            intent.putExtra("selectedParagraph", selectedParagraph)
            startActivity(intent)
        }

        alertDialog.show()
    }

    private fun showArticleDialog() {
        val setArticleDialog = DialogSetArticleBinding.inflate(layoutInflater)
        val titles = resources.getStringArray(R.array.article_title)
        val adapter = ArrayAdapter(this, R.layout.listview_typing_list, titles)

        setArticleDialog.articlesList.adapter = adapter

         val alertDialog = AlertDialog.Builder(this)
            .setView(setArticleDialog.root)
            .setCancelable(true)
            .create()

        setArticleDialog.loadBtn.setOnClickListener {
            Log.d("Typedu_monitor", "[메인_긴글] 글 불러오기")
            selectedParagraph = null
            article = -1

            chooseFile()
        }

        setArticleDialog.cancelBtn.setOnClickListener {
            alertDialog.dismiss()
            Log.d("Typedu_monitor", "[메인_긴글] 글 선택 닫기")
        }
        setArticleDialog.nextBtn.setOnClickListener {
            article = setArticleDialog.articlesList.checkedItemPosition
            if(article != -1) {
                selectedParagraph = setArticleDialog.articlesList.adapter.getItem(article).toString()
            }

            Log.d("Typedu_monitor", "[메인_긴글] 글 번호: $article / 글 내용: $selectedParagraph")

            if(selectedParagraph == null) // 글 선택하지 않을 시
                Toast.makeText(this, R.string.article_dialog_not_selected, Toast.LENGTH_SHORT).show()
            else { // 다음 다이얼로그 (목표 설정)
                alertDialog.dismiss()

                val intent = Intent(this, LongParagraphActivity::class.java)
                showTargetDialogLong()
            }
        }
        alertDialog.show()
    }

    private fun showLanguageSetDialog() {
        val setLanguageSetDialog = DialogSetLangaugeBinding.inflate(layoutInflater)
        val languages = resources.getStringArray(R.array.language)
        val adapter = ArrayAdapter(this, R.layout.spinner_language_list, languages)

        setLanguageSetDialog.languageSpinner.adapter = adapter

        val alertDialog = AlertDialog.Builder(this)
            .setView(setLanguageSetDialog.root)
            .create()
        setLanguageSetDialog.cancelBtn.setOnClickListener {
            when (selectedLanguage){
                "한국어" -> setLocate("ko-rKR")
                "English" -> {
                    setLocate("en")
                }
                "日本語" -> setLocate("ja")
            }
            alertDialog.dismiss()
            recreate()
        }

        setLanguageSetDialog.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                selectedLanguage = setLanguageSetDialog.languageSpinner.selectedItem as String
                Log.d("Typedu_monitor", "[메인_언어설정] $selectedLanguage")
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
        alertDialog.show()
    }

    // 언어 설정 - "en", "ja", "ko-rKR" 입력하면 그에 맞게 언어가 바뀜
    private fun setLocate(lang: String) {
        Log.d("Typedu_monitor", "[메인_언어설정] setLocate")
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val config = Configuration()

        config.setLocale(locale)
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        val editor = getSharedPreferences("Settings", Context.MODE_PRIVATE).edit()
        editor.putString("My_Lang", lang)
        editor.apply()
    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime <= 2000) {
            Log.d("Typedu_monitor", "end")
            finish()
        } else {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(this, R.string.main_go_back, Toast.LENGTH_SHORT).show()
        }
    }


    // 글 불러오기
    private fun chooseFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "text/plain"
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val content = readTextFromUri(uri)
            }
        }
    }

    private fun readTextFromUri(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val stringBuilder = StringBuilder()
        var line: String?

        Log.d("Typedu_monitor", "[메인_긴글] 글 불러오기 Uri: $uri")
        Toast.makeText(this, "File loading in progress...", Toast.LENGTH_SHORT).show()

        try {
            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
                stringBuilder.append("\n")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "File loading failed", Toast.LENGTH_SHORT).show()
        } finally {
            try {
                selectedParagraph = stringBuilder.toString()
                inputStream?.close()
                Log.d("Typedu_monitor", "[메인_긴글] 글 불러오기 완료")
                Toast.makeText(this, "File loading completed", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this, "File loading failed", Toast.LENGTH_SHORT).show()
            }
        }

        return stringBuilder.toString()
    }



}