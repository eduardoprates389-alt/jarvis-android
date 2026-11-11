
package com.jarvis.ai

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import android.net.Uri
import android.speech.SpeechRecognizer
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.Manifest
import android.content.pm.PackageManager
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private var fala: TextToSpeech? = null
    private var reconhecedor: SpeechRecognizer? = null
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fala = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(30, 40, 30, 40)
            setBackgroundColor(0xFF101820.toInt())
        }

        val titulo = TextView(this).apply {
            text = "J.A.R.V.I.S AI"
            textSize = 30f
            setTextColor(0xFF00BFFF.toInt())
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "Olá! Estou pronto, senhor."
            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            setPadding(10, 40, 10, 40)
        }

        val botao = Button(this).apply {
            text = "FALAR COM JARVIS"
            textSize = 18f
            setOnClickListener {
                iniciarEscuta()
            }
        }

        layout.addView(titulo)
        layout.addView(status)
        layout.addView(botao)

        setContentView(layout)

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO), 100
            )
        }

        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            reconhecedor = SpeechRecognizer.createSpeechRecognizer(this)
            configurarReconhecimento()
        } else {
            status.text = "Reconhecimento de voz indisponível."
        }
    }

    override fun onInit(result: Int) {
        if (result == TextToSpeech.SUCCESS) {
            fala?.language = Locale("pt", "BR")
            falar("Olá, senhor. JARVIS está pronto.")
        }
    }

    private fun falar(texto: String) {
        fala?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "jarvis")
        status.text = texto
    }

    private fun iniciarEscuta() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO), 100
            )
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        try {
            reconhecedor?.startListening(intent)
            status.text = "Estou ouvindo, senhor..."
        } catch (e: Exception) {
            falar("Não consegui iniciar o reconhecimento de voz.")
        }
    }

    private fun configurarReconhecimento() {
        reconhecedor?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                status.text = "Pode falar, senhor."
            }

            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                status.text = "Não entendi. Tente novamente."
            }

            override fun onResults(results: Bundle?) {
                val lista = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )

                val comando = lista?.firstOrNull()?.lowercase()
                    ?: return

                executarComando(comando)
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun executarComando(comando: String) {

        when {
            comando.contains("spotify") ||
            comando.contains("tocar música") ||
            comando.contains("tocar musica") -> {

                val pesquisa = comando
                    .replace("jarvis", "")
                    .replace("spotify", "")
                    .replace("tocar música", "")
                    .replace("tocar musica", "")
                    .trim()

                val url = "https://open.spotify.com/search/${Uri.encode(pesquisa)}"

                abrirLink(url)
                falar("Abrindo sua pesquisa no Spotify.")
            }

            comando.contains("whatsapp") -> {
                abrirApp("com.whatsapp", "WhatsApp")
            }

            comando.contains("instagram") -> {
                abrirApp("com.instagram.android", "Instagram")
            }

            comando.contains("youtube") -> {
                abrirApp("com.google.android.youtube", "YouTube")
            }

            comando.startsWith("copiar ") -> {
                val texto = comando.removePrefix("copiar ").trim()

                val clipboard = getSystemService(
                    Context.CLIPBOARD_SERVICE
                ) as ClipboardManager

                clipboard.setPrimaryClip(
                    ClipData.newPlainText("JARVIS", texto)
                )

                falar("Texto copiado, senhor.")
            }

            comando.contains("que horas") -> {
                val hora = java.text.SimpleDateFormat(
                    "HH:mm", Locale("pt", "BR")
                ).format(java.util.Date())

                falar("Agora são $hora.")
            }

            comando.contains("olá") ||
            comando.contains("ola") ||
            comando.contains("jarvis") -> {
                falar("Olá, senhor. Estou à disposição.")
            }

            else -> {
                falar("Ainda não conheço esse comando, senhor.")
            }
        }
    }

    private fun abrirApp(pacote: String, nome: String) {
        val intent = packageManager.getLaunchIntentForPackage(pacote)

        if (intent != null) {
            startActivity(intent)
            falar("Abrindo $nome.")
        } else {
            falar("$nome não está instalado.")
        }
    }

    private fun abrirLink(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            falar("Não consegui abrir o link.")
        }
    }

    override fun onDestroy() {
        reconhecedor?.destroy()
        reconhecedor = null
        fala?.stop()
        fala?.shutdown()
        fala = null
        super.onDestroy()
    }
}
