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
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewParent;

public final class VerticalColumnLayoutGestureDetector
      extends ColumnLayoutGestureDetector<VerticalColumnLayoutManager>
{
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
      return true;
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
               final float dx = Math.abs(x - mLastMotionX);
               final float y = event.getY(pointerIndex);
               final float dy = Math.abs(y - mLastMotionY);

               if (dy > mTouchSlop && dy > dx) {
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
               layoutManager.performDrag(view, y, dy);
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
            mLastMotionY = event.getY(index);
            mActivePointerId = event.getPointerId(index);
            break;
         }

         case MotionEvent.ACTION_POINTER_UP: {
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
      mIsUnableToDrag = false;
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
}
