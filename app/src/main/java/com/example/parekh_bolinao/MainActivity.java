package com.example.parekh_bolinao;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    AlertDialog.Builder builder;
    AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);

        builder = new AlertDialog.Builder(MainActivity.this);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // Get a reference of the database
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    public String checkHealth(int syst, int dias) {
        if (syst < 120 && dias < 80) {
            return "normal";
        } else if (syst <= 129 && syst >= 120 && dias < 80) {
            return "elevated";
        } else if ((syst <= 139 && syst >= 130) || (dias >= 80 && dias <= 89)) {
            return "hbp1";
        } else if ((syst <= 179 && syst >= 140) || (dias >= 90 && dias <= 120)) {
            return "hbp2";
        } else {
            return "Hypertensive Crisis";
        }
    }

    protected void onStart() {
        super.onStart();
        View btn = findViewById(R.id.btn_check);

        btn.setOnClickListener((v) -> {
            // Store in database, display condition
            EditText nameEdit = findViewById(R.id.name_edit);
            EditText systEdit = findViewById(R.id.systolic_edit);
            EditText diasEdit = findViewById(R.id.diastolic_edit);

            int systolicRead, diastolicRead;

            String user_name = nameEdit.getText().toString();
            try {
                systolicRead = Integer.valueOf(systEdit.getText().toString());
                diastolicRead = Integer.valueOf(diasEdit.getText().toString());
            }
            catch (NumberFormatException e) {
                Toast.makeText(MainActivity.this,"Blood pressures can only be numbers", Toast.LENGTH_LONG).show();
                return;
            }

            Record record = new Record(user_name, systolicRead, diastolicRead);
            // Get an ID for the entry from firebase
            String id = mDatabase.push().getKey();
            assert id != null;
            Task insert = mDatabase.child("Records").child(id).setValue(record);

            insert.addOnSuccessListener((o) -> {
                Toast.makeText(MainActivity.this,"Record added.",Toast.LENGTH_LONG).show();
                nameEdit.setText("");
                systEdit.setText("");
                diasEdit.setText("");
            });

            insert.addOnFailureListener((o) -> Toast.makeText(MainActivity.this,
                    "Something went wrong! Please check your connection and try again later.",
                    Toast.LENGTH_LONG).show());


            String alert_message = checkHealth(systolicRead, diastolicRead);
            builder.setMessage(alert_message);
            builder.setNegativeButton("Close", (dialog, id1) -> dialog.cancel());
            alert = builder.create();
            alert.show();
        });
    }

}
