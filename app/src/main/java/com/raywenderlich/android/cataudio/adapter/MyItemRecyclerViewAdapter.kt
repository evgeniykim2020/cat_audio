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

package com.raywenderlich.android.cataudio.adapter

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.raywenderlich.android.cataudio.R

import com.raywenderlich.android.cataudio.dummy.DummyContent.DummyItem
import java.io.File

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyItemRecyclerViewAdapter(
    private val values: List<Pair<File, Int>>,
    private val listener: (Pair<File, Int>) -> Unit)
  : RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder>() {

  private var isPlaying = false

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_recording_list_item, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val item = values[position]
    holder.idView.text = (position + 1).toString()
    holder.contentView.text = item.first.name
    holder.button.isEnabled = true
    holder.button.colorFilter = null
    isPlaying = false
    holder.button.setOnClickListener {
      if (!isPlaying) {
        isPlaying = true
        listener(item)
        if (item.second == position) {
          holder.button.colorFilter = PorterDuffColorFilter(holder.itemView.resources.getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP)
          holder.button.isEnabled = true
        }
      }
    }
  }

  override fun getItemCount(): Int = values.size

  inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val idView: TextView = view.findViewById(R.id.item_number)
    val contentView: TextView = view.findViewById(R.id.content)
    val button : ImageButton = view.findViewById(R.id.play_button)

    override fun toString(): String {
      return super.toString() + " '" + contentView.text + "'"
    }
  }
}
