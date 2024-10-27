package me.tcklpl.naturaldisaster.disasters.tornado

import me.tcklpl.naturaldisaster.data.Vec2d
import me.tcklpl.naturaldisaster.data.Vec3d
import org.bukkit.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

class TornadoWindField(private val coreLocation: Location) {

    /**
     * Angular velocity (ω) in rad/s
     * Controls the rotation speed of the wind around the tornado center.
     * Typical Range: 0.5 to 5 rad/s
     *
     * Higher values make the tornado spin faster, which increases tangential velocity.
     * For larger tornadoes, a lower angular velocity may feel more realistic, while smaller tornadoes may
     * have higher values.
     */
    private val angularVelocity = 0.08

    /**
     * Radial Inflow velocity (V_in) in m/s
     * Controls how strongly objects are pulled toward the tornado center.
     * Typical Range: 20 to 70 m/s near the core, decreasing with distance
     *
     * This value should be high near the core and decay with distance.
     * The rate of decay (e.g., 0.05) controls how quickly the inflow weakens farther from the core
     */
    private val radialInflowCoreVelocity = 30.0
    private val radialInflowDecayRate = 0.09

    /**
     * Vertical Velocity (Vz)
     * Defines the upward force of the tornado, lifting objects off the ground
     * Typical Range: 10 to 30 m/s near the core, reducing with height and distance from the center
     *
     * Vertical velocity typically increases near the tornado's center and decays both with radial distance and height.
     * The decay rate (0.03) depends on how high you want the lifting force to reach
     */
    private val verticalVelocity = 5.0
    private val verticalVelocityDecayRate = 0.05

    /**
     * Calculates the distance (r) from the core of the tornado
     *
     * @param pos Object location
     */
    private fun calculateDistanceFromCore(pos: Location): Double {
        return coreLocation.distance(pos)
    }

    /**
     * Calculates the objects position relative to the core of the tornado
     *
     * @param pos Object location
     */
    private fun calculateCenterRelativeCoordinate(pos: Location): Vec3d {
        return Vec3d(
            pos.x - coreLocation.x,
            pos.y - coreLocation.y,
            pos.z - coreLocation.z
        )
    }

    /**
     * Calculate the tangential velocity around the tornado based on the angular speed and distance from the core:
     *
     *      v_θ = ω * r
     *
     * @param r Distance from the core
     */
    private fun calculateAngularVelocity(r: Double): Double {
        return angularVelocity * r
    }

    /**
     * Calculate the inflow velocity as a function of r:
     *
     *      v_in = Vmax * e^(-kr)
     *
     * @param r Distance from the core
     */
    private fun calculateInflowVelocity(r: Double): Double {
        return radialInflowCoreVelocity * exp(-radialInflowDecayRate * r)
    }

    /**
     * Calculate the vertical velocity as a function of r:
     *
     *      v_y = Ymax * e^(-mr)
     *
     * @param r Distance from the core
     */
    private fun calculateVerticalVelocity(r: Double): Double {
        return verticalVelocity * exp(-verticalVelocityDecayRate * r)
    }

    /**
     * Calculate tangential velocities from the center relative coordinates the distance from the core
     *
     *      v_x = -v_θ * sin(θ) + v_in * cos(θ)
     *      v_z =  v_θ * cos(θ) + v_in * sin(θ)
     *
     * @param r Distance from the center of the tornado
     * @param theta Angle from the center of the tornado
     */
    private fun calculateTangentialVelocities(r: Double, theta: Double): Vec2d {
        val vTheta = calculateAngularVelocity(r)
        val vIn = calculateInflowVelocity(r)

        val vX = -vTheta * sin(theta) + vIn * cos(theta)
        val vZ = vTheta * cos(theta) + vIn * sin(theta)

        return Vec2d(vX, vZ)
    }

    /**
     * Calculate the final object velocity based on its position
     *
     * @param pos Object position
     */
    fun calculateObjectVelocityFromPosition(pos: Location): Vec3d {
        val r = calculateDistanceFromCore(pos)
        val centerRelativeCoordinate = calculateCenterRelativeCoordinate(pos)
        val theta = atan2(centerRelativeCoordinate.z, centerRelativeCoordinate.x)

        val vXZ = calculateTangentialVelocities(r, theta)
        val vY = calculateVerticalVelocity(r)

        return Vec3d(vXZ.x, vY, vXZ.y)
    }
}