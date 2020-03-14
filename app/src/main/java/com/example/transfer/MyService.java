package com.example.transfer;

import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyService {

    private String TAG = "MyService";

    private IsNetwork isNetwork = new IsNetwork();

    private int progress = 0;
    private boolean isPause = false;

    public void startDownload(final String downloadUrl, final long downloadedContent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                isPause = false;
                long downloadedContent1 = downloadedContent;
                File file = null;
                String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                InputStream is = null;
                RandomAccessFile savedFile = null;
                try {
                    file = new File(directory + fileName);
                    if (file.exists()) {
                        downloadedContent1 = file.length();
                    }
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().addHeader("RANGE", "bytes=" + downloadedContent1 + "-")
                            .url(downloadUrl).build();
                    Response response = client.newCall(request).execute();
                    if (response != null) {
                        Log.d(TAG, "startDownload: start download");
                        long contentLength = getContentLength(downloadUrl);
                        is = response.body().byteStream();
                        Looper.prepare();
                        if (getContentLength(downloadUrl) == downloadedContent1) {
                            Toast.makeText(MyApplication.getContext(), "已经下载完成", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                        savedFile = new RandomAccessFile(file, "rw");
                        savedFile.seek(downloadedContent1);
                        byte[] bytes = new byte[1024];
                        long total = 0;
                        int length;
                        while ((length = is.read(bytes)) != -1) {
                            if (isPause||!isNetwork.isNetworkConnected(MyApplication.getContext())) {
                                Toast.makeText(MyApplication.getContext(), "下载暂停", Toast.LENGTH_SHORT).show();
                                Looper.loop();
                                break;
                            }
                            savedFile.write(bytes, 0, length);
                            total += length;
                            progress = (int) ((total + downloadedContent1) * 100 / contentLength);
                            Log.d(TAG, "run: " + progress);
                        }
                    }
                    response.body().close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                        if (savedFile != null) {
                            savedFile.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

    }

    public int getProgress() {
        return progress;
    }

    public void pauseDownload() {
        isPause = true;
    }

    public long getContentLength(String downloadUrl) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(downloadUrl).build();
            Response response = client.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.close();
                return contentLength;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public boolean deleteFile(String downloadUrl) {
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
        File file = new File(directory + fileName);
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }

    public void startDownload(String downloadUrl) {
        startDownload(downloadUrl, 0);
    }

}

