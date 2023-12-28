package jetzt.jfdi.silvy

import kotlin.math.sqrt


data class V(
    val x: Float,
    val y: Float,
)

fun V(x: Number, y: Number) = V(x.toFloat(), y.toFloat())
fun V(x: Int, y: Int) = V(x.toFloat(), y.toFloat())

fun V.normalized(): V = this / length()

fun V.length(): Float = sqrt(x * x + y * y)

operator fun V.plus(other: V) = V(x + other.x, y + other.y)
operator fun V.minus(other: V) = V(x - other.x, y - other.y)
operator fun V.times(factor: Float) = V(x * factor, y * factor)
operator fun V.div(factor: Float) = V(x / factor, y / factor)
operator fun V.times(factor: Double) = V((x * factor).toFloat(), (y * factor).toFloat())

