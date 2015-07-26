package io.dwak.android.knockknock;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import io.dwak.android.knockknock.databinding.MainActivityBinding;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static final int MY_DATA_CHECK_CODE = 200;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextToSpeech mTts;
    private PublishSubject<TextToSpeech> mTextToSpeechPublishSubject = PublishSubject.create();
    private PublishSubject<String> mUserResponse = PublishSubject.create();
    private int mJokeSection = 0;
    private Joke mJoke;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initTTS();
        MainViewModel mainViewModel = new MainViewModel();

        Action1<Joke> jokeAction = joke -> {
            mJoke = joke;
            mTts.speak(mJoke.mJokeArray.get(mJokeSection), TextToSpeech.QUEUE_FLUSH, null);
            if(mJokeSection < mJoke.mJokeArray.size() - 1) {
                mJokeSection++;
                new Handler(Looper.getMainLooper())
                        .postDelayed(MainActivity.this::promptSpeechInput,
                                     300);
            }
            else {
                mainViewModel.getJokeImage(mJoke.mJokeArray.get(2));
                finish();
            }
        };

        mTextToSpeechPublishSubject
                .flatMap(textToSpeech -> {
                    mTts = textToSpeech;
                    return mainViewModel.getJoke();
                })
                .subscribe(jokeAction::call);

        mUserResponse.asObservable()
                     .subscribe(s -> {
                         mJokeSection++;
                         jokeAction.call(mJoke);
                     });

        mainViewModel.getNewJoke();

        binding.newJoke.setOnClickListener(v -> {
            mJokeSection = 0;
            mainViewModel.getNewJoke();
        });

    }

    public void initTTS() {
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        mJoke.mJokeArray.get(mJokeSection));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                           getString(R.string.speech_not_supported),
                           Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mUserResponse.onNext(result.get(0));
                }
                else {
                    mJokeSection = 0;
                }
                break;
            }
            case MY_DATA_CHECK_CODE:
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    // success, create the TTS instance
                    mTextToSpeechPublishSubject.onNext(new TextToSpeech(this, this));
                }
                else {
                    // missing data, install it
                    Intent installIntent = new Intent();
                    installIntent.setAction(
                            TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                    startActivity(installIntent);
                }
                break;

        }
    }

    @Override
    public void onInit(int status) {

    }
}
