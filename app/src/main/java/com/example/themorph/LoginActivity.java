package com.example.themorph;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.themorph.DeafMap.MyApplication;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {
    // 控件声明
    private TextInputEditText etPhone, etPassword;
    private MaterialButton btnLogin;
    private TextView tvGoRegister;
    private UserDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 初始化控件
        initView();
        // 初始化数据库
        dbHelper = UserDBHelper.getInstance(this);
        // 点击事件
        initClick();
    }

    private void initView() {
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvGoRegister = findViewById(R.id.tv_go_register);
    }

    private void initClick() {
        // 登录按钮点击
        btnLogin.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // 输入校验
            if (!UserDBHelper.isPhoneValid(phone)) {
                Toast.makeText(this, "请输入正确的11位手机号", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                Toast.makeText(this, "密码不能少于6位", Toast.LENGTH_SHORT).show();
                return;
            }

            // 登录校验
            String userType = dbHelper.loginUser(phone, password);
            if (userType == null) {
                Toast.makeText(this, "手机号或密码错误", Toast.LENGTH_SHORT).show();
            } else {
                // ===================== 在这里加 2 行 =====================
                // 保存当前登录的手机号（全局可用）
                MyApplication.loginPhone = phone;

                // 登录成功,跳转到主界面
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("user_type", userType);
                startActivity(intent);
                finish();
            }
        });

        // 跳转到注册页
        tvGoRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}