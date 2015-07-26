package io.dwak.android.knockknock;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Joke {
    @SerializedName("joke") public final List<String> mJokeArray;

    public Joke(List<String> jokeArray) {
        mJokeArray = jokeArray;
    }
}
