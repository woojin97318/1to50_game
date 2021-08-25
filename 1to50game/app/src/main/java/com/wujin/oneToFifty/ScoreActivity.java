package com.wujin.oneToFifty;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        Intent secondIntent = getIntent();
        String i = secondIntent.getStringExtra("클리어시간");    // 클리어시간을 GameActivity에서 가져와
        ((TextView) findViewById(R.id.rankTime)).setText(i);           // TextView에 출력
    }
}
