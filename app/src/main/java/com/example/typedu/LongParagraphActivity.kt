package com.example.typedu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.typedu.databinding.ActivityLongParagraphBinding
import kotlinx.coroutines.*

class LongParagraphActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLongParagraphBinding
    private var txtHeader = ""
    private var targetScore = ""
    private var targetAccuracy = ""

    private var currentSentenceIndex = 0
    private var typedCharCount = 0
    private var correctCharCount = 0

    private var totalTypedCount = 0
    private var totalCorrectCount = 0
    private var calcTypingSpeed = 0

    private var isTyping = false
    private var elapsedTimeInSeconds = 0

    private var highestTypingSpeed = 0
    private var currentTypingSpeed = 0
    private var MAX_ITEM_COUNT = 0

    private var typingSpeedJob : Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLongParagraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // EditText 설정
        setupEditText()

        // ScrollView 설정 - 파일 이름으로 리소스 동적으로 선택
        val intent = intent
        txtHeader = intent.getStringExtra("selectedParagraph").toString()
        Log.d("txtHeader", txtHeader)
        setupScrollView(selectParagraph(txtHeader))

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
            (typedCharCount * 36) / elapsedTimeInSeconds
        } else {
            0
        }
    }

    var index: Int = 0
    var tempTypingCount = 0
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
        } else if(sentence.length < userText.length) {
            if(sentence[index] == userText[index])
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
            tempTypingCount++
            if(index < userText.length -1) {

                if(sentence[index] == userText[index]) {
                    correctCharCount++
                    typedCharCount++
                } else {
                    typedCharCount++
                }
                index++
                calcTypingSpeed += tempTypingCount
                tempTypingCount = 0
            } else if(index > userText.length - 1 && userText.length - 1 >= 0) {
                typedCharCount--
                index--

                correctCharCount = 0
                tempTypingCount = 0
                for(i in 0 until index) {
                    if(sentence[i] == userText[i])
                        correctCharCount++
                }
            }
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

        // 결과창에 값 설정
        val intent = intent
        val goalTypingTextView: TextView = resultView.findViewById(R.id.goalTypingTextView)
        targetScore = intent.getStringExtra("LongParagraphTargetScore").toString()
        goalTypingTextView.text = ": ${targetScore}"

        val averageTypingTextView: TextView = resultView.findViewById(R.id.averageTypingTextView)
            averageTypingTextView.text = ": ${currentTypingSpeed}${getString(R.string.ta)}"

        val highestTypingTextView: TextView = resultView.findViewById(R.id.highestTypingTextView)
        highestTypingTextView.text = ": ${highestTypingSpeed}${getString(R.string.ta)}" // 여기에 최고 타수 변수 추가

        val goalAccuracyTextView: TextView = resultView.findViewById(R.id.goalAccuracyTextView)
        targetAccuracy = intent.getStringExtra("LongParagraphTargetAccuracy").toString()
        goalAccuracyTextView.text = ": ${targetAccuracy}"

        val accuracyTextView: TextView = resultView.findViewById(R.id.accuracyTextView)
        accuracyTextView.text = ": ${calculateAccuracy()}%"

        val elapsedTimeTextView: TextView = resultView.findViewById(R.id.elapsedTimeTextView)
        elapsedTimeTextView.text = ": ${formatElapsedTime()}"

        // 다시하기 버튼
        val restartButton: Button = resultView.findViewById(R.id.restartButton)
        restartButton.setOnClickListener {
            resultDialog.dismiss()

            val intent = Intent(this, LongParagraphActivity::class.java)
            Log.d("txtHeader", txtHeader)
            intent.putExtra("selectedParagraph",txtHeader)
            intent.putExtra("LongParagraphTargetScore", targetScore)
            intent.putExtra("LongParagraphTargetAccuracy", targetAccuracy)
            finish()
            startActivity(intent)
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

    private fun selectParagraph(sentence: String) : String{
        return when(sentence){
            "별 헤는 밤" -> "ko_counting_the_starts_at_night"
            "애국가" -> "ko_national_anthem"
            "금도끼 은도끼" -> "ko_gold_ax_silver_ax"
            "별주부전" -> "ko_tales_of_zara"
            "어린왕자" -> "ko_le_petit_prince"
            "님의 침묵" -> "ko_the_silence_of_love"
            "愛を伝えたいだとか" -> "ja_ai_wo_tsutaetaidatoka"
            "猫" -> "ja_neko"
            "逆夢" -> "ja_sakayume"
            "シャッタ" -> "ja_shutter"
            "レオ" -> "ja_leo"
            "ベテルギウス" -> "ja_betelgeuse"
            "Bad" -> "en_bad"
            "Dangerously" -> "en_dangerously"
            "Snowman" -> "en_snowman"
            "Toxic" -> "en_toxic"
            "Wellerman" -> "en_wellerman"
            "When i was your man" -> "en_when_i_was_your_man"
            "test"->"test"
            else -> "ko_the_silence_of_love"
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
