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

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.CallSuper;

import com.wcaokaze.android.twiqua.BuildConfig;

import vue.VComponentInterface;

public abstract class ColumnLayoutManager {
   private static final String TAG = "2wiqua::ColumnLayoutMan";

   private int mColumnWidth = 0;

   @CallSuper
   protected void relayout(final ColumnLayout view) {
      final int columnMargin = view.getColumnMargin();
      final int layoutWidth = view.getWidth() - view.getPadding() * 2;
      mColumnWidth = layoutWidth / view.getVisibleColumnCount() - columnMargin * 2;
   }

   protected final int getColumnWidth() {
      return mColumnWidth;
   }

   protected final void addNewVisibleView(final ColumnLayout columnLayout,
                                          final ColumnLayoutAdapter adapter,
                                          final long oldVisiblePositionRange,
                                          final long newVisiblePositionRange)
   {
      final int oldLeftmostPosition  = (int) (oldVisiblePositionRange >> 32);
      final int oldRightmostPosition = (int)  oldVisiblePositionRange;
      final int newLeftmostPosition  = (int) (newVisiblePositionRange >> 32);
      final int newRightmostPosition = (int)  newVisiblePositionRange;

      if (newLeftmostPosition  > oldRightmostPosition ||
          newRightmostPosition < oldLeftmostPosition)
      {
         columnLayout.internalLayout.removeAllViews();
         addColumnViewInRange(columnLayout, adapter, newLeftmostPosition, newRightmostPosition);
         return;
      }

      if (newLeftmostPosition > oldLeftmostPosition) {
         removeColumnViewInRange(columnLayout, adapter,
               oldLeftmostPosition, newLeftmostPosition - 1);
      } else if (newLeftmostPosition < oldLeftmostPosition) {
         addColumnViewInRange(columnLayout, adapter,
               newLeftmostPosition, oldLeftmostPosition - 1);
      }

      if (newRightmostPosition > oldRightmostPosition) {
         addColumnViewInRange(columnLayout, adapter,
               oldRightmostPosition + 1, newRightmostPosition);
      } else if (newRightmostPosition < oldRightmostPosition) {
         removeColumnViewInRange(columnLayout, adapter,
               newRightmostPosition + 1, oldRightmostPosition);
      }
   }

   protected final void addColumnViewInRange(final ColumnLayout columnLayout,
                                             final ColumnLayoutAdapter adapter,
                                             final int startPosition, final int lastPosition)
   {
      final int columnWidth = mColumnWidth;

      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "adding the Column View at " + position);
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View columnView = component.getComponentView();

         final ViewGroup.LayoutParams p = columnView.getLayoutParams();

         if (p instanceof FrameLayout.LayoutParams) {
            final FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) p;
            lParams.width = columnWidth;
            lParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
         } else {
            final FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(
                  columnWidth, FrameLayout.LayoutParams.MATCH_PARENT);
            columnView.setLayoutParams(lParams);
         }

         columnLayout.internalLayout.addView(columnView);
      }
   }

   protected final void removeColumnViewInRange(final ColumnLayout columnLayout,
                                                final ColumnLayoutAdapter adapter,
                                                final int startPosition, final int lastPosition)
   {
      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "removing the Column View at " + position);
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View columnView = component.getComponentView();
         columnLayout.internalLayout.removeView(columnView);
      }
   }
}
