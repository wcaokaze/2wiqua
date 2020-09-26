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
import com.wcaokaze.android.twiqua.anim.AnimationFrameHandler;
import com.wcaokaze.android.twiqua.anim.FloatAnimator;

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

   private float mScrollPosition = 0.0f;
   private long mVisiblePositionRange = 0L;

   private int mRearrangingColumnPosition = -1;
   private float mRearrangingColumnTop = Float.NaN;

   private final int mTopMargin;
   private final float mElevation;
   private final float mPositionGap;

   private boolean mIsAutomaticScrolling = false;

   private final AnimationFrameHandler.Callback mAutomaticScroller = new AnimationFrameHandler.Callback() {
      @Override
      public final void onFrame(final long timeMillis) {
         final ColumnLayout columnLayout = getColumnLayout();
         if (columnLayout == null) { return; }

         final float velocity = getAutoScrollVelocity(
               (float) columnLayout.getHeight(),
               mRearrangingColumnTop);

         incrementScrollPosition(columnLayout, velocity);
         rearrangeIfNecessary(columnLayout);
         applyTranslationY(columnLayout);
      }
   };

   // ==========================================================================

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

   // ==========================================================================

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

   // ==========================================================================

   /* package */ final void performDrag(final ColumnLayout view,
                                        final float y, final float dy)
   {
      incrementScrollPosition(view, dy);
      applyTranslationY(view);
   }

   private void incrementScrollPosition(final ColumnLayout columnLayout,
                                        final float dy)
   {
      final ColumnLayoutAdapter adapter = columnLayout.getAdapter();
      if (adapter == null) { return; }

      final float t = mScrollPosition - dy;

      final int lastIndex = adapter.getItemCount() - 1;
      final float viewHeight = (float) columnLayout.getHeight();
      final float min = (float) lastIndex * viewHeight / -5.0f;
      final float max = viewHeight / 5.0f;

      if (t < min) {
         mScrollPosition = min;
      } else if (t > max) {
         mScrollPosition = max;
      } else {
         mScrollPosition = t;
      }
   }

   // ==========================================================================

   /* package */ final void startRearrangingMode(final ColumnLayout view,
                                                 final float y)
   {
      final ColumnLayoutAdapter adapter = view.getAdapter();
      if (adapter == null) { return; }

      final float scrollPosition = mScrollPosition;
      final float viewHeight = (float) view.getHeight();
      final float positionGap = mPositionGap;

      mRearrangingColumnPosition = getPosition(
            scrollPosition,
            viewHeight,
            positionGap,
            y
      );

      mRearrangingColumnTop = getTop(
            scrollPosition,
            mRearrangingColumnPosition,
            viewHeight,
            positionGap
      );

      applyTranslationY(view);
   }

   /* package */ final void finishRearrangingMode(final ColumnLayout view,
                                                  final float y)
   {
      final float from = mRearrangingColumnTop;

      final float to = getTop(
            mScrollPosition,
            mRearrangingColumnPosition,
            (float) view.getHeight(),
            mPositionGap
      );

      new FloatAnimator(from, to, /* duration = */ 150L) {
         @Override
         public void update(final long animationTime,
                            final float animationTimeRate,
                            final float value)
         {
            mRearrangingColumnTop = value;
            applyTranslationY(view);
         }

         @Override
         public void onFinish() {
            mRearrangingColumnPosition = -1;
            mRearrangingColumnTop = Float.NaN;
            applyTranslationY(view);
         }
      };
   }

   /* package */ final void performRearrangingDrag(final ColumnLayout view,
                                                   final float y, final float dy)
   {
      final float rearrangingColumnTop = mRearrangingColumnTop - dy;
      mRearrangingColumnTop = rearrangingColumnTop;

      rearrangeIfNecessary(view);

      if (mIsAutomaticScrolling) {
         if (getAutoScrollVelocity((float) view.getHeight(), rearrangingColumnTop) == 0.0f) {
            stopAutomaticScroll();
         }
      } else {
         if (getAutoScrollVelocity((float) view.getHeight(), rearrangingColumnTop) != 0.0f) {
            startAutomaticScroll();
         }
      }

      applyTranslationY(view);
   }

   private void rearrangeIfNecessary(final ColumnLayout columnLayout) {
      final float scrollPosition = mScrollPosition;
      final int rearrangingColumnPosition = mRearrangingColumnPosition;
      final float rearrangingColumnTop = mRearrangingColumnTop;
      final float viewHeight = (float) columnLayout.getHeight();
      final float positionGap = mPositionGap;

      final float prevColumnTop = getTop(scrollPosition, rearrangingColumnPosition - 1, viewHeight, positionGap);
      final float nextColumnTop = getTop(scrollPosition, rearrangingColumnPosition + 1, viewHeight, positionGap);

      if (rearrangingColumnTop < prevColumnTop) {
         final int oldPosition = mRearrangingColumnPosition;
         final int newPosition = oldPosition - 1;

         mRearrangingColumnPosition = newPosition;

         final ColumnLayoutAdapter adapter = columnLayout.getAdapter();
         if (adapter != null) {
            if (newPosition == 2) {
               removeColumnView(adapter, 2, 2);
               removeColumnViewFromInternalLayout(adapter, 3, 3);
               adapter.onRearranged(oldPosition, newPosition);
               addColumnView(adapter, 2, 2);
               addColumnViewIntoInternalLayout(adapter, 3, 3);
            } else {
               adapter.onRearranged(oldPosition, newPosition);
            }
         }
      } else if (rearrangingColumnTop > nextColumnTop) {
         final int oldPosition = mRearrangingColumnPosition;
         final int newPosition = oldPosition + 1;

         mRearrangingColumnPosition = newPosition;

         final ColumnLayoutAdapter adapter = columnLayout.getAdapter();
         if (adapter != null) {
            if (newPosition == 3) {
               removeColumnView(adapter, 2, 2);
               removeColumnViewFromInternalLayout(adapter, 3, 3);
               adapter.onRearranged(oldPosition, newPosition);
               addColumnView(adapter, 2, 2);
               addColumnViewIntoInternalLayout(adapter, 3, 3);
            } else {
               adapter.onRearranged(oldPosition, newPosition);
            }
         }
      }
   }

   // ==========================================================================

   private static float getAutoScrollVelocity(final float columnLayoutHeight,
                                              final float rearrangingColumnTop)
   {
      final float automaticScrollHeight = columnLayoutHeight * 0.2f;

      if (rearrangingColumnTop < automaticScrollHeight) {
         return (rearrangingColumnTop - automaticScrollHeight) * 0.1f;
      } else if (rearrangingColumnTop > columnLayoutHeight - automaticScrollHeight) {
         return (rearrangingColumnTop - columnLayoutHeight + automaticScrollHeight) * 0.1f;
      } else {
         return 0.0f;
      }
   }

   private void startAutomaticScroll() {
      if (mIsAutomaticScrolling) { return; }
      mIsAutomaticScrolling = true;
      AnimationFrameHandler.INSTANCE.addCallback(mAutomaticScroller);
   }

   private void stopAutomaticScroll() {
      if (!mIsAutomaticScrolling) { return; }
      mIsAutomaticScrolling = false;
      AnimationFrameHandler.INSTANCE.removeCallback(mAutomaticScroller);
   }

   // ==========================================================================

   private void applyTranslationY(final ColumnLayout view) {
      final ColumnLayoutAdapter adapter = view.getAdapter();
      if (adapter == null) { return; }

      final float scrollPosition = mScrollPosition;
      final int rearrangingColumnPosition = mRearrangingColumnPosition;

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

      final float viewHeight = (float) view.getHeight();
      final float elevation = mElevation;
      final float positionGap = mPositionGap;

      // ----

      if (bottommostPosition < 0) { return; }

      final float translationY0;
      if (rearrangingColumnPosition == 0) {
         translationY0 = mRearrangingColumnTop;
      } else {
         translationY0 = getTranslationY(scrollPosition, 0, viewHeight);
      }

      mWrapperLayout.setTranslationY(translationY0);

      // ----

      if (bottommostPosition < 1) { return; }

      final float translationY1;
      if (rearrangingColumnPosition == 1) {
         translationY1 = mRearrangingColumnTop - positionGap;
      } else {
         translationY1 = getTranslationY(scrollPosition, 1, viewHeight);
      }

      final View columnView1 = adapter.getVComponentAt(1).getComponentView();
      columnView1.setTranslationY(translationY1 - translationY0 + positionGap);
      columnView1.setTranslationZ(elevation);

      // ----

      if (bottommostPosition < 2) { return; }

      final float translationY2;
      if (rearrangingColumnPosition == 2) {
         translationY2 = mRearrangingColumnTop - 2.0f * positionGap;
      } else {
         translationY2 = getTranslationY(scrollPosition, 2, viewHeight);
      }

      final View columnView2 = adapter.getVComponentAt(2).getComponentView();
      columnView2.setTranslationY(translationY2 - translationY0 + 2.0f * positionGap);
      columnView2.setTranslationZ(2.0f * elevation);

      // ----

      if (bottommostPosition < 3) { return; }

      final float translationY3;
      if (rearrangingColumnPosition == 3) {
         translationY3 = mRearrangingColumnTop - 3.0f * positionGap;
      } else {
         translationY3 = getTranslationY(scrollPosition, 3, viewHeight);
      }

      mInternalLayout.setTranslationY(translationY3 - translationY0 + 3.0f * positionGap);
      mInternalLayout.setTranslationZ(columnView2.getElevation() + 3.0f * elevation);

      // ----

      if (topmostPosition <= 3) {
         final View columnView3 = adapter.getVComponentAt(3).getComponentView();
         columnView3.setTranslationY(0.0f);
         columnView3.setTranslationZ(3.0f * elevation);
      }

      for (int position = Math.max(4, topmostPosition);
           position <= bottommostPosition;
           position++)
      {
         final View columnViewP = adapter.getVComponentAt(position).getComponentView();
         final float translationYP = getTranslationY(scrollPosition, position, viewHeight);

         if (position == rearrangingColumnPosition) {
            columnViewP.setTranslationY(mRearrangingColumnTop - translationY3 - 3.0f * positionGap);
         } else {
            columnViewP.setTranslationY(translationYP - translationY3);
         }

         columnViewP.setTranslationZ((float) position * elevation);
      }
   }

   private static float getTranslationY
         (final float scrollPosition, final int position, final float viewHeight)
   {
      final float scaledY = (float) position / 5.0f + scrollPosition / viewHeight;
      return (scaledY <= 0.0f) ? 0.0f : (float) Math.pow((double) scaledY, 1.3) * viewHeight;
   }

   private static float getTop(final float scrollPosition,
                               final int position,
                               final float viewHeight,
                               final float positionGap)
   {
      final float translationY = getTranslationY(scrollPosition, position, viewHeight);

      if (position <= 2) {
         return translationY + (float) position * positionGap;
      } else {
         return translationY + 3.0f * positionGap;
      }
   }

   private static int getPosition(final float scrollPosition,
                                  final float viewHeight,
                                  final float positionGap,
                                  final float touchY)
   {
      /*
       *                                   1
       *                                 -----
       *   ⎧ ⎛  touchY - 3 positionGap  ⎞ 1.3      scrollPosition  ⎫
       * 5 ⎨ ⎜ ------------------------ ⎟      -  ---------------- ⎬
       *   ⎩ ⎝       viewHeight         ⎠            viewHeight    ⎭
       */
      return (int) (5.0f * ((float) Math.pow(
            (double) ((touchY - 3.0f * positionGap) / (float) viewHeight), 1.0 / 1.3)
            - scrollPosition / (float) viewHeight));
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

      final double position = (double) mScrollPosition;
      final double viewHeight = (double) view.getHeight();

      final int topmostPosition = (int) (-5.0 * position / viewHeight);
      final int bottommostPosition = (int) (5.0 * (1.0 - position / viewHeight));

      final int higher = MathUtils.clamp(topmostPosition,    0, itemCount - 1);
      final int lower  = MathUtils.clamp(bottommostPosition, 0, itemCount - 1);

      return (long) higher << 32 | (long) lower;
   }

   // ==========================================================================

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

   private void removeColumnView(final ColumnLayoutAdapter adapter,
                                 final int startPosition, final int lastPosition)
   {
      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "removing the Column View at " + position);
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View columnView = component.getComponentView();
         mWrapperLayout.removeView(columnView);
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
}
