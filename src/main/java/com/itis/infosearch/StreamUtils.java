package com.itis.infosearch;

import java.io.*;
import java.net.URL;

public class StreamUtils {

    public static void write(String filePath, String value) {
        try {
            FileWriter writer = new FileWriter(filePath, false);
            writer.write(value);
            writer.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public static String read(File file) {
        StringBuilder content = new StringBuilder();
        try {
            FileReader reader = new FileReader(file);
            int c;
            while ((c = reader.read()) != -1) {
                content.append((char) c);
            }
            reader.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return content.toString();
    }

    private static String readUrl(URL url) {
        StringBuilder document = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(url.openStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                document.append(line);
            }
            reader.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return document.toString();
    }
}
