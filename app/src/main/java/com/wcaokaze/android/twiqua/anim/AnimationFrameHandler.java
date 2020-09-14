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

import android.view.Choreographer;

import androidx.annotation.UiThread;

public final class AnimationFrameHandler {
   public static interface Callback {
      public void onFrame(final long timeMillis);
   }

   private static final Callback[] EMPTY_CALLBACKS = new Callback[0];
   public static final AnimationFrameHandler INSTANCE = new AnimationFrameHandler();

   private final Choreographer mChoreographer = Choreographer.getInstance();

   private Callback[] mAnimationCallbacks = EMPTY_CALLBACKS;;

   private final Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
      @Override
      public void doFrame(final long frameTimeNanos) {
         final long timeMillis = System.currentTimeMillis();

         for (final Callback callback : mAnimationCallbacks) {
            callback.onFrame(timeMillis);
         }

         if (mAnimationCallbacks.length > 0) {
            mChoreographer.postFrameCallback(this);
         }
      }
   };

   @UiThread
   public final void addCallback(final Callback callback) {
      final Callback[] oldArray = mAnimationCallbacks;
      final int newLength = oldArray.length + 1;

      final Callback[] newArray;
      if (newLength == 1) {
         newArray = new Callback[] { callback };
      } else {
         newArray = new Callback[newLength];
         System.arraycopy(oldArray, 0, newArray, 0, newLength - 1);
         newArray[newLength - 1] = callback;
      }

      mAnimationCallbacks = newArray;

      if (newArray.length == 1) {
         mChoreographer.postFrameCallback(mFrameCallback);
      }
   }

   @UiThread
   public final void removeCallback(final Callback callback) {
      final Callback[] oldArray = mAnimationCallbacks;
      final int oldLength = oldArray.length;

      if (oldLength == 0) { return; }

      if (oldLength == 1) {
         if (oldArray[0] == callback) {
            mAnimationCallbacks = EMPTY_CALLBACKS;
         }
      } else {
         int index = -1;

         for (int i = 0; i < oldLength; i++) {
            if (oldArray[i] == callback) {
               index = i;
               break;
            }
         }

         if (index < 0) { return; }

         final int newLength = oldLength - 1;
         final Callback[] newArray = new Callback[newLength];

         System.arraycopy(oldArray,         0, newArray,     0, index);
         System.arraycopy(oldArray, index + 1, newArray, index, newLength - index);

         mAnimationCallbacks = newArray;
      }
   }
}
