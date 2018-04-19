package org.liaohailong.pdftestapp.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Describe as : 自动创建生成各种value-xxx类型的尺寸值
 * Created by LHL on 2018/4/19.
 */

public class AutoDimenUtil {

    public static void main(String[] args) {
        File file = new File("D:\\liao\\workspace02\\AutoDimen\\app\\src\\main\\java\\org\\liaohailong\\autodimen\\victor.xml");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);

            //create dimens
            StringBuilder sb = new StringBuilder();
            sb.append("<resources>").append("\n");
            for (int i = 1; i <= 3000; i++) {
                sb.append("    <dimen name=\"dim")
                        .append(i)
                        .append("\">")
                        .append(i)
                        .append("dp")
                        .append("</dimen>")
                        .append("\n");
            }
            sb.append("</resources>");
            fos.write(sb.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(fos);
        }
    }

    private static void close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
