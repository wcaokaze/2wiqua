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

import com.wcaokaze.android.twiqua.anim.AnimationFrameHandler;

public final class NormalScrollGestureDetector extends HorizontalColumnLayoutGestureDetector {
   private static final float ACCELERATION = -0.001f;

   private final VelocityTracker mVelocityTracker = new VelocityTracker();

   private final Settler mSettler = new Settler();
   private final class Settler implements AnimationFrameHandler.Callback {
      private float mLastPosition = 0.0f;
      private long mSettledTime = 0L;
      private float mSettledPosition = 0.0f;

      @Override
      public void onFrame(final long timeMillis) {
         final float position = mVelocityTracker.getPosition();
         final float dx = mLastPosition - position;
         mLastPosition = position;

         final ColumnLayout columnLayout = layoutManager.getColumnLayout();
         if (columnLayout != null) {
            layoutManager.performDrag(columnLayout, dx);
         }

         if (System.currentTimeMillis() > mSettledTime) {
            if (columnLayout != null) {
               layoutManager.performDrag(columnLayout, mSettledPosition - mLastPosition);
            }
            stopSettling();
         }
      }
   }

   public NormalScrollGestureDetector(final HorizontalColumnLayoutManager layoutManager,
                                      final Context context)
   {
      super(layoutManager, context);
   }

   @Override
   protected void onTouchDown(final float x, final float y) {
      stopSettling();
      mVelocityTracker.setPosition(x);
   }

   @Override
   protected void onStartDragging(final float x, final float y) {
      mVelocityTracker.setPosition(x);
   }

   @Override
   protected void onDrag(final float x, final float y) {
      mVelocityTracker.setVelocityByCurrentPosition(x);
   }

   @Override
   protected void onReleaseDragging() {
      startSettling();
   }

   private void startSettling() {
      final float sign = Math.signum(mVelocityTracker.getVelocity());
      mVelocityTracker.setAcceleration(sign * ACCELERATION);

      final float columnDistance = (float) layoutManager.getColumnDistance();
      final float position = layoutManager.getScrollPosition();
      mVelocityTracker.setPosition(position);

      final float estimatedPosition = mVelocityTracker.estimateSettledPosition();

      final int currentIdx   = (int) (position          / columnDistance);
      final int estimatedIdx = (int) (estimatedPosition / columnDistance);

      final float targetPosition;

      if (estimatedIdx < currentIdx) {
         targetPosition = (float) (currentIdx - 1) * columnDistance;
      } else if (estimatedIdx > currentIdx) {
         targetPosition = (float)  currentIdx      * columnDistance;
      } else {
         targetPosition =
               columnDistance * (float) Math.round(estimatedPosition / columnDistance);
      }

      mVelocityTracker.setAccelerationBySettledPosition(targetPosition);

      if (Math.abs(targetPosition - position) < columnDistance
            && (
                  Math.abs(mVelocityTracker.estimateSettledDuration()) > 2500L ||
                  Math.abs(mVelocityTracker.estimateSettledPosition() - targetPosition) > columnDistance * 0.01f
            )
      ) {
         mVelocityTracker.setVelocity((targetPosition - position) / 500.0f);
         mVelocityTracker.setAccelerationBySettledPosition(targetPosition);
      }

      mSettler.mLastPosition = position;
      mSettler.mSettledTime = mVelocityTracker.estimateSettledTime();
      mSettler.mSettledPosition = targetPosition;
      AnimationFrameHandler.INSTANCE.addCallback(mSettler);
   }

   private void stopSettling() {
      AnimationFrameHandler.INSTANCE.removeCallback(mSettler);
      mVelocityTracker.setAcceleration(0.0f);
      mVelocityTracker.setVelocity(0.0f);
   }
}
