package dev.georgekazan.lifttrack;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.List;

import dev.georgekazan.lifttrack.adapter.LiftItemAdapter;
import dev.georgekazan.lifttrack.exercise.Exercise;
import dev.georgekazan.lifttrack.workout.LiftRep;
import dev.georgekazan.lifttrack.workout.Routine;
import dev.georgekazan.lifttrack.workout.Workout;
import kotlin.Pair;

public class RoutineEditActivity extends AppCompatActivity {

    private boolean isSaved = false;
    private boolean isDoing = false;

    private RecyclerView recyclerSets;
    private LiftItemAdapter adapter;
    private EditText routineName;
    private TextView totalTimeView;
    long startTime = 0;

    private Routine routine;

    private Workout workout;
    private MediaPlayer mp;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;

            totalTimeView.setText("Elapsed Time: " + String.format("%d:%02d", minutes, seconds));
            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_edit);

        if(Configuration.keepScreenOn) getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String name = intent.getStringExtra("RoutineName");
        isDoing = intent.getBooleanExtra("IsDoing", false);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setTitle(isDoing ? name : getResources().getString(R.string.edit_routine));

        recyclerSets = findViewById(R.id.sets_list);
        routineName = findViewById(R.id.routine_name);
        totalTimeView = findViewById(R.id.total_time);

        if(isDoing) {
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
        }
        else{
            ((ViewGroup)totalTimeView.getParent()).removeView(totalTimeView);
        }

        if(name == null){
            //We are making a new routine
            List<Routine> routines = DataUtils.INSTANCE.getRoutinesFrom(this);
            routine = new Routine(findGoodName("Routine", 0, routines));
        }
        else{
            //We are editing a current routine
            routine = DataUtils.INSTANCE.getRoutineByName(this, name);
        }

        routineName.setText(routine.getName());

        if(isDoing){
            //Routine is null until around here
            workout = new Workout(routine);
        }

        adapter = new LiftItemAdapter(this, isDoing, routine, workout);

        recyclerSets.setAdapter(adapter);
        recyclerSets.setLayoutManager(new LinearLayoutManager(this));

        mp = MediaPlayer.create(this, R.raw.timer_sound);
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        timerHandler.postDelayed(timerRunnable, 0);
    }

    //Generate a name based on the name param which isnt in the list
    public String findGoodName(String name, int i, List<Routine> routines){
        String a = i == 0 ? name : name + i;
        for (Routine ro : routines) if(ro.getName().equals(a)) return findGoodName(name, ++i, routines);
        return a;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add) {
            try {
                addExercisePopup();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        if (id == R.id.action_finished) {
            finishWorkout();
            return true;
        }

        if(id == R.id.action_timer){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.time));

            View view = View.inflate(this, R.layout.timer_view, null);

            TextView tim = view.findViewById(R.id.time_left);

            Button add = view.findViewById(R.id.add_time);
            Button remove = view.findViewById(R.id.remove_time);

            builder.setView(view);

            builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());

            AlertDialog dia = builder.show();
            dia.setCanceledOnTouchOutside(false);
            dia.setCancelable(false);
            dia.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            CountDownTimer timer = new CountDownTimer(30000, 1000){

                @Override
                public void onTick(long millisUntilFinished) {
                    tim.setText("" + millisUntilFinished / 1000);
                }

                @Override
                public void onFinish() {
                    mp.start();
                    dia.cancel();
                }
            }.start();

            //TODO fix the timer add and remove time
            add.setOnClickListener(v -> {
                timer.cancel();
            });

            return true;
        }

        if(id == android.R.id.home){
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void finishWorkout(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.you_finished));

        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);

        builder.setView(view);

        builder.setPositiveButton(getResources().getString(R.string.finished), (dialog, which) -> {

            workout.setElapsedTime(System.currentTimeMillis() - startTime);

            super.onBackPressed();
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void addExercisePopup() throws JSONException {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.add_exercise));

        LinearLayout view = new LinearLayout(this);
        view.setOrientation(LinearLayout.VERTICAL);

        Spinner input = new Spinner(this);

        //This gets our data
        List<Exercise> ours = DataUtils.INSTANCE.getExercisesFrom(this);
        //Add the users data
        ours.addAll(DataUtils.INSTANCE.getExercisesFrom(this, DataUtils.INSTANCE.getUserExercisesData(this)));

        ArrayAdapter<Exercise> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, ours);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.sort(Exercise::compareTo);

        input.setAdapter(adapter);

        view.addView(input);

        builder.setView(view);

        builder.setPositiveButton(getResources().getString(R.string.add), (dialog, which) -> {
            String name = input.getSelectedItem().toString();
            routine.getExercises().add(new Pair<>(name, new LiftRep[]{new LiftRep(name, 10, 5)}));
            this.adapter.notifyDataSetChanged();
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.cancel());

        builder.show();
    }

    public void saveRoutine(View view){
        /*
        TODO we need to handle if the user re-names a routine since right now it will just create a copy
         */
        routine.setName(routineName.getText().toString());

        isSaved = true;

        System.out.println(routine);

        DataUtils.INSTANCE.saveRoutine(this, routine);
        Toast.makeText(this, getResources().getString(R.string.saved), Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(isDoing ? R.menu.menu_play_routine : R.menu.menu_edit_routine, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if(isDoing) { finishWorkout(); return; }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Make sure you have saved your routine.");

        builder.setPositiveButton(getResources().getString(R.string.ihave), (dialog, which) -> {
            super.onBackPressed();
            dialog.dismiss();
        });

        builder.setNegativeButton(getResources().getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }
}