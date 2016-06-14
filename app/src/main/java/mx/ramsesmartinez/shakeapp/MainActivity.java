package mx.ramsesmartinez.shakeapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements OnClickListener, OnFocusChangeListener {

    EditText editTextEmail;
    EditText editTextPassword;
    TextView textViewEmail;
    TextView textViewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbar();

        editTextEmail= (EditText) findViewById(R.id.edit_text_email);
        editTextPassword = (EditText) findViewById(R.id.edit_text_password);
        textViewEmail  = (TextView) findViewById(R.id.text_view_email);
        textViewPassword = (TextView) findViewById(R.id.text_view_password);

        editTextEmail.setOnFocusChangeListener(this);
        editTextPassword.setOnFocusChangeListener(this);
    }

    public void setToolbar(){
        Toolbar toolbar =(Toolbar) findViewById(R.id.activity_my_toolbar);
        toolbar.setTitle("Login");
        setSupportActionBar(toolbar);
    }


    @Override
    public void onClick(View v) {

    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch(v.getId()){
            case R.id.edit_text_email:
                if(hasFocus) {
                    editTextEmail.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (editTextEmail.getText().toString().isEmpty())
                                textViewEmail.setVisibility(View.GONE);
                            else
                                textViewEmail.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void afterTextChanged(Editable s) { }
                    });
                }
                break;
            case R.id.edit_text_password:
                if(hasFocus){
                    editTextPassword.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                            if (editTextPassword.getText().toString().isEmpty())
                                textViewPassword.setVisibility(View.GONE);
                            else
                                textViewPassword.setVisibility(View.VISIBLE);
                        }
                        @Override
                        public void afterTextChanged(Editable s) { }
                    });
                }
                break;
        }
    }
}
