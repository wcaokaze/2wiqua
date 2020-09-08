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
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.math.MathUtils;

import com.wcaokaze.android.twiqua.BuildConfig;

import vue.VComponentInterface;

public final class VerticalColumnLayoutManager extends ColumnLayoutManager {
   private static final String TAG = "2wiqua::VerticalColumnM";

   /**
    * 0〜2までは直接wrapperLayout、3以降internalLayoutに入れる。
    * こうしないと影が濃くなりすぎる
    * <pre>
    * columnLayout {
    *    wrapperLayout {
    *       layout.gravity = CENTER_HORIZONTAL
    *
    *       columnView0 {}
    *       columnView1 {}
    *       columnView2 {}
    *
    *       internalLayout {
    *          columnView3 {}
    *          columnView4 {}
    *          ...
    *       }
    *    }
    * }
    * </pre>
    */
   private final FrameLayout mWrapperLayout;
   private final FrameLayout mInternalLayout;

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

      mWrapperLayout = new FrameLayout(context);
      mWrapperLayout.setBackgroundColor(0xffffffff);

      mInternalLayout = new FrameLayout(context);
      mInternalLayout.setBackgroundColor(0xffffffff);
   }

   @Override
   protected final void relayout(final ColumnLayout columnLayout) {
      super.relayout(columnLayout);

      mInternalLayout.removeAllViews();
      mWrapperLayout .removeAllViews();
      columnLayout   .removeAllViews();

      {
         final FrameLayout.LayoutParams wrapperLayoutParams =
               new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT);

         final int margin = (columnLayout.getWidth() - getColumnWidth()) / 2;
         wrapperLayoutParams.setMarginStart(margin);
         wrapperLayoutParams.topMargin = mTopMargin;
         wrapperLayoutParams.setMarginEnd(margin);
         mWrapperLayout.setElevation(mElevation);

         columnLayout.addView(mWrapperLayout, wrapperLayoutParams);
      }

      {
         final FrameLayout.LayoutParams internalLayoutParams =
               new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                            FrameLayout.LayoutParams.MATCH_PARENT);

         mWrapperLayout.addView(mInternalLayout, internalLayoutParams);
      }

      final ColumnLayoutAdapter adapter = columnLayout.getAdapter();
      if (adapter == null) { return; }

      final long positionRange = getVisiblePositionRange(columnLayout);
      final int topmostPosition  = (int) (positionRange >> 32);
      final int bottommostPosition = (int)  positionRange;

      addColumnView(adapter, 0, Math.min(2, bottommostPosition));
      addColumnViewIntoInternalLayout(adapter, Math.max(3, topmostPosition), bottommostPosition);

      mVisiblePositionRange = positionRange;

      applyTranslationY(columnLayout);
   }

   @Override
   protected void onDetachedFromColumnLayout(final ColumnLayout columnLayout) {
      super.onDetachedFromColumnLayout(columnLayout);
      columnLayout.removeView(mWrapperLayout);
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

         addNewVisibleView(adapter, mVisiblePositionRange, positionRange);
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
            mInternalLayout.setTranslationZ(
                  columnView.getElevation() + 3.0f * mElevation);
         } else if (p == 3) {
            mInternalLayout.setTranslationY((float) weightedPosition + 3.0f * mPositionGap);
            columnView.setTranslationZ(3.0f * mElevation);
         } else {
            columnView.setTranslationY((float) weightedPosition);
            columnView.setTranslationZ((float) p * mElevation);
         }
      }
   }

   private void addNewVisibleView(final ColumnLayoutAdapter adapter,
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
         removeAllColumnViews();
         addColumnView(adapter, 0, Math.min(2, newBottommostPosition));
         addColumnViewIntoInternalLayout(adapter,
               Math.min(3, newTopmostPosition), newBottommostPosition);
         return;
      }

      if (newTopmostPosition > oldTopmostPosition) {
         removeColumnViewFromInternalLayout(adapter,
               Math.max(3, oldTopmostPosition), newTopmostPosition - 1);
      } else if (newTopmostPosition < oldTopmostPosition) {
         addColumnViewIntoInternalLayout(adapter,
               Math.max(3, newTopmostPosition), oldTopmostPosition - 1);
      }

      if (newBottommostPosition > oldBottommostPosition) {
         addColumnViewIntoInternalLayout(adapter,
               Math.max(3, oldBottommostPosition + 1), newBottommostPosition);
      } else if (newBottommostPosition < oldBottommostPosition) {
         removeColumnViewFromInternalLayout(adapter,
               Math.max(3, newBottommostPosition + 1), oldBottommostPosition);
      }
   }

   private void addColumnView(final ColumnLayoutAdapter adapter,
                              final int startPosition, final int lastPosition)
   {
      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "adding the Column View at " + position);
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View columnView = component.getComponentView();

         initializeLayoutParams(columnView);
         mWrapperLayout.addView(columnView);
      }
   }

   private void addColumnViewIntoInternalLayout(
         final ColumnLayoutAdapter adapter,
         final int startPosition, final int lastPosition
   ) {
      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "adding the Column View at " + position + " into internalLayout");
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View columnView = component.getComponentView();

         initializeLayoutParams(columnView);
         mInternalLayout.addView(columnView);
      }
   }

   protected final void removeColumnViewFromInternalLayout(
         final ColumnLayoutAdapter adapter,
         final int startPosition, final int lastPosition
   ) {
      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "removing the Column View at " + position + " from internalLayout");
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View columnView = component.getComponentView();
         mInternalLayout.removeView(columnView);
      }
   }

   protected final void removeAllColumnViews() {
      final FrameLayout wrapperLayout = mWrapperLayout;
      final FrameLayout internalLayout = mInternalLayout;

      for (int i = wrapperLayout.getChildCount(); i >= 0; i--) {
         final View child = wrapperLayout.getChildAt(i);
         if (child == internalLayout) { continue; }
         wrapperLayout.removeView(child);
      }

      internalLayout.removeAllViews();
   }

   private void initializeLayoutParams(final View columnView) {
      final ViewGroup.LayoutParams p = columnView.getLayoutParams();

      if (p instanceof FrameLayout.LayoutParams) {
         final FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) p;
         lParams.width  = FrameLayout.LayoutParams.MATCH_PARENT;
         lParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
      } else {
         final FrameLayout.LayoutParams lParams = new FrameLayout.LayoutParams(
               FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
         columnView.setLayoutParams(lParams);
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
