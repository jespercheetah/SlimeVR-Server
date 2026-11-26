package dev.slimevr.tracker

import com.jme3.math.FastMath.ZERO_TOLERANCE
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class FlexCalibrationTest {
    private fun setFlex(rawFlex: Float) = TrackerActions.SetFlex(rawFlex)
    private fun fullReset() = TrackerActions.FullReset(referenceRotation = null, resetHmdAttitude = false)
    private fun poseMountingReset() = TrackerActions.PoseMountingReset(referenceRotation = null, yawOffset = 0.0f)

    // SetFlex before calibration should set the raw flex values,
    // but not the normalized value nor calibration parameters.
    @Test
    fun `Samples before calibration`() {
        val samples = listOf(0.0f, 100.0f, 1024.0f)

        for (sample in samples) {
            // Arrange
            var state = Tracker.DEFAULT_STATE

            // Act
            state = reduce(state, setFlex(sample))

            // Assert
            assertEquals(sample, state.flexCalibration.rawFlex)
            assertNull(state.flexCalibration.rawFlexAtFullCalibration)
            assertEquals(sample, state.flexCalibration.maxObserved)
            assertEquals(sample, state.flexCalibration.minObserved)
            assertNull(state.flexCalibration.lowMeansStraight)
            assertNull(state.flexCalibration.normalizedFlex)
        }
    }

    // With a sample, a full reset should capture the straight baseline, seed the extremas and set the normalized flex to straight
    @Test
    fun `Full Reset with a sample`() {
        val samples = listOf(0.0f, 100.0f, 1024.0f)

        for (sample in samples) {
            // Arrange
            var state = Tracker.DEFAULT_STATE
            state = reduce(state, setFlex(sample))

            // Act
            state = reduce(state, fullReset())

            // Assert
            assertEquals(sample, state.flexCalibration.rawFlex)
            assertEquals(sample, state.flexCalibration.rawFlexAtFullCalibration)
            assertEquals(sample, state.flexCalibration.maxObserved)
            assertEquals(sample, state.flexCalibration.minObserved)
            assertNull(state.flexCalibration.lowMeansStraight)
            assertEquals(0.0f, state.flexCalibration.normalizedFlex)
        }
    }

    // Full reset before getting any samples should leave calibration unset
    @Test
    fun `Full Reset without samples`() {
        // Arrange
        var state = Tracker.DEFAULT_STATE

        // Act
        state = reduce(state, fullReset())

        // Assert
        assertNull(state.flexCalibration.rawFlex)
        assertNull(state.flexCalibration.rawFlexAtFullCalibration)
        assertNull(state.flexCalibration.maxObserved)
        assertNull(state.flexCalibration.minObserved)
        assertNull(state.flexCalibration.lowMeansStraight)
        assertNull(state.flexCalibration.normalizedFlex)
    }

    @Test
    fun `Mounting reset when low raw flex means straight`() {
        // Arrange
        var state = Tracker.DEFAULT_STATE
        
        // Act
        state = reduce(state, setFlex(0.0f))
        state = reduce(state, fullReset())
        state = reduce(state, setFlex(100.0f))
        state = reduce(state, poseMountingReset())
        
        // Assert
        assertEquals(true, state.flexCalibration.lowMeansStraight)
        assertEquals(1.0f, state.flexCalibration.normalizedFlex)
    }
        
    @Test
    fun `Mounting reset when high raw flex means straight`() {
        // Arrange
        var state = Tracker.DEFAULT_STATE
        
        // Act
        state = reduce(state, setFlex(100.0f))
        state = reduce(state, fullReset())
        state = reduce(state, setFlex(0.0f))
        state = reduce(state, poseMountingReset())
        
        // Assert
        assertEquals(false, state.flexCalibration.lowMeansStraight)
        assertEquals(1.0f, state.flexCalibration.normalizedFlex)
    }
    
    @Test
    fun `New samples expand the range`()
    {
        // Arrange
        var state = Tracker.DEFAULT_STATE

        // Act
        state = reduce(state, setFlex(90.0f))
        state = reduce(state, setFlex(10.0f))
        state = reduce(state, setFlex(50.0f))

        // Assert
        assertEquals(90.0f, state.flexCalibration.maxObserved)
        assertEquals(10.0f, state.flexCalibration.minObserved)
        assertEquals(50.0f, state.flexCalibration.rawFlex)
    }

    @Test
    fun `Normalized value should be half when halfway between endpoints (low = straight)`() {
        // Arrange
        var state = Tracker.DEFAULT_STATE
        state = reduce(state, setFlex(10.0f))
        state = reduce(state, fullReset())
        state = reduce(state, setFlex(90.0f))
        state = reduce(state, poseMountingReset())
        
        // Act
        state = reduce(state, setFlex(50.0f))
        
        // Assert
        assertNotNull(state.flexCalibration.normalizedFlex)
        assertEquals(0.5f, state.flexCalibration.normalizedFlex, ZERO_TOLERANCE)
    }

    @Test
    fun `Normalized value shold be half when halfway between endpoints (high = straight)`() {
        // Arrange
        var state = Tracker.DEFAULT_STATE
        state = reduce(state, setFlex(90.0f))
        state = reduce(state, fullReset())
        state = reduce(state, setFlex(10.0f))
        state = reduce(state, poseMountingReset())
        
        // Act
        state = reduce(state, setFlex(50.0f))
        
        // Assert
        assertNotNull(state.flexCalibration.normalizedFlex)
        assertEquals(0.5f, state.flexCalibration.normalizedFlex, ZERO_TOLERANCE)
    }

    // TODO: Other tests to write:
    //
    // Invalid endpoints: Missing or equal straight/bent readings leave direction and normalized flex unset.
    //
    // Range expansion: Samples beyond the straight or bent endpoint expand the range and normalize to the corresponding endpoint.
}
