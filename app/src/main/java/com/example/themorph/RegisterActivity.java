package com.example.themorph;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etPhone, etPwd, etPwdConfirm, etDisabilityCard;
    private MaterialButton btnRegister;
    private TextView tvGoLogin;
    private UserDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 初始化控件
        initView();
        // 初始化数据库
        dbHelper = UserDBHelper.getInstance(this);
        // 点击事件
        initClick();
    }

    private void initView() {
        etPhone = findViewById(R.id.et_register_phone);
        etPwd = findViewById(R.id.et_register_pwd);
        etPwdConfirm = findViewById(R.id.et_register_pwd_confirm);
        etDisabilityCard = findViewById(R.id.et_disability_card);
        btnRegister = findViewById(R.id.btn_register);
        tvGoLogin = findViewById(R.id.tv_go_login);
    }

    private void initClick() {
        // 注册按钮点击
        btnRegister.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String password = etPwd.getText().toString().trim();
            String pwdConfirm = etPwdConfirm.getText().toString().trim();
            String disabilityCard = etDisabilityCard.getText().toString().trim();

            // 输入校验
            if (!UserDBHelper.isPhoneValid(phone)) {
                Toast.makeText(this, "请输入正确的11位手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                Toast.makeText(this, "密码不能少于6位", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(pwdConfirm)) {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!UserDBHelper.isDisabilityCardValid(disabilityCard)) {
                Toast.makeText(this, "残疾人证号必须为20位数字", Toast.LENGTH_SHORT).show();
                return;
            }

            // 执行注册
            long result = dbHelper.registerUser(phone, password, disabilityCard);
            if (result == -1) {
                Toast.makeText(this, "该手机号已注册", Toast.LENGTH_SHORT).show();
            } else if (result > 0) {
                // 注册成功
                String userType = disabilityCard.length() == 20 ? "听障用户" : "健听用户";
                Toast.makeText(this, "注册成功! 您的账号类型为:" + userType, Toast.LENGTH_LONG).show();
                // 跳转到登录页
                finish();
            } else {
                Toast.makeText(this, "注册失败,请重试", Toast.LENGTH_SHORT).show();
            }
        });

        // 跳转到登录页
        tvGoLogin.setOnClickListener(v -> finish());
    }
}