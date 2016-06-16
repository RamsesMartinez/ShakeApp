package mx.ramsesmartinez.shakeapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements OnClickListener, OnFocusChangeListener {

    private Bitmap bitmapImageProfile;
    private CallbackManager callbackManager;
    private Drawable drawableImageProfile;
    private DownloadImages downloadImages;
    private Profile facebookProfile;
    private RoundedBitmapDrawable roundedDrawable;
    private Toolbar toolbar;
    private LoginButton loginButtonFacebook;
    private Uri uriPhoto;

    private Button buttonLoggin;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private ImageView imageViewProfileLogin;
    private TextView textViewEmail;
    private TextView textViewPassword;

    private String strUrlPhoto;
    private String strName;
    private String strFirstName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        callbackManager = CallbackManager.Factory.create();
        drawableImageProfile = ResourcesCompat.getDrawable(getResources(), R.drawable.img_boy, null);
//        drawableImageProfile= getResources().getDrawable(R.drawable.img_boy);
        bitmapImageProfile = ((BitmapDrawable) drawableImageProfile).getBitmap();

        //Creates the rounded drawable
        roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmapImageProfile);
        roundedDrawable.setCornerRadius(bitmapImageProfile.getHeight());

        buttonLoggin = (Button) findViewById(R.id.button_login);
        editTextEmail = (EditText) findViewById(R.id.edit_text_email);
        editTextPassword = (EditText) findViewById(R.id.edit_text_password);
        imageViewProfileLogin = (ImageView) findViewById(R.id.image_view_login_profile);
        textViewEmail  = (TextView) findViewById(R.id.text_view_email);
        textViewPassword = (TextView) findViewById(R.id.text_view_password);

        buttonLoggin.setOnClickListener(this);
        editTextEmail.setOnFocusChangeListener(this);
        editTextPassword.setOnFocusChangeListener(this);
        imageViewProfileLogin.setImageDrawable(roundedDrawable);
//        validateLogged();
//        intentShakeActivity();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        validateLogged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {

        switch(v.getId()) {
            case R.id.button_login:
                if(validateEditTexts(v))    intentShakeActivity();
                else    Toast.makeText(getApplicationContext(),"Revisa tus credenciales",Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        /**
         * Shows the textViews of email and password if the EditTexts don't have anything
         */
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

    private void validateLogged(){
        if (AccessToken.getCurrentAccessToken() != null && com.facebook.Profile.getCurrentProfile() != null) {
            facebookProfile = com.facebook.Profile.getCurrentProfile();
            strName = facebookProfile.getName().toString();
            strFirstName = facebookProfile.getFirstName().toString();
            uriPhoto = facebookProfile.getProfilePictureUri(200, 200);
            strUrlPhoto = uriPhoto.toString();

            // App code
            try {
                Thread.sleep(500);
                Toast.makeText(getApplicationContext(),"Hola "+ strFirstName + "!!!",Toast.LENGTH_SHORT).show();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            intentShakeActivity();
        }

        loginButtonFacebook = (LoginButton) findViewById(R.id.button_facebook_login);
        loginButtonFacebook.setReadPermissions("user_friends");
        loginButtonFacebook.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                try {
                    facebookProfile = com.facebook.Profile.getCurrentProfile();
                    strName = facebookProfile.getName().toString();
                    uriPhoto = facebookProfile.getProfilePictureUri(200, 200);
                    strUrlPhoto = uriPhoto.toString();
                    downloadImages = new DownloadImages();
                    downloadImages.execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                intentShakeActivity();
            }

            @Override
            public void onCancel() { }

            @Override
            public void onError(FacebookException error) { }
        });
    }

    private void intentShakeActivity() {
        Intent intent = new Intent(MainActivity.this, ShakeActivity.class);
        intent.putExtra("NAME", strName);
        intent.putExtra("PHOTO", strUrlPhoto);
        startActivity(intent);
        finish();
    }

    private boolean validateEditTexts(View v){
        boolean valido = false;
        String strEmail = editTextEmail.getText().toString();
        String strPassword = editTextPassword.getText().toString();

        if (strEmail.isEmpty() || strPassword.isEmpty()) {
            if (strEmail.isEmpty())
                editTextEmail.setError("Ingresa tu email");
            if (strPassword.isEmpty())
                editTextPassword.setError("Ingresa tu contraseña");
        }
        if (strEmail.equals("shake@gmail.com") && strPassword.equals("shake")) {
            valido = true;
        }
        return valido;
    }

    private class DownloadImages extends AsyncTask<Void,Void,Void>{
        Bitmap bitmap = null;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                bitmap = Picasso.with(MainActivity.this).load(strUrlPhoto).get();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            /** If the image download was completed succesfully */
            if (bitmap != null) {
                imageViewProfileLogin.setImageBitmap(bitmap);

                //Creates the rounded drawable
                roundedDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                roundedDrawable.setCornerRadius(bitmap.getHeight());
                imageViewProfileLogin.setImageDrawable(roundedDrawable);

            } else {
                Toast.makeText(getApplicationContext(),"Problemas al cargar la imagen",Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(aVoid);
        }

    }

}
