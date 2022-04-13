package com.ccg.futurerealization.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.ccg.futurerealization.R;
import com.ccg.futurerealization.request.RetrofitHelper;
import com.ccg.futurerealization.request.service.DownloadFileService;
import com.ccg.futurerealization.utils.LogUtils;
import com.ccg.futurerealization.utils.SPUtils;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.ResponseBody;

public class DownloadService extends Service {

    private DownloadBinder mDownloadBinder;

    private Context mContext;

    private DownloadListener listener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onSuccess() {
            // 下载成功时将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Success", -1));
            Toast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            // 下载失败时将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Failed", -1));
            Toast.makeText(DownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            Toast.makeText(DownloadService.this, "Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled() {
            stopForeground(true);
            Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
        }

    };

    public DownloadService() {
    }

    @Override
    public void onCreate() {
        mContext = this;
        mDownloadBinder = new DownloadBinder(this, listener);
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mDownloadBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent();
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        String CHANNEL_ID = "your_custom_id";//应用频道Id唯一值， 长度若太长可能会被截断，
        String CHANNEL_NAME = "your_custom_name";//最长40个字符，太长会被截断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
            getNotificationManager().createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        if (progress >= 0) {
            // 当progress大于或等于0时才需显示下载进度
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
        }
        return builder.build();
    }

    public static class DownloadBinder extends Binder {

        private WeakReference<DownloadService> mDownloadServiceWeakReference;

        private DownloadListener mDownloadListener;

        private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

        private volatile boolean mIsPaused;

        private final ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();

        public DownloadBinder(DownloadService downloadService, DownloadListener downloadListener) {
            mDownloadServiceWeakReference = new WeakReference<>(downloadService);
            mDownloadListener = downloadListener;
        }

        public void startDownload(@NonNull String url) {
            DownloadService downloadService = mDownloadServiceWeakReference.get();
            if (downloadService == null) {
                LogUtils.e("downloadService == null");
                return;
            }
            downloadService.startForeground(1, downloadService.getNotification("Downloading...", 0));
            Toast.makeText(downloadService, "Downloading...", Toast.LENGTH_SHORT).show();
            String fileName = url.substring(url.lastIndexOf("/"));
            final String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            LogUtils.i("fileName=" + fileName + ", directory=" + directory);
            File file = new File(directory + fileName);
            long fileLen = 0;
            if (file.exists()) {
                fileLen = SPUtils.getLong(downloadService, url);
                if (fileLen == file.length()) {
                    LogUtils.i("file download finish");
                    return;
                }
            }
            String range = "bytes=" + fileLen + "-";
            LogUtils.i("range= " + range);
            DownloadFileService service = RetrofitHelper.getInstance().getRetrofitIgCerWithRx()
                    .create(DownloadFileService.class);

            Observable<ResponseBody> observable = service.downloadFileByRxjava(range, url);
            long finalFileLen = fileLen;
            observable.subscribeOn(Schedulers.io())
                    .map(new Function<ResponseBody, State>() {
                        @Override
                        public State apply(ResponseBody responseBody) throws Throwable {
                            //子线程中保存文件
                            RandomAccessFile randomAccessFile = null;
                            InputStream inputStream = null;
                            long total = finalFileLen;
                            long responseLength = 0;
                            State state = State.DEFAULT;
                            try {
                                byte[] buf = new byte[2048];
                                int len = 0;
                                //剩余下载的长度
                                responseLength = responseBody.contentLength();

                                inputStream = responseBody.byteStream();
                                String filePath = directory;
                                File file = new File(filePath, fileName);
                                File dir = new File(filePath);
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                randomAccessFile = new RandomAccessFile(file, "rwd");
                                if (finalFileLen == 0) {
                                    randomAccessFile.setLength(responseLength);
                                }
                                randomAccessFile.seek(finalFileLen);

                                int progress = 0;
                                int lastProgress = 0;

                                while ((len = inputStream.read(buf)) != -1) {
                                    if (isPaused()) {
                                        mDownloadListener.onPaused();
                                        return State.PAUSE;
                                    }
                                    randomAccessFile.write(buf, 0, len);
                                    total += len;
                                    //已下载进度
                                    SPUtils.put(downloadService, url, total);
                                    lastProgress = progress;
                                    progress = (int) (total * 100 / randomAccessFile.length());
                                    LogUtils.d("download size=" + total + ", progess=" + progress);
                                    if (progress > 0 && progress != lastProgress && progress >= lastProgress) {
                                        mDownloadListener.onProgress(progress);
                                    }
                                }
                                SPUtils.remove(downloadService, url);
                                state = State.SUCCESS;
                            } catch (Exception e) {
                                LogUtils.e(e.getMessage());
                                state = State.FAILED;
                            } finally {
                                try {
                                    if (randomAccessFile != null) {
                                        randomAccessFile.close();
                                    }
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            return state;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<State>() {
                        @Override
                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {
                            addDisposable(d);
                        }

                        @Override
                        public void onNext(@io.reactivex.rxjava3.annotations.NonNull DownloadService.State state) {
                            //主线程中更新UI,弹出Toast必须是在主线程中,通知进度条主要是在别的进程中更新的
                            switch (state) {
                                case SUCCESS:{
                                    mDownloadListener.onSuccess();
                                    break;
                                }
                                case PAUSE: {
                                    mDownloadListener.onPaused();
                                    break;
                                }
                                case FAILED: {
                                    mDownloadListener.onFailed();
                                    break;
                                }
                                default:
                            }
                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {
                            mDownloadListener.onFailed();
                        }

                        @Override
                        public void onComplete() {
                            LogUtils.i("success");

                        }
                    });
        }

        public void pauseDownload() {
            setPaused(true);
        }

        public void cancelDownload() {
            setPaused(true);
            unDisposable();
            mDownloadListener.onCanceled();
        }

        private void addDisposable(Disposable disposable) {
            mCompositeDisposable.add(disposable);
        }

        private void unDisposable() {
            if (mCompositeDisposable.size() > 0) {
                LogUtils.v("unDisposable");
                mCompositeDisposable.dispose();
            }
        }

        private boolean isPaused() {
            Lock lock = mReadWriteLock.readLock();
            lock.lock();
            try {
                return mIsPaused;
            } finally {
                lock.unlock();
            }
        }

        private void setPaused(boolean paused) {
            Lock lock = mReadWriteLock.writeLock();
            lock.lock();
            try {
                mIsPaused = paused;
            } finally {
                lock.unlock();
            }
        }
    }

    interface DownloadListener {

        void onProgress(int progress);

        void onSuccess();

        void onFailed();

        void onPaused();

        void onCanceled();

    }

    enum State {
        SUCCESS,
        FAILED,
        PAUSE,
        CANCEL,
        DEFAULT
    }
}