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
import android.view.ViewConfiguration;
import android.view.ViewParent;

public final class VerticalColumnLayoutGestureDetector
      extends ColumnLayoutGestureDetector<VerticalColumnLayoutManager>
{
   private static final int INVALID_POINTER = -1;

   private int mActivePointerId = INVALID_POINTER;

   private boolean mIsBeingDragged;
   private boolean mIsScrollStarted;

   private final float mTouchSlop;

   private float mLastMotionX;
   private float mLastMotionY;
   private float mInitialMotionX;
   private float mInitialMotionY;

   private boolean mIsRearrangingMode = false;

   private LongClickDetector mLongClickDetector = new LongClickDetector();

   public VerticalColumnLayoutGestureDetector
         (final VerticalColumnLayoutManager layoutManager, final Context context)
   {
      super(layoutManager);

      final ViewConfiguration configuration = ViewConfiguration.get(context);
      mTouchSlop = (float) configuration.getScaledPagingTouchSlop();
   }

   @Override
   protected final boolean onInterceptTouchEvent(final ColumnLayout view,
                                                 final MotionEvent ev)
   {
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
            final float y = ev.getY(pointerIndex);
            final float dy = y - mLastMotionY;
            final float yDiff = Math.abs(dy);
            final float x = ev.getX(pointerIndex);

            if (yDiff > mTouchSlop) {
               mLongClickDetector.cancel();
               mIsBeingDragged = true;
               requestParentDisallowInterceptTouchEvent(view, true);
               mLastMotionY = dy > 0.0f
                     ? mInitialMotionY + mTouchSlop
                     : mInitialMotionY - mTouchSlop;
               mLastMotionX = x;
            }

            break;
         }

         case MotionEvent.ACTION_DOWN: {
            mLastMotionX = mInitialMotionX = ev.getX();
            mLastMotionY = mInitialMotionY = ev.getY();
            mActivePointerId = ev.getPointerId(0);
            mIsScrollStarted = true;
            mIsBeingDragged = false;

            mLongClickDetector.cancel();
            mLongClickDetector = new LongClickDetector();
            view.postDelayed(mLongClickDetector,
                  (long) ViewConfiguration.getLongPressTimeout());

            break;
         }

         case MotionEvent.ACTION_POINTER_UP: {
            mLongClickDetector.cancel();
            onSecondaryPointerUp(ev);
            break;
         }
      }

      return mIsBeingDragged;
   }

   @Override
   protected final boolean onTouchEvent(final ColumnLayout view,
                                        final MotionEvent event)
   {
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
               final float y = event.getY(pointerIndex);
               final float dy = Math.abs(y - mLastMotionY);

               if (dy > mTouchSlop) {
                  mLongClickDetector.cancel();
                  mIsBeingDragged = true;
                  requestParentDisallowInterceptTouchEvent(view, true);
                  mLastMotionX = x;
                  mLastMotionY = y - mInitialMotionY > 0.0f
                        ? mInitialMotionY + mTouchSlop
                        : mInitialMotionY - mTouchSlop;
               }
            }

            if (mIsBeingDragged) {
               final int activePointerIndex = event.findPointerIndex(mActivePointerId);
               final float y = event.getY(activePointerIndex);
               final float dy = mLastMotionY - y;
               mLastMotionY = y;

               if (mIsRearrangingMode) {
               } else {
                  layoutManager.performDrag(view, y, dy);
               }
            }

            break;
         }

         case MotionEvent.ACTION_UP: {
            mLongClickDetector.cancel();
            if (mIsBeingDragged) {
            }
            break;
         }

         case MotionEvent.ACTION_CANCEL: {
            mLongClickDetector.cancel();
            if (mIsBeingDragged) {
            }
            break;
         }

         case MotionEvent.ACTION_POINTER_DOWN: {
            final int index = event.getActionIndex();
            mLastMotionY = event.getY(index);
            mActivePointerId = event.getPointerId(index);
            break;
         }

         case MotionEvent.ACTION_POINTER_UP: {
            mLongClickDetector.cancel();
            onSecondaryPointerUp(event);
            mLastMotionY = event.getY(event.findPointerIndex(mActivePointerId));
            break;
         }
      }

      return true;
   }

   private void resetTouch() {
      mActivePointerId = INVALID_POINTER;
      mIsBeingDragged = false;
      mIsRearrangingMode = false;
   }

   private void onSecondaryPointerUp(final MotionEvent ev) {
      final int pointerIndex = ev.getActionIndex();
      final int pointerId = ev.getPointerId(pointerIndex);
      if (pointerId == mActivePointerId) {
         final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
         mLastMotionY = ev.getX(newPointerIndex);
         mActivePointerId = ev.getPointerId(newPointerIndex);
      }
   }

   private void requestParentDisallowInterceptTouchEvent
         (final ColumnLayout columnLayout, final boolean disallowIntercept)
   {
      final ViewParent parent = columnLayout.getParent();
      if (parent != null) {
         parent.requestDisallowInterceptTouchEvent(disallowIntercept);
      }
   }

   private final class LongClickDetector implements Runnable {
      private boolean mIsCancelled = false;
      private boolean mHasDetected = false;

      /* package */ final void cancel() {
         if (mHasDetected) { return; }

         mIsCancelled = true;
         mIsRearrangingMode = false;
      }

      @Override
      public final void run() {
         if (mIsCancelled) { return; }

         mHasDetected = true;
         mIsRearrangingMode = true;

         final ColumnLayout columnLayout = layoutManager.getColumnLayout();
         if (columnLayout == null) { return; }

         layoutManager.startRearrangingMode(columnLayout, mLastMotionY);
      }
   }
}
