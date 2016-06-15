package mx.ramsesmartinez.shakeapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ShakeActivity extends AppCompatActivity {
    String name;
    String urlImageProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake);

        name = getIntent().getStringExtra("NAME");
        urlImageProfile = getIntent().getStringExtra("PHOTO");
    }
}
