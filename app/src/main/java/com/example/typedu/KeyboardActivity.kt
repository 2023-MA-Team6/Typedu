package com.example.typedu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import com.example.typedu.databinding.ActivityKeyboardBinding

class KeyboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityKeyboardBinding
    private var typedCharCount = 0
    private var currentTypingSpeed = 0
    private var isTyping = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKeyboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // EditText에 TextWatcher 추가
        binding.currentWordText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 이전 텍스트 변경 이벤트
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트 변경 중 이벤트
            }

            override fun afterTextChanged(s: Editable?) {
                // 텍스트 변경 후 이벤트
                val currentWord = binding.WordView.getChildAt(2) as TextView
                val currentWordText = binding.currentWordText.text.toString()

                if (currentWord.text == currentWordText) {
                    // 현재 타이핑 중인 단어가 일치하면 typedChars의 text를 +1
                    typedCharCount++
                    binding.typedChars.text = typedCharCount.toString()
                }

                if (!isTyping) {
                    // 타이핑 중이 아니라면 타이핑 중 상태로 변경하고 타수 계산 시작
                    isTyping = true
                    startTypingSpeedCalculator()
                }
            }
        })
    }

    private fun startTypingSpeedCalculator() {
        val handler = Handler()

        val typingSpeedCalculator = object : Runnable {
            override fun run() {
                // 1초마다 현재 타수 계산 및 업데이트
                currentTypingSpeed = (typedCharCount * 60) / 5 // 예시로 5초 기준으로 타수 계산
                binding.currentTyping.text = currentTypingSpeed.toString()

                // 다음 1초 후에 다시 실행
                handler.postDelayed(this, 1000)
            }
        }

        // 처음에도 1초 후에 실행
        handler.postDelayed(typingSpeedCalculator, 1000)
    }
}
