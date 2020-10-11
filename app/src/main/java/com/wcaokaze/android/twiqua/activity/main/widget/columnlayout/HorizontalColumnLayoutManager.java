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

import androidx.core.math.MathUtils;

import com.wcaokaze.android.twiqua.BuildConfig;

import vue.VComponentInterface;

public final class HorizontalColumnLayoutManager extends ColumnLayoutManager {
   private static final String TAG = "2wiqua::HorizontalColum";

   private int mPosition = 0;
   private long mVisiblePositionRange = 0L;
   private float mScrollOffset = 0.0f;

   @Override
   protected final void relayout(final ColumnLayout view) {
      super.relayout(view);

      view.removeAllViews();

      final ColumnLayoutAdapter adapter = view.getAdapter();
      if (adapter == null) { return; }

      final long positionRange = getVisiblePositionRange(view);
      final int leftmostPosition  = (int) (positionRange >> 32);
      final int rightmostPosition = (int)  positionRange;

      addColumnView(view, adapter, leftmostPosition, rightmostPosition);

      mVisiblePositionRange = positionRange;

      applyTranslationX(view);
   }

   /* package */ final void performDrag(final ColumnLayout view, final float dx) {
      float scrollOffset = mScrollOffset - dx;

      final float columnMargin = (float) view.getColumnMargin();
      final float columnWidth = (float) getColumnWidth();
      final float columnDistance = columnWidth + columnMargin * 2.0f;

      if (scrollOffset < -(columnWidth + columnMargin)) {
         final ColumnLayoutAdapter adapter = view.getAdapter();

         if (adapter != null && mPosition < adapter.getItemCount()) {
            mPosition++;
            scrollOffset += columnDistance;

            if (BuildConfig.DEBUG) {
               Log.i(TAG, "position: " + mPosition);
            }
         }
      } else if (scrollOffset > columnMargin) {
         if (mPosition > 0) {
            mPosition--;
            scrollOffset -= columnDistance;

            if (BuildConfig.DEBUG) {
               Log.i(TAG, "position: " + mPosition);
            }
         }
      }

      mScrollOffset = scrollOffset;
      applyTranslationX(view);
   }

   private void applyTranslationX(final ColumnLayout view) {
      final ColumnLayoutAdapter adapter = view.getAdapter();
      if (adapter == null) { return; }

      final int position = mPosition;
      final int columnDistance = getColumnWidth() + view.getColumnMargin() * 2;
      final float scrollOffset = mScrollOffset;

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

      for (int p = leftmostPosition; p <= rightmostPosition; p++) {
         final VComponentInterface<?> component = adapter.getVComponentAt(p);
         final View columnView = component.getComponentView();

         columnView.setTranslationX((float) ((p - position) * columnDistance) + scrollOffset);
      }
   }

   private void addNewVisibleView(final ColumnLayout columnLayout,
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
         columnLayout.removeAllViews();
         addColumnView(columnLayout, adapter, newLeftmostPosition, newRightmostPosition);
         return;
      }

      if (newLeftmostPosition > oldLeftmostPosition) {
         removeColumnView(columnLayout, adapter,
               oldLeftmostPosition, newLeftmostPosition - 1);
      } else if (newLeftmostPosition < oldLeftmostPosition) {
         addColumnView(columnLayout, adapter,
               newLeftmostPosition, oldLeftmostPosition - 1);
      }

      if (newRightmostPosition > oldRightmostPosition) {
         addColumnView(columnLayout, adapter,
               oldRightmostPosition + 1, newRightmostPosition);
      } else if (newRightmostPosition < oldRightmostPosition) {
         removeColumnView(columnLayout, adapter,
               newRightmostPosition + 1, oldRightmostPosition);
      }
   }

   private void addColumnView(
         final ColumnLayout columnLayout, final ColumnLayoutAdapter adapter,
         final int startPosition, final int lastPosition
   ) {
      final int columnWidth = getColumnWidth();

      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "adding the Column View at " + position);
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View columnView = component.getComponentView();

         initializeLayoutParams(columnView, columnWidth);
         columnLayout.addView(columnView);
      }
   }

   private void removeColumnView(
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

   private void initializeLayoutParams(final View columnView, final int columnWidth) {
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
   }

   /**
    * @return ColumnLayout内で左端に表示されているカラムのpositionを上位32ビット、
    *         右端に表示されているカラムのpositionを下位32ビットとして
    *         組み合わせた64ビットの数値。
    *         Adapterがセットされていない、もしくはセットされているが
    *         {@link ColumnLayoutAdapter#getItemCount()}が0を返す場合は-1。
    */
   private long getVisiblePositionRange(final ColumnLayout view) {
      final ColumnLayoutAdapter adapter = view.getAdapter();
      if (adapter == null) { return -1L; }

      final int itemCount = adapter.getItemCount();
      if (itemCount <= 0) { return -1L; }

      final int visibleColumnCount = view.getVisibleColumnCount();

      final int columnMargin = view.getColumnMargin();
      final int columnDistance = getColumnWidth() + columnMargin * 2;
      final float scrollOffset = mScrollOffset;

      final int leftmostPosition;
      final int rightmostPosition;

      if (scrollOffset <= (float) columnMargin) {
         leftmostPosition = mPosition;
      } else {
         leftmostPosition = mPosition - 1;
      }

      if (scrollOffset + (float) (-columnMargin + columnDistance * visibleColumnCount)
            >= (float) view.getWidth())
      {
         rightmostPosition = mPosition + visibleColumnCount - 1;
      } else {
         rightmostPosition = mPosition + visibleColumnCount;
      }

      final int higher = MathUtils.clamp(leftmostPosition,  0, itemCount - 1);
      final int lower  = MathUtils.clamp(rightmostPosition, 0, itemCount - 1);

      return (long) higher << 32 | (long) lower;
   }
}
