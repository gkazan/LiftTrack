package dev.georgekazan.lifttrack.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import dev.georgekazan.lifttrack.R;
import dev.georgekazan.lifttrack.workout.LiftRep;
import dev.georgekazan.lifttrack.workout.Routine;
import dev.georgekazan.lifttrack.workout.Workout;
import kotlin.Pair;

public class LiftSetAdapter extends RecyclerView.Adapter<LiftSetAdapter.LViewHolder> {

    private final Context context;
    private final boolean isDoing;
    private final Routine routine;
    private final LiftItemAdapter parAdat;
    private int pos;

    private LiftRep[] reps;
    private String name;

    @Nullable
    private final Workout workout;

    private List<LiftRep> completed;

    /*
    This class could use an overhaul with how positions are determined since right now it is awful
     */
    public LiftSetAdapter(Context context, boolean isDoing, LiftItemAdapter parAdapt, String name, Routine routine, Workout workout, int position, List<LiftRep> completed) {
        this.context = context;
        this.parAdat = parAdapt;
        this.routine = routine;
        this.isDoing = isDoing;
        this.workout = workout;
        this.name=  name;
        this.completed = completed;
        pos = position;
        updateReps();
    }

    /**
     * As we add/remove sets and therefore also exercises, the positions will have shifted
     * from what we originally passed, so we must re-calculate it
     */
    private void fixPosition(){
        int a = 0;
        for (Pair<String, LiftRep[]> exercise : routine.getExercises()) {
            if(exercise.getFirst().equals(name)){
                pos = a;
                break;
            }
            a++;
        }
    }

    private void updateReps(){
        fixPosition();
        reps = routine.getExercises().get(pos).getSecond();
    }

    @NotNull
    @Override
    public LViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.lift_set, parent, false);
        return new LViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull LViewHolder holder, int position) {

        LiftRep liftRep = reps[position];

        holder.weightEdit.setText("" + liftRep.getWeight());
        holder.weightEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String value = holder.weightEdit.getText().toString();
                if(value.isEmpty()) liftRep.setWeight(0);
                else{
                    try{
                        liftRep.setWeight(Float.parseFloat(holder.weightEdit.getText().toString()));
                    }
                    catch (Exception e){
                        liftRep.setWeight(0);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        holder.repsEdit.setText("" + liftRep.getReps());
        holder.repsEdit.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                String value = holder.repsEdit.getText().toString();
                if(value.isEmpty()) liftRep.setReps(0);
                else {
                    try{
                        liftRep.setReps(Integer.parseInt(holder.repsEdit.getText().toString()));
                    }
                    catch (Exception e){
                        liftRep.setReps(0);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        //This is kinda slow still, look to optimizing in the future
        //We could possibly start using IDs for LiftReps but that presents its own issues
        holder.removeSet.setOnClickListener(v1 -> {
            Pair<String, LiftRep[]> pp = routine.getExercises().get(pos);
            List<LiftRep> kept = new ArrayList<>();

            if(pp.getSecond().length - 1 != 0){

                boolean flag = false;

                for (int i = 0; i < pp.getSecond().length; i++) {
                    LiftRep a = pp.getSecond()[i];
                    if(!flag && a.equals(liftRep)){
                        flag = true;
                        continue;
                    }
                    kept.add(a);
                }
            }

            fixPosition();

            if(kept.size() == 0){
                routine.getExercises().remove(pos);
                notifyDataSetChanged();
                parAdat.notifyDataSetChanged();//Notify above because we removed an exercise
                return;
            }

            routine.getExercises().set(pos, new Pair<>(pp.getFirst(), kept.toArray(new LiftRep[0])));
            notifyDataSetChanged();
        });

        if(!isDoing) holder.completeBox.setEnabled(false);

        if(isDoing && workout != null){

            if(completed != null) {
                for (int i = completed.size() - 1; i >= 0; i--) {
                    if (completed.get(i) == liftRep) {
                        holder.completeBox.setChecked(true);
                        completed.remove(i);
                        break;
                    }
                }
            }

            holder.completeBox.setOnCheckedChangeListener((buttonView, isChecked) -> {

                Pair<String, LiftRep[]> pp = routine.getExercises().get(pos);

                List<LiftRep> reps = workout.getCompletedSets().get(pp.getFirst());

                if(isChecked){
                    //Cant use a constant value for this because if you click
                    //Very quickly youll only hear one sound since its the same source
                    MediaPlayer.create(context, R.raw.check_sound).start();
                    //No completed data for this exercise yet
                    if(reps == null){
                        workout.getCompletedSets().put(pp.getFirst(), new ArrayList<>(Collections.singletonList(liftRep)));
                    }
                    else{
                        reps.add(liftRep);
                    }
                }
                else{
                    if(reps != null){
                        reps.remove(liftRep);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        updateReps();
        return reps.length;
    }

    public static class LViewHolder extends RecyclerView.ViewHolder{

        private final Button removeSet;
        private final CheckBox completeBox;
        private final EditText weightEdit, repsEdit;

        public LViewHolder(@NonNull View itemView) {
            super(itemView);
            completeBox = itemView.findViewById(R.id.completed_button);
            weightEdit = itemView.findViewById(R.id.weight_edit);
            repsEdit = itemView.findViewById(R.id.reps_edit);
            removeSet = itemView.findViewById(R.id.remove_set);
        }

    }
}
