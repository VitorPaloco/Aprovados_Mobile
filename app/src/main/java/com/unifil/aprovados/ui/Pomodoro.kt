package com.unifil.aprovados.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unifil.aprovados.R
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.content.Intent
import android.widget.LinearLayout


class Pomodoro : AppCompatActivity() {
    private val WORK_TIME = 25 * 60 * 1000L
    private val SHORT_BREAK = 5 * 60 * 1000L
    private val LONG_BREAK = 15 * 60 * 1000L
    private val WORK_CYCLES = 4

    // Views
    private lateinit var timerDisplay: TextView
    private lateinit var cycleText: TextView
    private lateinit var startButton: Button
    private lateinit var resetButton: Button
    private lateinit var progressCircle: ProgressBar

    private var timer: CountDownTimer? = null
    private var timeLeftInMillis = WORK_TIME
    private var isWorking = true
    private var isRunning = false
    private var currentCycle = 1
    private var workSessionsCompleted = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pomodoro)

        // Inicializa views
        timerDisplay = findViewById(R.id.timerDisplay)
        cycleText = findViewById(R.id.cycleText)
        startButton = findViewById(R.id.startButton)
        resetButton = findViewById(R.id.resetButton)
        progressCircle = findViewById(R.id.progressCircle)

        updateTimerDisplay()
        updateCycleText()

        startButton.setOnClickListener {
            if (isRunning) pauseTimer() else startTimer()
        }

        resetButton.setOnClickListener { resetTimer() }

        val botaoKanban = findViewById<LinearLayout>(R.id.bottom_nav).getChildAt(1) as LinearLayout

        botaoKanban.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }

    private fun startTimer() {
        timer = object : CountDownTimer(timeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerDisplay()
                updateProgressCircle()
            }

            override fun onFinish() {
                if (isWorking) {
                    workSessionsCompleted++
                    if (workSessionsCompleted % WORK_CYCLES == 0) {
                        timeLeftInMillis = LONG_BREAK
                        cycleText.text = "Descanso Longo!"
                    } else {
                        // Descanso curto normal
                        timeLeftInMillis = SHORT_BREAK
                    }
                } else {

                    currentCycle = if (workSessionsCompleted < WORK_CYCLES) {
                        workSessionsCompleted + 1
                    } else {
                        workSessionsCompleted = 0
                        1
                    }
                    timeLeftInMillis = WORK_TIME
                }

                isWorking = !isWorking
                updateCycleText()
                startTimer()
            }
        }.start()

        isRunning = true
        startButton.text = "Pausar"
    }

    private fun updateCycleText() {
        if (isWorking) {
            cycleText.text = "Ciclo $currentCycle/$WORK_CYCLES"
        } else {
            if (workSessionsCompleted % WORK_CYCLES == 0) {
                cycleText.text = "Descanso Longo!"
            } else {
                cycleText.text = "Descanso Curto"
            }
        }
    }

    private fun pauseTimer() {
        timer?.cancel()
        isRunning = false
        startButton.text = "Iniciar"
    }

    private fun resetTimer() {
        timer?.cancel()
        isWorking = true
        currentCycle = 1
        workSessionsCompleted = 0
        timeLeftInMillis = WORK_TIME
        isRunning = false
        updateTimerDisplay()
        updateCycleText()
        updateProgressCircle()
        startButton.text = "Iniciar"
    }

    private fun updateTimerDisplay() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        timerDisplay.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateProgressCircle() {
        val totalTime = if (isWorking) WORK_TIME else if (workSessionsCompleted % WORK_CYCLES == 0) LONG_BREAK else SHORT_BREAK
        val progress = ((timeLeftInMillis.toFloat() / totalTime) * 100).toInt()
        progressCircle.progress = progress
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
    }
}
