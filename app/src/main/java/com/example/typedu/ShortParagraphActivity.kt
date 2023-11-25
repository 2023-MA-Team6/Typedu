package com.example.typedu

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.typedu.databinding.ActivityShortParagraphBinding

class ShortParagraphActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShortParagraphBinding
    private var currentSentenceIndex = 0
    private val MAX_SENTENCES = 20  // 표시할 최대 문장 개수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShortParagraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // EditText 설정
        setupEditText()

        // ScrollView 설정
        setupScrollView()

        // 다음 문장 표시
        showNextSentence()
    }

    private fun setupEditText() {
        val currentWordText: EditText = findViewById(R.id.currentWordText)
        currentWordText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 이전 텍스트 변경 이벤트
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트 변경 중 이벤트
            }

            override fun afterTextChanged(s: Editable?) {
                // 사용자의 입력이 변경될 때 처리하는 부분
                // 필요한 기능을 여기에 추가하세요.
            }
        })
    }

    private fun setupScrollView() {
        val scrollView: LinearLayout = findViewById(R.id.contentScrollView)
        val sentenceArray: Array<String> = resources.getStringArray(R.array.sentence_array)

        for (sentence in sentenceArray) {
            val textView = TextView(this)
            textView.text = sentence
            textView.textSize = 20f
            scrollView.addView(textView)
        }
    }

    // 다음 문장 표시
    private fun showNextSentence() {
        val scrollView: LinearLayout = findViewById(R.id.contentScrollView)
        val currentStatementView: TextView = findViewById(R.id.currentStatement)

        if (currentSentenceIndex < scrollView.childCount) {
            val currentSentenceTextView = scrollView.getChildAt(currentSentenceIndex) as? TextView
            val currentSentence = currentSentenceTextView?.text.toString()
            currentStatementView.text = currentSentence
            currentSentenceIndex++

            scrollView.removeViewAt(0)
        } else {
            // 추가 작업이 필요한 경우 여기에 코드를 추가하세요.
        }
    }
}
