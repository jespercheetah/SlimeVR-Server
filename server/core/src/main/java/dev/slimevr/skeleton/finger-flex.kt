package dev.slimevr.skeleton

import com.jme3.math.FastMath
import dev.slimevr.tracker.FlexValue
import solarxr_protocol.datatypes.BodyPart

// Angle of a body part flex, given in radians
typealias FlexAngle = Float

fun flexToAngle(bodyPart: BodyPart, normalizedFlex: FlexValue): FlexAngle? {
    return when (bodyPart) {
        BodyPart.LEFT_INDEX_PROXIMAL -> normalizedFlex * FastMath.HALF_PI
        BodyPart.RIGHT_INDEX_PROXIMAL -> normalizedFlex * FastMath.HALF_PI
        else -> null
    }
}