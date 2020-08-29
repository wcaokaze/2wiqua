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

@file:Suppress("UNUSED")
package com.wcaokaze.android.twiqua.activity.main.widget

import android.content.Context
import koshian.*
import kotlin.contracts.*

object ColumnLayoutConstructor : KoshianViewConstructor<ColumnLayout> {
   override fun instantiate(context: Context?) = ColumnLayout(context)
}

/**
 * creates a new ColumnLayout and adds it into this ViewGroup.
 */
@ExperimentalContracts
@Suppress("FunctionName")
inline fun <L> CreatorParent<L>.ColumnLayout(
   creatorAction: ViewCreator<ColumnLayout, L>.() -> Unit
): ColumnLayout {
   contract { callsInPlace(creatorAction, InvocationKind.EXACTLY_ONCE) }
   return create(ColumnLayoutConstructor, creatorAction)
}

/**
 * creates a new ColumnLayout with name, and adds it into this ViewGroup.
 *
 * The name can be referenced in [applyKoshian]
 */
@ExperimentalContracts
@Suppress("FunctionName")
inline fun <L> CreatorParent<L>.ColumnLayout(
   name: String,
   creatorAction: ViewCreator<ColumnLayout, L>.() -> Unit
): ColumnLayout {
   contract { callsInPlace(creatorAction, InvocationKind.EXACTLY_ONCE) }
   return create(name, ColumnLayoutConstructor, creatorAction)
}

/**
 * If the next View is a ColumnLayout, applies Koshian to it.
 *
 * Otherwise, creates a new ColumnLayout and inserts it to the current position.
 *
 * @see applyKoshian
 */
@Suppress("FunctionName")
inline fun <L, S : KoshianStyle>
      ApplierParent<L, S>.ColumnLayout(
         applierAction: ViewApplier<ColumnLayout, L, S>.() -> Unit
      )
{
   apply(ColumnLayoutConstructor, applierAction)
}

/**
 * If the next View is a ColumnLayout, applies Koshian to it.
 *
 * Otherwise, creates a new ColumnLayout and inserts it to the current position.
 *
 * @see applyKoshian
 */
@Suppress("FunctionName")
inline fun <L, S : KoshianStyle>
      ApplierParent<L, S>.ColumnLayout(
         styleElement: KoshianStyle.StyleElement<ColumnLayout>,
         applierAction: ViewApplier<ColumnLayout, L, S>.() -> Unit
      )
{
   apply(ColumnLayoutConstructor, styleElement, applierAction)
}

/**
 * Applies Koshian to all ColumnLayouts that are named the specified in this ViewGroup.
 * If there are no ColumnLayouts named the specified, do nothing.
 *
 * @see applyKoshian
 */
@Suppress("FunctionName")
inline fun <L, S : KoshianStyle>
      ApplierParent<L, S>.ColumnLayout(
         name: String,
         applierAction: ViewApplier<ColumnLayout, L, S>.() -> Unit
      )
{
   apply(name, applierAction)
}

/**
 * Applies Koshian to all ColumnLayouts that are named the specified in this ViewGroup.
 * If there are no ColumnLayouts named the specified, do nothing.
 *
 * @see applyKoshian
 */
@Suppress("FunctionName")
inline fun <L, S : KoshianStyle>
      ApplierParent<L, S>.ColumnLayout(
         name: String,
         styleElement: KoshianStyle.StyleElement<ColumnLayout>,
         applierAction: ViewApplier<ColumnLayout, L, S>.() -> Unit
      )
{
   apply(name, styleElement, applierAction)
}

/**
 * registers a style applier function into this [KoshianStyle].
 *
 * Styles can be applied via [applyKoshian]
 */
@Suppress("FunctionName")
inline fun KoshianStyle.ColumnLayout(
   crossinline styleAction: ViewStyle<ColumnLayout>.() -> Unit
): KoshianStyle.StyleElement<ColumnLayout> {
   return createStyleElement(styleAction)
}
