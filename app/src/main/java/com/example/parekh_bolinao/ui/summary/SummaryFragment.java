package com.example.parekh_bolinao.ui.summary;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.parekh_bolinao.MainActivity;
import com.example.parekh_bolinao.R;
import com.example.parekh_bolinao.Record;
import com.example.parekh_bolinao.Summary;
import com.example.parekh_bolinao.SummaryAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

public class SummaryFragment extends Fragment {

    private SummaryViewModel summaryViewModel;
    private View root;
    ValueEventListener dataChangeListener;
    DatabaseReference mDatabase;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        summaryViewModel =
                ViewModelProviders.of(this).get(SummaryViewModel.class);
        root = inflater.inflate(R.layout.fragment_summaries, container, false);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        mDatabase = ((MainActivity)getActivity()).getmDatabase();

        ListView lv = root.findViewById(R.id.summary_list);

        dataChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Summary> summaries = new ArrayList<>(0);
                if (dataSnapshot.exists()) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        String name = ds.getKey();
                        name = name.substring(0, name.indexOf('-'));
                        double sys = 0, dia = 0;
                        double count = ds.getChildrenCount();
                        for(DataSnapshot rec : ds.getChildren()) {
                            Record r = rec.getValue(Record.class);
                            sys += r.systolic_reading;
                            dia += r.diastolic_reading;
                        }
                        summaries.add(new Summary(name, sys/count, dia/count));
                    }

                    SummaryAdapter adapter = new SummaryAdapter(getActivity(), summaries);
                    lv.setAdapter(adapter);
                }
                else {
                    Log.d(TAG, "Firebase Error: Cannot find snapshot of db.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mDatabase.addValueEventListener(dataChangeListener);
    }

    private void getDataForMonth(int mon) {
        DatabaseReference mDatabase = ((MainActivity)getActivity()).getmDatabase();
        Query query = mDatabase.child("Records");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void onStop() {
        super.onStop();
        mDatabase.removeEventListener(dataChangeListener);
    }
}