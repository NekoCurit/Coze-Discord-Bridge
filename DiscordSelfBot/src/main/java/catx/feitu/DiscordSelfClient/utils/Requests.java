package catx.feitu.DiscordSelfClient.utils;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.nio.charset.StandardCharsets;

public class Requests {

    protected String token;
    public CloseableHttpClient httpClient;

    public Requests(String token) {
        executor();
        this.token = token;
    }



    public synchronized JsonElement post(String url, String json) {
        try {
            HttpPost httpPost = new HttpPost(url);
            StringEntity entity = new StringEntity(json, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json; charset=utf-8");
            httpPost.setHeader("Authorization", token);
            CloseableHttpResponse response = httpClient.execute(httpPost);
            String responseJSON = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            return new JsonParser().parse(responseJSON);
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized JsonElement patch(String url, String json) {
        try {
            HttpPatch httpPatch = new HttpPatch(url);
            StringEntity entity = new StringEntity(json);
            httpPatch.setEntity(entity);
            httpPatch.setHeader("Accept", "application/json");
            httpPatch.setHeader("Content-type", "application/json; charset=utf-8");
            httpPatch.setHeader("Authorization", token);
            CloseableHttpResponse response = httpClient.execute(httpPatch);
            String responseJSON = EntityUtils.toString(response.getEntity());
            return new JsonParser().parse(responseJSON);
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized JsonElement put(String url, String json) {
        try {
            HttpPut httpPut = new HttpPut(url);
            StringEntity entity = new StringEntity(json);
            httpPut.setEntity(entity);

            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Content-type", "application/json; charset=utf-8");
            httpPut.setHeader("Authorization", token);

            CloseableHttpResponse response = httpClient.execute(httpPut);
            String responseJSON = EntityUtils.toString(response.getEntity());
            return new JsonParser().parse(responseJSON);
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized JsonElement get(String url) {
        try {
            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Content-type", "application/json; charset=utf-8");
            httpGet.setHeader("Authorization", token);
            CloseableHttpResponse response = this.httpClient.execute(httpGet);
            String responseJSON = EntityUtils.toString(response.getEntity());
            return new JsonParser().parse(responseJSON);
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized JsonElement delete(String url, String json) {
        try {
            HttpDeleteWithBody httpDeleteWithBody = new HttpDeleteWithBody(url);
            StringEntity entity = new StringEntity(json);
            httpDeleteWithBody.setEntity(entity);
            httpDeleteWithBody.setHeader("Authorization", token);
            CloseableHttpResponse response = this.httpClient.execute(httpDeleteWithBody);
            String responseJSON = EntityUtils.toString(response.getEntity());
            System.out.println(responseJSON);
            return new JsonParser().parse(responseJSON);
        } catch (Exception e) {
            return null;
        }
    }

    void executor() {
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (Exception e) {
                return;
            }
        }
        httpClient = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build()).build();
    }

}