package ru.cactus.progressbar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import ru.cactus.progressbar.animation.CustomProgressBar
import ru.cactus.progressbar.animation.EngineState

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        val cpb: CustomProgressBar = findViewById(R.id.bar)

        val switch = findViewById<Switch>(R.id.switch1) as Switch
        switch.setOnClickListener{
            if (switch.isChecked) {
                switch.text = "DayMode"
                delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
                Log.d("Switcher", "Swich ON")
            } else {
                switch.text = "NightMode"
                delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
                Log.d("Switcher", "Swich OFF")

            }
        }



        val btnStart = findViewById<Button>(R.id.start)
        btnStart.setOnClickListener{
            cpb.handlerState(EngineState.START) // Передаю state на изменение цвета
        }

        val btnStartSuccess = findViewById<Button>(R.id.startSuccess)
        btnStartSuccess.setOnClickListener{
            cpb.handlerState(EngineState.START_SUCCESS) // Передаю state на остановку анимации
        }

        val btnStartTimeout = findViewById<Button>(R.id.startTimeout)
        btnStartTimeout.setOnClickListener{
            cpb.handlerState(EngineState.START_TIMEOUT) // Передаю state на изменение цвета
        }

        val btnStop = findViewById<Button>(R.id.stop)
        btnStop.setOnClickListener{
            cpb.handlerState(EngineState.STOP) // Передаю state на остановку анимации
        }

        val btnStopSuccess = findViewById<Button>(R.id.stopSuccess)
        btnStopSuccess.setOnClickListener{
            cpb.handlerState(EngineState.STOP_SUCCESS) // Передаю state на остановку анимации
        }

        val btnStopTimeout = findViewById<Button>(R.id.stopTimeout)
        btnStopTimeout.setOnClickListener{
            cpb.handlerState(EngineState.STOP_TIMEOUT) // Передаю state на остановку анимации
        }
    }
}