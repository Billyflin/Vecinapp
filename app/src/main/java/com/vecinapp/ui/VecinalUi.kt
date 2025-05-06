package com.vecinapp.ui

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal object Dim {
    private fun k(senior: Boolean) = if (senior) 1.25f else 1f
    fun gap(s: Boolean) = (20 * k(s)).dp
    fun cardH(s: Boolean) = (140 * k(s)).dp
    fun icon(s: Boolean) = (48 * k(s)).dp
    fun text(s: Boolean) = (18 * k(s)).sp
}
