package dev.georgekazan.lifttrack.workout

/**
 * A Workout is a way of storing the actual things the user has done during their active routine session.
 * We use this to handle statistics eventually.
 * Will hold information such as completed sets, time elapsed, etc.
 * @param routine The routine the user is doing
 */
open class Workout(val routine: Routine){

    open var elapsedTime: Long = 0

    /**
     * Total time that was used for breaks (the timer was used)
     */
    //We arent going to use this for now, but its a good reference
    open var timerTime: Long = 0

    /**
     * List containing the pair of exercise and array of lift reps
     * Barbell Curl and LiftRep []
     */
    open val completedSets = mutableMapOf<String, List<LiftRep>>()

}