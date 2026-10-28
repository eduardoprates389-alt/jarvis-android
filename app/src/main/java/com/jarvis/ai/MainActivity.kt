
package com.jarvis.ai

import android.app.Activity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.RecognizerIntent
import android.content.Intent
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var fala: TextToSpeech
    private lateinit var resposta: TextView
    private var pronto = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fala = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 35, 28, 25)
            gravity = Gravity.CENTER_HORIZONTAL
            setBackgroundColor(0xFF101820.toInt())
        }

        val titulo = TextView(this).apply {
            text = "J.A.R.V.I.S"
            textSize = 32f
            gravity = Gravity.CENTER
            setTextColor(0xFF00D9FF.toInt())
        }
        layout.addView(titulo)

        val subtitulo = TextView(this).apply {
            text = "Assistente pessoal inteligente"
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(0xFFFFFFFF.toInt())
        }
        layout.addView(subtitulo)

        resposta = TextView(this).apply {
            text = "Olá, senhor. JARVIS iniciado."
            textSize = 18f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 30, 0, 25)
        }
        layout.addView(resposta)

        val entrada = EditText(this).apply {
            hint = "Digite um comando..."
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFFAAAAAA.toInt())
        }
        layout.addView(entrada)

        val enviar = Button(this).apply {
            text = "Enviar comando"
            setOnClickListener {
                processar(entrada.text.toString())
                entrada.text.clear()
            }
        }
        layout.addView(enviar)

        val voz = Button(this).apply {
            text = "🎙 Falar com JARVIS"
            setOnClickListener { ouvir() }
        }
        layout.addView(voz)

        val abrir = Button(this).apply {
            text = "Abrir YouTube"
            setOnClickListener { abrirApp("com.google.android.youtube") }
        }
        layout.addView(abrir)

        val rolagem = ScrollView(this)
        rolagem.addView(layout)
        setContentView(rolagem)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            pronto = fala.setLanguage(Locale("pt", "BR")) !=
                    TextToSpeech.LANG_MISSING_DATA &&
                    fala.setLanguage(Locale("pt", "BR")) !=
                    TextToSpeech.LANG_NOT_SUPPORTED
        }
    }

    private fun dizer(texto: String) {
        resposta.text = texto
        if (pronto) {
            fala.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "jarvis")
        }
    }

    private fun processar(comando: String) {
        when {
            comando.isBlank() ->
                dizer("Diga um comando, senhor.")

            comando.contains("youtube", true) -> {
                dizer("Abrindo YouTube, senhor.")
                abrirApp("com.google.android.youtube")
            }

            comando.contains("instagram", true) -> {
                dizer("Abrindo Instagram, senhor.")
                abrirApp("com.instagram.android")
            }

            comando.contains("whatsapp", true) -> {
                dizer("Abrindo WhatsApp, senhor.")
                abrirApp("com.whatsapp")
            }

            comando.contains("que horas", true) -> {
                dizer("Agora são " +
                    java.text.SimpleDateFormat(
                        "HH:mm", Locale("pt", "BR")
                    ).format(java.util.Date()))
            }

            comando.contains("seu nome", true) ->
                dizer("Meu nome é JARVIS, seu assistente pessoal.")

            comando.contains("olá", true) ||
            comando.contains("oi", true) ->
                dizer("Olá, senhor. Como posso ajudar?")

            else ->
                dizer("Ainda estou aprendendo esse comando, senhor.")
        }
    }

    private fun ouvir() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-BR")

        try {
            startActivityForResult(intent, 100)
        } catch (e: Exception) {
            dizer("O reconhecimento de voz não está disponível.")
        }
    }

    @Deprecated("Compatibilidade com reconhecimento de voz")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val comandos = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )
            comandos?.firstOrNull()?.let { processar(it) }
        }
    }

    private fun abrirApp(pacote: String) {
        val intent = packageManager.getLaunchIntentForPackage(pacote)
        if (intent != null) {
            startActivity(intent)
        } else {
            dizer("Senhor, esse aplicativo não está instalado.")
        }
    }

    override fun onDestroy() {
        if (::fala.isInitialized) {
            fala.stop()
            fala.shutdown()
        }
        super.onDestroy()
    }
}
