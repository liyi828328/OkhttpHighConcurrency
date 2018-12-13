package perseverance.li.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * ---------------------------------------------------------------
 * Author: perseverance.li
 * ---------------------------------------------------------------
 */
public class RequeueHandler implements RejectedExecutionHandler {

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (r == null || executor == null || executor.isShutdown()) {
            return;
        }

        BlockingQueue<Runnable> queue = executor.getQueue();
        if (queue == null) {
            return;
        }

        try {
            //TODO:将溢出的任务重新put进队列，put当队列已满时会阻塞，直到队列有空位时在放入队列
            queue.put(r);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}