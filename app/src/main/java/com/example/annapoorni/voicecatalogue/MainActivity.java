package com.example.annapoorni.voicecatalogue;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.Locale;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {

    private TextToSpeech textToSpeech;
    private Button btn;
    private EditText editText;

    public static final String API_BASE_URL = "http://192.168.43.43:8091/products/descriptions/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = findViewById(R.id.btn);

        editText = findViewById(R.id.et);
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int ttsLang = textToSpeech.setLanguage(Locale.US);

                    if (ttsLang == TextToSpeech.LANG_MISSING_DATA
                            || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                try {
                    String data = editText.getText().toString();
                    String url1 =  API_BASE_URL + data;

                    AsyncHttpClient client = new AsyncHttpClient();
                    client.addHeader("Authorization", "Bearer eyJraWQiOiJoNVJTQ01xX2FlSkVERUhkTlo4V0g5UDZTd0N2VDhpR3pMRWRvUk5PZDJRIiwiYWxnIjoiUlMyNTYifQ.eyJ2ZXIiOjEsImp0aSI6IkFULk1HUFVHOWdiZnBsMGl3VlhqM0RCNi16QVlrTHd0aVZrLUF1YUVIS2pycjAiLCJpc3MiOiJodHRwczovL2Rldi04MDQyMzgub2t0YXByZXZpZXcuY29tL29hdXRoMi9kZWZhdWx0IiwiYXVkIjoiYXBpOi8vZGVmYXVsdCIsImlhdCI6MTU1ODE4OTk5NCwiZXhwIjoxNTU4MTkzNTk0LCJjaWQiOiIwb2Fmb3I0b29tRmtoMk9FUjBoNyIsInNjcCI6WyJteXJldGFpbCJdLCJzdWIiOiIwb2Fmb3I0b29tRmtoMk9FUjBoNyJ9.cLMQDbL9mZD0nWwmJP90YULFEekjjqJ8NbDyJjsMyLg2tg5UbJ0tBJvy61jjcllu6rV2wKfdAOjmOHIQaepnHu3FK8jIBCum4x1LtQ8nzqephDk5C8TSAi0LqB0w6aUz1qV2YOB2raQ5ET_eVDL6KMnMPKI3bzvQ9Yi6puFOsmCWXRi5jZKdStOdS6vsgHZGz5_K0HGsrKDoFlmV1vAn3FU6tsKkfdR5NaLzF8mvt_L6oRxhMVGUhYXoJmorCmIjxV0YC8v2metrKf38ZNSmp4XB7EF6gqOWE6LiSyXh1JaW8hoCyRLpGw6FH0iIcojTVHxy64ZSXhpFQNlbyP2UGA");

                    client.get(url1, null, new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                            String json = new String(bytes);
                            JsonObject contentObj = new JsonParser().parse(json).getAsJsonObject();

                            String productDesc = "Product Description for the given code is " + contentObj.get("product_desc").getAsString();

                            Log.i("TTS", "button clicked: " + productDesc);
                            int speechStatus = textToSpeech.speak(productDesc, TextToSpeech.QUEUE_FLUSH, null);

                            if (speechStatus == TextToSpeech.ERROR) {
                                Log.e("TTS", "Error in converting Text to Speech!");
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                            Log.e("TTS", "Failed with Status code " + statusCode);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }
}
