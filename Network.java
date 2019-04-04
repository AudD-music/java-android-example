package io.audd.example.utility;

import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.zip.GZIPInputStream;

public class Network {
    public static final int ConnectionTimeoutMs = 30000;
    public static final int MaxUploadAttempts = 7;
    public static final int ReadTimeoutMs = 30000;
    public static final int StreamBufferSizeBytes = 8192;
    private static final String TAG = "NetworkLog";
    public static final String USER_AGENT;

    static {
        ArrayMap<String, String> agentData = new ArrayMap();
        agentData.put("Android", String.valueOf(VERSION.RELEASE));
        List<String> data = new ArrayList();
        for (Entry<String, String> entry : agentData.entrySet()) {
            data.add(((String) entry.getKey()) + " " + ((String) entry.getValue()));
        }
        Log.e(TAG, String.valueOf(data));
        USER_AGENT = "AudD Example App {" + TextUtils.join("; ", data) + "}";
        Log.e(TAG, "User-Agent: " + USER_AGENT);
    }

    static HttpURLConnection createHttpConnection(String httpURL, long length) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) new URL(httpURL).openConnection();
        httpConnection.addRequestProperty("User-Agent", USER_AGENT);
        httpConnection.setRequestProperty("Connection", "Keep-Alive");
        httpConnection.addRequestProperty("Accept-Encoding", "gzip");
        if (length != 0) {
            httpConnection.setFixedLengthStreamingMode(length);
            httpConnection.setInstanceFollowRedirects(true);
            httpConnection.setDoInput(true);
        }
        httpConnection.setConnectTimeout(30000);
        httpConnection.setReadTimeout(30000);
        return httpConnection;
    }

    public static String uploadFile(String httpURL, File file, String valueName, int attempt) {
        attempt++;
        String boundary = "--AudDExampleAndroid" + new Random().nextInt();
        FileUploader fileUpload = new FileUploader(file, boundary, valueName);
        HttpURLConnection connection = null;
        try {
            connection = createHttpConnection(httpURL, fileUpload.getContentLength());
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            connection.setRequestProperty("Content-Length", fileUpload.getContentLength() + "");
            connection.setRequestMethod("POST");
            OutputStream os = connection.getOutputStream();
            fileUpload.writeTo(os);
            os.close();
            connection.connect();
            InputStream is = new BufferedInputStream(connection.getInputStream(), 8192);
            String enc = connection.getHeaderField("Content-Encoding");
            if (enc != null && enc.equalsIgnoreCase("gzip")) {
                is = new GZIPInputStream(is);
            }
            return streamToString(is);
        } catch (Throwable th) {
        }
        if (e instanceof InterruptedIOException) {
            return null;
        }
        if (attempt >= 7) {
            return null;
        }
        return uploadFile(httpURL, file, valueName, attempt);
    }

    private static String streamToString(InputStream is) throws IOException {
        InputStreamReader r = new InputStreamReader(is);
        StringWriter sw = new StringWriter();
        char[] buffer = new char[8192];
        while (true) {
            try {
                int n = r.read(buffer);
                if (n == -1) {
                    break;
                }
                sw.write(buffer, 0, n);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return String.valueOf(sw);
    }
}
