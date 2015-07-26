package io.dwak.android.knockknock;

import com.google.gson.annotations.SerializedName;

public class JokeServiceImage {
    @SerializedName("image") String url;

    public JokeServiceImage(String url) {
        this.url = url;
    }
}
