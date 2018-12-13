package perseverance.li.utils;

import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

import java.io.File;
import java.io.IOException;

/**
 * ---------------------------------------------------------------
 * Author: perseverance.li
 * ---------------------------------------------------------------
 */
public class FileUtil {

    /**
     * 使用okio写文件
     *
     * @param content
     * @param targetFilePath
     */
    public static void writeFile(String content, String targetFilePath) {
        Sink sink = null;
        BufferedSink bufferedSink = null;
        try {
            File file = new File(targetFilePath);
            String parentPath = file.getParent();
            File dir = new File(parentPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            sink = Okio.appendingSink(file);
            bufferedSink = Okio.buffer(sink);
            bufferedSink.write(content.getBytes());
            bufferedSink.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sink != null) {
                try {
                    sink.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bufferedSink != null) {
                try {
                    bufferedSink.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}