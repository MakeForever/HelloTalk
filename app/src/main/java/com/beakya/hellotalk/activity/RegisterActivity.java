package com.beakya.hellotalk.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.retrofit.GeneralResponseBody;
import com.beakya.hellotalk.retrofit.RegisterRequestBody;
import com.beakya.hellotalk.retrofit.RegisterService;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;

import static com.beakya.hellotalk.retrofit.RetrofitCreator.retrofit;

public class RegisterActivity extends AppCompatActivity {
    public static final String TAG = RegisterActivity.class.getSimpleName();
    private TextInputEditText mEmailEditText;
    private TextInputEditText mNameEditText;
    private TextInputEditText mPasswordEditText;
    private AppCompatButton mSignUpButton;
    private ConstraintLayout sign_up_root_layout;
    private RadioGroup genderRadioGroup;
    private RadioButton femaleRadioButton;
    private RadioButton maleRadioButton;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mEmailEditText = (TextInputEditText) findViewById(R.id.sign_up_input_email);
        mNameEditText = (TextInputEditText) findViewById(R.id.sign_up_input_name);
        mPasswordEditText = (TextInputEditText) findViewById(R.id.sign_up_input_password);
        mSignUpButton = (AppCompatButton) findViewById(R.id.sign_up_btn);
        sign_up_root_layout = (ConstraintLayout) findViewById(R.id.sign_up_root_layout);
        genderRadioGroup = (RadioGroup) findViewById(R.id.sign_up_radioGroup);
        femaleRadioButton = (RadioButton) findViewById(R.id.sign_up_rb_female);
        maleRadioButton = (RadioButton) findViewById(R.id.sign_up_rb_male);
        progressBar = (ProgressBar) findViewById(R.id.sign_up_progressBar);
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }
    private void onLoading(){
        progressBar.setVisibility(View.VISIBLE);
        sign_up_root_layout.setVisibility(View.INVISIBLE);

    }
    private void onFinished() {
        progressBar.setVisibility(View.INVISIBLE);
        sign_up_root_layout.setVisibility(View.VISIBLE);
    }
    private void registerUser() {
        if(!validate())
            return;
        int gender = 0;
        if( femaleRadioButton.isChecked() )
            gender = 1;

        RegisterTask task = new RegisterTask();
        RegisterRequestBody body = new RegisterRequestBody();
        body.setName(mNameEditText.getText().toString());
        body.setEmail(mEmailEditText.getText().toString());
        body.setGender(gender);
        body.setPassword(mPasswordEditText.getText().toString());
        task.execute(body);
    }
    private boolean validate() {
        boolean valid = true;

        String email = mEmailEditText.getText().toString();
        String name = mNameEditText.getText().toString();
        String password = mPasswordEditText.getText().toString();


        if( email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mEmailEditText.setError("enter a valid email address");
            valid = false;
            return valid;
        } else {
            mEmailEditText.setError(null);
        }

        if(name.isEmpty() || name.length() < 2 ) {
            mNameEditText.setError("name is empty!");
            valid = false;
            return valid;
        } else {
            mNameEditText.setError(null);
        }

        if( password.isEmpty() || password.length() < 4 ) {
            mPasswordEditText.setError("enter your password");
            valid = false;
            return valid;
        } else {
            mPasswordEditText.setError(null);
        }
        if( genderRadioGroup.getCheckedRadioButtonId() <= 0 ) {
            femaleRadioButton.setError("  ");
            valid = false;
            return valid;
        } else {
            femaleRadioButton.setError(null);
        }
        return valid;
    }

    private class RegisterTask extends AsyncTask<RegisterRequestBody, Void, Response<GeneralResponseBody>>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onLoading();
        }

        @Override
        protected Response<GeneralResponseBody> doInBackground(RegisterRequestBody... params) {
            RegisterRequestBody body = params[0];
            RegisterService registerService = retrofit.create(RegisterService.class);
            Call<GeneralResponseBody> call = registerService.register(body);
            Response<GeneralResponseBody> result = null;
            try {
                result = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Response<GeneralResponseBody> value) {
            super.onPostExecute(value);
            onFinished();
            GeneralResponseBody errorBody = null;
            if( value != null && !value.isSuccessful() && value.errorBody() != null) {
                try {
                    errorBody = (GeneralResponseBody) retrofit.responseBodyConverter(
                            GeneralResponseBody.class, GeneralResponseBody.class.getAnnotations()).convert(value.errorBody());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if( errorBody != null ) {
                    Snackbar snackbar = Snackbar.make(sign_up_root_layout, errorBody.getMessage(), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            } else {
                Intent mainIntent = new Intent();
                mainIntent.putExtra("result", 1);
                setResult(RESULT_OK, mainIntent);
                finish();
            }
        }
    }
}
