package io.audd.example.utility;

import android.text.TextUtils;
import android.util.ArrayMap;
import java.net.URLEncoder;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;

public class Params {
    private final ArrayMap<String, String> params = new ArrayMap();

    public Params addValue(String name, Object value) {
        this.params.put(name, String.valueOf(value));
        return this;
    }

    public boolean containsKey(String key) {
        return this.params.containsKey(key);
    }

    public boolean containsValue(String value) {
        return this.params.containsValue(value);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject result = new JSONObject();
        try {
            for (Entry<String, String> entry : this.params.entrySet()) {
                result.put((String) entry.getKey(), URLEncoder.encode((String) entry.getValue(), "utf-8"));
            }
        } catch (Throwable th) {
        }
        return result;
    }

    public static Params newInstance() {
        return new Params();
    }

    public String toString() {
        String result = "";
        try {
            for (Entry<String, String> entry : this.params.entrySet()) {
                result = result + "&" + ((String) entry.getKey()) + "=" + URLEncoder.encode((String) entry.getValue(), "utf-8");
            }
        } catch (Throwable th) {
        }
        return TextUtils.isEmpty(result) ? "from_android=1" : String.valueOf(result.subSequence(1, result.length()));
    }
}
