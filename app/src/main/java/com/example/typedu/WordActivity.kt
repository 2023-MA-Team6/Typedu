package com.example.typedu

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.typedu.databinding.ActivityWordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWordBinding
    private lateinit var wordList: List<String>
    private var currentWordIndex = 0

    private var isTyping = false
    private var highestTypingSpeed = 0
    private var currentTypingSpeed = 0

    private var elapsedTimeInSeconds = 0

    private var totalTypedCount = 0
    private var totalCorrectCount = 0
    private var calcTypingSpeed = 0

    private val MAX_ITEM_COUNT = 20

    private var typingSpeedJob : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        wordList = resources.getStringArray(R.array.word_arr).toList()
        wordList = wordList.shuffled().take(MAX_ITEM_COUNT)

        // 초기화면에 첫 번째 단어 표시
        showNextWord()

        binding.currentWordText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 이전 텍스트 변경 이벤트
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트 변경 중 이벤트
            }

            override fun afterTextChanged(s: Editable?) {
                val currentWord = binding.currentWord.text.toString()
                val currentEditText = binding.currentWordText.text.toString()

                checkTypingAccuracy(currentWord, currentEditText)

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
                currentTypingSpeed = calculateTypingSpeedPerSecond(calcTypingSpeed, elapsedTimeInSeconds)
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
            typedCharCount * 36 / elapsedTimeInSeconds
        } else {
            0
        }
    }

    private var index: Int = 0
    private var tempTypingCount = 0
    private fun checkTypingAccuracy(sentence: String, userText: String) {
        val spannableString = SpannableString(sentence)

        if(sentence == userText) {
            val typedCharCount = index
            var correctCharCount = 0

            for(i in 0 until index) {
                if(sentence[i] == userText[i])
                    correctCharCount++
            }

            totalTypedCount += typedCharCount
            totalCorrectCount += correctCharCount
            index = 0

            showNextWord()
            binding.currentWordText.setText("")
            calcTypingSpeed++
        } else if(sentence.length < userText.length) {
            val typedCharCount = index
            var correctCharCount = 0

            for(i in 0 until index) {
                if(sentence[i] == userText[i])
                    correctCharCount++
            }

            totalTypedCount += typedCharCount + 1
            totalCorrectCount += correctCharCount
            index = 0

            showNextWord()
            binding.currentWordText.setText("")
            calcTypingSpeed++
        } else {
            tempTypingCount++
            if(index < userText.length -1) {
                index++
                calcTypingSpeed += tempTypingCount
                tempTypingCount = 0
            } else if(userText.length - 1 in 0..<index) {
                index--

                tempTypingCount = 0
            }
            for(i in sentence.indices)  {
                if(i < userText.length) {
                    if(sentence[i] != userText[i]) {
                        //틀린 글자는 빨강으로
                        spannableString.setSpan(
                            ForegroundColorSpan(Color.RED),
                            i,
                            i + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    } else {
                        // 맞는 글자는 파랑으로
                        spannableString.setSpan(
                            ForegroundColorSpan(Color.BLUE),
                            i,
                            i + 1,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
            }
            binding.currentWord.text = spannableString
        }
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
            showResultDialog()
        }
    }

    private fun showResultDialog() {
        val resultView = LayoutInflater.from(this).inflate(R.layout.result_dialog_layout, null)
        val builder = AlertDialog.Builder(this)
            .setView(resultView)
            .setCancelable(false)

        typingSpeedJob?.cancel()

        val resultDialog = builder.create()
        resultDialog.show()

        // 결과창에 값 설정
        val goalTypingTextView: TextView = resultView.findViewById(R.id.goalTypingTextView)
        goalTypingTextView.text = "-"

        val averageTypingTextView: TextView = resultView.findViewById(R.id.averageTypingTextView)
        averageTypingTextView.text = "$currentTypingSpeed"

        val highestTypingTextView: TextView = resultView.findViewById(R.id.highestTypingTextView)
        highestTypingTextView.text = "$highestTypingSpeed" // 여기에 최고 타수 변수 추가

        val goalAccuracyTextView: TextView = resultView.findViewById(R.id.goalAccuracyTextView)
        goalAccuracyTextView.text = "-"

        val accuracyTextView: TextView = resultView.findViewById(R.id.accuracyTextView)
        accuracyTextView.text = "${calculateAccuracy()}%"

        val elapsedTimeTextView: TextView = resultView.findViewById(R.id.elapsedTimeTextView)
        elapsedTimeTextView.text = "${formatElapsedTime()}"

        // 다시하기 버튼
        val restartButton: Button = resultView.findViewById(R.id.restartButton)
        restartButton.setOnClickListener {
            resultDialog.dismiss()
            val intent = Intent(this, WordActivity::class.java)
            finish()
            startActivity(intent)
        }

        // 그만하기 버튼
        val finishButton: Button = resultView.findViewById(R.id.finishButton)
        finishButton.setOnClickListener {
            resultDialog.dismiss()
            finish()
            // 액티비티 종료하는 로직 추가
        }
    }

    private fun calculateAccuracy(): Int {
        return if (totalTypedCount > 0) {
            (totalCorrectCount.toDouble() / totalTypedCount.toDouble() * 100).toInt()
        } else {
            0
        }
    }

    private fun formatElapsedTime(): String {
        val minutes = elapsedTimeInSeconds / 60
        val seconds = elapsedTimeInSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // ActionBar 뒤로가기 추가
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

