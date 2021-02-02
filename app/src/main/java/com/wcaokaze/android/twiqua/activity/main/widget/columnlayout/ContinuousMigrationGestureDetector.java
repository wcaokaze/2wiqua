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
import android.util.DisplayMetrics;

import androidx.core.math.MathUtils;

import com.wcaokaze.android.twiqua.anim.AnimationFrameHandler;

public final class ContinuousMigrationGestureDetector extends HorizontalColumnLayoutGestureDetector {
   private static final long MIGRATION_DURATION = 350L;

   private float mMigrationStartSwipeWidth;

   private int mPosition = 0;

   // 移動中でない場合NaNが入るものとする
   private float mMovementStartX = Float.NaN;

   public ContinuousMigrationGestureDetector(final HorizontalColumnLayoutManager layoutManager,
                                             final Context context)
   {
      super(layoutManager, context);

      final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
      final float displayDensity = displayMetrics.density;

      mMigrationStartSwipeWidth = 32.0f * displayDensity;
   }

   public final float getMigrationStartSwipeWidth() {
      return mMigrationStartSwipeWidth;
   }

   public final void setMigrationStartSwipeWidth(final float migrationStartSwipeWidth) {
      mMigrationStartSwipeWidth = migrationStartSwipeWidth;
   }

   private final Migrator mMigrator = new Migrator();
   private boolean mIsMigratorWorking = false;

   private final class Migrator implements AnimationFrameHandler.Callback {
      private float mStartScrollPosition = 0.0f;
      private float mEndScrollPosition   = 0.0f;
      private float mLastScrollPosition  = 0.0f;

      private long mStartTime = 0L;
      private long mDuration = 0L;

      @Override
      public final void onFrame(final long timeMillis) {
         final float timeRate = (float) (timeMillis - mStartTime) / (float) mDuration;
         final float interpolatedTime = 1.0f - (1.0f - timeRate) * (1.0f - timeRate);

         final float scrollPosition =
               mStartScrollPosition
               + (mEndScrollPosition - mStartScrollPosition) * interpolatedTime;

         final float dx = mLastScrollPosition - scrollPosition;
         mLastScrollPosition = scrollPosition;

         final ColumnLayout columnLayout = layoutManager.getColumnLayout();
         if (columnLayout != null) {
            layoutManager.performDrag(columnLayout, dx);
         }

         if (timeMillis > mStartTime + mDuration) {
            removeMigratorFromAnimationFrameHandler();
         }
      }
   }

   private void addMigratorIntoAnimationFrameHandler() {
      if (mIsMigratorWorking) { return; }
      AnimationFrameHandler.INSTANCE.addCallback(mMigrator);
      mIsMigratorWorking = true;
   }

   private void removeMigratorFromAnimationFrameHandler() {
      if (!mIsMigratorWorking) { return; }
      AnimationFrameHandler.INSTANCE.removeCallback(mMigrator);
      mIsMigratorWorking = false;
   }

   @Override
   protected final void onDrag(final float x, final float y) {
      final float swipeWidth = x - mMovementStartX;

      if (Float.isNaN(mMovementStartX)) {
         mMovementStartX = x;
      } else if (swipeWidth > mMigrationStartSwipeWidth) {
         mMovementStartX = x;
         startMigrationToLeaveRight();
      } else if (-swipeWidth > mMigrationStartSwipeWidth) {
         mMovementStartX = x;
         startMigrationToLeaveLeft();
      }
   }

   @Override
   protected void onReleaseDragging() {
      mMovementStartX = Float.NaN;
   }

   // ==========================================================================

   private void startMigrationToLeaveLeft() {
      startMigration(mPosition + 1);
   }

   private void startMigrationToLeaveRight() {
      startMigration(mPosition - 1);
   }

   private void startMigration(final int position) {
      mPosition = position;

      final long currentTime = System.currentTimeMillis();
      final int columnDistance = layoutManager.getColumnDistance();

      final float start = mMigrator.mLastScrollPosition;
      final float end = (float) (position * -columnDistance);

      mMigrator.mStartScrollPosition = start;
      mMigrator.mEndScrollPosition = end;

      if (Math.abs(end - start) < (float) (columnDistance * 2 / 3)) {
         mMigrator.mDuration = MathUtils
               .clamp(currentTime - mMigrator.mStartTime, 0L, MIGRATION_DURATION);
      } else {
         mMigrator.mDuration = MIGRATION_DURATION;
      }

      mMigrator.mStartTime = currentTime;

      addMigratorIntoAnimationFrameHandler();
   }
}
