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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import com.wcaokaze.android.twiqua.BuildConfig;

import vue.VComponentInterface;

public final class ColumnLayout extends FrameLayout {
   private static final class LayoutParams extends FrameLayout.LayoutParams {
      /* package */ LayoutParams(final int width, final int height) {
         super(width, height);
      }
   }

   private static final String TAG = "2wiqua::ColumnLayout";

   private static final int INVALID_POINTER = -1;

   private int mActivePointerId = INVALID_POINTER;

   private boolean mIsBeingDragged;
   private boolean mIsUnableToDrag;
   private boolean mIsScrollStarted;

   private final float mTouchSlop;

   private float mLastMotionX;
   private float mLastMotionY;
   private float mInitialMotionX;
   private float mInitialMotionY;

   private int mVisibleColumnCount = 1;
   private int mColumnWidth = 0;
   private int mColumnMargin = 0;

   private int mPosition = 0;
   private long mVisiblePositionRange = 0L;
   private float mScrollOffset = 0.0f;

   @Nullable
   private ColumnLayoutAdapter mAdapter = null;

   public ColumnLayout(final Context context) {
      super(context);

      final ViewConfiguration configuration = ViewConfiguration.get(context);
      mTouchSlop = (float) configuration.getScaledPagingTouchSlop();
   }

   public final ColumnLayoutAdapter getAdapter() {
      return mAdapter;
   }

   public final void setAdapter(final ColumnLayoutAdapter adapter) {
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

   public final int getVisibleColumnCount() {
      return mVisibleColumnCount;
   }

   public final void setVisibleColumnCount(final int visibleColumnCount) {
      mVisibleColumnCount = visibleColumnCount;
      relayout();
   }

   @Override
   public final boolean onInterceptTouchEvent(final MotionEvent ev) {
      /*
       * このメソッドはモーション処理にinterceptするかどうかを判断するだけです。
       * trueを返した場合onMotionEventが呼び出されるので実際のスクロール処理は
       * そこで行います。
       */

      final int action = ev.getAction() & MotionEvent.ACTION_MASK;

      if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
         resetTouch();
         return false;
      }

      if (action != MotionEvent.ACTION_DOWN) {
         if (mIsBeingDragged) { return true; }
      }

      switch (action) {
         case MotionEvent.ACTION_MOVE: {
            final int activePointerId = mActivePointerId;
            if (activePointerId == INVALID_POINTER) { break; }

            final int pointerIndex = ev.findPointerIndex(activePointerId);
            final float x = ev.getX(pointerIndex);
            final float dx = x - mLastMotionX;
            final float xDiff = Math.abs(dx);
            final float y = ev.getY(pointerIndex);
            final float yDiff = Math.abs(y - mInitialMotionY);

            if (dx != 0.0f && canScroll(this, false, (int) dx, (int) x, (int) y)) {
               mLastMotionX = x;
               mLastMotionY = y;
               mIsUnableToDrag = true;
               return false;
            }

            if (xDiff > mTouchSlop && xDiff * 0.5f > yDiff) {
               mIsBeingDragged = true;
               requestParentDisallowInterceptTouchEvent(true);
               mLastMotionX = dx > 0.0f
                     ? mInitialMotionX + mTouchSlop
                     : mInitialMotionX - mTouchSlop;
               mLastMotionY = y;
            } else if (yDiff > mTouchSlop) {
               mIsUnableToDrag = true;
            }

            break;
         }

         case MotionEvent.ACTION_DOWN: {
            mLastMotionX = mInitialMotionX = ev.getX();
            mLastMotionY = mInitialMotionY = ev.getY();
            mActivePointerId = ev.getPointerId(0);
            mIsUnableToDrag = false;
            mIsScrollStarted = true;
            mIsBeingDragged = false;
            break;
         }

         case MotionEvent.ACTION_POINTER_UP: {
            onSecondaryPointerUp(ev);
            break;
         }
      }

      return mIsBeingDragged;
   }

   @Override
   public final boolean onTouchEvent(final MotionEvent event) {
      final int action = event.getAction();

      if (action == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
         return false;
      }

      switch (action & MotionEvent.ACTION_MASK) {
         case MotionEvent.ACTION_DOWN: {
            mLastMotionX = mInitialMotionX = event.getX();
            mLastMotionY = mInitialMotionY = event.getY();
            mActivePointerId = event.getPointerId(0);
            break;
         }

         case MotionEvent.ACTION_MOVE: {
            if (!mIsBeingDragged) {
               final int pointerIndex = event.findPointerIndex(mActivePointerId);
               if (pointerIndex == -1) {
                  resetTouch();
                  break;
               }

               final float x = event.getX(pointerIndex);
               final float dx = Math.abs(x - mLastMotionX);
               final float y = event.getY(pointerIndex);
               final float dy = Math.abs(y - mLastMotionY);

               if (dx > mTouchSlop && dx > dy) {
                  mIsBeingDragged = true;
                  requestParentDisallowInterceptTouchEvent(true);
                  mLastMotionX = x - mInitialMotionX > 0.0f
                        ? mInitialMotionX + mTouchSlop
                        : mInitialMotionX - mTouchSlop;
                  mLastMotionY = y;
               }
            }

            if (mIsBeingDragged) {
               final int activePointerIndex = event.findPointerIndex(mActivePointerId);
               final float x = event.getX(activePointerIndex);
               final float dx = mLastMotionX - x;
               mLastMotionX = x;
               performDrag(x, dx);
            }

            break;
         }

         case MotionEvent.ACTION_UP: {
            if (mIsBeingDragged) {
            }
            break;
         }

         case MotionEvent.ACTION_CANCEL: {
            if (mIsBeingDragged) {
            }
            break;
         }

         case MotionEvent.ACTION_POINTER_DOWN: {
            final int index = event.getActionIndex();
            mLastMotionX = event.getX(index);
            mActivePointerId = event.getPointerId(index);
            break;
         }

         case MotionEvent.ACTION_POINTER_UP: {
            onSecondaryPointerUp(event);
            mLastMotionX = event.getX(event.findPointerIndex(mActivePointerId));
            break;
         }
      }

      return true;
   }

   private void performDrag(final float x, final float dx) {
      float scrollOffset = mScrollOffset - dx;

      final float columnMargin = (float) mColumnMargin;
      final float columnWidth = (float) mColumnWidth;
      final float columnDistance = columnWidth + columnMargin * 2.0f;

      if (scrollOffset < -(columnWidth + columnMargin)) {
         final ColumnLayoutAdapter adapter = mAdapter;

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
      applyTranslationX();
   }

   private void resetTouch() {
      mActivePointerId = INVALID_POINTER;
      mIsBeingDragged = false;
      mIsUnableToDrag = false;
   }

   private void onSecondaryPointerUp(final MotionEvent ev) {
      final int pointerIndex = ev.getActionIndex();
      final int pointerId = ev.getPointerId(pointerIndex);
      if (pointerId == mActivePointerId) {
         final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
         mLastMotionX = ev.getX(newPointerIndex);
         mActivePointerId = ev.getPointerId(newPointerIndex);
      }
   }

   private void requestParentDisallowInterceptTouchEvent(final boolean disallowIntercept) {
      final ViewParent parent = getParent();
      if (parent != null) {
         parent.requestDisallowInterceptTouchEvent(disallowIntercept);
      }
   }

   /**
    * vの子Viewにdxを入力した際にスクロール可能かどうかを判定します
    *
    * @param v      水平方向のスクロールが可能かどうかの判定対象のView
    * @param checkV スクロール可能かどうかの判定対象にv自体を含める(true)か、
    *               vの子Viewだけか(false)
    * @param dx     スクロール量 (px)
    * @param x      タップ位置のx座標
    * @param y      タップ位置のy座標
    * @return vの子Viewにdxを入力した際にスクロール可能な場合true
    */
   protected final boolean canScroll(final View v,
                                     final boolean checkV,
                                     final int dx,
                                     final int x,
                                     final int y)
   {
      if (v instanceof ViewGroup) {
         final ViewGroup group = (ViewGroup) v;
         final int scrollX = v.getScrollX();
         final int scrollY = v.getScrollY();
         final int count = group.getChildCount();

         // 逆走査 - 最上段のViewから優先してスクロールする
         for (int i = count - 1; i >= 0; i--) {
            final View child = group.getChildAt(i);

            if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight()  &&
                y + scrollY >= child.getTop()  && y + scrollY < child.getBottom() &&
                canScroll(child, true, dx,
                          x + scrollX - child.getLeft(),
                          y + scrollY - child.getTop()))
            {
               return true;
            }
         }
      }

      return checkV && v.canScrollHorizontally(-dx);
   }

   private void applyTranslationX() {
      final ColumnLayoutAdapter adapter = mAdapter;
      if (adapter == null) { return; }

      final int position = mPosition;
      final int columnDistance = mColumnWidth + mColumnMargin * 2;
      final float scrollOffset = mScrollOffset;

      final long positionRange = getVisiblePositionRange();
      final int leftmostPosition  = (int) (positionRange >> 32);
      final int rightmostPosition = (int)  positionRange;

      if (positionRange != mVisiblePositionRange) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "visiblePositionRange: " + leftmostPosition + " - " + rightmostPosition);
         }

         addNewVisibleView(adapter, mVisiblePositionRange, positionRange);
         mVisiblePositionRange = positionRange;
      }

      for (int p = leftmostPosition; p <= rightmostPosition; p++) {
         final VComponentInterface<?> component = adapter.getVComponentAt(p);
         final View view = component.getComponentView();

         view.setTranslationX((float) ((p - position) * columnDistance) + scrollOffset);
      }
   }

   private void addNewVisibleView(final ColumnLayoutAdapter adapter,
                                  final long oldVisiblePositionRange,
                                  final long newVisiblePositionRange)
   {
      final int oldLeftmostPosition  = (int) (oldVisiblePositionRange >> 32);
      final int oldRightmostPosition = (int)  oldVisiblePositionRange;
      final int newLeftmostPosition  = (int) (newVisiblePositionRange >> 32);
      final int newRightmostPosition = (int)  newVisiblePositionRange;

      if (newLeftmostPosition > oldLeftmostPosition) {
         removeColumnViewInRange(adapter, oldLeftmostPosition, newLeftmostPosition - 1);
      } else if (newLeftmostPosition < oldLeftmostPosition) {
         addColumnViewInRange(adapter, newLeftmostPosition, oldLeftmostPosition - 1);
      }

      if (newRightmostPosition > oldRightmostPosition) {
         addColumnViewInRange(adapter, oldRightmostPosition + 1, newRightmostPosition);
      } else if (newRightmostPosition < oldRightmostPosition) {
         removeColumnViewInRange(adapter, newRightmostPosition + 1, oldRightmostPosition);
      }
   }

   private void addColumnViewInRange(final ColumnLayoutAdapter adapter,
                                     final int startPosition, final int lastPosition)
   {
      final int columnWidth = mColumnWidth;

      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "adding the Column View at " + position);
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View view = component.getComponentView();

         final ViewGroup.LayoutParams p = view.getLayoutParams();

         if (p instanceof LayoutParams) {
            final LayoutParams lParams = (LayoutParams) p;
            lParams.width = columnWidth;
            lParams.height = LayoutParams.MATCH_PARENT;
         } else {
            final LayoutParams lParams = new LayoutParams(columnWidth, LayoutParams.MATCH_PARENT);
            view.setLayoutParams(lParams);
         }

         addView(view);
      }
   }

   private void removeColumnViewInRange(final ColumnLayoutAdapter adapter,
                                        final int startPosition, final int lastPosition)
   {
      for (int position = startPosition; position <= lastPosition; position++) {
         if (BuildConfig.DEBUG) {
            Log.i(TAG, "removing the Column View at " + position);
         }

         final VComponentInterface<?> component = adapter.getVComponentAt(position);
         final View view = component.getComponentView();
         removeView(view);
      }
   }

   private void relayout() {
      removeAllViews();

      final int layoutWidth = getWidth() - mColumnMargin * 2;
      mColumnWidth = layoutWidth / mVisibleColumnCount - mColumnMargin * 2;

      final ColumnLayoutAdapter adapter = mAdapter;
      if (adapter == null) { return; }

      final long positionRange = getVisiblePositionRange();
      final int leftmostPosition  = (int) (positionRange >> 32);
      final int rightmostPosition = (int)  positionRange;

      addColumnViewInRange(adapter, leftmostPosition, rightmostPosition);

      mVisiblePositionRange = positionRange;

      applyTranslationX();
   }

   /**
    * @return このView内で左端に表示されているカラムのpositionを上位32ビット、
    *         右端に表示されているカラムのpositionを下位32ビットとして
    *         組み合わせた64ビットの数値。
    *         Adapterがセットされていない、もしくはセットされているが
    *         {@link ColumnLayoutAdapter#getItemCount()}が0を返す場合は-1。
    */
   private long getVisiblePositionRange() {
      final ColumnLayoutAdapter adapter = mAdapter;
      if (adapter == null) { return -1L; }

      final int itemCount = mAdapter.getItemCount();
      if (itemCount <= 0) { return -1L; }

      final int columnMargin = mColumnMargin;
      final int columnDistance = mColumnWidth + columnMargin * 2;
      final float scrollOffset = mScrollOffset;

      final int leftmostPosition;
      final int rightmostPosition;

      if (scrollOffset <= (float) columnMargin) {
         leftmostPosition = mPosition;
      } else {
         leftmostPosition = mPosition - 1;
      }

      if (scrollOffset + (float) (-columnMargin + columnDistance * mVisibleColumnCount)
            >= (float) getWidth())
      {
         rightmostPosition = mPosition + mVisibleColumnCount - 1;
      } else {
         rightmostPosition = mPosition + mVisibleColumnCount;
      }

      final int higher = MathUtils.clamp(leftmostPosition,  0, itemCount - 1);
      final int lower  = MathUtils.clamp(rightmostPosition, 0, itemCount - 1);

      return (long) higher << 32 | (long) lower;
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
