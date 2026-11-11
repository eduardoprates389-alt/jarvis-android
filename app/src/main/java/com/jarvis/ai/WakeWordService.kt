
package com.jarvis.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder

class WakeWordService : Service() {

    private val channelId = "jarvis_voice_channel"

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(
                channelId,
                "JARVIS AI",
                NotificationManager.IMPORTANCE_LOW
            )

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("JARVIS AI")
            .setContentText("Serviço do assistente ativo")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        val comando = intent?.getStringExtra("comando")

        if (!comando.isNullOrBlank()) {
            executarComando(comando)
        }

        return START_NOT_STICKY
    }

    private fun executarComando(comando: String) {
        val texto = comando.lowercase().trim()

        when {
            texto.contains("spotify") ||
            texto.contains("tocar música") ||
            texto.contains("tocar musica") -> {

                val pesquisa = texto
                    .replace("jarvis", "")
                    .replace("spotify", "")
                    .replace("tocar música", "")
                    .replace("tocar musica", "")
                    .trim()

                abrirSpotify(pesquisa)
            }
        }
    }

    private fun abrirSpotify(pesquisa: String) {
        val termo = Uri.encode(pesquisa.ifBlank { "músicas" })

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://open.spotify.com/search/$termo")
        )

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
