package com.beakya.hellotalk.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.retrofit.LoginResponseBody;
import com.beakya.hellotalk.retrofit.LoginRequestBody;
import com.beakya.hellotalk.retrofit.LoginService;
import com.beakya.hellotalk.services.SocketService;
import com.beakya.hellotalk.utils.SocketTask;
import com.beakya.hellotalk.utils.Utils;

import java.io.IOException;
import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.beakya.hellotalk.retrofit.RetrofitCreator.retrofit;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private AppCompatButton loginButton;
    private TextView signUpTextView;
    private ConstraintLayout login_area;
    private ProgressBar login_progressBar;
    public static int RESULT_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        emailEditText = (TextInputEditText) findViewById(R.id.input_email);
        passwordEditText = (TextInputEditText) findViewById(R.id.input_password);
        loginButton = (AppCompatButton) findViewById(R.id.btn_login);
        signUpTextView = (TextView) findViewById(R.id.link_signup);
        login_area = (ConstraintLayout) findViewById(R.id.login_area);
        login_progressBar = (ProgressBar) findViewById(R.id.login_progressBar);


        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent RegisterIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(RegisterIntent, RESULT_CODE);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == RESULT_CODE) {
            if (data.hasExtra("result")) {
                showLocationDialog();
            }
        }
    }
    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle(getString(R.string.login_dialog_title));
        builder.setMessage(getString(R.string.login_dialog_content));

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // positive button logic
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }
    private void login() {
        if(!validate())
            return;

        LoginRequestBody body = new LoginRequestBody();
        body.setName(emailEditText.getText().toString());
        body.setPassword(passwordEditText.getText().toString());
        loginRetrofit(body);

    }

    private boolean validate() {
        boolean valid = true;

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if( email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.setError("enter a valid email address");
            valid = false;

        } else {
            emailEditText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 ) {
            passwordEditText.setError("more then 4 alphanumeric characters");
            valid = false;
        } else {
            passwordEditText.setError(null);
        }


        return valid;
    }

    private void loginInvisible() {
        login_progressBar.setVisibility(View.VISIBLE);
        login_area.setVisibility(View.INVISIBLE);
    }

    private void loginVisible() {
        login_progressBar.setVisibility(View.INVISIBLE);
        login_area.setVisibility(View.VISIBLE);
    }


    private void loginRetrofit( LoginRequestBody body ) {

        loginInvisible();

        LoginService loginService = retrofit.create(LoginService.class);
        Call<LoginResponseBody> call = loginService.repoTest(body);
        call.enqueue(new Callback<LoginResponseBody>() {
            @Override
            public void onResponse(Call<LoginResponseBody> call, Response<LoginResponseBody> response) {

                if ( response.isSuccessful() ) {
                    String id = emailEditText.getText().toString();
                    LoginResponseBody body = response.body();
                    int login = body.getLogin();
                    boolean isFirstLogin = false;
                    if( login == 0) {
                        isFirstLogin = true;
                    }
                    if ( body.getImg() != null ) {
                        String fileName =  getString(R.string.setting_friends_profile_img_name);
                        String extension = getString(R.string.setting_profile_img_extension);
                        String directory = getString(R.string.setting_friends_img_directory);
                        Context context = LoginActivity.this;
                        Bitmap mBitmap = Utils.decodeImgStringBase64(body.getImg());
                        Utils.saveToInternalStorage(context, mBitmap, fileName, extension, Arrays.asList(new String[] { directory, id }));
                    }
                    SharedPreferences tokenStorage = getSharedPreferences(getString(R.string.my_info), MODE_PRIVATE);
                    SharedPreferences.Editor editor = tokenStorage.edit();
                    editor.putString(getString(R.string.token), body.getToken());
                    editor.putString(getString(R.string.user_id), id);
                    editor.putString(getString(R.string.user_name), body.getName());
                    editor.putBoolean(getString(R.string.user_img_boolean), !isFirstLogin);
                    editor.commit();
                    Intent socketIntent = new Intent(LoginActivity.this, SocketService.class);
                    socketIntent.setAction(SocketTask.ACTION_SOCKET_CREATE);
                    startService(socketIntent);



                    Log.d(TAG, "is firstLogin: " + response.body().getLogin());
                    Intent mainIntent;
                    if( isFirstLogin ) {
                        mainIntent = new Intent(LoginActivity.this, ImageSelectionActivity.class);
                    } else {
                        mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                    }

                    startActivity(mainIntent);
                    finish();
                }
                if( !response.isSuccessful() && response.errorBody() != null ) {
                    loginVisible();
                    LoginResponseBody restError = null;
                    try {
                        restError = (LoginResponseBody) retrofit.responseBodyConverter(
                                LoginResponseBody.class, LoginResponseBody.class.getAnnotations()).convert(response.errorBody());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if( restError != null ) {
                        Snackbar snackbar = Snackbar.make(login_area, restError.getMessage(), Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }

            }

            @Override
            public void onFailure(Call<LoginResponseBody> call, Throwable t) {
                loginVisible();
                Log.d("Error", t.getMessage());
                Snackbar snackbar = Snackbar.make(login_area, getString(R.string.error_connection_timeout), Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
