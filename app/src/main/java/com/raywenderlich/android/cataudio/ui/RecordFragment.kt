/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.raywenderlich.android.cataudio.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.raywenderlich.android.cataudio.R
import com.raywenderlich.android.cataudio.service.MediaCaptureService
import kotlinx.android.synthetic.main.fragment_record.*

class RecordFragment : Fragment(R.layout.fragment_record) {

  private lateinit var mediaProjectionManager: MediaProjectionManager

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)

    button_start_recording.setOnClickListener {
      startCapturing()
    }

    button_stop_recording.setOnClickListener {
      stopCapturing()
    }

  }

  private fun startCapturing() {
    Toast.makeText(this.context, "start recording", Toast.LENGTH_LONG).show()
    if (!isRecordAudioPermissionGranted()) {
      requestRecordAudioPermission()
    } else {
      startMediaProjectionRequest()
    }
  }

  private fun isRecordAudioPermissionGranted(): Boolean {
    return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
  }

  private fun requestRecordAudioPermission() {
    ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.RECORD_AUDIO),
            Companion.RECORD_AUDIO_PERMISSION_REQUEST_CODE
    )
  }

  override fun onRequestPermissionsResult(
          requestCode: Int,
          permissions: Array<out String>,
          grantResults: IntArray
  ) {
    if (requestCode == Companion.RECORD_AUDIO_PERMISSION_REQUEST_CODE) {
      if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(
                requireContext(),
                "Permissions to capture audio granted.",
                Toast.LENGTH_SHORT
        ).show()
      } else {
        Toast.makeText(
                requireContext(), "Permissions to capture audio denied.",
                Toast.LENGTH_SHORT
        ).show()
      }
    }
  }

  private fun stopCapturing() {
    Toast.makeText(this.context, "stop recording", Toast.LENGTH_LONG).show()
    ContextCompat.startForegroundService(requireContext(), Intent(requireContext(), MediaCaptureService::class.java).apply {
      action = MediaCaptureService.ACTION_STOP
    })

    setButtonsEnabled(isCapturingAudio = false)
  }

  private fun startMediaProjectionRequest() {
    // 1
    mediaProjectionManager = requireContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

    // 2
    startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), MEDIA_PROJECTION_REQUEST_CODE)

  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

    // 1
    if (requestCode == MEDIA_PROJECTION_REQUEST_CODE) {

      // 2
      if (resultCode == Activity.RESULT_OK) {
          val audioCaptureIntent = Intent(requireContext(), MediaCaptureService::class.java).apply {
            action = MediaCaptureService.ACTION_START
            putExtra(MediaCaptureService.EXTRA_RESULT_DATA, data!!)
          }

          ContextCompat.startForegroundService(requireContext(), audioCaptureIntent)

          setButtonsEnabled(isCapturingAudio = true)
        // 3
        Toast.makeText(
                requireContext(),
                "MediaProjection permission obtained. Foreground service will start to capture audio.",
                Toast.LENGTH_SHORT
        ).show()

      }

    } else {

      // 4
      Toast.makeText(
              requireContext(), "Request to get MediaProjection denied.",
              Toast.LENGTH_SHORT
      ).show()
    }
  }

  private fun setButtonsEnabled(isCapturingAudio: Boolean) {
    button_start_recording.isEnabled = !isCapturingAudio
    button_stop_recording.isEnabled = isCapturingAudio
  }

  companion object {
    private const val MEDIA_PROJECTION_REQUEST_CODE = 13
    private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 42
  }
}
