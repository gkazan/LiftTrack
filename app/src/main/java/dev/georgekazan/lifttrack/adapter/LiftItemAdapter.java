package dev.georgekazan.lifttrack.adapter;

import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import dev.georgekazan.lifttrack.R;
import dev.georgekazan.lifttrack.workout.LiftRep;
import dev.georgekazan.lifttrack.workout.Routine;
import dev.georgekazan.lifttrack.workout.Workout;
import kotlin.Pair;

public class LiftItemAdapter extends RecyclerView.Adapter<LiftItemAdapter.EViewHolder> {

    private final Context context;
    private final boolean isDoing;
    private final Routine routine;
    @Nullable
    private final Workout workout;

    public LiftItemAdapter(Context context, boolean isDoing, Routine routine, Workout workout) {
        this.context = context;
        this.routine = routine;
        this.isDoing = isDoing;
        this.workout = workout;
    }

    @NotNull
    @Override
    public EViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.lift_item, parent, false);
        return new EViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull EViewHolder holder, int position) {
        Pair<String, LiftRep[]> pair = routine.getExercises().get(position);

        //Used to properly re-tick the checkboxes when this is recycled
        //There still seems to be some bugs with this to look into later but it works well enough for now
        List<LiftRep> completedData = new ArrayList<>();
        if(workout != null && workout.getCompletedSets().get(pair.getFirst()) != null) {
            completedData.addAll(workout.getCompletedSets().get(pair.getFirst()));
        }
        if(completedData.isEmpty()) completedData = null;

        holder.name.setText(pair.getFirst());

        final LiftSetAdapter adapter = new LiftSetAdapter(context, isDoing, this, pair.getFirst(), routine, workout, position, completedData);
        holder.lifts.setAdapter(adapter);
        holder.lifts.setLayoutManager(new LinearLayoutManager(context));

        holder.addSet.setOnClickListener(v -> {
            Pair<String, LiftRep[]> pp = routine.getExercises().get(position);

            LiftRep[] r = new LiftRep[pp.getSecond().length + 1];

            if(r.length != 1) {
                /*
                Clones the lift rep array r, and adds an extra LiftRep to the end, copying the previous
                data. Just to make adding sets easier as opposed to having to re-type and replace
                the default data each time.
                 */
                for (int i = 0, b = 0; i < r.length; i++, b++) {
                    if (b >= r.length - 1){
                        r[i] = pp.getSecond()[--b].clone();
                        continue;
                    }
                    r[i] = pp.getSecond()[b];
                }
            }
            else{
                r[0] = new LiftRep(pp.getFirst(), 10, 5);
            }

            routine.getExercises().set(position, new Pair<>(pp.getFirst(), r));
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return routine.getExercises().size();
    }

    public static class EViewHolder extends RecyclerView.ViewHolder{

        private final TextView name;
        private final Button addSet;
        private final RecyclerView lifts;

        public EViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textViewLiftName);
            addSet = itemView.findViewById(R.id.add_set_button);
            lifts = itemView.findViewById(R.id.lift_items_recycle);
        }
    }
}
