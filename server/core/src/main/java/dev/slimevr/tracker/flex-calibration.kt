package dev.slimevr.tracker

typealias RawFlex = Float
typealias FlexValue = Float

data class FlexCalibration(
    val rawFlex: RawFlex? = null,
    val rawFlexAtFullCalibration: RawFlex? = null,
    val maxObserved: RawFlex? = null,
    val minObserved: RawFlex? = null,
    val lowMeansStraight: Boolean? = null,
    // 0.0 = Fully straight
    // 1.0 = Fully bent
    val normalizedFlex: FlexValue? = null,
)
