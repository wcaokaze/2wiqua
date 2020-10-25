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

public final class FreeScrollGestureDetector extends HorizontalColumnLayoutGestureDetector {
   private static final float ACCELERATION = -0.001f;

   private final VelocityTracker mVelocityTracker = new VelocityTracker();

   private final Settler mSettler = new Settler();
   private final class Settler implements AnimationFrameHandler.Callback {
      private float mLastPosition = 0.0f;

      @Override
      public void onFrame(final long timeMillis) {
         final VelocityTracker velocityTracker = mVelocityTracker;

         final float position = velocityTracker.getPosition();
         final float dy = mLastPosition - position;
         mLastPosition = position;

         final ColumnLayout columnLayout = layoutManager.getColumnLayout();
         if (columnLayout != null) {
            layoutManager.performDrag(columnLayout, dy);
         }

         if (velocityTracker.getAcceleration() > 0.0f ==
             velocityTracker.getVelocity()     > 0.0f)
         {
            stopSettling();
         }
      }
   }

   public FreeScrollGestureDetector(final HorizontalColumnLayoutManager layoutManager,
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
      AnimationFrameHandler.INSTANCE.addCallback(mSettler);

      final float sign = Math.signum(mVelocityTracker.getVelocity());
      mVelocityTracker.setAcceleration(sign * ACCELERATION);

      final float columnDistance = (float) layoutManager.getColumnDistance();
      final float position = layoutManager.getScrollPosition();
      mVelocityTracker.setPosition(position);
      mSettler.mLastPosition = position;

      final float estimatedPosition = mVelocityTracker.estimateSettledPosition();

      mVelocityTracker.setAccelerationBySettledPosition(
            columnDistance * (float) Math.round(estimatedPosition / columnDistance)
      );
   }

   private void stopSettling() {
      AnimationFrameHandler.INSTANCE.removeCallback(mSettler);
      mVelocityTracker.setAcceleration(0.0f);
      mVelocityTracker.setVelocity(0.0f);
   }
}
