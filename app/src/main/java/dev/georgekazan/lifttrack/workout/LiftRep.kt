package dev.georgekazan.lifttrack.workout

open class LiftRep(val workout: String, var reps: Int, var weight: Float) : Cloneable{

    override fun equals(other: Any?): Boolean {
        if(other !is LiftRep) return false

        return workout == other.workout && reps == other.reps && weight == other.weight
    }

    public override fun clone() = LiftRep(workout, reps, weight)

    override fun toString(): String {
        return "$workout $reps $weight"
    }

}