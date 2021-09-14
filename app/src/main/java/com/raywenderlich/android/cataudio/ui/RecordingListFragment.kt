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

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.raywenderlich.android.cataudio.adapter.MyItemRecyclerViewAdapter
import com.raywenderlich.android.cataudio.R
import com.raywenderlich.android.cataudio.dummy.DummyContent
import kotlinx.android.synthetic.main.fragment_recording_list_item.*
import java.io.File
import java.io.FileNotFoundException
import java.util.ArrayList

/**
 * A fragment representing a list of Items.
 */
class RecordingListFragment : Fragment() {

  private var columnCount = 1
  private var isPlaying = false
  private lateinit var viewAdapter: MyItemRecyclerViewAdapter

  private var items: MutableList<Pair<File, Int>> = ArrayList()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    arguments?.let {
      columnCount = it.getInt(
          ARG_COLUMN_COUNT)
    }
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_recording_list_container, container, false)

    // Set the adapter
    if (view is RecyclerView) {
      with(view) {
        layoutManager = when {
          columnCount <= 1 -> LinearLayoutManager(context)
          else -> GridLayoutManager(context, columnCount)
        }
        createItems()
        adapter = MyItemRecyclerViewAdapter(items) {
          if (!isPlaying) {
            playSound(it.first)
          }
        }
        viewAdapter = adapter as MyItemRecyclerViewAdapter
      }
    }
    return view
  }

  private fun createItems() {
    // 1
    val files = File(context?.getExternalFilesDir(null), "/AudioCaptures")
    items.clear()

    if (files.listFiles() != null) {
      val file : Array<File> = files.listFiles()!!

      // 2
      for (i in file.indices) {
        items.add(Pair(file[i], i))
      }
    } else {
      Log.d("Files", "No files")
    }
  }


  private fun playSound(file: File) {
    Thread(
        Runnable {
          playShortAudioFileViaAudioTrack(file)
        }
    ).start()
  }

  private fun playShortAudioFileViaAudioTrack(file: File) {
    try {
      val inputStream = file.inputStream()

      val bytes = ByteArray(inputStream.available())

      inputStream.read(bytes)

      val audioTrack = configureAudioTrack()

      isPlaying = true

      audioTrack.play()

      audioTrack.write(bytes, 0, bytes.size)

      audioTrack.stop()

      resetUI()

      isPlaying = false

      audioTrack.flush()
    } catch (e: FileNotFoundException) {
      e.printStackTrace()
    }
  }

  private fun resetUI() {
    Handler(Looper.getMainLooper()).post(Runnable {
      createItems()

      viewAdapter.notifyDataSetChanged()
    })
  }

  private fun configureAudioTrack() : AudioTrack {
    val size = AudioTrack.getMinBufferSize(
        SAMPLING_RATE,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT)

    val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build())
        .setAudioFormat(AudioFormat.Builder()
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(SAMPLING_RATE)
            .build())
        .setBufferSizeInBytes(size)
        .build()

    audioTrack.playbackRate = SAMPLING_RATE

    val params = audioTrack.playbackParams

    audioTrack.playbackParams = params

    return audioTrack
  }

  companion object {
    const val ARG_COLUMN_COUNT = "column-count"
    const val SAMPLING_RATE = 8000
  }
}
