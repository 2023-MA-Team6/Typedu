package com.example.typedu

import android.annotation.SuppressLint
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
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.typedu.databinding.ActivityShortParagraphBinding
import kotlinx.coroutines.*

class ShortParagraphActivity : AppCompatActivity() {
    private lateinit var binding: ActivityShortParagraphBinding
    private var currentSentenceIndex = 0

    private var totalTypedCount = 0
    private var totalCorrectCount = 0
    private var calcTypingSpeed = 0

    private var isTyping = false
    private var elapsedTimeInSeconds = 0

    private var highestTypingSpeed = 0
    private var currentTypingSpeed = 0
    private var MAX_ITEM_COUNT = 20

    private var typingSpeedJob : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShortParagraphBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // EditText 설정
        setupEditText()

        // ScrollView 설정
        setupScrollView()

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

    private fun formatElapsedTime(): String {
        val minutes = elapsedTimeInSeconds / 60
        val seconds = elapsedTimeInSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateTimer() {
        val timeString = formatElapsedTime()
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

    private fun setupScrollView() {
        val scrollView: LinearLayout = findViewById(R.id.contentScrollView)
        var sentenceArray: List<String> = resources.getStringArray(R.array.statement_array).toList()
        sentenceArray = sentenceArray.shuffled().take(MAX_ITEM_COUNT)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val dp5 = (5 * resources.displayMetrics.density + 0.5f).toInt()
        layoutParams.setMargins(0, dp5, 0, dp5)

        for (sentence in sentenceArray) {
            val myView = LayoutInflater.from(this).inflate(R.layout.scrollview_short_paragraph_list, null)

            val textView = myView.rootView as TextView
            textView.text = sentence
            scrollView.addView(textView, layoutParams)
        }
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
            showResultDialog()
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

            showNextSentence()
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

            showNextSentence()
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
            binding.currentStatement.text = spannableString
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showResultDialog() {
        val resultView = LayoutInflater.from(this).inflate(R.layout.result_dialog_layout, null)
        val builder = AlertDialog.Builder(this)
            .setView(resultView)
            .setCancelable(false)

        typingSpeedJob?.cancel()

        val resultDialog = builder.create()
        resultDialog.show()

        val intent = intent
        // 결과창에 값 설정
        val goalTypingTextView: TextView = resultView.findViewById(R.id.goalTypingTextView)
        goalTypingTextView.text = "${intent.getStringExtra("ShortParagraphTargetScore")}"

        val averageTypingTextView: TextView = resultView.findViewById(R.id.averageTypingTextView)
        averageTypingTextView.text = "$currentTypingSpeed"

        val highestTypingTextView: TextView = resultView.findViewById(R.id.highestTypingTextView)
        highestTypingTextView.text = "$highestTypingSpeed" // 여기에 최고 타수 변수 추가

        val goalAccuracyTextView: TextView = resultView.findViewById(R.id.goalAccuracyTextView)
        goalAccuracyTextView.text = "${intent.getStringExtra("ShortParagraphTargetAccuracy")}"

        val accuracyTextView: TextView = resultView.findViewById(R.id.accuracyTextView)
        accuracyTextView.text = "${calculateAccuracy()}%"

        val elapsedTimeTextView: TextView = resultView.findViewById(R.id.elapsedTimeTextView)
        elapsedTimeTextView.text = formatElapsedTime()

        // 다시하기 버튼
        val restartButton: Button = resultView.findViewById(R.id.restartButton)
        restartButton.setOnClickListener {
            resultDialog.dismiss()
            val intentRestart = Intent(this, ShortParagraphActivity::class.java)
            finish()

            startActivity(intentRestart)
            // 다시 시작하는 로직 추가
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
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
