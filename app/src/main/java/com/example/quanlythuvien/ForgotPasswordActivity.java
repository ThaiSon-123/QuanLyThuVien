package com.example.quanlythuvien;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quanlythuvien.db.UserDao;
import com.example.quanlythuvien.util.EmailSender;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private UserDao userDao;

    private LinearLayout layoutStep1, layoutStep2, layoutStep3;
    private TextView step1Dot, step2Dot, step3Dot;

    private EditText edtEmail;
    private TextView tvStep1Error, btnSendPin;
    private ProgressBar progressSend;

    private EditText edtPin;
    private TextView tvEmailHint, tvCountdown, tvStep2Error, btnVerifyPin, btnResend;
    private CountDownTimer countDownTimer;

    private EditText edtNewPassword, edtConfirmPassword;
    private TextView tvStep3Error, btnResetPassword;

    private String verifiedUsername;
    private String verifiedEmail;
    private String generatedPin;
    private long pinExpiryTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        userDao = new UserDao(this);
        bindViews();
        setupActions();
    }

    private void bindViews() {
        layoutStep1 = findViewById(R.id.layoutStep1);
        layoutStep2 = findViewById(R.id.layoutStep2);
        layoutStep3 = findViewById(R.id.layoutStep3);
        step1Dot    = findViewById(R.id.step1Dot);
        step2Dot    = findViewById(R.id.step2Dot);
        step3Dot    = findViewById(R.id.step3Dot);

        edtEmail     = findViewById(R.id.edtEmail);
        tvStep1Error = findViewById(R.id.tvStep1Error);
        btnSendPin   = findViewById(R.id.btnSendPin);
        progressSend = findViewById(R.id.progressSend);

        edtPin       = findViewById(R.id.edtPin);
        tvEmailHint  = findViewById(R.id.tvEmailHint);
        tvCountdown  = findViewById(R.id.tvCountdown);
        tvStep2Error = findViewById(R.id.tvStep2Error);
        btnVerifyPin = findViewById(R.id.btnVerifyPin);
        btnResend    = findViewById(R.id.btnResend);

        edtNewPassword     = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        tvStep3Error       = findViewById(R.id.tvStep3Error);
        btnResetPassword   = findViewById(R.id.btnResetPassword);
    }

    private void setupActions() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        btnSendPin.setOnClickListener(v -> onSendPin());
        btnVerifyPin.setOnClickListener(v -> onVerifyPin());
        btnResend.setOnClickListener(v -> onResend());
        btnResetPassword.setOnClickListener(v -> onResetPassword());
    }


    private void onSendPin() {
        String email = edtEmail.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            showError(tvStep1Error, "Vui lòng nhập email");
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(tvStep1Error, "Email không hợp lệ");
            return;
        }

        String username = userDao.findUsernameByEmail(email);
        if (username == null) {
            showError(tvStep1Error, "Email không tồn tại trong hệ thống");
            return;
        }

        hideError(tvStep1Error);
        verifiedUsername = username;
        verifiedEmail = email;
        sendPinToEmail(email);
    }

    private void sendPinToEmail(String email) {
        btnSendPin.setEnabled(false);
        progressSend.setVisibility(View.VISIBLE);

        generatedPin  = String.valueOf(100000 + new Random().nextInt(900000));
        pinExpiryTime = System.currentTimeMillis() + 5 * 60 * 1000;

        EmailSender.sendPin(email, generatedPin, new EmailSender.Callback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    progressSend.setVisibility(View.GONE);
                    btnSendPin.setEnabled(true);
                    goToStep2(email);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    progressSend.setVisibility(View.GONE);
                    btnSendPin.setEnabled(true);
                    showError(tvStep1Error, "Gửi email thất bại. Kiểm tra kết nối mạng.");
                });
            }
        });
    }

    private void goToStep2(String email) {
        layoutStep1.setVisibility(View.GONE);
        layoutStep2.setVisibility(View.VISIBLE);
        markStep(2);

        String masked = maskEmail(email);
        tvEmailHint.setText("Mã PIN đã được gửi đến: " + masked);

        startCountdown();
    }

    private void startCountdown() {
        if (countDownTimer != null) countDownTimer.cancel();

        long remaining = pinExpiryTime - System.currentTimeMillis();
        countDownTimer = new CountDownTimer(remaining, 1000) {
            @Override
            public void onTick(long ms) {
                long mins = ms / 60000;
                long secs = (ms % 60000) / 1000;
                tvCountdown.setText(String.format("Mã hết hạn sau: %d:%02d", mins, secs));
            }

            @Override
            public void onFinish() {
                tvCountdown.setText("Mã đã hết hạn — bấm Gửi lại");
                btnVerifyPin.setEnabled(false);
            }
        }.start();
    }

    private void onVerifyPin() {
        String entered = edtPin.getText().toString().trim();
        if (TextUtils.isEmpty(entered)) {
            showError(tvStep2Error, "Vui lòng nhập mã PIN");
            return;
        }
        if (System.currentTimeMillis() > pinExpiryTime) {
            showError(tvStep2Error, "Mã PIN đã hết hạn. Vui lòng gửi lại.");
            return;
        }
        if (!entered.equals(generatedPin)) {
            showError(tvStep2Error, "Mã PIN không đúng");
            return;
        }

        if (countDownTimer != null) countDownTimer.cancel();
        hideError(tvStep2Error);
        layoutStep2.setVisibility(View.GONE);
        layoutStep3.setVisibility(View.VISIBLE);
        markStep(3);
    }

    private void onResend() {
        if (verifiedEmail != null) sendPinToEmail(verifiedEmail);
    }


    private void onResetPassword() {
        String newPass     = edtNewPassword.getText().toString();
        String confirmPass = edtConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(newPass)) {
            showError(tvStep3Error, "Vui lòng nhập mật khẩu mới");
            return;
        }
        if (newPass.length() < 3) {
            showError(tvStep3Error, "Mật khẩu phải có ít nhất 3 ký tự");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            showError(tvStep3Error, "Mật khẩu xác nhận không khớp");
            return;
        }

        boolean ok = userDao.resetPassword(verifiedUsername, newPass);
        if (ok) {
            Toast.makeText(this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_LONG).show();
            finish();
        } else {
            showError(tvStep3Error, "Đặt lại thất bại, thử lại sau");
        }
    }


    private void markStep(int step) {
        step1Dot.setBackgroundResource(step >= 1 ? R.drawable.bg_btn_primary : R.drawable.bg_phieu_card);
        step2Dot.setBackgroundResource(step >= 2 ? R.drawable.bg_btn_primary : R.drawable.bg_phieu_card);
        step3Dot.setBackgroundResource(step >= 3 ? R.drawable.bg_btn_primary : R.drawable.bg_phieu_card);
        step1Dot.setTextColor(step >= 1 ? 0xFFFFFFFF : 0xFFAAAAAA);
        step2Dot.setTextColor(step >= 2 ? 0xFFFFFFFF : 0xFFAAAAAA);
        step3Dot.setTextColor(step >= 3 ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    private String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) return email;
        String local  = email.substring(0, at);
        String domain = email.substring(at);
        String visible = local.substring(0, Math.min(3, local.length()));
        return visible + "***" + domain;
    }

    private void showError(TextView tv, String msg) {
        tv.setText(msg);
        tv.setVisibility(View.VISIBLE);
    }

    private void hideError(TextView tv) {
        tv.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
