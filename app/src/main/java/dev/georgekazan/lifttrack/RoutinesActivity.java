package dev.georgekazan.lifttrack;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import dev.georgekazan.lifttrack.workout.Routine;

public class RoutinesActivity extends AppCompatActivity {

    private ListView listView;
    private List<Routine> routines = new ArrayList<>();
    private RoutinesAdapter adapter;
    private JSONObject routinesJson;

    // override fun onCreate(savedInstanceState: Bundle?) {

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_routines);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();

        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle(getResources().getString(R.string.button_routines));

        listView = findViewById(R.id.routines_list);

        try {
            loadRoutines();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        adapter = new RoutinesAdapter(this, routines);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {

            for (int i = 0; i < parent.getCount(); i++){
                View v = parent.getChildAt(i);
                CheckedTextView check = v.findViewById(R.id.checkbox);
                check.setChecked(false);
            }

            CheckedTextView check = view.findViewById(R.id.checkbox);
            check.toggle();
        });
    }

    @Override
    protected void onResume() {
        routines.clear();
        try {
            loadRoutines();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(adapter != null) adapter.notifyDataSetChanged();
        super.onResume();
    }

    private void loadRoutines() throws JSONException {
        routinesJson = new JSONObject(PreferenceManager.getDefaultSharedPreferences(this).getString("routines", "{ \"routines\": [] }"));

        JSONArray array = routinesJson.getJSONArray("routines");

        for(int i = 0; i < array.length(); i++){
            routines.add(new Gson().fromJson(array.get(i).toString(), Routine.class));
        }
    }

    public Routine getRoutineIfValid(){
        if(adapter == null || adapter.getCount() <= 0 || listView.getCheckedItemPosition() == -1){
            return null;
        }
        return adapter.getItem(listView.getCheckedItemPosition());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_start){
            Routine routine = getRoutineIfValid();

            if(routine == null){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.select_item), Toast.LENGTH_LONG).show();
                return true;
            }

            Intent intent = new Intent(this, RoutineEditActivity.class);
            intent.putExtra("RoutineName", routine.getName());
            intent.putExtra("IsDoing", true);
            startActivity(intent);
            return true;
        }

        if(id == R.id.action_edit){
            Routine routine = getRoutineIfValid();

            if(routine == null){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.select_item), Toast.LENGTH_LONG).show();
                return true;
            }

            Intent intent = new Intent(this, RoutineEditActivity.class);
            intent.putExtra("RoutineName", routine.getName());
            startActivity(intent);
            return true;
        }

        if(id == R.id.action_remove){
            Routine routine = getRoutineIfValid();

            if(routine == null){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.select_item), Toast.LENGTH_LONG).show();
                return true;
            }

            AlertDialog.Builder build = new AlertDialog.Builder(this);
            build.setMessage("Are you sure you want to delete the " + routine.getName() + " routine?");

            build.setPositiveButton(getResources().getString(R.string.delete), (dialog, which) -> {
                routines.remove(routine);
                adapter.notifyDataSetChanged();
                DataUtils.INSTANCE.removeRoutine(this, routine);
                dialog.dismiss();
            });

            build.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

            build.create().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void createRoutine(View view){
        Intent intent = new Intent(this, RoutineEditActivity.class);
        intent.putExtra("RoutineName", (String)null);
        intent.putExtra("IsDoing", false);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_routines, menu);
        return true;
    }

    public static class RoutinesAdapter extends ArrayAdapter<Routine>{

        private final Context ctx;
        private final List<Routine> routines;

        public RoutinesAdapter(@NonNull Context context, List<Routine> routines) {
            super(context, R.layout.routine_item, R.id.textViewName, routines);
            ctx = context;
            this.routines = routines;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) ctx.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.routine_item, parent, false);
            TextView text = row.findViewById(R.id.textViewName);
            text.setText(routines.get(position).getName());
            return row;
        }
    }

}
