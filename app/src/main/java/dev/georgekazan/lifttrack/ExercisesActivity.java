package dev.georgekazan.lifttrack;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.text.InputType;
import android.util.Xml;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import dev.georgekazan.lifttrack.exercise.Exercise;

public class ExercisesActivity extends AppCompatActivity {

    private ListView listView;
    private final List<Exercise> exercises = new ArrayList<>();
    private ArrayAdapter<Exercise> adapter;

    private JSONObject customExercises;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercises);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle(getResources().getString(R.string.button_exercises));

        try {
            loadExercises();
            loadUserExercises();
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        listView = findViewById(R.id.exercise_list);

        adapter = new ArrayAdapter<Exercise>(this, android.R.layout.simple_list_item_single_choice, exercises) {
            @Override
            public boolean isEnabled(int position) {
                //Exercises created by us should not be deletable, since they would simply showup again the next
                //time the view is launched
                return getItem(position).isUserCreated() && super.isEnabled(position);
            }

        };

        adapter.sort(Exercise::compareTo);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);
    }

    /**
     * Loads pre-defined exercises from raw exercise.json
     */
    public void loadExercises() throws JSONException, IOException {
        InputStream ins = getResources().openRawResource(getResources().getIdentifier("exercise", "raw", getPackageName()));
        JSONObject json = new JSONObject(IOUtils.toString(ins, Charset.defaultCharset()));
        JSONArray exerciseArray = json.getJSONArray("exercises");
        populateExercises(exerciseArray, false);
    }

    /**
     * Loads all user defined exercises from a file on their device
     */
    public void loadUserExercises() throws JSONException {
        customExercises = new JSONObject(PreferenceManager.getDefaultSharedPreferences(this).getString("custom_exercises", "{ \"exercises\": [] }"));
        JSONArray exerciseArray = customExercises.getJSONArray("exercises");
        populateExercises(exerciseArray, true);
    }

    private void populateExercises(JSONArray exerciseArray, boolean isUser) throws JSONException {
        for (int i = 0; i < exerciseArray.length(); ++i) {
            JSONObject data = exerciseArray.getJSONObject(i);

            String name = data.getString("name");
            Exercise e = new Exercise(name, isUser, false);
            exercises.add(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_exercises, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            showExerciseCreate();
            return true;
        }

        if (id == R.id.action_remove) {
            removeSelectedExercise();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showExerciseCreate() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.create_exercise));

        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(getResources().getString(R.string.name));
        view.addView(input);

        CheckBox reverse = new CheckBox(this);
        reverse.setHint(getResources().getString(R.string.create_reverse));
        view.addView(reverse);

        builder.setView(view);

        builder.setPositiveButton(getResources().getString(R.string.create), (dialog, which) -> {
            String name = input.getText().toString();

            if (name.isEmpty()) return;

            try {
                createExercise(name, reverse.isChecked());
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.failed_create), Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Adds a new exercise to the list and save data
     *
     * @param name       Exercise name
     * @param reversible Should a Reverse version be created
     * @throws JSONException
     */
    private void createExercise(String name, boolean reversible) throws JSONException {
        //TODO can we make sure that it doesnt already exist here
        JSONArray array = customExercises.getJSONArray("exercises");

        Exercise e = new Exercise(name, true, false);
        exercises.add(e);

        if (reversible) createExercise("Reverse " + name, false);

        adapter.sort(Exercise::compareTo);
        adapter.notifyDataSetChanged();

        JSONObject ob = new JSONObject();
        ob.put("name", name);
        array.put(ob);

        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("custom_exercises", customExercises.toString()).apply();
    }

    /**
     * Confirms the user wants to delete the listView selected exercise and removes from the list
     * and calls other methods to remove from wherever else it may be
     */
    private void removeSelectedExercise() {
        Exercise e = adapter.getItem(listView.getCheckedItemPosition());
        if (e == null) {
            //Tell the user to select something if they want to delete
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.select_item), Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete the " + e.getName() + " exercise?" +
                "\nDeleting will only prevent this exercise from being added and will not remove it from any current routines");

        builder.setPositiveButton(getResources().getString(R.string.delete), (dialog, which) -> {
            try {
                removeExerciseFromSave(e);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
            exercises.remove(e);
            adapter.sort(Exercise::compareTo);
            adapter.notifyDataSetChanged();

            //TODO deleting will select a non-deletable item, which then can be deleted

            dialog.dismiss();
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    /**
     * Removes the given exercise from preferences
     */
    private void removeExerciseFromSave(Exercise e) throws JSONException {
        JSONArray array = customExercises.getJSONArray("exercises");

        for (int i = 0; i < array.length(); ++i) {
            JSONObject data = array.getJSONObject(i);

            String name = data.getString("name");
            if (name.equals(e.getName())) {
                array.remove(i);
                break;
            }
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("custom_exercises", customExercises.toString()).apply();
    }

}