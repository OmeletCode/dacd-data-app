package org.ulpgc.dacd;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class SpaceXApiClient {
    private final OkHttpClient client;
    private final String url = "https://api.spacexdata.com/v4/starlink";

    public SpaceXApiClient() {
        this.client = new OkHttpClient();
    }

    public String getRawData() throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("Error en la petición HTTP: " + response.code());
            }
            return response.body().string();
        }
    }
}