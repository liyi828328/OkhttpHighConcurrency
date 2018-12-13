package perseverance.li.task;

import perseverance.li.utils.FileUtil;
import perseverance.li.utils.OkhttpUtil;

/**
 * ---------------------------------------------------------------
 * Author: perseverance.li
 * ---------------------------------------------------------------
 */
public class DataTask implements Runnable {

    private String url;

    public DataTask(String url) {
        this.url = url;
    }

    @Override
    public void run() {

        String result = OkhttpUtil.getInstance().doGet(url);
        //TODO:结果写入文件
        FileUtil.writeFile(result + "\r\n", "/home/liyi/work/okhttp/result.dat");
    }
}
