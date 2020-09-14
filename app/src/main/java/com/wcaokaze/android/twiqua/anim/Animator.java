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

package com.wcaokaze.android.twiqua.anim;

import android.animation.TimeInterpolator;

/**
 * {@link android.animation.ValueAnimator ValueAnimator}は便利だし
 * 多くの機能を持っているがそれ故に無駄も多い。
 * このクラスは「フレーム毎に{@link #update(long, float)}を呼ぶ」という
 * アニメーションに必要最低限の機能のみを再実装したものである。
 * よいこのみんなはValueAnimatorを使おうね
 */
public abstract class Animator implements AnimationFrameHandler.Callback {
   private final long mStartTime;
   private final long mDuration;

   private final TimeInterpolator mInterpolator;

   public Animator(final long duration,
                   final TimeInterpolator interpolator)
   {
      mStartTime = System.currentTimeMillis();
      mDuration = duration;

      mInterpolator = interpolator;

      AnimationFrameHandler.INSTANCE.addCallback(this);
   }

   @Override
   public final void onFrame(final long timeMillis) {
      final long animationTime = timeMillis - mStartTime;

      if (animationTime < mDuration) {
         final float animationTimeRate = (float) animationTime / (float) mDuration;
         final float interpolatedTime = mInterpolator.getInterpolation(animationTimeRate);

         update(animationTime, interpolatedTime);
      } else {
         update(mDuration, 1.0f);
         AnimationFrameHandler.INSTANCE.removeCallback(this);

         onFinish();
      }
   }

   public abstract void update(final long animationTime,
                               final float animationTimeRate);

   public void onFinish() {}
}
