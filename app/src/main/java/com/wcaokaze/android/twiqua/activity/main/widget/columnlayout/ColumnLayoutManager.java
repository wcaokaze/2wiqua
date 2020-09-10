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

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

public abstract class ColumnLayoutManager {
   private int mColumnWidth = 0;

   @Nullable
   private ColumnLayout mColumnLayout = null;

   @Nullable
   protected final ColumnLayout getColumnLayout() {
      return mColumnLayout;
   }

   @CallSuper
   protected void relayout(final ColumnLayout view) {
      final int columnMargin = view.getColumnMargin();
      final int layoutWidth = view.getWidth() - view.getPadding() * 2;
      mColumnWidth = layoutWidth / view.getVisibleColumnCount() - columnMargin * 2;
   }

   @CallSuper
   protected void onAttachedToColumnLayout(final ColumnLayout columnLayout) {
      if (mColumnLayout != null) {
         throw new IllegalStateException(
               "This ColumnLayoutManager already attached to another ColumnLayout. " +
               "Remove from ColumnLayout first.");
      }

      mColumnLayout = columnLayout;
   }

   @CallSuper
   protected void onDetachedFromColumnLayout(final ColumnLayout columnLayout) {
      mColumnLayout = null;
   }

   protected final int getColumnWidth() {
      return mColumnWidth;
   }
}
