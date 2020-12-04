package ru.cactus.progressbar

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import ru.cactus.progressbar.animation.CustomProgressBar
import ru.cactus.progressbar.animation.EngineState

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cpb: CustomProgressBar = findViewById(R.id.bar)

        cpb.handlerState(EngineState.START) // Передаю state на запуск анимации

        val btn = findViewById<Button>(R.id.button)
        btn.text = getString(R.string.success)
        btn.setOnClickListener{
            cpb.handlerState(EngineState.START_SUCCESS) // Передаю state на изменение цвета
        }

        val btn2 = findViewById<Button>(R.id.button2)
        btn2.text = getString(R.string.timeout)
        btn2.setOnClickListener{
            cpb.handlerState(EngineState.START_TIMEOUT) // Передаю state на остановку анимации
        }
    }
}