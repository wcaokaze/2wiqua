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

/* package */ final class VelocityTracker {
   private long mLastTime = System.currentTimeMillis();

   private float mAcceleration;
   private float mVelocity = 0.0f;
   private float mPosition = 0.0f;

   public VelocityTracker() {
      this(0.001f);
   }

   public VelocityTracker(final float acceleration) {
      mAcceleration = acceleration;
   }

   /**
    * 現在の加速度(px/ms²)を返します。
    *
    * このインスタンスの加速度が{@link #setAcceleration(float)}以外から
    * 変更されることはありません。
    */
   public final float getAcceleration() {
      calc();
      return mAcceleration;
   }

   /**
    * 加速度(px/ms²)をセットします。
    *
    * このインスタンスの加速度がこのメソッド以外から変更されることはありません。
    */
   public final void setAcceleration(final float acceleration) {
      calc();
      mAcceleration = acceleration;
   }

   /**
    * 現在の速さ(px/ms)を返します。
    *
    * 速さはこのインスタンスにセットされている{@link #getAcceleration() 加速度}と
    * このメソッドを呼び出した時刻に従って変動します。
    */
   public final float getVelocity() {
      calc();
      return mVelocity;
   }

   /**
    * 速さ(px/ms)をセットします。
    */
   public final void setVelocity(final float velocity) {
      calc();
      mVelocity = velocity;
   }

   /**
    * 直前にこのインスタンスにセットされていた位置から新しい位置までの距離、
    * および時刻から速さを算出し、セットします。
    *
    * この呼び出しによって{@link #setPosition(float) 位置}も同時にセットされます。
    */
   public final void setVelocityByCurrentPosition(final float position) {
      final float dx = position - mPosition;
      final long dt = System.currentTimeMillis() - mLastTime;

      final float oldVelocity = mVelocity;
      final float newVelocity = dx / (float) dt;

      if (newVelocity != 0.0f && !Float.isInfinite(newVelocity)) {
         if (oldVelocity == 0.0f) {
            mVelocity = newVelocity;
         } else {
            mVelocity = (oldVelocity + newVelocity) / 2.0f;
         }
      }

      mPosition = position;
   }

   /**
    * 現在の位置を返します。
    *
    * このメソッドを呼び出す時刻、このインスタンスにセットされている
    * {@link #getAcceleration() 加速度}、{@link #getVelocity 速さ}に従って
    * この返り値は変動します。
    */
   public final float getPosition() {
      calc();
      return mPosition;
   }

   /**
    * 現在の位置をセットします。
    *
    * {@link #setVelocity(float) 速さ}、{@link #setAcceleration(float) 加速度}
    * には影響しません。
    */
   public final void setPosition(final float position) {
      calc();
      mPosition = position;
   }

   private void calc() {
      final long time = System.currentTimeMillis();
      final float d = (float) (time - mLastTime);
      mLastTime = time;

      mPosition += mVelocity * d + mAcceleration * d * d / 2.0f;
      mVelocity += mAcceleration * d;
   }
}
