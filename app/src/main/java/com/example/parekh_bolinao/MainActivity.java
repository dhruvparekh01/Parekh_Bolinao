package com.example.parekh_bolinao;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference db;
    public ArrayList<Record> records;
    public ArrayList<Summary> summaries;
    public ArrayList<Record> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_recent, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Get a reference of the database
        db = FirebaseDatabase.getInstance().getReference();

        records = new ArrayList<>();
        summaries = new ArrayList<>();
        usersList = new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Calendar calendar = Calendar.getInstance();

        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                records = new ArrayList<>();
                summaries = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    boolean firstAdded = true;
                    boolean firstSummary = true;
                    Summary s = null;
                    for(DataSnapshot recordSnapshot : ds.getChildren()) {
                        Record record = recordSnapshot.getValue(Record.class);
                        records.add(record);

                        int currentMonth = calendar.get(Calendar.MONTH);
                        assert record != null;
                        if (firstSummary && record.getMonth() == currentMonth) {
                            s = new Summary(record.getName(),
                                    record.getSystolic_reading(),
                                    record.getDiastolic_reading(),
                                    1,
                                    record.getParent_id());
                            summaries.add(s);
                            firstSummary = false;
                        } else if (record.getMonth() == currentMonth) {
                            if (s != null) {
                                int count = s.getRecordCount();
                                double syst_new = (s.getSyst() * count + record.getSystolic_reading()) / (count + 1);
                                double dia_new = (s.getDia() * count + record.getDiastolic_reading()) / (count + 1);

                                s.setRecordCount(++count);
                                s.setDia(dia_new);
                                s.setSyst(syst_new);
                            }
                        }

//                        addToSummaries(record);
                        if (firstAdded) {
                            usersList.add(record);
                            firstAdded = false;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Unable to add record!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public DatabaseReference getDb() {
        return db;
    }

    public ArrayList<Record> getRecords() {
        return records;
    }

    public ArrayList<Record> getUsersList() {
        return usersList;
    }

    public ArrayList<Summary> getSummaries() {
        return summaries;
    }
}
