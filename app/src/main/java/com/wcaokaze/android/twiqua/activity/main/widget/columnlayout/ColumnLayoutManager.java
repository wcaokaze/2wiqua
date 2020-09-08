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

   protected final void addColumnView(
         final ColumnLayout columnLayout, final ColumnLayoutAdapter adapter,
         final int startPosition, final int lastPosition
   ) {
      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "adding the Column View at " + position);
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View columnView = component.getComponentView();

         initializeLayoutParams(columnView);
         columnLayout.addView(columnView);
      }
   }

   protected final void addColumnViewIntoInternalLayout(
         final ColumnLayout columnLayout, final ColumnLayoutAdapter adapter,
         final int startPosition, final int lastPosition
   ) {
      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "adding the Column View at " + position + " into internalLayout");
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View columnView = component.getComponentView();

         initializeLayoutParams(columnView);
         columnLayout.internalLayout.addView(columnView);
      }
   }

   private void initializeLayoutParams(final View columnView) {
      final ViewGroup.LayoutParams p = columnView.getLayoutParams();

      if (p instanceof FrameLayout.LayoutParams) {
         final FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) p;
         lParams.width = mColumnWidth;
         lParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
      } else {
         final FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(
               mColumnWidth, FrameLayout.LayoutParams.MATCH_PARENT);
         columnView.setLayoutParams(lParams);
      }
   }

   protected final void removeAllColumnViews(final ColumnLayout columnLayout) {
      final FrameLayout internalLayout = columnLayout.internalLayout;

      for (int i = columnLayout.getChildCount(); i >= 0; i--) {
         final View child = columnLayout.getChildAt(i);
         if (child == internalLayout) { continue; }
         columnLayout.removeView(child);
      }

      internalLayout.removeAllViews();
   }

   protected final void removeColumnView(
         final ColumnLayout columnLayout, final ColumnLayoutAdapter adapter,
         final int startPosition, final int lastPosition
   ) {
      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "removing the Column View at " + position);
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View columnView = component.getComponentView();
         columnLayout.removeView(columnView);
      }
   }

   protected final void removeColumnViewFromInternalLayout(
         final ColumnLayout columnLayout, final ColumnLayoutAdapter adapter,
         final int startPosition, final int lastPosition
   ) {
      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "removing the Column View at " + position + " from internalLayout");
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View columnView = component.getComponentView();
         columnLayout.internalLayout.removeView(columnView);
      }
   }
}
