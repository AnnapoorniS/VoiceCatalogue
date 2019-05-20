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

    public static final String API_BASE_URL = "http://<ip>/products/descriptions/";

    private String BEARER_TOKEN = "eyJraWQiOiJoNVJTQ01xX2FlSkVERUhkTlo4V0g5UDZTd0N2VDhpR3pMRWRvUk5PZDJRIiwiYWxnIjoiUlMyNTYifQ.eyJ2ZXIiOjEsImp0aSI6IkFULndGc0Vkc1Z1Q0t5b21IaVJUYlI3RkdlZDhyX2Qxcm13T3lvbTNfekpSMnMiLCJpc3MiOiJodHRwczovL2Rldi04MDQyMzgub2t0YXByZXZpZXcuY29tL29hdXRoMi9kZWZhdWx0IiwiYXVkIjoiYXBpOi8vZGVmYXVsdCIsImlhdCI6MTU1ODM3MzU4MCwiZXhwIjoxNTU4Mzc3MTgwLCJjaWQiOiIwb2Fmb3I0b29tRmtoMk9FUjBoNyIsInNjcCI6WyJteXJldGFpbCJdLCJzdWIiOiIwb2Fmb3I0b29tRmtoMk9FUjBoNyJ9.U__fR0ak0otw2jFy9Kjef1xeXLMiTk_YAE4FsKarNM1VcXkQCiwN_KS1gL8_pRX8kGIsJgH8LbfFh9kwnuBpUw_Tk3zRGxrRcs5B0Pj-LoeNbZr0SBqeuPGhia4B2bI0wRokGXEbKqnpQL8ed77xODvW7K4p5ZtGnMcAStOhJH0P6BRitoDCiAB9ekeEfuNciCJASTUq-Y_2TV13M-adapY33J_ozl-3ke2FYyLcGL5Bm_jNzAXKD1y5-nzaf_-GvcjwcJACW7xA_G__NN8AjqHFMJB0CL02k586AGNkHRjcoTrwsYvX7o9tnH9p1EGRqK7RDpY0kTCY3Sp9mERiJw";

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
                        Log.e("VoiceOver", "The Language is not supported!");
                    } else {
                        Log.i("VoiceOver", "Language Supported.");
                    }
                    Log.i("VoiceOver", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                voiceOverTheProductDetails(editText.getText().toString());
            }

        });
    }

    private void voiceOverTheProductDetails(final String productId) {
        try{
            btn.setEnabled(false);
            String url1 =  API_BASE_URL + productId;
            AsyncHttpClient client = new AsyncHttpClient();
            client.addHeader("Authorization", "Bearer "+ BEARER_TOKEN);

            client.get(url1, null, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                    String json = new String(bytes);
                    JsonObject contentObj = new JsonParser().parse(json).getAsJsonObject();

                    String productDesc = "Product Description for the given code is " + contentObj.get("product_desc").getAsString();

                    Log.i("VoiceOver", "button clicked: " + productDesc);
                    int speechStatus = textToSpeech.speak(productDesc, TextToSpeech.QUEUE_FLUSH, null);

                    if (speechStatus == TextToSpeech.ERROR) {
                        Log.e("VoiceOver", "Error in converting Text to Speech!");
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                    Log.e("VoiceOver", "Failed with Status code " + statusCode);
                    if(statusCode == 401){
                        refreshToken(productId);
                    }
                }
            });
            btn.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshToken(final String productId) {
        try {
            AsyncHttpClient refreshClient = new AsyncHttpClient();
            refreshClient.setBasicAuth("<client_id>", "<client_secret>");
            refreshClient.addHeader("Content-Type", "application/x-www-form-urlencoded");

            refreshClient.post("https://dev-804238.oktapreview.com/oauth2/default/v1/token?grant_type=client_credentials&scope=myretail", null, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] bytes) {
                    String json = new String(bytes);
                    JsonObject contentObj = new JsonParser().parse(json).getAsJsonObject();

                    BEARER_TOKEN = contentObj.get("access_token").getAsString();
                    Log.i("token", "Token Refreshed Successfully! " + BEARER_TOKEN);
                    voiceOverTheProductDetails(productId);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable throwable) {
                    Log.e("token", "Failed to refresh token with Status code " + statusCode);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
