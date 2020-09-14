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

public abstract class FloatAnimator extends Animator {
   private final float mFrom;
   private final float mTo;

   public FloatAnimator(final float from,
                        final float to,
                        final long duration,
                        final TimeInterpolator interpolator)
   {
      super(duration, interpolator);

      mFrom = from;
      mTo = to;
   }

   public FloatAnimator(final float from,
                        final float to,
                        final long duration)
   {
      this(from, to, duration, new LinearInterpolator());
   }

   @Override
   public final void update(final long animationTime, final float animationTimeRate) {
      final float value = mFrom + ((mTo - mFrom) * animationTimeRate);
      update(animationTime, animationTimeRate, value);
   }

   public abstract void update(final long animationTime,
                               final float animationTimeRate,
                               final float value);
}
