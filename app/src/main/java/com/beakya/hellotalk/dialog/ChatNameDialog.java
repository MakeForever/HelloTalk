package com.beakya.hellotalk.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.beakya.hellotalk.R;
import com.beakya.hellotalk.activity.NewChatActivity;


/**
 * Created by goodlife on 2017. 6. 9..
 */

public class ChatNameDialog extends AlertDialog {
    private static final String TAG = ChatNameDialog.class.getSimpleName();
    private TextView chatNameCountView;
    private TextInputEditText chatNameEditText;
    private Button okButton;
    private Button cancelButton;
    private String storedName = null;
    private int keyCount;
    private Activity parentActivity;
    private NewChatActivity.DialogResultListener listener;
    private static final int NAME_LIMIT_COUNT = 20;
    private static final int BACK_SPACE_KEY_CODE = 67;
    public ChatNameDialog(@NonNull Context context) {
        super(context);
        parentActivity = (Activity) context;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_new_chat);
        keyCount = 0;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        chatNameEditText = (TextInputEditText) findViewById(R.id.chat_name_text_edit);
        chatNameCountView = (TextView) findViewById(R.id.chat_name_count_view);
        okButton = (Button) findViewById(R.id.dialog_ok_button);
        cancelButton = (Button) findViewById(R.id.dialog_cancel_button);
        chatNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if ( hasFocus ) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                } else {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }
            }
        });
        chatNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ( count == 1 && keyCount <= NAME_LIMIT_COUNT) {
                    keyCount++;
                } else if ( count == 0 && keyCount > 0 ) {
                   keyCount--;
                }
                chatNameCountView.setText(keyCount + "/" + NAME_LIMIT_COUNT);

            }
            @Override
            public void afterTextChanged(Editable s) {
                if ( keyCount > NAME_LIMIT_COUNT ) {
                    s.delete(NAME_LIMIT_COUNT, NAME_LIMIT_COUNT + 1);
                }
                storedName = s.toString();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onOKClick(storedName);
                onBackPressed();
            }
        });
    }
    public void setOnOkListener(NewChatActivity.DialogResultListener listener) {
        this.listener = listener;
    }
    public String getEnteredName() {
        return storedName;
    }
}
