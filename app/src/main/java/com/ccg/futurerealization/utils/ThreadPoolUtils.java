package com.ccg.futurerealization.utils;


import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description:线程池
 * @Author: cgaopeng
 * @CreateDate: 22-3-9 下午5:42
 * @Version: 1.0
 */
public class ThreadPoolUtils {

    private ThreadPoolUtils() {}

    //核心线程数
    private static int CORE_POOL_SIZE = 8;
    //最大线程数
    private static int MAX_POOL_SIZE = 64;
    //线程池中超过corePoolSize数目的空闲线程最大存活时间；可以allowCoreThreadTimeOut(true)使得核心线程有效时间
    private static int KEEP_ALIVE_TIME = 5;
    //任务队列
    private static BlockingQueue<Runnable> mQueue = new ArrayBlockingQueue<>(64);

    private static ExecutorService mPool;

    static {
        mPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, mQueue);
    }

    public static void execute(Runnable runnable) {
        mPool.execute(runnable);
    }

    public static void submit(Runnable runnable) {
        mPool.submit(runnable);
    }

    public static ExecutorService getInstance() {
        return mPool;
    }
}
