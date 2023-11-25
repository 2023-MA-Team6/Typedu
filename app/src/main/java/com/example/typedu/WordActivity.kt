package com.example.typedu

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.typedu.databinding.ActivityWordBinding

class WordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWordBinding
    private lateinit var wordList: List<String>
    private var currentWordIndex = 0
    private var typedCharCount = 0

    private var typedWordCount = 0
    private var correctWordCount = 0

    private var isTyping = false
    private var lastTypedCount = 0
    private val handler = Handler()

    private var currentTypingSpeed = 0
    private var highestTypingSpeed = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wordList = resources.getStringArray(R.array.random_korean_verbs).toList()

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
                val currentWord = binding.currentWord.text.toString()
                val currentEditText = binding.currentWordText.text.toString()

                if (currentWord == currentEditText || currentWord.length < currentEditText.length) {
                    typedCharCount += currentWord.length

                    typedWordCount++
                    if(currentWord == currentEditText) {
                        correctWordCount++
                    }
                    binding.accuracy.text = calculateAccuracy().toString() + "%"

                    // 단어 일치 시 다음 단어로 이동
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

        // 주기적으로 미입력 시 타수 감소 체크 및 1분마다 예측된 타수 초기화
        val checkTypingRunnable = object : Runnable {
            override fun run() {
                if (lastTypedCount == typedCharCount && typedCharCount > 0) {
                    // 미입력 시 타수 감소
                    typedCharCount--
                }
                lastTypedCount = typedCharCount

                handler.postDelayed(this, 1000)
            }
        }

        handler.postDelayed(checkTypingRunnable, 1000)
    }

    private fun startTypingSpeedCalculator() {
        val typingSpeedCalculator = object : Runnable {
            override fun run() {
                currentTypingSpeed = (typedCharCount * 60) / 5
                binding.currentTyping.text = currentTypingSpeed.toString()

                // 최고 타수 갱신
                if (currentTypingSpeed > highestTypingSpeed) {
                    highestTypingSpeed = currentTypingSpeed
                    binding.highestTyping.text = highestTypingSpeed.toString()
                }

                handler.postDelayed(this, 1000)
            }
        }

        handler.postDelayed(typingSpeedCalculator, 1000)
    }

    private fun showNextWord() {
        val currentWordTextView = binding.currentWord
        val nextWordTextView = binding.nextWord

        if (currentWordIndex < wordList.size) {
            currentWordTextView.text = wordList[currentWordIndex]
            if (currentWordIndex + 1 < wordList.size) {
                nextWordTextView.text = wordList[currentWordIndex + 1]
            } else {
                nextWordTextView.text = ""
            }
            currentWordIndex++
        } else {
            // 추가 작업이 필요한 경우 여기에 코드를 추가하세요.
        }
    }

    private fun calculateAccuracy(): Int {
        return if (typedWordCount > 0) {
            (correctWordCount.toDouble() / typedWordCount.toDouble() * 100).toInt()
        } else {
            0
        }
    }
}
