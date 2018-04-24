package com.flagrun;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

public class FinishGameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_game);
        TextView status = findViewById(R.id.status);
        status.setText(getIntent().getStringExtra("status"));
    }

    public void goToLoginScreen(View view) {
        UtilityClass.setIsLoggedIn(this, false);
        UtilityClass.setUserOutStatus(this, 0);
        Intent intent = new Intent(this,Login.class);
        startActivity(intent);
        this.finish();
    }
}
