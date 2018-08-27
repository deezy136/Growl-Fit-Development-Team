package otf.project.otf.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import org.greenrobot.eventbus.EventBus;

import otf.project.otf.R;
import otf.project.otf.fragments.base.BaseFragment;
import otf.project.otf.messages.VerifyCodeMessage;

/**
 * Created by denismalcev on 04.06.17.
 */

public class VerificationCodeFragment extends BaseFragment implements View.OnClickListener {

    private EditText codeField;
    private ImageButton verifyButton;
    private Button changePhoneNumberButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_code_verification, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        codeField = (EditText) view.findViewById(R.id.code_field);
        codeField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                verifyButton.setEnabled(codeField.getText().toString().length() > 0);
            }
        });
        verifyButton = (ImageButton) view.findViewById(R.id.verification_button);
        verifyButton.setEnabled(false);
        verifyButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == verifyButton) {
            String code = codeField.getText().toString();
            EventBus.getDefault().post(new VerifyCodeMessage(code));
        } else if (v == changePhoneNumberButton) {

        }
    }
}
