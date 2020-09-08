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

import androidx.core.math.MathUtils;

import com.wcaokaze.android.twiqua.BuildConfig;

import vue.VComponentInterface;

public final class VerticalColumnLayoutManager extends ColumnLayoutManager {
   private static final String TAG = "2wiqua::VerticalColumnM";

   private float mPosition = 0.0f;
   private long mVisiblePositionRange = 0L;

   private final int mTopMargin;
   private final float mElevation;
   private final float mPositionGap;

   public VerticalColumnLayoutManager(final Context context) {
      final float density = context.getResources().getDisplayMetrics().density;
      mTopMargin = (int) (8.0f * density);
      mElevation = 4.0f * density;
      mPositionGap = 6.0f * density;
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

      removeAllColumnViews(view);

      final ColumnLayoutAdapter adapter = view.getAdapter();
      if (adapter == null) { return; }

      final long positionRange = getVisiblePositionRange(view);
      final int topmostPosition  = (int) (positionRange >> 32);
      final int bottommostPosition = (int)  positionRange;

      addColumnView(view, adapter, 0, Math.min(2, bottommostPosition));
      addColumnViewIntoInternalLayout(view, adapter, Math.max(3, topmostPosition), bottommostPosition);

      mVisiblePositionRange = positionRange;

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

      final long positionRange = getVisiblePositionRange(view);
      final int topmostPosition  = (int) (positionRange >> 32);
      final int bottommostPosition = (int)  positionRange;

      if (positionRange != mVisiblePositionRange) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "visiblePositionRange: " + topmostPosition + " - " + bottommostPosition);
         }

         addNewVisibleView(view, adapter, mVisiblePositionRange, positionRange);
         mVisiblePositionRange = positionRange;
      }

      final double viewHeight = (double) view.getHeight();

      for (int p = topmostPosition; p <= bottommostPosition; p++) {
         final VComponentInterface<?> component = adapter.getVComponentAt(p);
         final View columnView = component.getComponentView();

         final double scaledPosition = (double) p / 5.0 + position / viewHeight;
         final double weightedPosition =
               (scaledPosition <= 0.0)
                     ? 0.0
                     : Math.pow(scaledPosition, 1.3) * viewHeight;

         if (p == 1) {
            columnView.setTranslationY((float) weightedPosition + (float) p * mPositionGap);
            columnView.setTranslationZ(mElevation);
         } else if (p == 2) {
            columnView.setTranslationY((float) weightedPosition + (float) p * mPositionGap);
            columnView.setTranslationZ(2.0f * mElevation);
            view.internalLayout.setTranslationZ(
                  columnView.getElevation() + 3.0f * mElevation);
         } else if (p == 3) {
            view.internalLayout.setTranslationY((float) weightedPosition + 3.0f * mPositionGap);
            columnView.setTranslationZ(3.0f * mElevation);
         } else {
            columnView.setTranslationY((float) weightedPosition);
            columnView.setTranslationZ((float) p * mElevation);
         }
      }
   }

   private void addNewVisibleView(final ColumnLayout columnLayout,
                                  final ColumnLayoutAdapter adapter,
                                  final long oldVisiblePositionRange,
                                  final long newVisiblePositionRange)
   {
      final int oldTopmostPosition    = (int) (oldVisiblePositionRange >> 32);
      final int oldBottommostPosition = (int)  oldVisiblePositionRange;
      final int newTopmostPosition    = (int) (newVisiblePositionRange >> 32);
      final int newBottommostPosition = (int)  newVisiblePositionRange;

      if (newTopmostPosition    > oldBottommostPosition ||
          newBottommostPosition < oldTopmostPosition)
      {
         removeAllColumnViews(columnLayout);
         addColumnView(columnLayout, adapter,
               0, Math.min(2, newBottommostPosition));
         addColumnViewIntoInternalLayout(columnLayout, adapter,
               Math.min(3, newTopmostPosition), newBottommostPosition);
         return;
      }

      if (newTopmostPosition > oldTopmostPosition) {
         removeColumnViewFromInternalLayout(columnLayout, adapter,
               Math.max(3, oldTopmostPosition), newTopmostPosition - 1);
      } else if (newTopmostPosition < oldTopmostPosition) {
         addColumnViewIntoInternalLayout(columnLayout, adapter,
               Math.max(3, newTopmostPosition), oldTopmostPosition - 1);
      }

      if (newBottommostPosition > oldBottommostPosition) {
         addColumnViewIntoInternalLayout(columnLayout, adapter,
               Math.max(3, oldBottommostPosition + 1), newBottommostPosition);
      } else if (newBottommostPosition < oldBottommostPosition) {
         removeColumnViewFromInternalLayout(columnLayout, adapter,
               Math.max(3, newBottommostPosition + 1), oldBottommostPosition);
      }
   }

   /**
    * @return ColumnLayout内で一番上に表示されているカラムのpositionを上位32ビット、
    *         一番下に表示されているカラムのpositionを下位32ビットとして
    *         組み合わせた64ビットの数値。
    *         Adapterがセットされていない、もしくはセットされているが
    *         {@link ColumnLayoutAdapter#getItemCount()}が0を返す場合は-1。
    */
   private long getVisiblePositionRange(final ColumnLayout view) {
      final ColumnLayoutAdapter adapter = view.getAdapter();
      if (adapter == null) { return -1L; }

      final int itemCount = adapter.getItemCount();
      if (itemCount <= 0) { return -1L; }

      final double position = (double) mPosition;
      final double viewHeight = (double) view.getHeight();

      final int topmostPosition = (int) (-5.0 * position / viewHeight);
      final int bottommostPosition = itemCount - 1;

      final int higher = MathUtils.clamp(topmostPosition,    0, itemCount - 1);
      final int lower  = MathUtils.clamp(bottommostPosition, 0, itemCount - 1);

      return (long) higher << 32 | (long) lower;
   }
}
