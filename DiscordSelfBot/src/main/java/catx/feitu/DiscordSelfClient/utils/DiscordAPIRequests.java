package catx.feitu.DiscordSelfClient.utils;

import catx.feitu.DiscordSelfClient.client.Exceptions.*;
import com.alibaba.fastjson.JSON;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class DiscordAPIRequests {
    public InetSocketAddress proxy = null;
    public String token;
    public String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3";

    public DiscordAPIRequests(String token) {
        this.token = token;
    }

    public Object get(String api) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(api))
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .header("Content-type", "application/json; charset=utf-8")
                .header("Authorization", token)
                .build();
        return responseHandle(getHttpClient(), request);
    }
    public Object post(String api, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(api))
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .header("Content-type", "application/json; charset=utf-8")
                .header("Authorization", token)
                .POST(HttpRequest.BodyPublishers.ofString(json ,StandardCharsets.UTF_8))
                .build();
        return responseHandle(getHttpClient(), request);
    }
    public Object put(String api, String content) throws Exception {
        return put(api,content.getBytes(StandardCharsets.UTF_8));
    }
    public Object put(String api, byte[] bytes) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(api))
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .header("Content-type", "application/json; charset=utf-8")
                .header("Authorization", token)
                .PUT(HttpRequest.BodyPublishers.ofByteArray(bytes))
                .build();
        return responseHandle(client, request);
    }
    public Object delete(String api) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(api))
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .header("Content-type", "application/json; charset=utf-8")
                .header("Authorization", token)
                .DELETE()
                .build();
        return responseHandle(getHttpClient(), request);
    }
    public Object patch(String api, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(api))
                .header("User-Agent", userAgent)
                .header("Accept", "application/json")
                .header("Content-type", "application/json; charset=utf-8")
                .header("Authorization", token)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json ,StandardCharsets.UTF_8))
                .build();
        return responseHandle(getHttpClient(), request);
    }

    private Object responseHandle(HttpClient client, HttpRequest request) throws java.io.IOException, InterruptedException, InvalidRequestBodyException, InvalidTokenException {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        switch (response.statusCode()) {
            case 400:
                throw new InvalidRequestBodyException();
            case 401:
                throw new InvalidTokenException(token);
            default:
                return JSON.parse(response.body());
        }
    }
    private HttpClient getHttpClient() {
        HttpClient.Builder response = HttpClient.newBuilder();
        if(this.proxy != null) response.proxy(ProxySelector.of(this.proxy));
        return response.build();
    }
}
