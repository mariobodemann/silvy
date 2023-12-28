package jetzt.jfdi.silvy

import android.graphics.Rect
import jetzt.jfdi.silvy.Particle.*
import kotlin.math.atan2
import kotlin.math.roundToLong

val yearPictureWidth = 15
val yearPicture = listOf(
    1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 1, 0, 1,
    0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 1, 0, 1, 0, 1,
    1, 1, 1, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 1, 1,
    1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1,
    1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0, 0, 0, 1,
)

val flightTime = 1000L
val flightTimeStartDerivation = 250L

val starMinLifeTime = 750L
val starMaxLifeTime = 1000L

val poofLifeTime = 200L

sealed class Particle(
    open val initial: V,
    open val target: V,
    open val createdAt: Long,
    open val lifetimeMillis: Long,
    open val position: V,
) {
    open fun tweenX(progress: Double): Double = progress
    open fun tweenY(progress: Double): Double = progress

    data class Rocket(
        override val initial: V,
        override val target: V,
        override val createdAt: Long,
        override val lifetimeMillis: Long,
        override val position: V = initial,
        val rotationInDegrees: Float,
    ) : Particle(
        initial = initial,
        target = target,
        createdAt = createdAt,
        lifetimeMillis = lifetimeMillis,
        position = position,
    ) {
        override fun tweenY(progress: Double): Double =
            0.07814 * progress * progress * progress - 2.05971 * progress * progress + 2.98091 * progress - 0.00138
    }

    data class Poof(
        override val initial: V,
        override val target: V,
        override val createdAt: Long,
        override val lifetimeMillis: Long,
        override val position: V = initial,
    ) : Particle(
        initial = initial,
        target = target,
        createdAt = createdAt,
        lifetimeMillis = lifetimeMillis,
        position = position,
    )

    data class Star(
        override val initial: V,
        override val target: V,
        override val createdAt: Long,
        override val lifetimeMillis: Long,
        override val position: V = initial,
    ) : Particle(
        initial = initial,
        target = target,
        createdAt = createdAt,
        lifetimeMillis = lifetimeMillis,
        position = position,
    )
}

fun generateParticles(from: Rect, to: Rect): List<Particle> {
    val cellWidth = to.width() / yearPictureWidth
    val cellHeight = to.height() / (yearPicture.size / yearPictureWidth)
    val time = System.currentTimeMillis()

    return yearPicture.mapIndexedNotNull { index, bitSet ->
        if (bitSet == 0) {
            null
        } else {
            val x = index % yearPictureWidth
            val y = index / yearPictureWidth

            val createdDelay = (Math.random() * flightTimeStartDerivation).roundToLong()
            Rocket(
                initial = from.randomPoint(),
                target = V(to.left + x * cellWidth, to.top + y * cellHeight),
                createdAt = time + createdDelay,
                lifetimeMillis = flightTime - createdDelay,
                rotationInDegrees = 0f,
            )
        }
    }.toList<Particle>()
}

fun Particle.update(time: Long): Particle? {
    val timeAlive = time - createdAt
    val progress = timeAlive.toDouble() / lifetimeMillis.toDouble()

    val direction = target - initial
    val oldPosition = this.position
    val newPosition =
        V(
            x = initial.x + (direction.x * tweenX(progress)),
            y = initial.y + (direction.y * tweenY(progress))
        )

    return when (this) {
        is Rocket ->
            if (isDead(time)) {
                Star(
                    initial = target,
                    position = target,
                    target = target,
                    createdAt = time,
                    lifetimeMillis = starMinLifeTime + (Math.random() * (starMaxLifeTime - starMinLifeTime)).roundToLong(),
                )
            } else if (isAlive(time)) {
                val rotationInDegrees = atan2(
                    newPosition.y - oldPosition.y,
                    newPosition.x - oldPosition.x
                ).toDegrees() + 90

                copy(
                    position = newPosition,
                    rotationInDegrees = rotationInDegrees
                )
            } else {
                this
            }


        is Star ->
            if (isDead(time)) {
                Poof(
                    initial = target,
                    position = target,
                    target = target,
                    createdAt = time,
                    lifetimeMillis = poofLifeTime,
                )
            } else if (isAlive(time)) {
                copy(position = newPosition)
            } else {
                this
            }

        is Poof ->
            if (isDead(time)) {
                null
            } else {
                this
            }
    }
}


fun List<Particle>.update(): List<Particle> {
    val time = System.currentTimeMillis()

    return mapNotNull {
        it.update(time)
    }
}

fun Particle.isAlive(time: Long): Boolean = time >= createdAt && time < createdAt + lifetimeMillis
fun Particle.isDead(time: Long): Boolean = time >= createdAt + lifetimeMillis

private fun Rect.center(): V = V(left + width() / 2.0f, top + height() / 2.0f)
private fun Rect.randomPoint(): V =
    V(left + Math.random() * width(), top + Math.random() * height())

private fun Float.toDegrees() = Math.toDegrees(this.toDouble()).toFloat()