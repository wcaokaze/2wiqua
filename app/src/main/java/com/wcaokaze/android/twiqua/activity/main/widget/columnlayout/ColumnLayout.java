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

package com.wcaokaze.android.twiqua.activity.main.widget.columnlayout;

import android.content.Context;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

public final class ColumnLayout extends FrameLayout {
   private int mVisibleColumnCount = 1;
   private int mColumnMargin = 0;
   private int mPadding = 0;

   @Nullable private ColumnLayoutAdapter mAdapter = null;
   @Nullable private ColumnLayoutManager mLayoutManager = null;
   @Nullable private ColumnLayoutGestureDetector<?> mGestureDetector = null;

   public ColumnLayout(final Context context) {
      super(context);
   }

   @Nullable
   public final ColumnLayoutAdapter getAdapter() {
      return mAdapter;
   }

   public final void setAdapter(@Nullable final ColumnLayoutAdapter adapter) {
      mAdapter = adapter;
      relayout();
   }

   public final int getColumnMargin() {
      return mColumnMargin;
   }

   public final void setColumnMargin(final int columnMargin) {
      mColumnMargin = columnMargin;
      relayout();
   }

   @Nullable
   public final ColumnLayoutGestureDetector<?> getGestureDetector() {
      return mGestureDetector;
   }

   public final void setGestureDetector
         (@Nullable final ColumnLayoutGestureDetector<?> gestureDetector)
   {
      mGestureDetector = gestureDetector;
   }

   @Nullable
   public final ColumnLayoutManager getLayoutManager() {
      return mLayoutManager;
   }

   public final void setLayoutManager(@Nullable final ColumnLayoutManager layoutManager) {
      if (mLayoutManager != null) {
         mLayoutManager.onDetachedFromColumnLayout(this);
      }

      mLayoutManager = layoutManager;

      if (layoutManager != null) {
         layoutManager.onAttachedToColumnLayout(this);
      }

      relayout();
   }

   public final int getPadding() {
      return mPadding;
   }

   public final void setPadding(final int padding) {
      mPadding = padding;
      relayout();
   }

   public final int getVisibleColumnCount() {
      return mVisibleColumnCount;
   }

   public final void setVisibleColumnCount(final int visibleColumnCount) {
      mVisibleColumnCount = visibleColumnCount;
      relayout();
   }

   @Override
   public final boolean onInterceptTouchEvent(final MotionEvent ev) {
      final ColumnLayoutGestureDetector<?> gestureDetector = mGestureDetector;
      if (gestureDetector == null) { return false; }
      return gestureDetector.onInterceptTouchEvent(this, ev);
   }

   @Override
   public final boolean onTouchEvent(final MotionEvent event) {
      final ColumnLayoutGestureDetector<?> gestureDetector = mGestureDetector;
      if (gestureDetector == null) { return false; }
      return gestureDetector.onTouchEvent(this, event);
   }

   private void relayout() {
      final ColumnLayoutManager layoutManager = mLayoutManager;
      if (layoutManager != null) {
         layoutManager.relayout(this);
      }
   }

   @Override
   protected final void onSizeChanged(final int w, final int h,
                                      final int oldw, final int oldh)
   {
      super.onSizeChanged(w, h, oldw, oldh);

      post(new Runnable() {
         @Override
         public void run() {
            relayout();
         }
      });
   }
}
