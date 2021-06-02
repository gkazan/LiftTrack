package dev.georgekazan.lifttrack.exercise

/**
 * @param isBodyWeight used to determine if it's added weight to the movement or that exact weight
 */
//TODO eventually implement the isBodyWeight variable into the LiftSet to display a separate icon
open class Exercise(val name: String, val isUserCreated: Boolean, val isBodyWeight: Boolean) : Comparable<Exercise?> {

    override fun toString(): String = name

    override fun compareTo(other: Exercise?): Int = if (other == null) -1 else name.compareTo(other.name)

}