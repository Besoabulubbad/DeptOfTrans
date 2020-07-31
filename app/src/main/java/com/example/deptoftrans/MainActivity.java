package com.example.deptoftrans;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;

public class MainActivity extends AppCompatActivity {
    CircularProgressButton loginBtn;
    EditText emailEdt,passwordEdt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        loginBtn=findViewById(R.id.login_btn);
        emailEdt=findViewById(R.id.email_edt);
        passwordEdt=findViewById(R.id.password_edt);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginBtn.startAnimation();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                       Toast.makeText(getApplicationContext(),"Login succeed",Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this,Map_Activity.class));
                    }
                }, 2000);

            }
        });

    }
}