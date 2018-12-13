package perseverance.li.utils;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;

/**
 * ---------------------------------------------------------------
 * Author: perseverance.li
 * ---------------------------------------------------------------
 */
public class ThreadManager {

    /**
     * 空闲10s回收线程
     */
    private static final int KEEPALIVE_TIME = 10;
    private volatile static ThreadManager mInstance;
    private ThreadPoolExecutor mThreadPool;
    private LinkedBlockingQueue<Runnable> mQueue;
    private RequeueHandler mHandler;
    /**
     * 线程等待时间，单位秒
     */
    private static final int AWAIT = 1;

    private ThreadManager() {
    }

    /**
     * 初始化线程池
     * 线程池采用队列重入机制，防止数量过大占用较大内存
     *
     * @param poolSize  线程池数量
     * @param queueSize 队列大小
     * @see RequeueHandler
     */
    public void init(int poolSize, int queueSize) {
        mQueue = new LinkedBlockingQueue<>(queueSize);
        mHandler = new RequeueHandler();
        //TODO:核心线程数与最大线程数统一
        mThreadPool = new ThreadPoolExecutor(poolSize, poolSize,
                KEEPALIVE_TIME, TimeUnit.SECONDS,
                mQueue, Executors.defaultThreadFactory(), mHandler);
    }

    /**
     * 单例获取ThreadManager对象
     *
     * @return
     */
    public static ThreadManager getInstance() {
        if (mInstance == null) {
            synchronized (ThreadManager.class) {
                if (mInstance == null) {
                    mInstance = new ThreadManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 提交任务
     * Callable 提供返回值,通过Future.get获取返回值,get会阻塞所在线程
     *
     * @param task
     * @return
     */
    public <T> Future<T> submit(Callable<T> task) {
        if (task == null || mThreadPool == null) {
            return null;
        }

        return mThreadPool.submit(task);
    }

    /**
     * 提交任务
     *
     * @param task
     */
    public void execute(Runnable task) {
        if (task == null || mThreadPool == null) {
            return;
        }

        mThreadPool.execute(task);
    }

    /**
     * 使所在线程进入等待状态，当线程池所有线程执行完之后返回true
     *
     * @return
     */
    public boolean awaitTermination() {
        if (mThreadPool == null) {
            return true;
        }
        boolean status = true;
        try {
            status = mThreadPool.awaitTermination(AWAIT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return status;
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        if (mThreadPool == null) {
            return;
        }
        mThreadPool.shutdown();
    }
}