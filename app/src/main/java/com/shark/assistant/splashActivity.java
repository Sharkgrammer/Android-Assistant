package com.shark.assistant;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class splashActivity extends AppCompatActivity {

    private ConstraintLayout layMain, layFirst, laySecond;
    private ImageView btnNext, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        layFirst = findViewById(R.id.splashFirst);
        laySecond = findViewById(R.id.splashSecond);
        layMain = findViewById(R.id.splashMain);

        btnNext = findViewById(R.id.btnSplashNext);
        btnBack = findViewById(R.id.btnSplashBack);
        btnBack.setVisibility(View.INVISIBLE);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnNext.setImageDrawable(getDrawable(R.drawable.next));
                btnBack.setVisibility(View.VISIBLE);
                if (layMain.getVisibility() == View.VISIBLE){
                    change(layFirst, layMain);
                }else if (layFirst.getVisibility() == View.VISIBLE){
                    change(laySecond, layFirst);
                    btnNext.setImageDrawable(getDrawable(R.drawable.tick));
                }else{
                    finish();
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnNext.setImageDrawable(getDrawable(R.drawable.next));
                btnBack.setVisibility(View.VISIBLE);
                if (layFirst.getVisibility() == View.VISIBLE){
                    btnBack.setVisibility(View.INVISIBLE);
                    change(layMain, layFirst);
                }else if (laySecond.getVisibility() == View.VISIBLE){
                    change(layFirst, laySecond);
                }
            }
        });
    }

    private void change(ConstraintLayout to, ConstraintLayout from){
        to.setVisibility(View.VISIBLE);
        from.setVisibility(View.INVISIBLE);
    }
}
