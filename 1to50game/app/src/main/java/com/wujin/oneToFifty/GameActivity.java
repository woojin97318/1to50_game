package com.wujin.oneToFifty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Random;

public class GameActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    Button button[] = new Button[25]; // 숫자버튼 1 ~ 25
    int num[] = new int[50]; // 버튼에 입력해줄 숫자배열
    int step = 0; // 1 ~ 50까지 순서 제어, 0으로 해준 이유는 게임 도중 재시작 할 시 스레드의 인터럽트를 위해
    String clearTime; // 게임 클리어 시간을 저장하기 위한 변수

    private Thread thread = null; // 게임 시작을 할 때마다 초기화해주기위해 일단 null로 초기화
    SQLiteDatabase db; // SQLite DB를 사용
    TextToSpeech tts; // 게임 클리어를 알리는 음성합성
    private Vibrator vibrator; // 버튼 클릭시 진동
    Intent scoreIntent = null; // scoreActivity로 가기 위한 인텐트 초기화

    TextView time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        for (int i = 0; i < 25; i++) {
            button[i] = (Button)findViewById(R.id.b1+i);    // 1~25 버튼 초기화
            // 버튼 Invisible이유: Game Start를 누르기 전에 숫자가 나타나지 않은 25개의 버튼을 클릭할 때
            // 에러(강제종료)가 발생하여
            // 먼저 버튼을 숨긴 후에 게임을 시작하는 즉시 버튼을 보여주며 숫자도 같이 초기화해준다.
            button[i].setVisibility(View.INVISIBLE);
        }

        dbHelper helper = new dbHelper(this);
        db = helper.getWritableDatabase();

        scoreIntent = new Intent(this, ScoreActivity.class); // 명시적인텐트를 위한 객체 생성
        tts = new TextToSpeech(this, this); // 음성 합성에 필요한 TTS 객체 생성
    }

    // 게임시작 버튼 메소드
    public void gameStart(View v) {
        // Game Start버튼 클릭시 숫자들이 초기화되어있는 버튼을 Visible한다.
        for (int i = 0; i < 25; i++)
            button[i].setVisibility(View.VISIBLE);

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);    // 게임시작 버튼을 누를 시
        vibrator.vibrate(50);                                   // 0.05초 진동

        if (step > 0 && step < 51) {    // 처음 게임 시작시 step는 0으로 진입하여 해당이 안된다.
            thread.interrupt();         // 하지만 게임시작 후 step는 1로 초기화 되어 게임 도중 재시작을 했을 때
        }                               // 타이머()의 중복실행을 막기 위해 스레드를 인터럽트 시킨 후 다시 재 시작한다.
        step = 1;

        for (int i = 0; i < 50; i++)    // 1 ~ 50 숫자
            num[i] = i + 1;             // 배열에 초기화

        shakeNumber(); // 배열에 있는 숫자를 섞어준다.

        for (int i = 0; i < 25; i++)        // 1 ~ 25 버튼에
            button[i].setText("" + num[i]); // num[0~24] 숫자를 입력

        time = (TextView)findViewById(R.id.time);   // 게임 재시작시
        time.setText("00:00:00");                   // 타이머 초기화

        thread = new Thread(new timeThread());  // 게임을 시작할때마다
        thread.start();                         // Thread객체를 새로 만들어 시작

        for (int i = 0; i < 25; i++) {                      // 게임 클리어 이후에
            button[i] = (Button)findViewById(R.id.b1+i);    // 게임을 다시 시작할 때
            button[i].setVisibility(View.VISIBLE);          // 없어졌던 버튼들을 다시 보여준다.
        }
    }

    // 숫자를 뒤섞는 메소드
    public void shakeNumber() {
        Random r = new Random();
        int x = 0, y = 0, t = 0;
        for (int i = 0; i < 100; i++) {
            x = r.nextInt(25); // 두개의 버튼을 선택해서
            y = r.nextInt(25);
            t = num[x];         // 두 버튼의 값을
            num[x] = num[y];    // 서로 변경한다.
            num[y] = t;
        }
        for (int i = 0; i < 100; i++) {
            x = r.nextInt(25)+25; // 두개의 버튼을 선택해서
            y = r.nextInt(25)+25;
            t = num[x];         // 두 버튼의 값을
            num[x] = num[y];    // 서로 변경한다.
            num[y] = t;
        }
    }

    // 게임안 숫자 클릭 메소드
    public void onClick(View v) {
        Button b = (Button)v;
        int n = Integer.parseInt(b.getText().toString());   // 클릭한 버튼 번호 확인

        // 순서에 맞는 버튼 클릭시
        if (n == step) {
            vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(50);   // 0.05초 진동 1번

            if (step >= 26) {                       // 26번 이상 버튼 클릭 시
                b.setVisibility(View.INVISIBLE);    // 추가될 숫자버튼이 없기 때문에 사라짐
            }else {                             // 25번 이하 버튼 클릭 시
                b.setText("" + num[step+24]);   // 배열 num[25]부터 숫자가 나타난다
            }
            step++; // 클릭 후 step 1 증가
        }else {     // 틀린 숫자버튼 클릭시 (0.03초 진동, 0.08초 대기) 3번 반복
            vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {30, 80, 30, 80, 30, 80};
            vibrator.vibrate(pattern, -1);

            // 순서에 맞는 번호를 클릭하지 않았다면 토스트메시지로 클릭해야 할 번호를 알려준다
            Toast.makeText(getApplicationContext(), step + "번을 클릭하세요", Toast.LENGTH_SHORT).show();
        }

        if (step == 51) {   // 게임 클리어 --> step값이 51로 증가하고 게임 종료
            thread.interrupt();     // Thread를 인터럽트해 멈춘다.

            clearTime = ((TextView)findViewById(R.id.time)).getText().toString();   // 게임클리어 시간을 가져온다.
            onAdd();    // 가져온 시간을 DB에 저장 후 저장된 모든 시간 정보를을 ScoreActivity로 전달

            //게임 클리어시 음성으로 게임clear를 알림
            String str = "게임 클리어!";
            if (str.length() > 0) {
                if (tts.isSpeaking()) tts.stop();   // 이미 말하고 있다면, 기존 음성 합성 정지
                // 음성 합성 시작
                tts.setSpeechRate(1.0f); // 말하기 속도는 1배속
                tts.speak(str, TextToSpeech.QUEUE_FLUSH, null);
            }

            startActivity(scoreIntent); // ScoreActivity 실행
        }
    }

    // 게임 타이머 핸들러
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            // 시간 format
            int mSec = msg.arg1 % 100;
            int sec = (msg.arg1 / 100) % 60;
            int min = (msg.arg1 / 100) / 60;
            String result = String.format("%02d:%02d:%02d", min, sec, mSec);
            TextView time = (TextView)findViewById(R.id.time);
            time.setText(result);
        }
    };

    // 타이머 스레드
    public class timeThread implements Runnable{
        @Override
        public void run() {
            int i = 0;
            while (true) {
                Message msg = new Message();
                msg.arg1 = i++;
                handler.sendMessage(msg);
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return; // 인터럽트 받을 경우 return됨
                }
            }
        }
    }

    // SQLite DB 생성 / 파일이름: 1to50RankList.db / 테이블이름: rankList / 컬럼: 클리어시간(TEXT)
    class dbHelper extends SQLiteOpenHelper {
        public dbHelper(Context context) {
            super(context, "1to50RankList.db", null, 1);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE rankList ( 클리어시간 TEXT );");
        }
        public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
            db.execSQL("DROP TABLE IF EXISTS rankList");
            onCreate(db);
        }
    }

    // 클리어시간을 가져와 DB에 저장 후 저장된 모든 rankTimeList를 ScoreAcitvity에 전달
    public void onAdd() {
        db.execSQL("INSERT INTO rankList VALUES ('" + clearTime + "');");
        Cursor cursor = db.rawQuery("SELECT 클리어시간 FROM rankList ORDER BY 클리어시간 ASC;", null);
        String rankTimeList = "";
        while(cursor.moveToNext()) {
            String n = cursor.getString(0);
            rankTimeList += n + "\n";
        }
        scoreIntent.putExtra("클리어시간", rankTimeList);
    }

    // 음성 합성 언어 설정
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            Locale locale = Locale.getDefault(); // 현재 설정된 기본 언어
            if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE)
                tts.setLanguage(locale); // 현재 설정된 언어를 지원함
            else
                Toast.makeText(this, "지원하지 않는 언어 오류", Toast.LENGTH_LONG).show();
        } else if (status == TextToSpeech.ERROR) {
            Toast.makeText(this, "음성 합성 초기화 오류", Toast.LENGTH_LONG).show();
        }
    }

    // 어플리케이션 종료 시 음성 합성 종료
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) tts.shutdown();    // 음성 합성의 종료
    }
}
