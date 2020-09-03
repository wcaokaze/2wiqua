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
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.wcaokaze.android.twiqua.BuildConfig;

import vue.VComponentInterface;

public final class VerticalColumnLayoutManager extends ColumnLayoutManager {
   private static final String TAG = "2wiqua::VerticalColumnMa";

   private float mPosition = 0.0f;
   private long mVisiblePositionRange = 0L;

   private final int mTopMargin;
   private final float mElevation;

   public VerticalColumnLayoutManager(final Context context) {
      final float density = context.getResources().getDisplayMetrics().density;
      mTopMargin = (int) (8.0f * density);
      mElevation = 4.0f * density;
   }

   @Override
   protected final void relayout(final ColumnLayout view) {
      super.relayout(view);

      final int margin = (view.getWidth() - getColumnWidth()) / 2;
      final FrameLayout.LayoutParams internalLayoutParams =
            (FrameLayout.LayoutParams) view.internalLayout.getLayoutParams();
      internalLayoutParams.setMarginStart(margin);
      internalLayoutParams.topMargin = mTopMargin;
      internalLayoutParams.setMarginEnd(margin);
      view.internalLayout.setLayoutParams(internalLayoutParams);
      view.internalLayout.setElevation(mElevation * 2.0f);

      view.internalLayout.removeAllViews();

      final ColumnLayoutAdapter adapter = view.getAdapter();
      if (adapter == null) { return; }

      /*
      final long positionRange = getVisiblePositionRange(view);
      final int leftmostPosition  = (int) (positionRange >> 32);
      final int rightmostPosition = (int)  positionRange;
      */

      addColumnViewInRange(view, adapter, 0, adapter.getItemCount() - 1);

      // mVisiblePositionRange = positionRange;

      applyTranslationY(view);
   }

   /* package */ final void performDrag(final ColumnLayout view,
                                        final float y, final float dy)
   {
      mPosition -= dy;
      applyTranslationY(view);
   }

   private void applyTranslationY(final ColumnLayout view) {
      final ColumnLayoutAdapter adapter = view.getAdapter();
      if (adapter == null) { return; }

      final double position = (double) mPosition;

      /*
      final long positionRange = getVisiblePositionRange(view);
      final int leftmostPosition  = (int) (positionRange >> 32);
      final int rightmostPosition = (int)  positionRange;

      if (positionRange != mVisiblePositionRange) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "visiblePositionRange: " + leftmostPosition + " - " + rightmostPosition);
         }

         addNewVisibleView(view, adapter, mVisiblePositionRange, positionRange);
         mVisiblePositionRange = positionRange;
      }
      */

      final double viewHeight = (double) view.getHeight();

      for (int p = 0; p < adapter.getItemCount(); p++) {
         final VComponentInterface<?> component = adapter.getVComponentAt(p);
         final View columnView = component.getComponentView();

         columnView.setTranslationY((float) (
               Math.pow((double) p / 5.0 + position / viewHeight, 1.3)
                     * viewHeight
         ));

         columnView.setTranslationZ((float) p * mElevation);
      }
   }
}