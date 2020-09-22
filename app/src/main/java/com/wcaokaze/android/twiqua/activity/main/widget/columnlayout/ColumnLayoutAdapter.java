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

import vue.VComponentInterface;

public abstract class ColumnLayoutAdapter {
   protected abstract int getItemCount();
   protected abstract VComponentInterface<?> getVComponentAt(final int position);

   /**
    * ユーザーの操作によってカラムが並び替えられたときに呼び出されます。
    * Adapterの実装クラスではoldPositionにあったカラムを
    * newPositionに移動する必要があります。
    */
   protected abstract void onRearranged(final int oldPosition, final int newPosition);
}
