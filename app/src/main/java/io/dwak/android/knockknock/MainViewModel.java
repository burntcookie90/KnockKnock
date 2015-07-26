package io.dwak.android.knockknock;

import com.google.gson.Gson;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class MainViewModel {

    private final JokeService mService;
    private final PublishSubject<Joke> mJokePublishSubject = PublishSubject.create();
    private final PublishSubject<JokeImage> mJokeImagePublishSubject = PublishSubject.create();
    private final JokeImageService mJokeImageService;

    public MainViewModel() {
        mService = new RestAdapter.Builder()
                .setEndpoint("https://jokeaday.herokuapp.com")
                .setConverter(new GsonConverter(new Gson()))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build().create(JokeService.class);

        mJokeImageService = new RestAdapter.Builder()
                .setConverter(new GsonConverter(new Gson()))
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint("https://joke-image-viewer.firebaseio.com/.json")
                .build().create(JokeImageService.class);
    }

    void getNewJoke(){
        mService.getRandomJoke()
                .subscribeOn(Schedulers.io())
                .map(Joke::new)
                .subscribe(mJokePublishSubject::onNext);
    }

    void getJokeImage(String queryWord){
        mService.getJokeImage(queryWord)
                .subscribeOn(Schedulers.io())
                .flatMap(url -> mJokeImageService.sendImage(new JokeServiceImage(url)))
        .subscribe(s1 -> {

        });
    }

    Observable<Joke> getJoke(){
        return mJokePublishSubject.asObservable();
    }

    Observable<JokeImage> getJokeImage(){
        return mJokeImagePublishSubject.asObservable();
    }
}
