package otf.project.otf.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import org.greenrobot.eventbus.EventBus;

import otf.project.otf.R;
import otf.project.otf.fragments.base.BaseFragment;
import otf.project.otf.messages.VerifyPhoneNumberMessage;

/**
 * Created by denismalcev on 04.06.17.
 */

public class VerificationPhoneFragment extends BaseFragment implements View.OnClickListener {

    private EditText phoneNumberField;
    private EditText userNameField;
    private ImageButton verifyButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_phone_verification, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        phoneNumberField = (EditText) view.findViewById(R.id.phone_number_field);
        phoneNumberField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                verifyButton.setEnabled(
                        phoneNumberField.getText().toString().length() > 0
                                &&
                        userNameField.getText().toString().length() > 0);
            }
        });

        userNameField = (EditText) view.findViewById(R.id.user_name_field);
        userNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                verifyButton.setEnabled(
//                        phoneNumberField.getText().toString().length() > 0
//                                &&
                        userNameField.getText().toString().length() > 0);
            }
        });

        verifyButton = (ImageButton) view.findViewById(R.id.verification_button);
        verifyButton.setEnabled(false);
        verifyButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == verifyButton) {
            String phoneNumber = phoneNumberField.getText().toString();
            String name = userNameField.getText().toString();
            EventBus.getDefault().post(new VerifyPhoneNumberMessage(phoneNumber, name));
        }
    }
}
