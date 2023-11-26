package com.example.typedu

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.typedu.databinding.ActivityLongParagraphBinding
import com.example.typedu.databinding.ActivityShortParagraphBinding
import kotlinx.coroutines.*

class LongParagraphActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLongParagraphBinding
    private var currentSentenceIndex = 0
    private var typedCharCount = 0
    private var correctCharCount = 0

    private var totalTypedCount = 0
    private var totalCorrectCount = 0
    private var calcTypingSpeed = 0

    private var isTyping = false
    private val handler = Handler()
    private var elapsedTimeInSeconds = 0

    private var lastTyping = 0
    private var highestTypingSpeed = 0
    private var MAX_ITEM_COUNT = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLongParagraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // EditText 설정
        setupEditText()

        // ScrollView 설정 - 파일 이름으로 리소스 동적으로 선택
        setupScrollView("european_cheese")

        // 다음 문장 표시
        showNextSentence()

        // 타이머 시작
        startTimer()

        // 타수 및 정확도 갱신
        startTypingSpeedCalculator()
    }

    private fun startTimer() {
        GlobalScope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(1000)
                elapsedTimeInSeconds++
                updateTimer()
            }
        }
    }

    private fun updateTimer() {
        val minutes = elapsedTimeInSeconds / 60
        val seconds = elapsedTimeInSeconds % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        binding.time.text = timeString
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
                val currentSentence = binding.currentStatement.text.toString()
                val currentEditText = binding.currentWordText.text.toString()

                // 글자 일치 여부 확인
                checkTypingAccuracy(currentSentence, currentEditText)


                if (!isTyping) {
                    isTyping = true
                    startTypingSpeedCalculator()
                }
            }
        })
    }

    private fun setupScrollView(resourceName: String) {
        val scrollView: LinearLayout = findViewById(R.id.contentScrollView)

        // 파일 이름을 기반으로 리소스 ID 가져오기
        val resourceId = resources.getIdentifier(resourceName, "raw", packageName)

        // Raw 파일에서 텍스트 읽어오기
        val inputStream = resources.openRawResource(resourceId)
        val byteArray = ByteArray(inputStream.available())
        inputStream.read(byteArray)
        inputStream.close()
        val text = String(byteArray)

        // 텍스트를 문장 단위로 나누어 ScrollView에 추가하기
        val sentenceArray = text.split("\r\n").toTypedArray()
        for (sentence in sentenceArray) {
            val textView = TextView(this)
            textView.text = sentence
            textView.textSize = 20f
            scrollView.addView(textView)
        }
        MAX_ITEM_COUNT = scrollView.childCount
    }


    // 다음 문장 표시
    private fun showNextSentence() {
        val scrollView: LinearLayout = findViewById(R.id.contentScrollView)
        val currentStatementView: TextView = findViewById(R.id.currentStatement)

        if (currentSentenceIndex < MAX_ITEM_COUNT) {
            val currentSentenceTextView = scrollView.getChildAt(0) as? TextView
            val currentSentence = currentSentenceTextView?.text.toString()
            currentStatementView.text = currentSentence
            scrollView.removeViewAt(0)
            currentSentenceIndex++
        } else {
            // 추가 작업이 필요한 경우 여기에 코드를 추가하세요.
        }
    }

    private fun startTypingSpeedCalculator() {
        GlobalScope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(1000)
                val currentTypingSpeed = calculateTypingSpeedPerSecond(calcTypingSpeed, elapsedTimeInSeconds)
                binding.currentTyping.text = currentTypingSpeed.toString()

                // 최고 타수 갱신
                if (currentTypingSpeed > highestTypingSpeed) {
                    highestTypingSpeed = currentTypingSpeed
                    binding.highestTyping.text = highestTypingSpeed.toString()
                }

                // 정확도 갱신
                binding.accuracy.text = calculateAccuracy().toString() + "%"
            }
        }
    }

    private fun calculateTypingSpeedPerSecond(typedCharCount: Int, elapsedTimeInSeconds: Int): Int {
        return if (elapsedTimeInSeconds > 0) {
            typedCharCount * 60 / elapsedTimeInSeconds
        } else {
            0
        }
    }


    var index: Int = 0
    private fun checkTypingAccuracy(sentence: String, userText: String) {
        if(sentence == userText) {
            index = 0
            totalTypedCount += typedCharCount
            totalCorrectCount += correctCharCount
            typedCharCount = 0
            correctCharCount = 0
            showNextSentence()
            binding.currentWordText.setText("")
            calcTypingSpeed++
        } else if(sentence.length <= userText.length) {
            if(sentence[index + 1] == userText[index + 1])
                correctCharCount++
            typedCharCount++
            index = 0

            totalTypedCount += typedCharCount
            totalCorrectCount += correctCharCount
            typedCharCount = 0
            correctCharCount = 0
            showNextSentence()
            binding.currentWordText.setText("")
            calcTypingSpeed++
        } else {
            if(index < userText.length -1) {

                if(sentence[index] == userText[index]) {
                    correctCharCount++
                    typedCharCount++
                } else {

                    typedCharCount++
                }
                index++
                calcTypingSpeed++
            } else if(index > userText.length - 1 && userText.length - 1 >= 0) {
                typedCharCount--
                index--

                correctCharCount = 0
                for(i in 0 until index) {
                    if(sentence[i] == userText[i])
                        correctCharCount++
                }
            }
        }
    }

    private fun calculateAccuracy(): Int {
        return if (totalTypedCount > 0) {
            (totalCorrectCount.toDouble() / totalTypedCount.toDouble() * 100).toInt()
        } else {
            0
        }
    }
}
