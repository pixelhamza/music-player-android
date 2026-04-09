package com.example.musicplayer;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.musicplayer.data.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AuthActivity extends AppCompatActivity {
    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;
    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;
    private CheckBox checkRemember;
    private CheckBox checkTerms;
    private MaterialButton btnSubmit;
    private TextView tvModeTitle;
    private TextView tvSwitchMode;

    private boolean signUpMode = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        sessionManager = new SessionManager(this);
        bindViews();
        setupInitialState();
        setupListeners();
    }

    private void bindViews() {
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        checkRemember = findViewById(R.id.checkRemember);
        checkTerms = findViewById(R.id.checkTerms);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvModeTitle = findViewById(R.id.tvModeTitle);
        tvSwitchMode = findViewById(R.id.tvSwitchMode);
    }

    private void setupInitialState() {
        etEmail.setText(sessionManager.getRememberedEmail());
        applyMode(false);
    }

    private void setupListeners() {
        tvSwitchMode.setOnClickListener(v -> applyMode(!signUpMode));
        btnSubmit.setOnClickListener(v -> {
            clearErrors();
            if (signUpMode) {
                handleSignUp();
            } else {
                handleLogin();
            }
        });
    }

    private void applyMode(boolean enableSignUp) {
        signUpMode = enableSignUp;
        tilName.setVisibility(enableSignUp ? View.VISIBLE : View.GONE);
        tilConfirmPassword.setVisibility(enableSignUp ? View.VISIBLE : View.GONE);
        checkTerms.setVisibility(enableSignUp ? View.VISIBLE : View.GONE);
        tvModeTitle.setText(enableSignUp ? R.string.create_account : R.string.welcome_back);
        btnSubmit.setText(enableSignUp ? R.string.sign_up : R.string.login);
        tvSwitchMode.setText(enableSignUp ? R.string.switch_to_login : R.string.switch_to_signup);
    }

    private void handleSignUp() {
        String name = getText(etName);
        String email = getText(etEmail);
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        boolean valid = true;
        if (TextUtils.isEmpty(name)) {
            tilName.setError("Enter your name");
            valid = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            valid = false;
        }
        if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            valid = false;
        }
        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            valid = false;
        }
        if (!checkTerms.isChecked()) {
            Toast.makeText(this, "Please accept the demo terms to continue.", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (!valid) {
            return;
        }

        sessionManager.registerUser(name, email, password, checkRemember.isChecked());
        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
        openHome();
    }

    private void handleLogin() {
        String email = getText(etEmail);
        String password = getText(etPassword);

        if (!sessionManager.hasRegisteredUser()) {
            Toast.makeText(this, "Create an account first using Sign up.", Toast.LENGTH_SHORT).show();
            applyMode(true);
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email");
            return;
        }
        if (password.length() < 6) {
            tilPassword.setError("Enter the saved password");
            return;
        }
        if (sessionManager.login(email, password, checkRemember.isChecked())) {
            Toast.makeText(this, "Logged in successfully", Toast.LENGTH_SHORT).show();
            openHome();
        } else {
            Toast.makeText(this, "Email or password did not match.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void clearErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}
