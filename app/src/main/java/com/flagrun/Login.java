package com.flagrun;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Login extends AppCompatActivity {

    EditText nameET, phoneET;
    Button button;
    FirebaseDatabase database;
    DatabaseReference root;
    RadioButton radioButton;
    RadioGroup teamSelector;
    String latLong = "";
    void checkGameStatus() {

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {

                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.getKey().equalsIgnoreCase("teamA") && UtilityClass.getTeam(Login.this).equalsIgnoreCase("Team A")) {
                            FlagModel model = child.getValue(FlagModel.class);
                            latLong = String.valueOf(model.latitude) + " - " + String.valueOf(model.longitude);
                            Log.d("PlayerDataModel--- key", String.valueOf(model.latitude));
                            Intent intent = new Intent(Login.this, MyGame.class);
                            intent.putExtra("latLong", latLong);
                            startActivity(intent);
                            Login.this.finish();
                            break;
                        } else if (child.getKey().equalsIgnoreCase("teamB") && UtilityClass.getTeam(Login.this).equalsIgnoreCase("Team B")) {
                            FlagModel model = child.getValue(FlagModel.class);
                            latLong = String.valueOf(model.latitude) + " - " + String.valueOf(model.longitude);
                            Intent intent = new Intent(Login.this, MyGame.class);
                            intent.putExtra("latLong", latLong);
                            startActivity(intent);
                            Login.this.finish();
                            break;
                        }

                        Log.d("PlayerDataModel key", child.getKey());
                        Log.d("PlayerDataModel ref", child.getRef().toString());
                        Log.d("PlayerDataModel val", child.getValue().toString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        root.addChildEventListener(childEventListener);
    }

            @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(UtilityClass.isLoggedIn(Login.this)){
            if(UtilityClass.getUserOutStatus(this) == 0){
                Intent intent = new Intent(this,MyGame.class);
                startActivity(intent);
                this.finish();
            }else{
                Intent intent = new Intent(this,FinishGameActivity.class);
                startActivity(intent);
                this.finish();
            }
        }else{
            nameET = findViewById(R.id.name);
            phoneET = findViewById(R.id.phone);
            button = findViewById(R.id.submit);
            teamSelector = findViewById(R.id.radioGroup);
            database = FirebaseDatabase.getInstance();
            root = database.getReference();
            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode
            final String token = pref.getString("token", ""); // Storing string
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name1 = nameET.getText().toString();
                    String phone = phoneET.getText().toString();
                    int selectedId = 0;
                    String team = null;
                    try {
                        selectedId = teamSelector.getCheckedRadioButtonId();
                        radioButton = (RadioButton) findViewById(selectedId);
                        team = radioButton.getText().toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if(name1!=null && phone !=null && team != null && !name1.trim().equals("") && !phone.trim().equals("") && !team.trim().equals("")){
                        UtilityClass.setTeam(Login.this, team);
                        UtilityClass.setPhone(Login.this, phone);
                        // find the radiobutton by returned id
                        User user = new User(name1, phone, token, team,false,false);
                        if(team.equalsIgnoreCase("Team A")){
                            root.child(Preferences.TEAM_A).push().setValue(user);
                        }else{
                            root.child(Preferences.TEAM_B).push().setValue(user);
                        }
                        UtilityClass.setIsLoggedIn(Login.this, true);
                        checkGameStatus();
                    }else{
                        Toast.makeText(getApplicationContext(),"All fields are required.",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

}
