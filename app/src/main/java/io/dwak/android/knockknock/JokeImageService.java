package io.dwak.android.knockknock;

import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface JokeImageService {
    @POST("/")
    Observable<String> sendImage(@Body JokeServiceImage jokeServiceImage);
}
