package com.example.diplom;

import android.content.Context;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ApiManager {
    private static final String TAG = "ApiManager";
    private static final String BASE_URL = "http://176.98.176.113/api/";
    private RequestQueue queue;

    public ApiManager(Context context) {
        queue = Volley.newRequestQueue(context);
    }

    public void getTopPlayers(int limit, TopCallback callback) {
        String url = BASE_URL + "get_top.php?limit=" + limit;
        Log.d(TAG, "Request URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d(TAG, "Response: " + response.toString());
                    try {
                        List<Player> players = new ArrayList<>();
                        JSONArray arr = response.getJSONArray("players");
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            players.add(new Player(obj.getString("name"), obj.getInt("score")));
                        }
                        callback.onSuccess(players);
                    } catch (Exception e) {
                        Log.e(TAG, "Parse error: " + e.getMessage());
                        callback.onError(e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Network error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e(TAG, "Status code: " + error.networkResponse.statusCode);
                        Log.e(TAG, "Response data: " + new String(error.networkResponse.data));
                    }
                    callback.onError("Ошибка сети");
                }
        );

        queue.add(request);
    }

    public void saveScore(String name, int score, SaveCallback callback) {
        String url = BASE_URL + "save_score.php";
        Log.d(TAG, "Save URL: " + url);

        JSONObject body = new JSONObject();
        try {
            body.put("name", name);
            body.put("score", score);
        } catch (Exception e) {
            callback.onError(e.getMessage());
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, body,
                response -> {
                    Log.d(TAG, "Save response: " + response.toString());
                    try {
                        int rank = response.getInt("rank");
                        callback.onSuccess(rank);
                    } catch (Exception e) {
                        callback.onError(e.getMessage());
                    }
                },
                error -> {
                    Log.e(TAG, "Save error: " + error.toString());
                    callback.onError("Ошибка сети");
                }
        );

        queue.add(request);
    }

    public interface TopCallback {
        void onSuccess(List<Player> players);
        void onError(String error);
    }

    public interface SaveCallback {
        void onSuccess(int rank);
        void onError(String error);
    }

    public static class Player {
        public String name;
        public int score;
        Player(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }
}