package perseverance.li.utils;


import okhttp3.OkHttpClient;
import okhttp3.Dispatcher;
import okhttp3.ConnectionPool;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * ---------------------------------------------------------------
 * Author: perseverance.li
 * ---------------------------------------------------------------
 */
public class OkhttpUtil {

    /**
     * 链接超时时间
     */
    private static final long CONNECTION_TIMEOUT = 10000;
    /**
     * 读写超时时间
     */
    private static final long READ_WRITE_TIMEOUT = 20000;
    private static volatile OkhttpUtil mInstance;
    private OkHttpClient mOkClient;

    private OkhttpUtil() {
    }

    public static OkhttpUtil getInstance() {
        if (mInstance == null) {
            synchronized (OkhttpUtil.class) {
                if (mInstance == null) {
                    mInstance = new OkhttpUtil();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化 okhttp
     *
     * @param maxRequests
     * @param maxPerHost
     */
    public void init(int maxRequests, int maxPerHost) {
        //默认链接池配置是 5 5 MINUTES
        //这里调整keepalive时间
        ConnectionPool pool = new ConnectionPool(5, 10, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(maxRequests);
        dispatcher.setMaxRequestsPerHost(maxPerHost);

        mOkClient = new OkHttpClient().newBuilder()
                .connectTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(READ_WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(READ_WRITE_TIMEOUT, TimeUnit.MILLISECONDS)
                .connectionPool(pool)
                .dispatcher(dispatcher)
                .build();
    }

    /**
     * get 请求
     *
     * @param url
     * @return
     */
    public String doGet(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = null;
        try {
            response = mOkClient.newCall(request).execute();
            if (!response.isSuccessful()) {
                //TODO:请求不成功时需要怎么处理？这个根据需求而定，这里默认返回null
                System.out.println("http get request failed , code : " + response.code() + "  msg : " + response.message());
                return null;
            }
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                return null;
            }
            return responseBody.string();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    /**
     * 销毁client
     */
    public void destroy() {
        if (mOkClient == null) {
            return;
        }
        mOkClient.dispatcher().executorService().shutdown();
        mOkClient.connectionPool().evictAll();
        mOkClient = null;
    }
}