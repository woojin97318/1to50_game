package com.wujin.oneToFifty;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    Intent gameIntent = null;   // 게임 화면
    Intent helpIntent = null;   // 도움말 화면
    private Vibrator vibrator;  // 바이브레이터 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameIntent = new Intent(this, GameActivity.class);
        helpIntent = new Intent(this, HelpActivity.class);
    }

    public void gameStart(View v) {
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(50);
        startActivity(gameIntent);
    }

    public void help(View v) {
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(50);
        startActivity(helpIntent);
    }
}
