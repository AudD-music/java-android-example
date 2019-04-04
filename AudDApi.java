package io.audd.example;

import android.util.Log;
import io.audd.example.data.ResultTrack;
import io.audd.example.utility.DispatchQueue;
import io.audd.example.utility.Network;
import io.audd.example.flash.utility.Params;
import java.io.File;
import java.util.Objects;
import org.json.JSONException;
import org.json.JSONObject;

public class AudDApi {
    private static final String API_TOKEN = "";
    private static volatile AudDApi Instance = null;
    private static final String TAG = "AudDApi";
    private static final String URL = "https://api.audd.io/";
    public static final DispatchQueue thread = new DispatchQueue(TAG);

    public static class Methods {
        public static final String RECOGNIZE = "recognize";
        public static final String RECOGNIZE_WITH_OFFSET = "recognizeWithOffset";
    }

    public interface RecognizeCallback {
        void error();

        void fail();

        void success(ResultTrack resultTrack);
    }

    public static AudDApi getInstance() {
        AudDApi localInstance = Instance;
        if (localInstance == null) {
            synchronized (AudDApi.class) {
                try {
                    localInstance = Instance;
                    if (localInstance == null) {
                        AudDApi localInstance2 = new AudDApi();
                        try {
                            Instance = localInstance2;
                            localInstance = localInstance2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            localInstance = localInstance2;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    throw th2;
                }
            }
        }
        return localInstance;
    }

    public void recognizeVoice(final File file, final boolean isHumming, final RecognizeCallback callback) {
        thread.postRunnable(new Runnable() {
            public void run() {
                ResultTrack track = null;
                try {
                    AudDApi audDApi = AudDApi.this;
                    File file = file;
                    String method = isHumming ? Methods.RECOGNIZE_WITH_OFFSET : Methods.RECOGNIZE;
                    JSONObject response = audDApi.upload(file, method);
                    Log.e(AudDApi.TAG, method + " = " + String.valueOf(response));
                    if (response.has("result") && !response.isNull("result")) {
                        track = new ResultTrack(response.getJSONObject("result"));
                    }
                } catch (Throwable ignored) {
                    Log.d(AudDApi.TAG, "Recognition JSON exception:" + ignored.getMessage());
                    callback.error();
                }
                if (track != null) {
                    Log.d(AudDApi.TAG, new StringBuilder().append("TRACK RECOGNIZED: ").append(track).toString() == null ? "NONE" : track.toString());
                    callback.success(track);
                    return;
                }
                Log.d(AudDApi.TAG, "Recognition failed: track is null");
                callback.fail();
            }
        });
    }

    public JSONObject upload(File file, String method, Params params) throws JSONException {
        if (params == null) {
            params = new Params();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(URL);
        sb.append("?method=");
        sb.append(method);
        sb.append("&api_token=");
        sb.append(API_TOKEN);
        if (Objects.equals(method, Methods.RECOGNIZE)) {
            sb.append("&return=lyrics,deezer,itunes&itunes_country=us&");
            sb.append(String.valueOf(params));
        }
        return new JSONObject(Network.uploadFile(sb.toString(), file, "file", 0));
    }

    public JSONObject upload(File file, String method) throws JSONException {
        return upload(file, method, new Params());
    }
}
