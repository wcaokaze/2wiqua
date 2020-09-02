/*
 * Copyright 2020 wcaokaze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wcaokaze.android.twiqua.activity.main

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.wcaokaze.android.twiqua.activity.main.widget.columnlayout.ColumnLayout
import com.wcaokaze.android.twiqua.activity.main.widget.columnlayout.ColumnLayoutAdapter
import com.wcaokaze.android.twiqua.activity.main.widget.columnlayout.HorizontalColumnLayoutManager
import com.wcaokaze.android.twiqua.activity.main.widget.columnlayout.DebugGestureDetector
import koshian.*
import vue.*

class MainActivity : Activity() {
   private class ColumnComponent(context: Context) : VComponent<Nothing>() {
      override val store: Nothing get() = throw UnsupportedOperationException()

      override val componentView = koshian(context) {
         TextView {
            view.backgroundColor = 0xfafafa.opaque
            view.text = "Hello"
            view.elevation = 32.dp.toFloat()
         }
      }
   }

   private val columnLayoutAdapter = object : ColumnLayoutAdapter() {
      private val components by lazy {
         List(3) { ColumnComponent(this@MainActivity) }
      }

      override fun getItemCount() = components.size
      override fun getVComponentAt(position: Int) = components[position]
   }

   private val columnLayoutManager =
      HorizontalColumnLayoutManager()

   private val columnLayoutGestureDetector by lazy {
      DebugGestureDetector(columnLayoutManager, this)
   }

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      buildLayout()
   }

   private fun buildLayout() {
      val contextView = koshian(this) {
         ColumnLayout {
            view.adapter = columnLayoutAdapter
            view.layoutManager = columnLayoutManager
            view.gestureDetector = columnLayoutGestureDetector
            view.columnMargin = 16.dp
            view.visibleColumnCount = 2
         }
      }

      setContentView(contextView)
   }
}
