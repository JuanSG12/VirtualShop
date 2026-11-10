package com.tramas.app.ui.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.material.color.MaterialColors
import com.tramas.app.R

/** Gráfico de barras propio (Canvas): ventas por día. Usa el color primario del tema del rol. */
class BarChartView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var values: List<Pair<String, Float>> = emptyList()
        set(v) { field = v; invalidate() }

    private val bar = Paint(Paint.ANTI_ALIAS_FLAG)
    private val peak = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.ochre) }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ContextCompat.getColor(context, R.color.ink_muted); textAlign = Paint.Align.CENTER; textSize = context.dp(11).toFloat() }
    private val grid = Paint().apply { color = ContextCompat.getColor(context, R.color.line); strokeWidth = 2f }

    override fun onDraw(canvas: Canvas) {
        if (values.isEmpty()) return
        bar.color = MaterialColors.getColor(this, androidx.appcompat.R.attr.colorPrimary)
        val bottom = height - context.dp(22).toFloat()
        val slot = width.toFloat() / values.size
        val barW = slot * 0.5f
        val max = values.maxOf { it.second }
        canvas.drawLine(0f, bottom, width.toFloat(), bottom, grid)
        values.forEachIndexed { i, (name, v) ->
            val h = (bottom - context.dp(8)) * (v / max)
            val left = i * slot + (slot - barW) / 2
            canvas.drawRoundRect(RectF(left, bottom - h, left + barW, bottom), 12f, 12f, if (v == max) peak else bar)
            canvas.drawText(name, left + barW / 2, height - context.dp(6).toFloat(), label)
        }
    }
}
