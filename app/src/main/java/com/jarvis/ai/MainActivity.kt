
package com.jarvis.ai

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var status: TextView
    private var tts: TextToSpeech? = null
    private var recognizer: SpeechRecognizer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 30, 30, 30)
            setBackgroundColor(0xFF08101F.toInt())
        }

        val title = TextView(this).apply {
            text = "J.A.R.V.I.S. 🤖"
            textSize = 34f
            gravity = Gravity.CENTER
            setTextColor(0xFF00DCFF.toInt())
        }

        status = TextView(this).apply {
            text = "Sistema iniciado. Estou pronto!"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 35, 0, 35)
        }

        val voiceButton = Button(this).apply {
            text = "🎙 FALAR COM JARVIS"
            setOnClickListener { listen() }
        }

        val settingsButton = Button(this).apply {
            text = "ABRIR CONFIGURAÇÕES"
            setOnClickListener {
                startActivity(
                    Intent(android.provider.Settings.ACTION_SETTINGS)
                )
            }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(voiceButton)
        layout.addView(settingsButton)

        setContentView(layout)
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            tts?.language = Locale("pt", "BR")
            speak("Olá! JARVIS iniciado. Como posso ajudar?")
        }
    }

    private fun speak(message: String) {
        status.text = message
        tts?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "jarvis"
        )
    }

    private fun listen() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO), 100
            )
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Reconhecimento de voz indisponível.")
            return
        }

        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)

        recognizer?.setRecognitionListener(
            object : android.speech.RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    status.text = "Estou ouvindo..."
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(value: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    status.text = "Processando comando..."
                }

                override fun onError(error: Int) {
                    status.text = "Não entendi. Tente novamente."
                }

                override fun onResults(results: Bundle?) {
                    val command = results
                        ?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )?.firstOrNull()?.lowercase(Locale.ROOT)
                        ?: ""

                    handleCommand(command)
                }

                override fun onPartialResults(results: Bundle?) {}
                override fun onEvent(type: Int, params: Bundle?) {}
            }
        )

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Fale com o JARVIS"
            )
        }

        recognizer?.startListening(intent)
    }

    private fun handleCommand(command: String) {
        when {
            command.contains("youtube") ->
                openApp("com.google.android.youtube", "YouTube")

            command.contains("instagram") ->
                openApp("com.instagram.android", "Instagram")

            command.contains("whatsapp") ->
                openApp("com.whatsapp", "WhatsApp")

            command.contains("tiktok") ||
            command.contains("tik tok") ->
                openApp(
                    "com.zhiliaoapp.musically", "TikTok"
                )

            command.contains("configurações") ||
            command.contains("configuracoes") -> {
                speak("Abrindo configurações.")
                startActivity(
                    Intent(android.provider.Settings.ACTION_SETTINGS)
                )
            }

            command.contains("câmera") ||
            command.contains("camera") -> {
                speak("Abrindo a câmera.")
                startActivity(
                    Intent("android.media.action.IMAGE_CAPTURE")
                )
            }

            command.contains("que horas") -> {
                val time = SimpleDateFormat(
                    "HH:mm", Locale("pt", "BR")
                ).format(Date())
                speak("Agora são $time.")
            }

            command.contains("oi") ||
            command.contains("olá") ||
            command.contains("ola") ->
                speak("Olá! Estou aqui. Qual é o próximo comando?")

            else ->
                speak("Ainda estou aprendendo esse comando.")
        }
    }

    private fun openApp(packageName: String, name: String) {
        val launchIntent =
            packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            speak("Abrindo $name.")
            startActivity(launchIntent)
        } else {
            speak("$name não foi encontrado no celular.")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            listen()
        }
    }

    override fun onDestroy() {
        recognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
