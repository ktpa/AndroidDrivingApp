package group01.smartcar.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechControl {


    private SpeechRecognizer speechRecognizer;
    private final Intent speechRecognizerIntent;
    private ResultsCallback resultsCallback;


    public SpeechControl(Context context){

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                System.out.println("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                if(resultsCallback != null){
                    resultsCallback.onResults(bundle);
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

    }

    public void start(){
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    public void stop(){
        speechRecognizer.stopListening();
    }

    public void destroy(){
        speechRecognizer.destroy();
    }

    public void onResults(ResultsCallback resultsCallback){
        this.resultsCallback = resultsCallback;
    }

    interface ResultsCallback{
        void onResults(Bundle bundle);
    }
}
