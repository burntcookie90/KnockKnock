package io.dwak.android.knockknock;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface JokeService {
    @GET("/getRandomJoke")
    Observable<List<String>> getRandomJoke();

    @GET("/getJokeImage/{query}")
    Observable<String> getJokeImage(@Path("query") String query);
}
