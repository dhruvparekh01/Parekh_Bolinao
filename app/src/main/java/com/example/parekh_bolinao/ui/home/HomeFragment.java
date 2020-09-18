package com.example.parekh_bolinao.ui.home;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.parekh_bolinao.MainActivity;
import com.example.parekh_bolinao.R;
import com.example.parekh_bolinao.Record;
import com.example.parekh_bolinao.RecordUsersAdapter;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private View root;
    ListView lv;
    List<Record> recordList;
    DatabaseReference mDatabase;
    ValueEventListener dataChangeListener;
    EditText nameEdit;
    EditText systEdit;
    EditText diasEdit;
    TextView currUser;
    String currentUserString;
    String currUserID;
    String noSelect;
    private RecordUsersAdapter adapter;
    boolean firstLoad = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        lv = root.findViewById(R.id.existing_users);
        nameEdit = root.findViewById(R.id.name_edit);
        systEdit = root.findViewById(R.id.systolic_edit);
        diasEdit = root.findViewById(R.id.diastolic_edit);
        currUser = root.findViewById(R.id.current_user_text);

        noSelect = "No User Selected.";
        currentUserString = noSelect;
        currUser.setText(currentUserString);

        View clearUserBtn = root.findViewById(R.id.clear_user);
        View addNewBtn = root.findViewById(R.id.add_new);
        mDatabase = ((MainActivity)getActivity()).getDb();

        addNewBtn.setOnClickListener((v) -> {
            String id = mDatabase.push().getKey();
            assert id != null;
            if (currUserID == null) {
                currentUserString = nameEdit.getText().toString();
                currUserID = currentUserString.toLowerCase() + id;
            }
            addToDatabase(mDatabase, currentUserString, id, currUserID);
        });

        clearUserBtn.setOnClickListener((v) -> {
            this.currentUserString = noSelect;
            currUserID = null;
            currUser.setText(currentUserString);
            nameEdit.setText("");
        });
        currUserID = null;
        recordList = new ArrayList<>();
        return root;
    }

    /**
     * Checks the users health based on their systolic and
     * diastolic readings.
     * @param syst int
     * @param dias int
     * @return an integer from 1-5 representing normal,
     *          elevated, high blood pressure (stage 1 and 2),
     *          and hypertensive crisis respectively
     */
    public boolean checkHealth(int syst, int dias) {
        return  (syst >= 180 || dias >= 120);
    }

    public void onStart() {
        super.onStart();
        if (recordList.size() == 0) {
            Log.e("Action", "force filling array.");
            forceFill();
            mDatabase.removeEventListener(dataChangeListener);
        } else {
            recordList = ((MainActivity)getActivity()).records;
            adapter = new RecordUsersAdapter(getActivity(), recordList);
            lv.setAdapter(adapter);
        }
        lv.setOnItemClickListener((parent, view, position, id) -> {
            Record record = recordList.get(position);
            currentUserString = record.getName();
            currUserID = record.getParent_id();
            currUser.setText(currentUserString);
            nameEdit.setText(currentUserString);
        });
    }

    public void onStop() {
        super.onStop();
        firstLoad = false;
    }

    private void forceFill() {
        dataChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recordList = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    for(DataSnapshot recordSnapshot : ds.getChildren()) {
                        Record record = recordSnapshot.getValue(Record.class);
                        recordList.add(record);
                        break;
                    }
                }
                RecordUsersAdapter adapter = new RecordUsersAdapter(getActivity(), recordList);
                lv.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Unable to add record!", Toast.LENGTH_LONG).show();
            }
        };
        mDatabase.addListenerForSingleValueEvent(dataChangeListener);
    }

    public void addToDatabase(DatabaseReference mDatabase, String user_name, String id, String parent_id) {
        // Store in database, display condition

        int systolicRead = Integer.valueOf(systEdit.getText().toString());
        int diastolicRead = Integer.valueOf(diasEdit.getText().toString());

        // Get an ID for the entry from firebase
        Record record = new Record(user_name, systolicRead, diastolicRead, id, parent_id);
        Task insert = mDatabase.child(parent_id).child(id).setValue(record);

        insert.addOnSuccessListener((o) -> {
            Toast.makeText(getActivity(),"Record added.",Toast.LENGTH_LONG).show();
            nameEdit.setText("");
            systEdit.setText("");
            diasEdit.setText("");
            currentUserString = record.getName();
            currUserID = record.getParent_id();
            currUser.setText(currentUserString);
            if (firstLoad) {
                forceFill();
            }
        });

        insert.addOnFailureListener((o) -> Toast.makeText(getActivity(),
                "Something went wrong! Please check your connection and try again later.",
                Toast.LENGTH_LONG).show());

        if (checkHealth(systolicRead, diastolicRead)) {
            String alert_message = "WARNING! Hypertensive Crisis. Consult your doctor IMMEDIATELY.";
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(alert_message);
            builder.setNegativeButton("Close", (dialog, id1) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}