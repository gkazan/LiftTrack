package dev.georgekazan.lifttrack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //No settings yet
       if (id == R.id.action_settings) {
           Toast.makeText(getApplicationContext(), getResources().getString(R.string.settings_unavailable), Toast.LENGTH_LONG).show();
//            Intent intent = new Intent(this, SettingsActivity.class);
//            startActivity(intent);
            return true;
        }

        if (id == R.id.action_source) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/gkazan/LiftTrack"));
            startActivity(browserIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openRoutines(View view) {
        Intent intent = new Intent(this, RoutinesActivity.class);
        startActivity(intent);
    }

    public void openExercises(View view){
        Intent intent = new Intent(this, ExercisesActivity.class);
        startActivity(intent);
    }

}