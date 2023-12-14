package com.example.typedu

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.typedu.databinding.ActivityKeyboardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
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

    private var lastTypedCount = 0
    private var elapsedTimeInSeconds = 0
    private var highestTypingSpeed = 0 // 최고 타수 변수 추가
    private var currentTypingSpeed = 0

    private val MAX_ITEM_COUNT = 20

    private var typingSpeedJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeyboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        wordList = resources.getStringArray(R.array.keyboard_arr).toList()
        wordList = wordList.shuffled().take(MAX_ITEM_COUNT)
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
        typingSpeedJob = GlobalScope.launch(Dispatchers.Main) {
            while (isActive) {
                delay(1000)
                currentTypingSpeed = (typedCharCount * 36) / elapsedTimeInSeconds
                binding.currentTyping.text = currentTypingSpeed.toString()

                // 최고 타수 업데이트
                if (currentTypingSpeed > highestTypingSpeed) {
                    highestTypingSpeed = currentTypingSpeed
                }
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
            showResultDialog()
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
        val goalTypingTextView: TextView = resultView.findViewById(R.id.goalTypingTextView)
        goalTypingTextView.text = "-"

        val averageTypingTextView: TextView = resultView.findViewById(R.id.averageTypingTextView)
        averageTypingTextView.text = "${currentTypingSpeed}${getString(R.string.ta)}"

        val highestTypingTextView: TextView = resultView.findViewById(R.id.highestTypingTextView)
        highestTypingTextView.text = "${highestTypingSpeed}${getString(R.string.ta)}" // 여기에 최고 타수 변수 추가

        val goalAccuracyTextView: TextView = resultView.findViewById(R.id.goalAccuracyTextView)
        goalAccuracyTextView.text = "-"

        val accuracyTextView: TextView = resultView.findViewById(R.id.accuracyTextView)
        accuracyTextView.text = "${calculateAccuracy()}%"

        val elapsedTimeTextView: TextView = resultView.findViewById(R.id.elapsedTimeTextView)
        elapsedTimeTextView.text = formatElapsedTime()

        // 그만하기 버튼
        val finishButton: Button = resultView.findViewById(R.id.finishButton)
        finishButton.setOnClickListener {
            resultDialog.dismiss()
            finish()
            // 액티비티 종료하는 로직 추가
        }
        // 다시하기 버튼
        val restartButton: Button = resultView.findViewById(R.id.restartButton)
        restartButton.setOnClickListener {
            resultDialog.dismiss()
            val intent = Intent(this, KeyboardActivity::class.java)
            finish()
            startActivity(intent)
            // 다시 시작하는 로직 추가
        }
    }

    private fun shiftTextLeft() {
        // 1번째 TextView 초기화
        (binding.WordView.getChildAt(0) as TextView).text = ""

        // 2번째와 3번째 TextView의 값 이동
        for (i in 0..1) {
            val currentWordTextView = binding.WordView.getChildAt(i + 1) as? TextView
            val previousWordTextView = binding.WordView.getChildAt(i) as? TextView

            previousWordTextView?.text = currentWordTextView?.text
        }
    }

    private fun calculateAccuracy(): Int {
        return if (typedCharCount > 0) {
            (passedCount.toDouble() / wordList.size.toDouble() * 100).toInt()
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
