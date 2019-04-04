package  io.audd.example.data;

import android.support.v4.os.EnvironmentCompat;
import android.util.Log;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResultTrack {
    public final String album;
    public String appleMusicUrl;
    public final String artist;
    public final String coverUrl;
    public final String fullJson;
    public final String lyrics;
    public final String previewUrl;
    public double score;
    public final String smallCoverUrl;
    public final String title;
    public String youtubePreviewUrl;
    public String youtubeUrl;

    public static String getYoutubeVideoIdFromUrl(String url) {
        String[] splited = url.split(Pattern.quote("?v="));
        if (splited.length > 1) {
            return splited[1].split(Pattern.quote("&"))[0];
        }
        return null;
    }

    public ResultTrack(JSONObject item) throws JSONException {
        this.appleMusicUrl = null;
        this.fullJson = item.toString();
        if (!item.has("humming") || !item.getBoolean("humming")) {
            JSONObject itunes;
            this.score = -1.0d;
            this.artist = item.getString("artist");
            this.title = item.getString("title");
            this.album = item.optString("album", EnvironmentCompat.MEDIA_UNKNOWN);
            this.youtubeUrl = null;
            this.youtubePreviewUrl = null;
            if (item.isNull("lyrics")) {
                this.lyrics = "No lyrics available";
            } else {
                JSONObject jsonLyrics = item.getJSONObject("lyrics");
                this.lyrics = jsonLyrics.getString("lyrics");
                this.lyrics.replaceAll("\b\r", "");
                if (jsonLyrics.has("media") && !jsonLyrics.isNull("media")) {
                    JSONArray mArray = new JSONArray(jsonLyrics.getString("media"));
                    for (int i = 0; i < mArray.length(); i++) {
                        JSONObject mobj = mArray.getJSONObject(i);
                        if (mobj.getString("provider").equalsIgnoreCase("youtube")) {
                            this.youtubeUrl = mobj.getString("url");
                            if (getYoutubeVideoIdFromUrl(this.youtubeUrl) != null) {
                                this.youtubePreviewUrl = String.format("https://img.youtube.com/vi/%s/default.jpg", new Object[]{getYoutubeVideoIdFromUrl(this.youtubeUrl)});
                            }
                        }
                    }
                }
            }
            if (item.isNull("itunes") || item.getString("itunes").equals("[]")) {
                this.appleMusicUrl = null;
            } else {
                itunes = item.getJSONObject("itunes");
                if (itunes.has("trackViewUrl")) {
                    this.appleMusicUrl = itunes.getString("trackViewUrl");
                }
            }
            if (item.isNull("deezer") || item.getString("deezer").equals("[]")) {
                Log.e("ResultTrackConstructor", "Deezer is NULL");
                if (item.isNull("itunes")) {
                    this.previewUrl = null;
                    this.coverUrl = null;
                    this.smallCoverUrl = null;
                } else {
                    itunes = item.getJSONObject("itunes");
                    this.previewUrl = itunes.getString("previewUrl");
                    this.smallCoverUrl = itunes.getString("artworkUrl100");
                    this.coverUrl = itunes.getString("artworkUrl100");
                }
            } else {
                JSONObject deezer = item.getJSONObject("deezer");
                this.previewUrl = deezer.getString("preview");
                JSONObject deezerAlbum = deezer.getJSONObject("album");
                this.coverUrl = deezerAlbum.getString("cover_big");
                this.smallCoverUrl = deezerAlbum.getString("cover_medium");
            }
            Log.d("JSON", this.fullJson);
        } else if (item.getInt("count") <= 0 || !item.has("list")) {
            throw new JSONException("list item count < 0 or list is missing");
        } else {
            JSONObject track = item.getJSONArray("list").getJSONObject(0);
            this.score = track.getDouble("score");
            this.artist = track.getString("artist");
            this.title = track.getString("title");
            this.album = null;
            this.coverUrl = null;
            this.smallCoverUrl = null;
            this.previewUrl = null;
            this.lyrics = null;
        }
    }

    public ResultTrack() {
        this.appleMusicUrl = null;
        this.youtubePreviewUrl = null;
        this.youtubeUrl = null;
        this.fullJson = "";
        this.artist = "Face";
        this.title = "Title";
        this.album = "Album";
        this.coverUrl = "";
        this.smallCoverUrl = "";
        this.previewUrl = "";
        this.lyrics = "No lyrics available";
    }

    public ResultTrack(String title, String artist, String album, String coverUrl, String smallCoverUrl, String previewUrl, String lyrics, String fullJson, String youtubePreviewUrl, String youtubeUrl) {
        this.appleMusicUrl = null;
        this.fullJson = fullJson;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.coverUrl = coverUrl;
        this.smallCoverUrl = smallCoverUrl;
        this.previewUrl = previewUrl;
        this.lyrics = lyrics;
        this.youtubePreviewUrl = youtubePreviewUrl;
        this.youtubeUrl = youtubeUrl;
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject item = new JSONObject();
        item.put("artist", this.artist);
        item.put("title", this.title);
        item.put("album", this.album);
        item.put("coverUrl", this.coverUrl);
        item.put("smallCoverUrl", this.smallCoverUrl);
        item.put("previewUrl", this.previewUrl);
        item.put("youtubeUrl", this.youtubeUrl);
        return item;
    }

    public String toString() {
        try {
            return String.valueOf(toJSON());
        } catch (Throwable th) {
            return "{}";
        }
    }

    public String getMusicTitle() {
        return String.valueOf(new StringBuilder().append(this.artist).append(" ").append(this.title));
    }
}
