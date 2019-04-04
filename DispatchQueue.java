package io.audd.example.utility;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.concurrent.CountDownLatch;

public class DispatchQueue extends Thread {
    private volatile Handler handler = null;
    private CountDownLatch syncLatch = new CountDownLatch(1);

    class C03501 extends Handler {
        C03501() {
        }

        public void handleMessage(Message msg) {
            DispatchQueue.this.handleMessage(msg);
        }
    }

    public DispatchQueue(String threadName) {
        setName(threadName);
        start();
    }

    public void sendMessage(Message msg, int delay) {
        try {
            this.syncLatch.await();
            if (delay <= 0) {
                this.handler.sendMessage(msg);
            } else {
                this.handler.sendMessageDelayed(msg, (long) delay);
            }
        } catch (Throwable th) {
        }
    }

    public void cancelRunnable(Runnable runnable) {
        try {
            this.syncLatch.await();
            this.handler.removeCallbacks(runnable);
        } catch (Throwable th) {
        }
    }

    public void postRunnable(Runnable runnable) {
        postRunnable(runnable, 0);
    }

    public void postRunnable(Runnable runnable, long delay) {
        try {
            this.syncLatch.await();
            if (delay <= 0) {
                this.handler.post(runnable);
            } else {
                this.handler.postDelayed(runnable, delay);
            }
        } catch (Throwable th) {
        }
    }

    public void cleanupQueue() {
        try {
            this.syncLatch.await();
            this.handler.removeCallbacksAndMessages(null);
        } catch (Throwable th) {
        }
    }

    public void handleMessage(Message inputMessage) {
    }

    public void run() {
        Looper.prepare();
        this.handler = new C03501();
        this.syncLatch.countDown();
        Looper.loop();
    }
}
