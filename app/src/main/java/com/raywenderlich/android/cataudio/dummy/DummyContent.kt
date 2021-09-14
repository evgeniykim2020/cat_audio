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

package com.raywenderlich.android.cataudio.dummy

import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object DummyContent {

  /**
   * An array of sample (dummy) items.
   */
  val ITEMS: MutableList<DummyItem> = ArrayList()

  /**
   * A map of sample (dummy) items, by ID.
   */
  private val ITEM_MAP: MutableMap<String, DummyItem> = HashMap()

  private const val COUNT = 25

  init {
    // Add some sample items.
    for (i in 1..COUNT) {
      addItem(createDummyItem(i))
    }
  }

  private fun addItem(item: DummyItem) {
    ITEMS.add(item)
    ITEM_MAP[item.id] = item
  }

  private fun createDummyItem(position: Int): DummyItem {
    return DummyItem(position.toString(), "Item $position", makeDetails(position))
  }

  private fun makeDetails(position: Int): String {
    val builder = StringBuilder()
    builder.append("Details about Item: ").append(position)
    for (i in 0 until position) {
      builder.append("\nMore details information here.")
    }
    return builder.toString()
  }

  /**
   * A dummy item representing a piece of content.
   */
  data class DummyItem(val id: String, val content: String, val details: String) {

    override fun toString(): String = content
  }
}
