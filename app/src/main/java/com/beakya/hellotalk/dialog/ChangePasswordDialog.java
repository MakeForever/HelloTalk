package com.beakya.hellotalk.dialog;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.retrofit.ChangePasswordBody;
import com.beakya.hellotalk.retrofit.GeneralResponseBody;
import com.beakya.hellotalk.retrofit.LoginResponseBody;
import com.beakya.hellotalk.retrofit.UserServices;
import com.beakya.hellotalk.utils.Utils;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.beakya.hellotalk.retrofit.RetrofitCreator.retrofit;

/**
 * Created by goodlife on 2017. 7. 16..
 */

public class ChangePasswordDialog extends AlertDialog {
    private static String TAG = ChangePasswordDialog.class.getSimpleName();
    private TextInputEditText currentPasswordEditText;
    private TextInputEditText newPasswordEditText;
    private TextInputEditText newPasswordConfirmEditText;
    private TextInputLayout currentPasswordLayout;
    private TextInputLayout newPasswordLayout;
    private TextInputLayout newPasswordConfirmLayout;
    private ProgressBar progressBar;
    private Button cancelButton;
    private Button startButton;
    private Context mContext;
    public ChangePasswordDialog(@NonNull Context context) {
        super(context);
        mContext = context;
        //dialog 넓이 설정
        WindowManager.LayoutParams params = this.getWindow().getAttributes();
        params.width = ActionBar.LayoutParams.MATCH_PARENT;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_change_password);
        this.setCancelable(true);
        currentPasswordEditText = (TextInputEditText) findViewById(R.id.current_password_text_input_edit_text);
        currentPasswordLayout = (TextInputLayout) findViewById(R.id.current_password_text_input_layout);
        newPasswordEditText = (TextInputEditText) findViewById(R.id.new_password_text_input_edit_text);
        newPasswordLayout = (TextInputLayout) findViewById(R.id.new_password_text_input_layout);
        newPasswordConfirmEditText = (TextInputEditText) findViewById(R.id.new_password_confirm_text_input_edit_text);
        newPasswordConfirmLayout = (TextInputLayout) findViewById(R.id.new_password_confirm_text_input_layout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        startButton = (Button) findViewById(R.id.start_button);

        addTextWatcher(currentPasswordEditText, currentPasswordLayout);
        addTextWatcher(newPasswordEditText, newPasswordLayout);
        newPasswordConfirmEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if ( !s.toString().equals(newPasswordEditText.getText().toString()) ) {
                    newPasswordConfirmLayout.setErrorEnabled(true);
                    newPasswordConfirmLayout.setError(mContext.getString(R.string.text_for_chagne_password_validate_not_matched));
                } else if  ( currentPasswordEditText.getText().toString().equals(newPasswordEditText.getText().toString()) ) {
                    newPasswordConfirmLayout.setErrorEnabled(true);
                    newPasswordConfirmLayout.setError("현재 비밀번호와 새로운 비밀번호가 같습니다");
                } else {
                    newPasswordConfirmLayout.setErrorEnabled(false);
                    newPasswordConfirmLayout.setError(null);
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( isTextSatisfy() ) {
                    //TODO : network 처리
                    String token = Utils.getToken(getContext());
                    ChangePasswordBody body = new ChangePasswordBody(
                            Utils.getMyInfo(getContext()).getId(),
                            currentPasswordEditText.getText().toString(),
                            newPasswordEditText.getText().toString()
                    );
                    UserServices userServices = retrofit.create(UserServices.class);
                    Call<GeneralResponseBody> call = userServices.changePassword(token, body);
                    call.enqueue(new Callback<GeneralResponseBody>() {
                        @Override
                        public void onResponse(Call<GeneralResponseBody> call, Response<GeneralResponseBody> response) {
                            if (response.errorBody() != null ) {
                                GeneralResponseBody restError = null;
                                try {
                                    restError = (GeneralResponseBody)retrofit.responseBodyConverter( GeneralResponseBody.class, GeneralResponseBody.class.getAnnotations()).convert(response.errorBody());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if ( restError != null ) {
                                    Toast.makeText(mContext, restError.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(mContext, "비밀번호 변경이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(Call<GeneralResponseBody> call, Throwable t) {
                            Toast.makeText(mContext, "서버와 연결이 좋지 않습니다. 조금 뒤 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    //TODO : alert 처리
                    Toast.makeText(mContext, "hello world", Toast.LENGTH_SHORT).show();
                }
            }
        });
        initialize();
    }
    private boolean isTextSatisfy () {
        return (
                !currentPasswordLayout.isErrorEnabled() &&
                !newPasswordConfirmLayout.isErrorEnabled() &&
                !newPasswordLayout.isErrorEnabled()
        );


    }
    private void initialize() {
        currentPasswordLayout.setErrorEnabled(true);
        newPasswordLayout.setErrorEnabled(true);
        newPasswordConfirmLayout.setErrorEnabled(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void addTextWatcher (final TextInputEditText editText, final TextInputLayout layout ) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                if ( s.length() < 5 ) {
                    layout.setErrorEnabled(true);
                    layout.setError(mContext.getString(R.string.text_for_change_password_validate_too_short));
                } else {
                    layout.setErrorEnabled(false);
                    layout.setError(null);
                }
            }
        });
    }
}
