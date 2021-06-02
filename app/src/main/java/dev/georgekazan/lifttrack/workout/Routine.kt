package dev.georgekazan.lifttrack.workout

open class Routine(var name: String) : Comparable<Routine?> {

    //Name and LiftRep[] the reps, then as many in array is the sets
    //EX: Curl : {(20lb, 10), (15lb, 10)}
    open val exercises = mutableListOf<Pair<String, Array<LiftRep>>>()

    override fun toString(): String{
        var st = name;
        for (exercise in exercises) {
            st += "\n"
            st += exercise.first
            for (liftRep in exercise.second) {
                st += "\t" + liftRep.toString()
            }
        }
        return st
    }

    override fun compareTo(other: Routine?): Int = if (other == null) -1 else name.compareTo(other.name)

}