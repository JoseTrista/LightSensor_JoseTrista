package trista.josecarlos.lightsensor_josetrista

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.RelativeLayout
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



class MainActivity : AppCompatActivity(), SensorEventListener{
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var rootView: RelativeLayout
    private lateinit var textView: TextView
    private lateinit var logTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootView = findViewById(R.id.rootView)
        textView = findViewById(R.id.textView)
        logTextView = findViewById(R.id.logTextView)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0]
            logLightIntensity(lightValue)

            if (lightValue < 60f) {
                rootView.setBackgroundColor(Color.BLACK)
                textView.setTextColor(Color.WHITE)
                textView.text = "Oscuro"
            } else {
                rootView.setBackgroundColor(Color.WHITE)
                textView.setTextColor(Color.BLACK)
                textView.text = "Luz"
            }
            showLogFileContents()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    private fun showLogFileContents() {
        val filename = "light_log.txt"
        try {
            val fileInputStream: FileInputStream = openFileInput(filename)
            val inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var text: String? = bufferedReader.readLine()
            while (text != null) {
                stringBuilder.append(text).append("\n")
                text = bufferedReader.readLine()
            }
            logTextView.text = stringBuilder.toString()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun logLightIntensity(lightIntensity: Float) {
        val filename = "light_log.txt"
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDateAndTime = sdf.format(Date())

        val lightDescription = when {
            lightIntensity < 10 -> "Muy Oscuro"
            lightIntensity < 40 -> "Oscuro"
            lightIntensity < 100 -> "Luz Moderada"
            lightIntensity < 1000 -> "Brillante"
            else -> "Muy Brillante"
        }

        val logEntry = "$currentDateAndTime - Intensidad: $lightIntensity lux ($lightDescription)\n"
        openFileOutput(filename, Context.MODE_APPEND).use {
            it.write(logEntry.toByteArray())
        }
    }
}