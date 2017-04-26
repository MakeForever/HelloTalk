package com.beakya.hellotalk.activity;

import android.app.Application;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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

import com.beakya.hellotalk.MyApp;
import com.beakya.hellotalk.R;
import com.beakya.hellotalk.database.TalkContract;
import com.beakya.hellotalk.events.UserInfoEvent;
import com.beakya.hellotalk.objs.Friend;
import com.beakya.hellotalk.retrofit.LoginResponseBody;
import com.beakya.hellotalk.retrofit.LoginRequestBody;
import com.beakya.hellotalk.retrofit.LoginService;
import com.beakya.hellotalk.services.SocketService;
import com.beakya.hellotalk.utils.SocketTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.net.URISyntaxException;

import retrofit2.Call;
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
                startActivity(RegisterIntent);
                finish();
            }
        });
        Intent intent = getIntent();
        int resultInt = intent.getIntExtra("result", 0);
        if(resultInt == 1) {
            showLocationDialog();
            intent.putExtra("result", 0);
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

        LoginAsyncTask loginAsyncTask = new LoginAsyncTask();
        LoginRequestBody body = new LoginRequestBody();
        body.setName(emailEditText.getText().toString());
        body.setPassword(passwordEditText.getText().toString());


        loginAsyncTask.execute(body);

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

    private class LoginAsyncTask extends AsyncTask<LoginRequestBody, Void, Response<LoginResponseBody>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loginInvisible();
        }

        @Override
        protected Response<LoginResponseBody> doInBackground(LoginRequestBody... params) {
            LoginRequestBody body = params[0];
            LoginService loginService = retrofit.create(LoginService.class);
            Call<LoginResponseBody> call = loginService.repoTest(body);
            Response<LoginResponseBody> result = null;
            try {
                result = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute( Response<LoginResponseBody> result ) {
            super.onPostExecute(result);
            loginVisible();

            if( result != null && !result.isSuccessful() && result.errorBody() != null ) {
                LoginResponseBody restError = null;
                try {
                    restError = (LoginResponseBody) retrofit.responseBodyConverter(
                            LoginResponseBody.class, LoginResponseBody.class.getAnnotations()).convert(result.errorBody());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if( restError != null ) {
                    Snackbar snackbar = Snackbar.make(login_area, restError.getMessage(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            } else {
                //TODO : 회원정보 저장 할것

                int login = result.body().getLogin();
                boolean isFirstLogin = false;
                if( login == 0) {
                    isFirstLogin = true;
                }
                SharedPreferences tokenStorage = getSharedPreferences(getString(R.string.user_info), MODE_PRIVATE);
                SharedPreferences.Editor editor = tokenStorage.edit();
                editor.putString(getString(R.string.token), result.body().getToken());
                editor.putString(getString(R.string.user_id), emailEditText.getText().toString());
                editor.putBoolean(getString(R.string.user_img_boolean), isFirstLogin);
                editor.commit();
//                Intent intent = new Intent(LoginActivity.this, SocketService.class);
//                intent.setAction(SocketTask.ACTION_SOCKET_CREATE);
//                startService(intent);

                Intent mainIntent;
                Log.d(TAG, "onPostExecute: " + result.body().getLogin());

                isFirstLogin = true;

                if( isFirstLogin ) {
                    mainIntent = new Intent(LoginActivity.this, ImageSelectionActivity.class);
                } else {
                    mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                }
                startActivity(mainIntent);
                finish();
            }
        }

        @Override
        protected void onCancelled(Response<LoginResponseBody> contributor) {
            super.onCancelled(contributor);
            loginVisible();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            loginVisible();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
