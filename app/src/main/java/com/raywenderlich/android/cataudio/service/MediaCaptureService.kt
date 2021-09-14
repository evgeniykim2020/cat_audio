package com.raywenderlich.android.cataudio.service

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.media.*
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.concurrent.thread
import kotlin.experimental.and

class MediaCaptureService : Service() {
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private var mediaProjection: MediaProjection? = null

    private lateinit var audioCaptureThread: Thread
    private var audioRecord: AudioRecord? = null

    private var audioManager: AudioManager? = null

    override fun onCreate() {
        super.onCreate()

        // 1
        createNotificationChannel()

// 2
        startForeground(SERVICE_ID, NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID).build())

// 3
        mediaProjectionManager = applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                "Audio Capture Service Channel", NotificationManager.IMPORTANCE_DEFAULT)

        val manager = getSystemService(NotificationManager::class.java) as NotificationManager
        manager.createNotificationChannel(serviceChannel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1
        return if (intent != null) {
            when (intent.action) {
                ACTION_START -> {

                    // 2
                    mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, intent.getParcelableExtra(EXTRA_RESULT_DATA)!!) as MediaProjection

                    // 3
                    startAudioCapture()

                    // 4
                    Service.START_STICKY
                }

                // 5
                ACTION_STOP -> {
                    stopAudioCapture()

                    // 6
                    Service.START_NOT_STICKY
                }

                // 7
                else -> throw IllegalArgumentException("Unexpected action received: ${intent.action}")
            }
        } else {

            // 8
            Service.START_NOT_STICKY
        }
    }

    private fun startAudioCapture() {
        // 1

        audioManager?.allowedCapturePolicy = AudioAttributes.ALLOW_CAPTURE_BY_ALL

        val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()

// 2
        val audioFormat = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(8000)
                .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                .build()

// 3
        audioRecord = AudioRecord.Builder()
                .setAudioFormat(audioFormat)
                .setBufferSizeInBytes(BUFFER_SIZE_IN_BYTES)
                .setAudioPlaybackCaptureConfig(config)
                .build()

        audioRecord!!.startRecording()

// 4
        audioCaptureThread = thread(start = true) {
            val outputFile = createAudioFile()
            Log.d(LOG_TAG, "Created file for capture target: ${outputFile.absolutePath}")
            writeAudioToFile(outputFile)
        }
    }

    private fun createAudioFile(): File {
        val audioCapturesDirectory = File(getExternalFilesDir(null), "/AudioCaptures")
        if (!audioCapturesDirectory.exists()) {
            audioCapturesDirectory.mkdirs()
        }
        val timestamp = SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.US).format(Date())
        val fileName = "Capture-$timestamp.pcm"
        return File(audioCapturesDirectory.absolutePath + "/" + fileName)
    }

    private fun writeAudioToFile(outputFile: File) {
        val fileOutputStream = FileOutputStream(outputFile)
        val capturedAudioSamples = ShortArray(NUM_SAMPLES_PER_READ)

        while (!audioCaptureThread.isInterrupted) {
            audioRecord?.read(capturedAudioSamples, 0, NUM_SAMPLES_PER_READ)

            fileOutputStream.write(
                    capturedAudioSamples.toByteArray(),
                    0,
                    BUFFER_SIZE_IN_BYTES
            )
        }

        fileOutputStream.close()
        Log.d(LOG_TAG, "Audio capture finished for ${outputFile.absolutePath}. File size is ${outputFile.length()} bytes.")
    }

    private fun stopAudioCapture() {
        // 1
        requireNotNull(mediaProjection) { "Tried to stop audio capture, but there was no ongoing capture in place!" }

// 2
        audioCaptureThread.interrupt()
        audioCaptureThread.join()

// 3
        audioRecord!!.stop()
        audioRecord!!.release()
        audioRecord = null

// 4
        mediaProjection!!.stop()
        stopSelf()
    }

    override fun onBind(p0: Intent?): IBinder? = null

    private fun ShortArray.toByteArray(): ByteArray {
        val bytes = ByteArray(size * 2)
        for (i in 0 until size) {
            bytes[i * 2] = (this[i] and 0x00FF).toByte()
            bytes[i * 2 + 1] = (this[i].toInt() shr 8).toByte()
            this[i] = 0
        }
        return bytes
    }

    companion object {
        private const val LOG_TAG = "AudioCaptureService"
        private const val SERVICE_ID = 123
        private const val NOTIFICATION_CHANNEL_ID = "AudioCapture channel"

        private const val NUM_SAMPLES_PER_READ = 1024
        private const val BYTES_PER_SAMPLE = 2 // 2 bytes since we hardcoded the PCM 16-bit format
        private const val BUFFER_SIZE_IN_BYTES = NUM_SAMPLES_PER_READ * BYTES_PER_SAMPLE

        const val ACTION_START = "AudioCaptureService:Start"
        const val ACTION_STOP = "AudioCaptureService:Stop"
        const val EXTRA_RESULT_DATA = "AudioCaptureService:Extra:ResultData"
    }
}
