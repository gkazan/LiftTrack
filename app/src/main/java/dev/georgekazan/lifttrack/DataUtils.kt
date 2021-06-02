package dev.georgekazan.lifttrack

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import dev.georgekazan.lifttrack.exercise.Exercise
import dev.georgekazan.lifttrack.workout.Routine
import org.apache.commons.io.IOUtils
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.nio.charset.Charset

object DataUtils {

    @Throws(JSONException::class)
    fun getUserExercisesData(context: Context): JSONArray {
        val ins: InputStream = context.resources.openRawResource(context.resources.getIdentifier("exercise", "raw", context.packageName))
        val json = JSONObject(IOUtils.toString(ins, Charset.defaultCharset()))
        return json.getJSONArray("exercises")
    }

    @Throws(JSONException::class)
    fun getExercisesData(context: Context): JSONArray =
        JSONObject(PreferenceManager.getDefaultSharedPreferences(context).getString("custom_exercises", "{ \"exercises\": [] }")!!
        ).getJSONArray("exercises")

    @JvmOverloads
    fun getExercisesFrom(context: Context, jarray: JSONArray = getExercisesData(context)): List<Exercise>{
        val a = mutableListOf<Exercise>()
        for(i in 0 until jarray.length()) {
            a.add(Gson().fromJson(jarray.getJSONObject(i).toString(), Exercise::class.java))
        }
        return a
    }

    @Throws(JSONException::class)
    fun getUserRoutines(context: Context): JSONArray =
        JSONObject(PreferenceManager.getDefaultSharedPreferences(context).getString("routines", "{ \"routines\": [] }")!!
        ).getJSONArray("routines")

    @JvmOverloads
    fun getRoutineByName(context: Context, name: String, routinesArray: JSONArray = getUserRoutines(context)): Routine? {
        for(i in 0 until routinesArray.length()){
            val routineObj = routinesArray.getJSONObject(i)

            val routineName = routineObj.getString("name")

            if(name != routineName) continue

            return Gson().fromJson(routineObj.toString(), Routine::class.java)
        }
        return null
    }

    fun saveRoutine(context: Context, routine: Routine) {
        val routines = JSONObject(PreferenceManager.getDefaultSharedPreferences(context).getString("routines", "{ \"routines\": [] }")!!)
        val arr = routines.getJSONArray("routines")

        var isFound = false
        val nroutine = JSONObject(Gson().toJson(routine))

        for(i in 0 until arr.length()) {
            val robj = arr.getJSONObject(i)

            if(robj.getString("name") == routine.name){
                isFound = true
                arr.put(i, nroutine)
                break
            }
        }

        if(!isFound){
            //Add the new exercise
            arr.put(nroutine)
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("routines", routines.toString()).apply();
    }

    fun removeRoutine(context: Context, routine: Routine) {
        val routines = JSONObject(PreferenceManager.getDefaultSharedPreferences(context).getString("routines", "{ \"routines\": [] }")!!)
        val arr = routines.getJSONArray("routines")

        for(i in 0 until arr.length()) {
            val robj = arr.getJSONObject(i)

            if(robj.getString("name") == routine.name){
                arr.remove(i)
                break
            }
        }

        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("routines", routines.toString()).apply();
    }

    @JvmOverloads
    fun getRoutinesFrom(context: Context, jarray: JSONArray = getUserRoutines(context)): List<Routine>{
        val a = mutableListOf<Routine>()
        for(i in 0 until jarray.length()) {
            a.add(Gson().fromJson(jarray.getJSONObject(i).toString(), Routine::class.java))
        }
        return a
    }
}