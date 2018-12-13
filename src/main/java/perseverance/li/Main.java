package perseverance.li;

import okio.BufferedSource;
import okio.Okio;
import okio.Source;
import perseverance.li.task.DataTask;
import perseverance.li.utils.OkhttpUtil;
import perseverance.li.utils.ThreadManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ---------------------------------------------------------------
 * Author: perseverance.li
 * ---------------------------------------------------------------
 */
public class Main {

    /**
     * 最大并发数,根据cpu核数来配置
     */
    private static final int MAX_REQUESTS = 100;
    /**
     * 每个主机最大请求数
     */
    private static final int MAX_PER_HOST = 10;
    /**
     * 线程池大小
     */
    private static final int MAX_THREAD_POOL_SIZE = 100;
    /**
     * 队列大小,过大的队列会导致消耗更多的内存,可根据机器情况配置
     */
    private static final int MAX_THREAD_QUEUE_SIZE = 20000;
    /**
     * base url
     */
    private static final String HTTP_BASE_URL = "";


    public static void main(String[] args) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:S");
        long startTime = System.currentTimeMillis();
        String startDate = dateFormat.format(new Date(startTime));
        System.out.println(">>> main start " + startDate + " <<<");

        ThreadManager.getInstance().init(MAX_THREAD_POOL_SIZE, MAX_THREAD_QUEUE_SIZE);
        OkhttpUtil.getInstance().init(MAX_REQUESTS, MAX_PER_HOST);

        //test
        testConcurrency();

        //等待线程池结束退出
        ThreadManager.getInstance().shutdown();
        boolean loop;
        do {
            //阻塞，直到线程池里所有任务结束
            loop = !ThreadManager.getInstance().awaitTermination();
        } while (loop);

        //等待所有线程结束后销毁okhttp client
        OkhttpUtil.getInstance().destroy();
        long endTime = System.currentTimeMillis();
        String endDate = dateFormat.format(new Date(endTime));
        String elapsedTime = (endTime - startTime) / 1000f + " s , " + endDate;
        System.out.println(">>> main end " + elapsedTime + " <<<");
        System.exit(0);
    }

    /**
     * 测试
     */
    private static void testConcurrency() {
        //TODO:读取的文件路径
        String filePath = "/home/liyi/work/okhttp/number.dat";
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println(filePath + " not exists ! ");
            return;
        }
        BufferedSource bufferedSource = null;
        Source source = null;
        try {
            source = Okio.source(file);
            bufferedSource = Okio.buffer(source);
            String content;
            int count = 0;
            while ((content = bufferedSource.readUtf8Line()) != null) {
                if (content == null || content.length() == 0) {
                    continue;
                }
                //TODO:拼接请求的url
                StringBuilder urlBuilder = new StringBuilder();
                urlBuilder.append(HTTP_BASE_URL);
                DataTask task = new DataTask(urlBuilder.toString());
                //TODO:提交任务到线程池,由于线程池配置了超出队列重排的机制,所以当队列数量超出指定大小时会阻塞当前线程,直到队列有空位
                ThreadManager.getInstance().execute(task);
                count++;
                if (count % 10000 == 0) {
                    System.out.println("submit task size : " + count);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedSource != null) {
                    bufferedSource.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (source != null) {
                    source.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}