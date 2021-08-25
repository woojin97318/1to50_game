package com.wujin.oneToFifty;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;

import android.os.Handler;
import android.os.Message;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class HelpActivity extends AppCompatActivity {

    String result = ""; // 파싱 결과를 담을 변수
    TextView tv;        // result를 출력할 TextView
    private Vibrator vibrator;  // 바이브레이터 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        tv = (TextView) findViewById(R.id.tv);
        Handler h = new Handler() {
            public void handleMessage(Message msg) {
                tv.setText(result);
            }
        };
        new WorkerThread(h).start();


    }

    class WorkerThread extends Thread {
        Handler h;
        WorkerThread(Handler h) {
            this.h = h;
        }

        public void run() {
            try {
                URL url = new URL("https://rss.blog.naver.com/woojin97318.xml");
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();

                NodeList itemNodeList = doc.getElementsByTagName("item");
                for (int i = 0; i < itemNodeList.getLength(); i++) {
                    Node itemNode = itemNodeList.item(i);
                    NodeList childNodeList = itemNode.getChildNodes();
                    for (int j = 0; j < childNodeList.getLength(); j++) {
                        Node childNode = childNodeList.item(j);
                        if (childNode.getNodeName().equals("title"))
                            result += childNode.getFirstChild().getNodeValue() + " ";
                        if (childNode.getNodeName().equals("description"))
                            result += childNode.getFirstChild().getNodeValue() + "\n\n";
                    }
                }
                h.sendMessage(new Message());
            } catch (Exception e){
                tv.setText("파싱 에러: " + e.toString());
            }
        }
    }

    public void gamePlay(View view) {
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(50);
        // 암시적 인텐트 : 게임 플레이 영상 Uri접속
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=Ctq1egblW8Q"));
        startActivity(intent);
    }
}
