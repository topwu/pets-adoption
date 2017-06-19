package com.topwu.petsadopt.util

import android.content.Context

object GuiUtils {

    fun dpToPx(ctx: Context, dp: Int): Float {
        val density = ctx.resources.displayMetrics.density
        return dp * density
    }

    fun pxToDp(ctx: Context, px: Int): Float {
        val density = ctx.resources.displayMetrics.density
        return px / density
    }
}