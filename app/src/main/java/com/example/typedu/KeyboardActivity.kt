package com.example.typedu

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.typedu.databinding.ActivityKeyboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class KeyboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeyboardBinding
    private lateinit var wordList: List<String>
    private var currentWordIndex = 0
    private var typedCharCount = 0
    private var passedCount = 0
    private var isTyping = false
    private var newTypeC = 0

    private var lastTypedCount = 0
    private var elapsedTimeInSeconds = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeyboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wordList = resources.getStringArray(R.array.random_korean_words).toList()

        // 초기화면에 첫 번째 단어 표시
        showNextWord()

        binding.currentWordText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 이전 텍스트 변경 이벤트
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트 변경 중 이벤트
            }

            override fun afterTextChanged(s: Editable?) {
                val currentWordView = binding.WordView.getChildAt(2) as? TextView
                val currentWord = currentWordView?.text.toString()
                val currentEditText = binding.currentWordText.text.toString()
                typedCharCount++

                if (currentWord == currentEditText || currentWord.length < currentEditText.length) {
                    if(currentWord == currentEditText) {
                        passedCount++
                    }
                    binding.typedChars.text = passedCount.toString()

                    // 단어 일치 시 다음 단어로 이동하고 텍스트를 왼쪽으로 이동
                    shiftTextLeft()
                    showNextWord()

                    // edittext 값 초기화
                    binding.currentWordText.setText("")

                    // 미입력 시 타수 초기화
                    lastTypedCount = 0
                }

                if (!isTyping) {
                    isTyping = true
                    startTypingSpeedCalculator()
                }
            }
        })

        GlobalScope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(1000)
                elapsedTimeInSeconds++
            }
        }
    }

    private fun startTypingSpeedCalculator() {
        GlobalScope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(1000)
                val currentTypingSpeed = (typedCharCount * 36) / elapsedTimeInSeconds
                binding.currentTyping.text = currentTypingSpeed.toString()
            }
        }
    }

    private fun showNextWord() {
        val currentWordTextView = binding.WordView.getChildAt(2) as TextView
        val nextWordTextView = binding.WordView.getChildAt(3) as TextView
        val thirdWordTextView = binding.WordView.getChildAt(4) as TextView

        if (currentWordIndex < wordList.size) {
            currentWordTextView.text = wordList[currentWordIndex]
            if(currentWordIndex + 2 < wordList.size) {
                nextWordTextView.text = wordList[currentWordIndex + 1]
                thirdWordTextView.text = wordList[currentWordIndex + 2]
            } else if(currentWordIndex + 1 == wordList.size - 1) {
                nextWordTextView.text = wordList[currentWordIndex + 1]
                thirdWordTextView.text = ""
            } else if(currentWordIndex == wordList.size - 1) {
                nextWordTextView.text = ""
                thirdWordTextView.text = ""
            }
            currentWordIndex++
        } else {
            /*currentWordIndex = 0
            currentWordTextView.text = wordList[currentWordIndex]
            결과창 코드는 여기에 삽입
            */
        }

    }
    private fun shiftTextLeft() {
        // 1번째 TextView 초기화
        (binding.WordView.getChildAt(0) as TextView)?.text = ""

        // 2번째와 3번째 TextView의 값 이동
        for (i in 0..1) {
            val currentWordTextView = binding.WordView.getChildAt(i + 1) as? TextView
            val previousWordTextView = binding.WordView.getChildAt(i) as? TextView

            previousWordTextView?.text = currentWordTextView?.text
        }
    }

}
