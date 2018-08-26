package org.liaohailong.library.widget;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Describe as : create various screen sizes xml like victor.xml
 * Created by LHL on 2018/4/19.
 */

public class AutoDimenHelper {
    //this is your destination                 change this if you need.
    private static final String sDirectory = "D:\\AndroidDimenCompat";
    private static final String sResName = "res";
    private static final String sFileName = "victor.xml";

    private static final String sValuesName = "values-%1$sx%2$s";
    private static final List<String[]> SUPPORT_DIMENSION = new ArrayList<>();

    static {
        //phone size and TV size
        SUPPORT_DIMENSION.add(new String[]{"320", "480"});
        SUPPORT_DIMENSION.add(new String[]{"480", "800"});
        SUPPORT_DIMENSION.add(new String[]{"480", "854"});
        SUPPORT_DIMENSION.add(new String[]{"540", "960"});
        SUPPORT_DIMENSION.add(new String[]{"600", "1024"});
        SUPPORT_DIMENSION.add(new String[]{"720", "1184"});
        SUPPORT_DIMENSION.add(new String[]{"720", "1196"});
        SUPPORT_DIMENSION.add(new String[]{"720", "1280"});
        SUPPORT_DIMENSION.add(new String[]{"768", "1024"});
        SUPPORT_DIMENSION.add(new String[]{"768", "1280"});
        SUPPORT_DIMENSION.add(new String[]{"800", "1280"});
        SUPPORT_DIMENSION.add(new String[]{"1080", "1812"});
        SUPPORT_DIMENSION.add(new String[]{"1080", "1920"});
        SUPPORT_DIMENSION.add(new String[]{"1440", "2560"});
        SUPPORT_DIMENSION.add(new String[]{"1440", "2880"});

        //pad size
        SUPPORT_DIMENSION.add(new String[]{"1200", "1920"});
        SUPPORT_DIMENSION.add(new String[]{"2048", "1536"});
        SUPPORT_DIMENSION.add(new String[]{"2560", "1600"});
        SUPPORT_DIMENSION.add(new String[]{"2560", "1800"});
    }

    //this config is depend on Mi-5s phone
    private static final int DEFAULT_WINDOW_WIDTH = 1080;
    private static final int DEFAULT_WINDOW_HEIGHT = 1920;
    private static final int DEFAULT_DIAGONAL_LINE = (int) Math.sqrt(Math.pow(DEFAULT_WINDOW_WIDTH, 2) + Math.pow(DEFAULT_WINDOW_HEIGHT, 2));

    public static void main(String[] args) {
        String directoryPath = sDirectory + "\\" + sResName;
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            boolean mkdir = directory.mkdir();
            System.out.print("mkdir = " + mkdir + "  directory = " + directory.getAbsolutePath());
        }

        String baseFilePath = directoryPath + "\\" + sFileName;
        generateBaseFile(baseFilePath);

        for (String[] screen : SUPPORT_DIMENSION) {
            String width = screen[0];
            String height = screen[1];
            generateValues(baseFilePath, directoryPath, width, height);
        }
    }

    private static void generateBaseFile(String baseFilePath) {
        File file = new File(baseFilePath);
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
                        .append("px")
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

    private static void generateValues(String baseFilePath, String directoryPath, String width, String height) {
        int widthInt = Integer.parseInt(width);
        int heightInt = Integer.parseInt(height);
        int tempDiagonalLine = (int) Math.sqrt(Math.pow(widthInt, 2) + Math.pow(heightInt, 2));

        File file = new File(baseFilePath);
        FileReader reader = null;
        BufferedReader bufferedReader = null;

        String valuesFileName = String.format(sValuesName, width, height);
        String valuesPath = directoryPath + "\\" + valuesFileName;
        File targetDirectory = new File(valuesPath);
        if (!targetDirectory.exists()) {
            boolean mkdir = targetDirectory.mkdir();
            System.out.print("mkdir = " + mkdir + "  directory = " + targetDirectory.getAbsolutePath());
        }

        String valueFilePath = valuesPath + "\\" + sFileName;
        File valueFile = new File(valueFilePath);
        FileOutputStream fos = null;

        try {
            reader = new FileReader(file);
            bufferedReader = new BufferedReader(reader);

            StringBuilder sb = new StringBuilder();
            sb.append("<resources>").append("\n");

            String tempLine;
            while ((tempLine = bufferedReader.readLine()) != null) {
                if (tempLine.contains("<dimen") && tempLine.contains("</dimen>")) {

                    int nameFrom = tempLine.lastIndexOf("\"dim");
                    int nameTo = tempLine.lastIndexOf("\">");
                    String valueName = tempLine.substring(nameFrom + "\"dim".length(), nameTo);

                    int from = tempLine.lastIndexOf("\">");
                    int to = tempLine.lastIndexOf("px");
                    String value = tempLine.substring(from + "\">".length(), to);
                    float valueFloat = Float.parseFloat(value);

                    //transform value to correct screen...
                    float scalePX = tempDiagonalLine * 1.0f / DEFAULT_DIAGONAL_LINE;
                    valueFloat *= scalePX;
                    String valueStr = new BigDecimal(valueFloat).setScale(1, BigDecimal.ROUND_HALF_UP).toString();

                    sb.append("    <dimen name=\"dim")
                            .append(valueName)
                            .append("\">")
                            .append(valueStr)
                            .append("px")
                            .append("</dimen>")
                            .append("\n");
                }
            }

            sb.append("</resources>");

            fos = new FileOutputStream(valueFile);
            fos.write(sb.toString().getBytes());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            close(reader);
            close(bufferedReader);
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
