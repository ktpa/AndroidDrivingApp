package group01.smartcar.client.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.Locale;

public class SpeechListener {

    private final SpeechRecognizer speechRecognizer;
    private final Intent speechRecognizerIntent;

    private ResultsCallback resultsCallback;

    public SpeechListener(Context context) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new SpeechRecognitionListener(resultsCallback));

        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
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

    public interface ResultsCallback{
        void onResults(Bundle bundle);
    }

    private static final class SpeechRecognitionListener implements RecognitionListener {

        private final ResultsCallback resultsCallback;

        private SpeechRecognitionListener(ResultsCallback resultsCallback) {
            this.resultsCallback = resultsCallback;
        }


        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {

        }

        @Override
        public void onResults(Bundle results) {
            if(resultsCallback != null){
                resultsCallback.onResults(results);
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {

        }

        @Override
        public void onEvent(int eventType, Bundle params) {

        }
    }
}
